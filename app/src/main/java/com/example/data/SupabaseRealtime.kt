package com.example.data

import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Supabase Realtime WebSocket client and Channel abstraction.
 * Enables reactive subscriptions to Supabase Postgres changes (realtime:public)
 * so Android UI updates automatically when edits occur in the Web Admin Panel.
 *
 * Syntax matches the requested pattern:
 *   supabase.channel("app_realtime_changes")
 *       .on("channel_profile") { record -> ... }
 *       .subscribe()
 */

// Convenience alias to support `supabase.channel(...)` syntax
val supabase = SupabaseClient

/**
 * Top-level listener registered on a table.
 */
data class RealtimeTableListener(
    val table: String,
    val event: String = "*",
    val callback: (JSONObject) -> Unit
) {
    fun matches(eventTable: String, eventType: String): Boolean {
        val tableMatches = table == "*" || table.equals(eventTable, ignoreCase = true)
        val eventMatches = event == "*" || event.equals(eventType, ignoreCase = true)
        return tableMatches && eventMatches
    }
}

/**
 * A Realtime Channel representing a Phoenix topic subscription.
 */
class SupabaseRealtimeChannel(
    val channelName: String,
    private val client: SupabaseRealtimeClient
) {
    private val listeners = CopyOnWriteArrayList<RealtimeTableListener>()
    private var onStatusChange: ((String) -> Unit)? = null

    var status: String = "CLOSED"
        internal set(value) {
            field = value
            onStatusChange?.invoke(value)
        }

    /**
     * Registers a listener for database changes on [table] (e.g. "channel_profile", "dashboard_metrics").
     * [event] can be "*", "INSERT", "UPDATE", or "DELETE".
     */
    fun on(
        table: String,
        event: String = "*",
        callback: (JSONObject) -> Unit
    ): SupabaseRealtimeChannel {
        listeners.add(RealtimeTableListener(table, event, callback))
        return this
    }

    /**
     * Subscribes this channel to the active Supabase Realtime WebSocket connection.
     */
    fun subscribe(statusCallback: ((String) -> Unit)? = null): SupabaseRealtimeChannel {
        this.onStatusChange = statusCallback
        client.subscribeChannel(this)
        return this
    }

    /**
     * Unsubscribes from the channel.
     */
    fun unsubscribe() {
        client.unsubscribeChannel(this)
        status = "CLOSED"
    }

    internal fun getListeners(): List<RealtimeTableListener> = listeners

    internal fun dispatchChange(table: String, eventType: String, record: JSONObject, rawPayload: JSONObject) {
        for (listener in listeners) {
            if (listener.matches(table, eventType)) {
                try {
                    listener.callback(record)
                } catch (e: Exception) {
                    Log.e("SupabaseRealtime", "Error in listener callback for $table ($eventType)", e)
                }
            }
        }
    }
}

/**
 * Central WebSocket client managing connection, heartbeats, channel joins,
 * and auto-reconnection to Supabase Realtime Phoenix endpoints.
 */
object SupabaseRealtimeClient {
    private const val TAG = "SupabaseRealtime"

    private val channels = ConcurrentHashMap<String, SupabaseRealtimeChannel>()
    private val clientScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val refCounter = AtomicInteger(1)

    private val httpClient = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // infinite for WebSocket
        .build()

    private var webSocket: WebSocket? = null
    private var isConnected = false
    private var isExplicitlyClosed = false
    private var heartbeatJob: Job? = null
    private var reconnectJob: Job? = null

    /**
     * Returns or creates a channel with the given identifier.
     */
    fun channel(name: String = "app_realtime_changes"): SupabaseRealtimeChannel {
        return channels.computeIfAbsent(name) {
            SupabaseRealtimeChannel(name, this)
        }
    }

    /**
     * Subscribes a channel. If WebSocket is not connected, initiates connection.
     */
    internal fun subscribeChannel(channel: SupabaseRealtimeChannel) {
        channels[channel.channelName] = channel
        isExplicitlyClosed = false

        if (isConnected && webSocket != null) {
            sendJoinChannelMessage(channel)
        } else {
            connectWebSocket()
        }
    }

    internal fun unsubscribeChannel(channel: SupabaseRealtimeChannel) {
        if (isConnected && webSocket != null) {
            sendLeaveChannelMessage(channel)
        }
        channels.remove(channel.channelName)
    }

    /**
     * Connects to Supabase Realtime WebSocket.
     */
    @Synchronized
    fun connect() {
        isExplicitlyClosed = false
        connectWebSocket()
    }

    /**
     * Disconnects WebSocket.
     */
    @Synchronized
    fun disconnect() {
        isExplicitlyClosed = true
        heartbeatJob?.cancel()
        reconnectJob?.cancel()
        try {
            webSocket?.close(1000, "App disconnected")
        } catch (_: Exception) {}
        webSocket = null
        isConnected = false
        channels.values.forEach { it.status = "CLOSED" }
    }

    private fun connectWebSocket() {
        val anonKey = SupabaseClient.anonKey
        if (anonKey.isBlank()) {
            Log.w(TAG, "Cannot connect to Supabase Realtime: publishable key is not set yet.")
            return
        }

        if (isConnected && webSocket != null) {
            return
        }

        try {
            val baseUrl = SupabaseClient.supabaseUrl
                .replace("https://", "wss://")
                .replace("http://", "ws://")
            val wsUrl = "$baseUrl/realtime/v1/websocket?apikey=$anonKey&vsn=1.0.0"

            Log.d(TAG, "Connecting to Supabase Realtime: $wsUrl")

            val request = Request.Builder()
                .url(wsUrl)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .build()

            webSocket?.cancel()
            webSocket = httpClient.newWebSocket(request, createWebSocketListener())

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start Realtime WebSocket", e)
            scheduleReconnect()
        }
    }

    private fun createWebSocketListener(): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d(TAG, "Realtime WebSocket connected successfully!")
                isConnected = true
                startHeartbeat()

                // Join all registered channels
                channels.values.forEach { channel ->
                    sendJoinChannelMessage(channel)
                }
            }

            override fun onMessage(ws: WebSocket, text: String) {
                handleIncomingMessage(text)
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Realtime WebSocket closing: $code / $reason")
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Realtime WebSocket closed: $code / $reason")
                isConnected = false
                heartbeatJob?.cancel()
                channels.values.forEach { it.status = "CLOSED" }
                scheduleReconnect()
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.w(TAG, "Realtime WebSocket failure: ${t.message}")
                isConnected = false
                heartbeatJob?.cancel()
                channels.values.forEach { it.status = "DISCONNECTED" }
                scheduleReconnect()
            }
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = clientScope.launch {
            while (isActive && isConnected) {
                delay(25_000) // 25s Phoenix heartbeat interval
                try {
                    val ref = refCounter.incrementAndGet().toString()
                    val msg = JSONObject().apply {
                        put("topic", "phoenix")
                        put("event", "heartbeat")
                        put("payload", JSONObject())
                        put("ref", ref)
                    }
                    webSocket?.send(msg.toString())
                } catch (e: Exception) {
                    Log.w(TAG, "Heartbeat failed", e)
                    break
                }
            }
        }
    }

    private fun sendJoinChannelMessage(channel: SupabaseRealtimeChannel) {
        val topic = "realtime:${channel.channelName}"
        val ref = refCounter.incrementAndGet().toString()

        val joinPayload = JSONObject().apply {
            val configObj = JSONObject().apply {
                put("broadcast", JSONObject().put("self", false))
                put("presence", JSONObject().put("key", ""))

                // Collect distinct tables from listeners
                val pgChangesArray = JSONArray()
                val registeredTables = channel.getListeners().map { it.table }.distinct()
                val tablesToWatch = if (registeredTables.isEmpty()) {
                    listOf("channel_profile", "dashboard_metrics", "videos", "analytics_overview")
                } else {
                    registeredTables
                }

                for (table in tablesToWatch) {
                    val item = JSONObject().apply {
                        put("event", "*")
                        put("schema", "public")
                        put("table", table)
                    }
                    pgChangesArray.put(item)
                }

                put("postgres_changes", pgChangesArray)
            }
            put("config", configObj)
        }

        val joinMsg = JSONObject().apply {
            put("topic", topic)
            put("event", "phx_join")
            put("payload", joinPayload)
            put("ref", ref)
        }

        channel.status = "JOINING"
        webSocket?.send(joinMsg.toString())
        Log.d(TAG, "Sent phx_join for $topic with tables: ${channel.getListeners().map { it.table }}")
    }

    private fun sendLeaveChannelMessage(channel: SupabaseRealtimeChannel) {
        val topic = "realtime:${channel.channelName}"
        val ref = refCounter.incrementAndGet().toString()
        val leaveMsg = JSONObject().apply {
            put("topic", topic)
            put("event", "phx_leave")
            put("payload", JSONObject())
            put("ref", ref)
        }
        webSocket?.send(leaveMsg.toString())
    }

    private fun handleIncomingMessage(text: String) {
        try {
            val json = JSONObject(text)
            val topic = json.optString("topic", "")
            val event = json.optString("event", "")
            val payload = json.optJSONObject("payload") ?: JSONObject()

            // Handle Phoenix Join Confirmation
            if (event == "phx_reply") {
                val status = payload.optString("status", "")
                if (status == "ok") {
                    val channelKey = topic.removePrefix("realtime:")
                    channels[channelKey]?.let { ch ->
                        ch.status = "SUBSCRIBED"
                        Log.d(TAG, "Channel [${ch.channelName}] successfully SUBSCRIBED to Supabase Realtime!")
                    }
                }
                return
            }

            // Handle Postgres Changes
            if (event == "postgres_changes" || event == "INSERT" || event == "UPDATE" || event == "DELETE") {
                val dataObj = payload.optJSONObject("data")
                val table = dataObj?.optString("table") ?: payload.optString("table", "")
                val eventType = dataObj?.optString("type") ?: payload.optString("type", event)
                val record = dataObj?.optJSONObject("record") ?: payload.optJSONObject("record") ?: JSONObject()

                Log.d(TAG, "Realtime event received -> table=$table, type=$eventType, record=$record")

                // Dispatch to matching channels
                val channelKey = topic.removePrefix("realtime:")
                val channel = channels[channelKey]
                if (channel != null) {
                    channel.dispatchChange(table, eventType, record, payload)
                } else {
                    // Fallback to all channels listening on this table
                    channels.values.forEach { ch ->
                        ch.dispatchChange(table, eventType, record, payload)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling Realtime message: $text", e)
        }
    }

    private fun scheduleReconnect() {
        if (isExplicitlyClosed) return
        reconnectJob?.cancel()
        reconnectJob = clientScope.launch {
            delay(3000)
            if (!isConnected && !isExplicitlyClosed && channels.isNotEmpty()) {
                Log.d(TAG, "Attempting Realtime WebSocket reconnection...")
                connectWebSocket()
            }
        }
    }
}

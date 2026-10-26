package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.BuildConfig
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object SupabaseClient {
    private const val TAG = "SupabaseClient"
    const val DEFAULT_SUPABASE_URL = "https://zzswtrpdjllmagjjmxbx.supabase.co"
    private const val PREFS_NAME = "studio_supabase_prefs"
    private const val KEY_ANON_KEY = "supabase_anon_key"
    private const val KEY_URL = "supabase_url"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private var appContext: Context? = null
    private var prefs: SharedPreferences? = null

    var runtimeUrlOverride: String = ""
    var runtimeAnonKeyOverride: String = ""
        set(value) {
            field = value.trim()
            if (field.isNotBlank()) {
                SupabaseRealtimeClient.connect()
            }
        }

    const val SUPABASE_URL = DEFAULT_SUPABASE_URL

    val supabaseUrl: String
        get() {
            if (runtimeUrlOverride.isNotBlank()) return runtimeUrlOverride
            val savedUrl = prefs?.getString(KEY_URL, "") ?: ""
            if (savedUrl.isNotBlank()) return savedUrl
            return DEFAULT_SUPABASE_URL
        }

    val projectUrl: String
        get() = supabaseUrl

    val anonKey: String
        get() {
            if (runtimeAnonKeyOverride.isNotBlank()) return runtimeAnonKeyOverride
            val savedKey = prefs?.getString(KEY_ANON_KEY, "") ?: ""
            if (savedKey.isNotBlank()) return savedKey
            return try {
                BuildConfig.SUPABASE_ANON_KEY
            } catch (_: Exception) {
                ""
            }
        }

    /**
     * Initializes SupabaseClient with application context to read/write persistent settings.
     */
    fun init(context: Context) {
        val app = context.applicationContext
        appContext = app
        prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val savedKey = prefs?.getString(KEY_ANON_KEY, "") ?: ""
        if (savedKey.isNotBlank()) {
            runtimeAnonKeyOverride = savedKey
            Log.d(TAG, "Loaded saved Supabase publishable key from SharedPreferences (${savedKey.take(10)}...)")
        }

        val savedUrl = prefs?.getString(KEY_URL, "") ?: ""
        if (savedUrl.isNotBlank()) {
            runtimeUrlOverride = savedUrl
            Log.d(TAG, "Loaded saved Supabase URL: $savedUrl")
        }
    }

    /**
     * Saves user-entered publishable key and optional URL persistently on device.
     */
    fun setCredentials(key: String, url: String? = null, context: Context? = null) {
        val trimmedKey = key.trim()
        val trimmedUrl = (url?.trim() ?: "").ifBlank { DEFAULT_SUPABASE_URL }

        runtimeAnonKeyOverride = trimmedKey
        runtimeUrlOverride = trimmedUrl

        val targetContext = context ?: appContext
        targetContext?.let {
            val p = it.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            p.edit()
                .putString(KEY_ANON_KEY, trimmedKey)
                .putString(KEY_URL, trimmedUrl)
                .apply()
            prefs = p
        }

        Log.d(TAG, "Supabase credentials updated and saved to SharedPreferences. Key length: ${trimmedKey.length}")
    }

    /**
     * Creates or returns a Supabase Realtime channel for WebSocket postgres_changes listeners.
     */
    fun channel(name: String = "app_realtime_changes"): SupabaseRealtimeChannel {
        return SupabaseRealtimeClient.channel(name)
    }

    suspend fun fetchChannelProfile(): ChannelProfile? = withContext(Dispatchers.IO) {
        val key = anonKey
        val urlBase = supabaseUrl

        if (key.isBlank()) {
            val msg = "Supabase publishable anon key is blank. Please configure SUPABASE_ANON_KEY in Account > Settings or .env file."
            Log.w(TAG, msg)
            StudioRepository.lastSyncError = "Missing publishable key. Open Account > Settings to connect."
            return@withContext null
        }

        try {
            val endpoint = "$urlBase/rest/v1/channel_profile?select=*"
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", key)
                .addHeader("Authorization", "Bearer $key")
                .addHeader("Accept", "application/json")
                .get()
                .build()

            Log.d(TAG, "Fetching channel_profile from: $endpoint")
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val err = "fetchChannelProfile failed: HTTP ${response.code} -> $body"
                Log.e(TAG, err)
                StudioRepository.lastSyncError = "HTTP ${response.code}: $body"
                return@withContext null
            }

            val array = JSONArray(body)
            if (array.length() == 0) {
                Log.w(TAG, "fetchChannelProfile returned 0 rows from database")
                return@withContext null
            }

            val obj = array.getJSONObject(0)
            val profile = ChannelProfile(
                id = obj.optString("id", "channel_daniel"),
                name = obj.optString("name", "Daniel Dalen"),
                handle = obj.optString("handle", "@danieldalen"),
                subscribersCount = obj.optString("subscribers_count", "48,237"),
                email = obj.optString("email", "creator@gmail.com"),
                isMonetized = obj.optBoolean("is_monetized", true)
            )

            Log.d(TAG, "Successfully fetched channel_profile: ${profile.name}, subs=${profile.subscribersCount}, monetized=${profile.isMonetized}")
            return@withContext profile
        } catch (e: Exception) {
            val err = "Network error fetching channel_profile: ${e.message}"
            Log.e(TAG, err, e)
            StudioRepository.lastSyncError = err
            null
        }
    }

    suspend fun fetchDashboardMetrics(): DashboardMetricsResponse? = withContext(Dispatchers.IO) {
        val key = anonKey
        val urlBase = supabaseUrl

        if (key.isBlank()) {
            Log.w(TAG, "Cannot fetch dashboard_metrics: anonKey is blank")
            return@withContext null
        }

        try {
            val endpoint = "$urlBase/rest/v1/dashboard_metrics?select=*"
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", key)
                .addHeader("Authorization", "Bearer $key")
                .addHeader("Accept", "application/json")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "fetchDashboardMetrics failed: HTTP ${response.code} -> $body")
                StudioRepository.lastSyncError = "HTTP ${response.code}: $body"
                return@withContext null
            }

            val array = JSONArray(body)
            if (array.length() == 0) {
                Log.w(TAG, "fetchDashboardMetrics returned 0 rows")
                return@withContext null
            }

            val obj = array.getJSONObject(0)
            val metrics = DashboardMetricsResponse(
                views = obj.optString("views", "887.7K"),
                watchTime = obj.optString("watch_time", "66.2K"),
                subscribers = obj.optString("subscribers", "+39.0K"),
                revenue = obj.optString("revenue", "$0"),
                viewsIsUp = obj.optBoolean("views_is_up", true),
                watchTimeIsUp = obj.optBoolean("watch_time_is_up", true),
                subscribersIsUp = obj.optBoolean("subscribers_is_up", true),
                revenueIsUp = obj.optBoolean("revenue_is_up", true),
                periodLabel = obj.optString("period_label", "Last 28 days")
            )

            Log.d(TAG, "Successfully fetched dashboard_metrics: views=${metrics.views}, revenue=${metrics.revenue}")
            return@withContext metrics
        } catch (e: Exception) {
            Log.e(TAG, "Network error fetching dashboard_metrics", e)
            StudioRepository.lastSyncError = e.message
            null
        }
    }

    suspend fun fetchVideos(): List<VideoItem>? = withContext(Dispatchers.IO) {
        val key = anonKey
        val urlBase = supabaseUrl

        if (key.isBlank()) return@withContext null

        try {
            val endpoint = "$urlBase/rest/v1/videos?select=*&order=display_order.asc"
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", key)
                .addHeader("Authorization", "Bearer $key")
                .addHeader("Accept", "application/json")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "fetchVideos failed: HTTP ${response.code} -> $body")
                return@withContext null
            }

            val array = JSONArray(body)
            val result = mutableListOf<VideoItem>()

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val typeStr = obj.optString("type", "VIDEO").uppercase()
                val contentType = when (typeStr) {
                    "SHORT" -> ContentType.SHORT
                    "LIVE" -> ContentType.LIVE
                    else -> ContentType.VIDEO
                }

                result.add(
                    VideoItem(
                        id = obj.optString("id", "vid_$i"),
                        title = obj.optString("title", "Untitled"),
                        type = contentType,
                        duration = obj.optString("duration", "0:00"),
                        publishedTimeAgo = obj.optString("published_time_ago", ""),
                        views = obj.optLong("views", 0),
                        likes = obj.optLong("likes", 0),
                        comments = obj.optLong("comments", 0),
                        rankingText = obj.optString("ranking_text", ""),
                        avgPercentageViewed = obj.optString("avg_percentage_viewed", ""),
                        realTimeViews = obj.optLong("real_time_views", 0),
                        thumbnailTitle = obj.optString("thumbnail_title", ""),
                        isExpanded = obj.optBoolean("is_expanded", false),
                        category = obj.optString("category", "Entertainment")
                    )
                )
            }

            Log.d(TAG, "Successfully fetched ${result.size} videos from database")
            return@withContext result
        } catch (e: Exception) {
            Log.e(TAG, "Network error fetching videos", e)
            null
        }
    }

    suspend fun fetchAnalyticsOverview(): List<MetricCardData>? = withContext(Dispatchers.IO) {
        val key = anonKey
        val urlBase = supabaseUrl

        if (key.isBlank()) return@withContext null

        try {
            val endpoint = "$urlBase/rest/v1/analytics_overview?select=*&order=display_order.asc"
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", key)
                .addHeader("Authorization", "Bearer $key")
                .addHeader("Accept", "application/json")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "fetchAnalyticsOverview failed: HTTP ${response.code} -> $body")
                return@withContext null
            }

            val array = JSONArray(body)
            val result = mutableListOf<MetricCardData>()

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val trendJson = obj.optJSONArray("trend_data")
                val trendList = mutableListOf<Float>()
                if (trendJson != null) {
                    for (j in 0 until trendJson.length()) {
                        trendList.add(trendJson.optDouble(j, 0.0).toFloat())
                    }
                }

                result.add(
                    MetricCardData(
                        title = obj.optString("title", "Metric"),
                        value = obj.optString("value", "0"),
                        subtitle = obj.optString("subtitle", ""),
                        isPositive = obj.optBoolean("is_positive", true),
                        showArrow = obj.optBoolean("show_arrow", true),
                        isCheckmark = obj.optBoolean("is_checkmark", false),
                        trendData = trendList,
                        yMinLabel = obj.optString("y_min_label", "0"),
                        yMidLabel = obj.optString("y_mid_label", ""),
                        yMaxLabel = obj.optString("y_max_label", ""),
                        startDateLabel = obj.optString("start_date_label", "24 Sept"),
                        endDateLabel = obj.optString("end_date_label", "21 Oct")
                    )
                )
            }

            Log.d(TAG, "Successfully fetched ${result.size} analytics overview cards from database")
            return@withContext result
        } catch (e: Exception) {
            Log.e(TAG, "Network error fetching analytics overview", e)
            null
        }
    }
}

data class DashboardMetricsResponse(
    val views: String,
    val watchTime: String,
    val subscribers: String,
    val revenue: String,
    val viewsIsUp: Boolean,
    val watchTimeIsUp: Boolean,
    val subscribersIsUp: Boolean,
    val revenueIsUp: Boolean,
    val periodLabel: String
)

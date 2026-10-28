package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.data.StudioRepository
import com.example.model.ContentType
import com.example.model.MetricCardData
import com.example.model.VideoItem
import org.json.JSONArray
import org.json.JSONObject
import java.util.Random

/**
 * Local-only persistent storage and business logic for the Admin Panel.
 * Functions 100% on-device without requiring Supabase.
 */
object LocalAdminStore {
    private const val TAG = "LocalAdminStore"
    private const val PREFS_NAME = "yt_studio_local_admin_prefs"
    private const val KEY_PREFS_NAME = "yt_studio_admin_key_redemptions_prefs"

    private const val KEY_AUTH_LOGGED_IN = "admin_logged_in"
    private const val KEY_HAS_LOCAL_DATA = "has_custom_local_data"

    // Timed Session Security Keys
    private const val KEY_SESSION_EXPIRY = "admin_session_expiry"
    private const val KEY_SESSION_IS_UNLIMITED = "admin_session_is_unlimited"

    const val DURATION_10_MINUTES_MS = 10 * 60 * 1000L
    const val DURATION_20_MINUTES_MS = 20 * 60 * 1000L

    enum class UnlockResult {
        SUCCESS_10_MIN,
        SUCCESS_20_MIN,
        SUCCESS_UNLIMITED,
        INVALID_OR_USED_KEY,
        INVALID_PASSWORD
    }

    private var prefs: SharedPreferences? = null
    private var keyPrefs: SharedPreferences? = null

    var sessionTick by mutableStateOf(System.currentTimeMillis())
        private set

    fun refreshSessionTick() {
        sessionTick = System.currentTimeMillis()
    }

    // Profile Keys
    private const val KEY_PROFILE_NAME = "channel_name"
    private const val KEY_PROFILE_HANDLE = "channel_handle"
    private const val KEY_PROFILE_SUBS = "channel_subs"
    private const val KEY_PROFILE_EMAIL = "channel_email"
    private const val KEY_PROFILE_DESC = "channel_desc"
    private const val KEY_PROFILE_AVATAR_URI = "channel_avatar_uri"
    private const val KEY_PROFILE_MONETIZED = "channel_monetized"

    // Statistics Keys
    private const val KEY_STATS_VIEWS = "stats_views"
    private const val KEY_STATS_WATCH_TIME = "stats_watch_time"
    private const val KEY_STATS_SUBS = "stats_subs"
    private const val KEY_STATS_REVENUE = "stats_revenue"
    private const val KEY_STATS_PERIOD = "stats_period"

    // Analytics Distribution Keys
    private const val KEY_ANALYTICS_DAYS = "analytics_days"
    private const val KEY_ANALYTICS_TOTAL_VIEWS = "analytics_total_views"
    private const val KEY_ANALYTICS_VIEWS_TREND = "analytics_views_trend"
    private const val KEY_ANALYTICS_WATCH_TREND = "analytics_watch_trend"
    private const val KEY_ANALYTICS_SUBS_TREND = "analytics_subs_trend"
    private const val KEY_ANALYTICS_REV_TREND = "analytics_rev_trend"

    // Videos & Content Keys
    private const val KEY_VIDEOS_JSON = "custom_videos_json"
    private const val KEY_SHORTS_JSON = "custom_shorts_json"
    private const val KEY_LIVE_VIDEOS_JSON = "custom_live_videos_json"
    private const val KEY_DASHBOARD_VIDEOS_JSON = "custom_dashboard_videos_json"

    // Monetization & Payout Keys
    private const val KEY_MONETIZATION_WATCH_HOURS = "monetization_watch_hours"
    private const val KEY_MONETIZATION_REQ_WATCH_HOURS = "monetization_req_watch_hours"
    private const val KEY_MONETIZATION_SHORTS_VIEWS = "monetization_shorts_views"
    private const val KEY_MONETIZATION_REQ_SHORTS_VIEWS = "monetization_req_shorts_views"
    private const val KEY_MONETIZATION_VIDEO_UPLOADS = "monetization_video_uploads"
    private const val KEY_PAYOUT_HEADLINE = "payout_headline"
    private const val KEY_PAYOUT_AMOUNT = "payout_amount"
    private const val KEY_PAYOUT_MONTHLY_MONTH = "payout_monthly_month"
    private const val KEY_PAYOUT_MONTHLY_AMOUNT = "payout_monthly_amount"
    private const val KEY_PAYOUT_MINIMUM_TEXT = "payout_minimum_text"
    private const val KEY_MONTHLY_AUDIENCE = "analytics_monthly_audience"

    fun init(context: Context) {
        val appCtx = context.applicationContext
        prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        keyPrefs = appCtx.getSharedPreferences(KEY_PREFS_NAME, Context.MODE_PRIVATE)
        loadSavedDataIntoRepository()
    }

    // --- Authentication & Timed Session Management ---

    fun isLoggedIn(): Boolean {
        return prefs?.getBoolean(KEY_AUTH_LOGGED_IN, false) ?: false
    }

    /**
     * Initial login with admin / Admin123.
     * On success, grants exactly 10 minutes of access.
     */
    fun login(username: String, password: String): Boolean {
        val cleanUser = username.trim()
        val isValid = cleanUser.equals("admin", ignoreCase = true) && password == "Admin123"
        if (isValid) {
            val expiry = System.currentTimeMillis() + DURATION_10_MINUTES_MS
            prefs?.edit()
                ?.putBoolean(KEY_AUTH_LOGGED_IN, true)
                ?.putLong(KEY_SESSION_EXPIRY, expiry)
                ?.putBoolean(KEY_SESSION_IS_UNLIMITED, false)
                ?.apply()
            sessionTick = System.currentTimeMillis()
            Log.d(TAG, "Admin login successful. Initial 10-minute session started.")
        }
        return isValid
    }

    /**
     * Checks if the admin access session is currently valid and unexpired.
     * Reactive in Compose via sessionTick.
     */
    fun isSessionActive(): Boolean {
        val now = sessionTick
        if (!isLoggedIn()) return false
        if (isUnlimitedSession()) return true
        val expiry = getSessionExpiry()
        return expiry > 0L && now < expiry
    }

    fun isUnlimitedSession(): Boolean {
        return prefs?.getBoolean(KEY_SESSION_IS_UNLIMITED, false) ?: false
    }

    fun getSessionExpiry(): Long {
        return prefs?.getLong(KEY_SESSION_EXPIRY, 0L) ?: 0L
    }

    fun getRemainingSeconds(): Long {
        if (!isLoggedIn()) return 0L
        if (isUnlimitedSession()) return Long.MAX_VALUE
        val expiry = getSessionExpiry()
        val remaining = (expiry - sessionTick) / 1000L
        return maxOf(0L, remaining)
    }

    fun startSession(durationMs: Long) {
        val expiry = System.currentTimeMillis() + durationMs
        prefs?.edit()
            ?.putBoolean(KEY_AUTH_LOGGED_IN, true)
            ?.putLong(KEY_SESSION_EXPIRY, expiry)
            ?.putBoolean(KEY_SESSION_IS_UNLIMITED, false)
            ?.apply()
        sessionTick = System.currentTimeMillis()
        Log.d(TAG, "Started timed session: duration = ${durationMs / 60000} mins")
    }

    fun startUnlimitedSession() {
        prefs?.edit()
            ?.putBoolean(KEY_AUTH_LOGGED_IN, true)
            ?.putLong(KEY_SESSION_EXPIRY, Long.MAX_VALUE)
            ?.putBoolean(KEY_SESSION_IS_UNLIMITED, true)
            ?.apply()
        sessionTick = System.currentTimeMillis()
        Log.d(TAG, "Started unlimited session (Dkpanel).")
    }

    /**
     * Checks if a one-time access key has already been redeemed.
     * Persisted in a dedicated SharedPreferences file that survives
     * timer expiry, app restarts, logouts, and demo data resets.
     */
    fun isKeyUsed(keyName: String): Boolean {
        return keyPrefs?.getBoolean("key_used_$keyName", false) ?: false
    }

    /**
     * Permanently marks a one-time key as USED upon successful redemption.
     */
    fun markKeyUsed(keyName: String) {
        keyPrefs?.edit()?.putBoolean("key_used_$keyName", true)?.commit()
        Log.d(TAG, "Access key '$keyName' marked permanently as USED.")
    }

    /**
     * Testing utility to reset key redemptions in unit tests.
     */
    fun clearUsedKeysForTesting() {
        keyPrefs?.edit()?.clear()?.commit()
    }

    /**
     * Validates access keys when locked:
     * - Kingpanel: one-time 10-minute access; marked USED permanently on first redemption.
     * - Rajsingpanel: one-time 20-minute access; marked USED permanently on first redemption.
     * - Dkpanel: permanent unlimited access; remains valid for repeated use.
     * Returns INVALID_OR_USED_KEY if key is unknown or if a one-time key was already redeemed.
     */
    fun unlockWithPassword(password: String): UnlockResult {
        val clean = password.trim()
        return when (clean) {
            "Kingpanel" -> {
                if (isKeyUsed("Kingpanel")) {
                    Log.w(TAG, "Attempted to reuse one-time key 'Kingpanel'. Rejected.")
                    UnlockResult.INVALID_OR_USED_KEY
                } else {
                    markKeyUsed("Kingpanel")
                    startSession(DURATION_10_MINUTES_MS)
                    UnlockResult.SUCCESS_10_MIN
                }
            }
            "Rajsingpanel" -> {
                if (isKeyUsed("Rajsingpanel")) {
                    Log.w(TAG, "Attempted to reuse one-time key 'Rajsingpanel'. Rejected.")
                    UnlockResult.INVALID_OR_USED_KEY
                } else {
                    markKeyUsed("Rajsingpanel")
                    startSession(DURATION_20_MINUTES_MS)
                    UnlockResult.SUCCESS_20_MIN
                }
            }
            "Dkpanel" -> {
                startUnlimitedSession()
                UnlockResult.SUCCESS_UNLIMITED
            }
            else -> UnlockResult.INVALID_OR_USED_KEY
        }
    }

    /**
     * Explicitly lock the session.
     */
    fun lockSession() {
        prefs?.edit()
            ?.putLong(KEY_SESSION_EXPIRY, 0L)
            ?.putBoolean(KEY_SESSION_IS_UNLIMITED, false)
            ?.apply()
        sessionTick = System.currentTimeMillis()
        Log.d(TAG, "Admin session locked.")
    }

    /**
     * Complete logout: clears auth and session state.
     */
    fun logout() {
        prefs?.edit()
            ?.putBoolean(KEY_AUTH_LOGGED_IN, false)
            ?.putLong(KEY_SESSION_EXPIRY, 0L)
            ?.putBoolean(KEY_SESSION_IS_UNLIMITED, false)
            ?.apply()
        sessionTick = System.currentTimeMillis()
        Log.d(TAG, "Admin logged out.")
    }

    // --- Data Persistence Check ---

    fun hasCustomLocalData(): Boolean {
        return prefs?.getBoolean(KEY_HAS_LOCAL_DATA, false) ?: false
    }

    // --- Channel Profile Management ---

    fun saveChannelProfile(
        name: String,
        handle: String,
        subscribersCount: String,
        email: String,
        description: String,
        avatarUri: String?,
        isMonetized: Boolean
    ) {
        prefs?.edit()
            ?.putString(KEY_PROFILE_NAME, name)
            ?.putString(KEY_PROFILE_HANDLE, handle)
            ?.putString(KEY_PROFILE_SUBS, subscribersCount)
            ?.putString(KEY_PROFILE_EMAIL, email)
            ?.putString(KEY_PROFILE_DESC, description)
            ?.putString(KEY_PROFILE_AVATAR_URI, avatarUri ?: "")
            ?.putBoolean(KEY_PROFILE_MONETIZED, isMonetized)
            ?.putBoolean(KEY_HAS_LOCAL_DATA, true)
            ?.apply()

        val updated = StudioRepository.danielProfile.copy(
            name = name,
            handle = handle,
            subscribersCount = subscribersCount,
            email = email,
            description = description,
            avatarUri = avatarUri,
            isMonetized = isMonetized
        )
        StudioRepository.danielProfile = updated
        StudioRepository.currentProfile = updated
        StudioRepository.isMonetized = isMonetized
        Log.d(TAG, "Saved local Channel Profile: $name, subs: $subscribersCount")
    }

    // --- Channel Statistics Management ---

    fun saveChannelStatistics(
        views: String,
        watchTime: String,
        subscribers: String,
        revenue: String,
        period: String
    ) {
        prefs?.edit()
            ?.putString(KEY_STATS_VIEWS, views)
            ?.putString(KEY_STATS_WATCH_TIME, watchTime)
            ?.putString(KEY_STATS_SUBS, subscribers)
            ?.putString(KEY_STATS_REVENUE, revenue)
            ?.putString(KEY_STATS_PERIOD, period)
            ?.putBoolean(KEY_HAS_LOCAL_DATA, true)
            ?.apply()

        StudioRepository.dashboardViews = views
        StudioRepository.dashboardWatchTime = watchTime
        StudioRepository.dashboardSubscribers = subscribers
        StudioRepository.dashboardRevenue = revenue
        StudioRepository.dashboardPeriodLabel = period
        Log.d(TAG, "Saved local Channel Statistics: views=$views, revenue=$revenue")
    }

    // --- Individual Field Inline Update Helpers ---

    fun updateChannelName(name: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        prefs?.edit()
            ?.putString(KEY_PROFILE_NAME, clean)
            ?.putBoolean(KEY_HAS_LOCAL_DATA, true)
            ?.apply()
        val updated = StudioRepository.danielProfile.copy(name = clean)
        StudioRepository.danielProfile = updated
        StudioRepository.currentProfile = updated
    }

    fun updateSubscribersCount(subs: String) {
        val clean = subs.trim()
        if (clean.isEmpty()) return
        prefs?.edit()
            ?.putString(KEY_PROFILE_SUBS, clean)
            ?.putBoolean(KEY_HAS_LOCAL_DATA, true)
            ?.apply()
        val updated = StudioRepository.danielProfile.copy(subscribersCount = clean)
        StudioRepository.danielProfile = updated
        StudioRepository.currentProfile = updated
    }

    fun updateChannelHandle(handle: String) {
        val clean = handle.trim()
        if (clean.isEmpty()) return
        prefs?.edit()
            ?.putString(KEY_PROFILE_HANDLE, clean)
            ?.putBoolean(KEY_HAS_LOCAL_DATA, true)
            ?.apply()
        val updated = StudioRepository.danielProfile.copy(handle = clean)
        StudioRepository.danielProfile = updated
        StudioRepository.currentProfile = updated
    }

    fun updateDashboardViews(views: String) {
        val clean = views.trim()
        if (clean.isEmpty()) return
        prefs?.edit()
            ?.putString(KEY_STATS_VIEWS, clean)
            ?.putBoolean(KEY_HAS_LOCAL_DATA, true)
            ?.apply()
        StudioRepository.dashboardViews = clean
        StudioRepository.overviewViewsMetric = StudioRepository.overviewViewsMetric.copy(value = clean)
    }

    fun updateDashboardWatchTime(watchTime: String) {
        val clean = watchTime.trim()
        if (clean.isEmpty()) return
        prefs?.edit()
            ?.putString(KEY_STATS_WATCH_TIME, clean)
            ?.putBoolean(KEY_HAS_LOCAL_DATA, true)
            ?.apply()
        StudioRepository.dashboardWatchTime = clean
        StudioRepository.overviewWatchTimeMetric = StudioRepository.overviewWatchTimeMetric.copy(value = clean)
    }

    fun updateDashboardSubscribers(subs: String) {
        val clean = subs.trim()
        if (clean.isEmpty()) return
        prefs?.edit()
            ?.putString(KEY_STATS_SUBS, clean)
            ?.putBoolean(KEY_HAS_LOCAL_DATA, true)
            ?.apply()
        StudioRepository.dashboardSubscribers = clean
        StudioRepository.overviewSubscribersMetric = StudioRepository.overviewSubscribersMetric.copy(value = clean)
    }

    fun updateDashboardRevenue(revenue: String) {
        val clean = revenue.trim()
        if (clean.isEmpty()) return
        prefs?.edit()
            ?.putString(KEY_STATS_REVENUE, clean)
            ?.putBoolean(KEY_HAS_LOCAL_DATA, true)
            ?.apply()
        StudioRepository.dashboardRevenue = clean
        StudioRepository.overviewEstimatedRevenueMetric = StudioRepository.overviewEstimatedRevenueMetric.copy(value = clean)
    }

    fun updatePayoutAmount(amount: String) {
        val clean = amount.trim()
        if (clean.isEmpty()) return
        prefs?.edit()
            ?.putString(KEY_PAYOUT_AMOUNT, clean)
            ?.putBoolean(KEY_HAS_LOCAL_DATA, true)
            ?.apply()
        StudioRepository.payoutAmount = clean
    }

    fun updatePayoutHeadline(headline: String) {
        val clean = headline.trim()
        if (clean.isEmpty()) return
        prefs?.edit()
            ?.putString(KEY_PAYOUT_HEADLINE, clean)
            ?.putBoolean(KEY_HAS_LOCAL_DATA, true)
            ?.apply()
        StudioRepository.payoutHeadline = clean
    }

    fun updateMonthlyPayout(month: String, amount: String) {
        val cleanMonth = month.trim()
        val cleanAmount = amount.trim()
        prefs?.edit()
            ?.putString(KEY_PAYOUT_MONTHLY_MONTH, cleanMonth)
            ?.putString(KEY_PAYOUT_MONTHLY_AMOUNT, cleanAmount)
            ?.putBoolean(KEY_HAS_LOCAL_DATA, true)
            ?.apply()
        StudioRepository.payoutMonthlyMonth = cleanMonth
        StudioRepository.payoutMonthlyAmount = cleanAmount
    }

    fun updateMonthlyAudience(audience: String) {
        saveMonthlyAudience(audience.trim())
    }

    fun updateVideoTitle(videoId: String, newTitle: String) {
        val cleanTitle = newTitle.trim()
        if (cleanTitle.isEmpty()) return

        val existingVideo = StudioRepository.findVideoById(videoId)
        if (existingVideo != null) {
            val updated = existingVideo.copy(title = cleanTitle)
            addOrUpdateVideo(updated, showOnDashboard = StudioRepository.latestPublishedContent.any { it.id == videoId })
        }
    }

    fun updateVideoViews(videoId: String, newViews: Long) {
        val existingVideo = StudioRepository.findVideoById(videoId)
        if (existingVideo != null) {
            val updated = existingVideo.copy(views = newViews)
            addOrUpdateVideo(updated, showOnDashboard = StudioRepository.latestPublishedContent.any { it.id == videoId })
        }
    }

    // --- Exact Sum Analytics Distribution Algorithm ---

    /**
     * Distributes a total view/metric count across [days] such that the daily integers
     * add up EXACTLY to [total]. Simulates realistic weekend and trend variations.
     */
    fun distributeExactSum(total: Long, days: Int): List<Float> {
        if (days <= 0) return emptyList()
        if (total <= 0) return List(days) { 0f }

        val random = Random(System.currentTimeMillis())
        val rawWeights = DoubleArray(days) { index ->
            // Realistic day of week variance (weekends have higher traffic)
            val dayOfWeekFactor = when (index % 7) {
                5, 6 -> 1.30 // Friday/Saturday boost
                2 -> 0.88   // Mid-week dip
                else -> 1.0
            }
            // Controlled organic noise (+/- 20%)
            val noise = 0.80 + random.nextDouble() * 0.40
            // Slight organic trend over the period
            val trend = 0.90 + (index.toDouble() / days.toDouble()) * 0.20
            dayOfWeekFactor * noise * trend
        }

        val totalWeight = rawWeights.sum()
        val dailyValues = LongArray(days)
        val fractionalRemainders = DoubleArray(days)
        var allocatedSum = 0L

        for (i in 0 until days) {
            val exactShare = (rawWeights[i] / totalWeight) * total
            val baseVal = exactShare.toLong()
            dailyValues[i] = baseVal
            fractionalRemainders[i] = exactShare - baseVal
            allocatedSum += baseVal
        }

        // Remainder distribution: add 1 to the days with the largest fractional remainders
        val remainder = (total - allocatedSum).toInt()
        if (remainder > 0) {
            val sortedIndices = (0 until days).sortedByDescending { fractionalRemainders[it] }
            for (r in 0 until minOf(remainder, days)) {
                dailyValues[sortedIndices[r]] += 1
            }
        }

        return dailyValues.map { it.toFloat() }
    }

    /**
     * Generates and saves consistent analytics charts for views, watch time, and revenue.
     */
    fun generateAndSaveAnalytics(
        days: Int,
        totalViews: Long,
        totalWatchTimeHours: Float,
        totalRevenueDollars: Float
    ) {
        val viewsDistribution = distributeExactSum(totalViews, days)
        
        // Generate consistent watch time based on views ratio
        val watchTimeRatio = if (totalViews > 0) (totalWatchTimeHours / totalViews) else 0.002f
        val watchTimeDistribution = viewsDistribution.map { dailyViews ->
            val hours = dailyViews * watchTimeRatio
            // Keep 2 decimal precision
            (Math.round(hours * 100.0) / 100.0).toFloat()
        }

        // Generate consistent revenue based on views ratio
        val revenueRatio = if (totalViews > 0) (totalRevenueDollars / totalViews) else 0.0015f
        val revenueDistribution = viewsDistribution.map { dailyViews ->
            val rev = dailyViews * revenueRatio
            (Math.round(rev * 100.0) / 100.0).toFloat()
        }

        // Subscribers trend (smooth representation)
        val subsDistribution = viewsDistribution.map { dailyViews ->
            val netSubs = (dailyViews * 0.008f) - (dailyViews * 0.002f)
            Math.round(netSubs).toFloat()
        }

        // Format dates label
        val dateLabel = "Last $days days"

        // Update StudioRepository MetricCardData
        val maxView = (viewsDistribution.maxOrNull() ?: 100f) * 1.2f
        val maxWatch = (watchTimeDistribution.maxOrNull() ?: 10f) * 1.2f
        val maxRev = (revenueDistribution.maxOrNull() ?: 100f) * 1.2f

        StudioRepository.overviewViewsMetric = StudioRepository.overviewViewsMetric.copy(
            value = formatNumberWithCommas(totalViews),
            subtitle = "Distributed evenly across $days days",
            trendData = viewsDistribution,
            yMinLabel = "0",
            yMidLabel = (maxView / 2).toInt().toString(),
            yMaxLabel = maxView.toInt().toString(),
            startDateLabel = "Day 1",
            endDateLabel = "Day $days"
        )

        StudioRepository.overviewWatchTimeMetric = StudioRepository.overviewWatchTimeMetric.copy(
            value = String.format("%.1f", totalWatchTimeHours),
            subtitle = "Based on $days days",
            trendData = watchTimeDistribution,
            yMinLabel = "0.0",
            yMidLabel = String.format("%.1f", maxWatch / 2),
            yMaxLabel = String.format("%.1f", maxWatch),
            startDateLabel = "Day 1",
            endDateLabel = "Day $days"
        )

        StudioRepository.overviewEstimatedRevenueMetric = StudioRepository.overviewEstimatedRevenueMetric.copy(
            value = String.format("$%.2f", totalRevenueDollars),
            subtitle = "Local demo revenue ($days days)",
            trendData = revenueDistribution,
            yMinLabel = "$0",
            yMidLabel = String.format("$%.0f", maxRev / 2),
            yMaxLabel = String.format("$%.0f", maxRev),
            startDateLabel = "Day 1",
            endDateLabel = "Day $days",
            isSimulatedDemo = true
        )

        StudioRepository.overviewSubscribersMetric = StudioRepository.overviewSubscribersMetric.copy(
            value = "+${subsDistribution.sum().toInt()}",
            subtitle = "$days days trend",
            trendData = subsDistribution,
            startDateLabel = "Day 1",
            endDateLabel = "Day $days"
        )

        // Save into local storage
        prefs?.edit()
            ?.putInt(KEY_ANALYTICS_DAYS, days)
            ?.putLong(KEY_ANALYTICS_TOTAL_VIEWS, totalViews)
            ?.putString(KEY_ANALYTICS_VIEWS_TREND, floatListToJson(viewsDistribution))
            ?.putString(KEY_ANALYTICS_WATCH_TREND, floatListToJson(watchTimeDistribution))
            ?.putString(KEY_ANALYTICS_SUBS_TREND, floatListToJson(subsDistribution))
            ?.putString(KEY_ANALYTICS_REV_TREND, floatListToJson(revenueDistribution))
            ?.putBoolean(KEY_HAS_LOCAL_DATA, true)
            ?.apply()

        Log.d(TAG, "Saved local analytics distribution for $days days. Sum check: ${viewsDistribution.sum().toLong()} == $totalViews")
    }

    // --- Videos and Shorts CRUD ---

    fun saveVideos(videos: List<VideoItem>) {
        StudioRepository.contentVideos = videos
        val regular = videos.filter { it.type == ContentType.VIDEO }
        if (regular.isNotEmpty() && StudioRepository.latestPublishedContent.isEmpty()) {
            StudioRepository.dashboardLatestVideo = regular.first()
        }
        val json = videosToJson(videos)
        prefs?.edit()
            ?.putString(KEY_VIDEOS_JSON, json)
            ?.putBoolean(KEY_HAS_LOCAL_DATA, true)
            ?.apply()
    }

    fun saveShorts(shorts: List<VideoItem>) {
        StudioRepository.contentShorts = shorts
        val json = videosToJson(shorts)
        prefs?.edit()
            ?.putString(KEY_SHORTS_JSON, json)
            ?.putBoolean(KEY_HAS_LOCAL_DATA, true)
            ?.apply()
    }

    fun saveLiveStreams(liveStreams: List<VideoItem>) {
        StudioRepository.contentLiveStreams = liveStreams
        val json = videosToJson(liveStreams)
        prefs?.edit()
            ?.putString(KEY_LIVE_VIDEOS_JSON, json)
            ?.putBoolean(KEY_HAS_LOCAL_DATA, true)
            ?.apply()
    }

    fun saveDashboardVideos(videos: List<VideoItem>) {
        StudioRepository.latestPublishedContent = videos
        if (videos.isNotEmpty()) {
            StudioRepository.dashboardLatestVideo = videos.first()
        }
        val json = videosToJson(videos)
        prefs?.edit()
            ?.putString(KEY_DASHBOARD_VIDEOS_JSON, json)
            ?.putBoolean(KEY_HAS_LOCAL_DATA, true)
            ?.apply()
    }

    fun addOrUpdateVideo(item: VideoItem, showOnDashboard: Boolean = false) {
        when (item.type) {
            ContentType.SHORT -> {
                val current = StudioRepository.contentShorts.toMutableList()
                val existingIndex = current.indexOfFirst { it.id == item.id }
                if (existingIndex >= 0) {
                    current[existingIndex] = item
                } else {
                    current.add(0, item)
                }
                saveShorts(current)
            }
            ContentType.LIVE -> {
                val current = StudioRepository.contentLiveStreams.toMutableList()
                val existingIndex = current.indexOfFirst { it.id == item.id }
                if (existingIndex >= 0) {
                    current[existingIndex] = item
                } else {
                    current.add(0, item)
                }
                saveLiveStreams(current)
            }
            else -> {
                val current = StudioRepository.contentVideos.toMutableList()
                val existingIndex = current.indexOfFirst { it.id == item.id }
                if (existingIndex >= 0) {
                    current[existingIndex] = item
                } else {
                    current.add(0, item)
                }
                saveVideos(current)
            }
        }

        if (showOnDashboard) {
            val dash = StudioRepository.latestPublishedContent.toMutableList()
            val existing = dash.indexOfFirst { it.id == item.id }
            if (existing >= 0) {
                dash[existing] = item
            } else {
                dash.add(0, item)
            }
            saveDashboardVideos(dash)
        }
    }

    fun deleteVideo(id: String, type: ContentType) {
        when (type) {
            ContentType.SHORT -> {
                val updated = StudioRepository.contentShorts.filter { it.id != id }
                saveShorts(updated)
            }
            ContentType.LIVE -> {
                val updated = StudioRepository.contentLiveStreams.filter { it.id != id }
                saveLiveStreams(updated)
            }
            else -> {
                val updated = StudioRepository.contentVideos.filter { it.id != id }
                saveVideos(updated)
            }
        }
        val updatedDash = StudioRepository.latestPublishedContent.filter { it.id != id }
        if (updatedDash.size != StudioRepository.latestPublishedContent.size) {
            saveDashboardVideos(updatedDash)
        }
    }

    fun deleteVideo(id: String, isShort: Boolean) {
        deleteVideo(id, if (isShort) ContentType.SHORT else ContentType.VIDEO)
    }

    // --- Monetization & Payout Management ---

    fun saveMonetization(
        isMonetized: Boolean,
        watchHours: String,
        requiredWatchHours: String,
        shortsViews: String,
        requiredShortsViews: String,
        videoUploads: String
    ) {
        prefs?.edit()
            ?.putBoolean(KEY_PROFILE_MONETIZED, isMonetized)
            ?.putString(KEY_MONETIZATION_WATCH_HOURS, watchHours)
            ?.putString(KEY_MONETIZATION_REQ_WATCH_HOURS, requiredWatchHours)
            ?.putString(KEY_MONETIZATION_SHORTS_VIEWS, shortsViews)
            ?.putString(KEY_MONETIZATION_REQ_SHORTS_VIEWS, requiredShortsViews)
            ?.putString(KEY_MONETIZATION_VIDEO_UPLOADS, videoUploads)
            ?.putBoolean(KEY_HAS_LOCAL_DATA, true)
            ?.apply()

        StudioRepository.isMonetized = isMonetized
        StudioRepository.monetizationWatchHours = watchHours
        StudioRepository.monetizationRequiredWatchHours = requiredWatchHours
        StudioRepository.monetizationShortsViews = shortsViews
        StudioRepository.monetizationRequiredShortsViews = requiredShortsViews
        StudioRepository.monetizationVideoUploads = videoUploads
        StudioRepository.danielProfile = StudioRepository.danielProfile.copy(isMonetized = isMonetized)
        StudioRepository.currentProfile = StudioRepository.currentProfile.copy(isMonetized = isMonetized)

        Log.d(TAG, "Saved local monetization: isMonetized=$isMonetized, watchHours=$watchHours")
    }

    fun savePayout(
        headline: String,
        amount: String,
        monthlyMonth: String,
        monthlyAmount: String,
        minimumText: String
    ) {
        prefs?.edit()
            ?.putString(KEY_PAYOUT_HEADLINE, headline)
            ?.putString(KEY_PAYOUT_AMOUNT, amount)
            ?.putString(KEY_PAYOUT_MONTHLY_MONTH, monthlyMonth)
            ?.putString(KEY_PAYOUT_MONTHLY_AMOUNT, monthlyAmount)
            ?.putString(KEY_PAYOUT_MINIMUM_TEXT, minimumText)
            ?.putBoolean(KEY_HAS_LOCAL_DATA, true)
            ?.apply()

        StudioRepository.payoutHeadline = headline
        StudioRepository.payoutAmount = amount
        StudioRepository.payoutMonthlyMonth = monthlyMonth
        StudioRepository.payoutMonthlyAmount = monthlyAmount
        StudioRepository.payoutMinimumText = minimumText
        Log.d(TAG, "Saved local payout: amount=$amount, month=$monthlyMonth")
    }

    fun saveMonthlyAudience(monthlyAudience: String) {
        prefs?.edit()
            ?.putString(KEY_MONTHLY_AUDIENCE, monthlyAudience)
            ?.putBoolean(KEY_HAS_LOCAL_DATA, true)
            ?.apply()

        StudioRepository.communityMonthlyAudience = monthlyAudience
    }

    // --- Reset Demo Data ---

    fun resetToDefaultDemoData(context: Context) {
        prefs?.edit()?.clear()?.apply()
        StudioRepository.resetToDefaults()
        Log.d(TAG, "Reset all local customizations to default demo values.")
    }

    // --- Loading & Restoring ---

    private fun loadSavedDataIntoRepository() {
        val p = prefs ?: return
        if (!p.getBoolean(KEY_HAS_LOCAL_DATA, false)) {
            return
        }

        try {
            // 1. Channel Profile
            val savedName = p.getString(KEY_PROFILE_NAME, null)
            if (!savedName.isNullOrBlank()) {
                val profile = StudioRepository.danielProfile.copy(
                    name = savedName,
                    handle = p.getString(KEY_PROFILE_HANDLE, StudioRepository.danielProfile.handle) ?: StudioRepository.danielProfile.handle,
                    subscribersCount = p.getString(KEY_PROFILE_SUBS, StudioRepository.danielProfile.subscribersCount) ?: StudioRepository.danielProfile.subscribersCount,
                    email = p.getString(KEY_PROFILE_EMAIL, StudioRepository.danielProfile.email) ?: StudioRepository.danielProfile.email,
                    description = p.getString(KEY_PROFILE_DESC, "") ?: "",
                    avatarUri = p.getString(KEY_PROFILE_AVATAR_URI, null).takeIf { !it.isNullOrBlank() },
                    isMonetized = p.getBoolean(KEY_PROFILE_MONETIZED, true)
                )
                StudioRepository.danielProfile = profile
                StudioRepository.currentProfile = profile
                StudioRepository.isMonetized = profile.isMonetized
            }

            // 2. Statistics
            p.getString(KEY_STATS_VIEWS, null)?.let { StudioRepository.dashboardViews = it }
            p.getString(KEY_STATS_WATCH_TIME, null)?.let { StudioRepository.dashboardWatchTime = it }
            p.getString(KEY_STATS_SUBS, null)?.let { StudioRepository.dashboardSubscribers = it }
            p.getString(KEY_STATS_REVENUE, null)?.let { StudioRepository.dashboardRevenue = it }
            p.getString(KEY_STATS_PERIOD, null)?.let { StudioRepository.dashboardPeriodLabel = it }

            // 3. Analytics Trends
            val viewsTrendJson = p.getString(KEY_ANALYTICS_VIEWS_TREND, null)
            if (!viewsTrendJson.isNullOrBlank()) {
                val viewsList = jsonToFloatList(viewsTrendJson)
                if (viewsList.isNotEmpty()) {
                    StudioRepository.overviewViewsMetric = StudioRepository.overviewViewsMetric.copy(
                        trendData = viewsList,
                        value = formatNumberWithCommas(p.getLong(KEY_ANALYTICS_TOTAL_VIEWS, viewsList.sum().toLong()))
                    )
                }
            }

            val watchTrendJson = p.getString(KEY_ANALYTICS_WATCH_TREND, null)
            if (!watchTrendJson.isNullOrBlank()) {
                val watchList = jsonToFloatList(watchTrendJson)
                if (watchList.isNotEmpty()) {
                    StudioRepository.overviewWatchTimeMetric = StudioRepository.overviewWatchTimeMetric.copy(trendData = watchList)
                }
            }

            val revTrendJson = p.getString(KEY_ANALYTICS_REV_TREND, null)
            if (!revTrendJson.isNullOrBlank()) {
                val revList = jsonToFloatList(revTrendJson)
                if (revList.isNotEmpty()) {
                    StudioRepository.overviewEstimatedRevenueMetric = StudioRepository.overviewEstimatedRevenueMetric.copy(trendData = revList)
                }
            }

            // 4. Videos & Shorts
            val videosJson = p.getString(KEY_VIDEOS_JSON, null)
            if (!videosJson.isNullOrBlank()) {
                val list = jsonToVideos(videosJson)
                if (list.isNotEmpty()) {
                    StudioRepository.contentVideos = list
                    StudioRepository.dashboardLatestVideo = list.first()
                }
            }

            val shortsJson = p.getString(KEY_SHORTS_JSON, null)
            if (!shortsJson.isNullOrBlank()) {
                val list = jsonToVideos(shortsJson)
                if (list.isNotEmpty()) {
                    StudioRepository.contentShorts = list
                }
            }

            // 5. Monetization & Payout
            p.getString(KEY_MONETIZATION_WATCH_HOURS, null)?.let { StudioRepository.monetizationWatchHours = it }
            p.getString(KEY_MONETIZATION_REQ_WATCH_HOURS, null)?.let { StudioRepository.monetizationRequiredWatchHours = it }
            p.getString(KEY_MONETIZATION_SHORTS_VIEWS, null)?.let { StudioRepository.monetizationShortsViews = it }
            p.getString(KEY_MONETIZATION_REQ_SHORTS_VIEWS, null)?.let { StudioRepository.monetizationRequiredShortsViews = it }
            p.getString(KEY_MONETIZATION_VIDEO_UPLOADS, null)?.let { StudioRepository.monetizationVideoUploads = it }
            p.getString(KEY_PAYOUT_HEADLINE, null)?.let { StudioRepository.payoutHeadline = it }
            p.getString(KEY_PAYOUT_AMOUNT, null)?.let { StudioRepository.payoutAmount = it }
            p.getString(KEY_PAYOUT_MONTHLY_MONTH, null)?.let { StudioRepository.payoutMonthlyMonth = it }
            p.getString(KEY_PAYOUT_MONTHLY_AMOUNT, null)?.let { StudioRepository.payoutMonthlyAmount = it }
            p.getString(KEY_PAYOUT_MINIMUM_TEXT, null)?.let { StudioRepository.payoutMinimumText = it }
            p.getString(KEY_MONTHLY_AUDIENCE, null)?.let { StudioRepository.communityMonthlyAudience = it }

            val liveJson = p.getString(KEY_LIVE_VIDEOS_JSON, null)
            if (!liveJson.isNullOrBlank()) {
                val list = jsonToVideos(liveJson)
                if (list.isNotEmpty()) {
                    StudioRepository.contentLiveStreams = list
                }
            }

            val dashJson = p.getString(KEY_DASHBOARD_VIDEOS_JSON, null)
            if (!dashJson.isNullOrBlank()) {
                val list = jsonToVideos(dashJson)
                if (list.isNotEmpty()) {
                    StudioRepository.latestPublishedContent = list
                    StudioRepository.dashboardLatestVideo = list.first()
                }
            }

            Log.d(TAG, "Successfully loaded custom local data into StudioRepository.")
        } catch (e: Exception) {
            Log.e(TAG, "Error reading local admin data", e)
        }
    }

    // --- JSON Serialization Helpers ---

    private fun floatListToJson(list: List<Float>): String {
        val array = JSONArray()
        list.forEach { array.put(it.toDouble()) }
        return array.toString()
    }

    private fun jsonToFloatList(json: String): List<Float> {
        val list = mutableListOf<Float>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                list.add(array.getDouble(i).toFloat())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed parsing float list JSON", e)
        }
        return list
    }

    private fun videosToJson(videos: List<VideoItem>): String {
        val array = JSONArray()
        videos.forEach { v ->
            val obj = JSONObject().apply {
                put("id", v.id)
                put("title", v.title)
                put("type", v.type.name)
                put("duration", v.duration)
                put("publishedTimeAgo", v.publishedTimeAgo)
                put("views", v.views)
                put("likes", v.likes)
                put("comments", v.comments)
                put("rankingText", v.rankingText)
                put("avgPercentageViewed", v.avgPercentageViewed)
                put("thumbnailTitle", v.thumbnailTitle)
                put("isExpanded", v.isExpanded)
                put("category", v.category)
                put("description", v.description)
                put("thumbnailUri", v.thumbnailUri ?: "")
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun jsonToVideos(json: String): List<VideoItem> {
        val list = mutableListOf<VideoItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                val typeStr = o.optString("type", "VIDEO")
                val type = try { ContentType.valueOf(typeStr) } catch (_: Exception) { ContentType.VIDEO }
                val uriStr = o.optString("thumbnailUri", "").takeIf { it.isNotBlank() }

                list.add(
                    VideoItem(
                        id = o.optString("id", "v_${System.currentTimeMillis()}_$i"),
                        title = o.optString("title", "Untitled Video"),
                        type = type,
                        duration = o.optString("duration", if (type == ContentType.SHORT) "0:30" else "5:00"),
                        publishedTimeAgo = o.optString("publishedTimeAgo", "Just now"),
                        views = o.optLong("views", 0L),
                        likes = o.optLong("likes", 0L),
                        comments = o.optLong("comments", 0L),
                        rankingText = o.optString("rankingText", "1 of 10"),
                        avgPercentageViewed = o.optString("avgPercentageViewed", "65%"),
                        thumbnailTitle = o.optString("thumbnailTitle", ""),
                        isExpanded = o.optBoolean("isExpanded", false),
                        category = o.optString("category", "Entertainment"),
                        description = o.optString("description", ""),
                        thumbnailUri = uriStr
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed parsing videos JSON", e)
        }
        return list
    }

    private fun formatNumberWithCommas(value: Long): String {
        return java.text.NumberFormat.getIntegerInstance().format(value)
    }
}

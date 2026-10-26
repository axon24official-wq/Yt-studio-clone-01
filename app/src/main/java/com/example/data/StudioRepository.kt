package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

object StudioRepository {
    private const val TAG = "StudioRepository"

    // Default Profile (Matching official YouTube Studio reference)
    private val defaultDanielProfile = ChannelProfile(
        id = "channel_daniel",
        name = "Amrit Ruhidas Official",
        handle = "@amritruhidassofficial",
        subscribersCount = "14,220",
        isMonetized = true
    )

    // Reactive Profile (updates automatically when synced)
    var danielProfile by mutableStateOf(defaultDanielProfile)
    var currentProfile by mutableStateOf(defaultDanielProfile)

    val demoChannels = listOf(
        defaultDanielProfile,
        ChannelProfile(
            id = "channel_angel",
            name = "Angel",
            handle = "@angel-664",
            subscribersCount = "2,460"
        ),
        ChannelProfile(
            id = "channel_music",
            name = "Angel Mashups",
            handle = "@angel_mashups",
            subscribersCount = "12.4K"
        )
    )

    // Dashboard analytics 2x2 grid data (reactive Compose state)
    var dashboardViews by mutableStateOf("24.8 lakh")
    var dashboardWatchTime by mutableStateOf("16.8K")
    var dashboardSubscribers by mutableStateOf("+3.8K")
    var dashboardRevenue by mutableStateOf("$416.93")
    var dashboardPeriodLabel by mutableStateOf("Last 28 days")

    // Monetization States (reactive)
    var isMonetized by mutableStateOf(true)
    var monetizationWatchHours by mutableStateOf("3,140")
    var monetizationRequiredWatchHours by mutableStateOf("3,000")
    var monetizationShortsViews by mutableStateOf("3.2M")
    var monetizationRequiredShortsViews by mutableStateOf("3.0M")
    var monetizationVideoUploads by mutableStateOf("5")

    // Sync State
    var isSyncing by mutableStateOf(false)
    var isConnectedToSupabase by mutableStateOf(false)
    var lastSyncError by mutableStateOf<String?>(null)

    // Dashboard latest published video
    var dashboardLatestVideo by mutableStateOf(
        VideoItem(
            id = "vid_motu_patlu",
            title = "Motu Patlu Characters In Real ...",
            type = ContentType.VIDEO,
            duration = "8:12",
            publishedTimeAgo = "First 1 hours, 9 minutes",
            views = 204,
            likes = 3,
            comments = 8,
            rankingText = "1 of 10",
            avgPercentageViewed = "72.4%",
            isExpanded = true,
            thumbnailTitle = "Motu Patlu Characters In Real ..."
        )
    )

    // Dashboard latest content list
    var latestPublishedContent by mutableStateOf(
        listOf(
            VideoItem(
                id = "vid_motu_patlu",
                title = "Motu Patlu Characters In Real ...",
                type = ContentType.VIDEO,
                duration = "8:12",
                publishedTimeAgo = "First 1 hours, 9 minutes",
                views = 204,
                likes = 3,
                comments = 8,
                rankingText = "1 of 10",
                avgPercentageViewed = "72.4%",
                isExpanded = false,
                thumbnailTitle = "Motu Patlu Characters In Real ..."
            ),
            VideoItem(
                id = "vid_jungle_book",
                title = "The Jungle Book Characters Tr...",
                type = ContentType.VIDEO,
                duration = "10:45",
                publishedTimeAgo = "First 1 days, 1 hours",
                views = 2400,
                likes = 15,
                comments = 19,
                rankingText = "2 of 10",
                avgPercentageViewed = "65.1%",
                isExpanded = false,
                thumbnailTitle = "The Jungle Book Characters Tr..."
            )
        )
    )

    val dashboardComment = CommentItem(
        id = "c1",
        authorHandle = "@gautamm24",
        timeAgo = "1 month ago",
        content = "Ye kosa gana hai bhai ??☠️🧐",
        videoTitle = "80 HAZAR KE MERE SHOES 👟 MEKO MUT TU KUDH KO PUCH#mcstan #f...",
        likesCount = 0
    )

    // Content tab lists (reactive)
    var contentVideos by mutableStateOf(
        listOf(
            VideoItem(
                id = "c_vid_ias_1",
                title = "गरीब लड़की बनी IAS OFFICER | Waqt Sabka Badalta Hai",
                type = ContentType.VIDEO,
                duration = "19:44",
                publishedTimeAgo = "6 years ago",
                views = 30000000,
                likes = 190000,
                comments = 4100,
                thumbnailTitle = "गरीब लड़की बनी IAS OFFICER"
            ),
            VideoItem(
                id = "c_vid_ias_2",
                title = "गरीब बना IAS OFFICER | Struggle of IAS Aspirant",
                type = ContentType.VIDEO,
                duration = "19:34",
                publishedTimeAgo = "6 years ago",
                views = 24000000,
                likes = 442000,
                comments = 15000,
                thumbnailTitle = "STRUGGLE OF IAS ASPIRANT"
            ),
            VideoItem(
                id = "c_vid_ias_3",
                title = "गरीब बना IAS OFFICER | Waqt Sabka Badalta Hai",
                type = ContentType.VIDEO,
                duration = "14:42",
                publishedTimeAgo = "6 years ago",
                views = 19000000,
                likes = 179000,
                comments = 6500,
                thumbnailTitle = "मजदूर बना IAS OFFICER"
            ),
            VideoItem(
                id = "c_vid_ias_4",
                title = "Motivational Story Of Every IAS Aspirant | Rahul Kumar",
                type = ContentType.VIDEO,
                duration = "32:28",
                publishedTimeAgo = "5 years ago",
                views = 15000000,
                likes = 168000,
                comments = 4400,
                thumbnailTitle = "STRUGGLE OF IAS ASPIRANT"
            ),
            VideoItem(
                id = "c_vid_ias_5",
                title = "Motivational Story Of Every IAS Aspirant | Real Struggle",
                type = ContentType.VIDEO,
                duration = "14:31",
                publishedTimeAgo = "6 years ago",
                views = 7800000,
                likes = 127000,
                comments = 3000,
                thumbnailTitle = "STRUGGLE OF IAS ASPIRANT"
            ),
            VideoItem(
                id = "c_vid_ias_6",
                title = "Biwi Ho Toh Aisi | HUSBAND HARRASING LOVING WIFE",
                type = ContentType.VIDEO,
                duration = "28:29",
                publishedTimeAgo = "5 years ago",
                views = 6100000,
                likes = 29000,
                comments = 570,
                thumbnailTitle = "काली लड़की का प्यार"
            )
        )
    )

    var contentShorts by mutableStateOf(
        listOf(
            VideoItem(
                id = "s_1",
                title = "Husband Harassing Loving Wife #shorts",
                type = ContentType.SHORT,
                duration = "0:15",
                publishedTimeAgo = "28 days ago",
                views = 73600,
                likes = 5400,
                comments = 412,
                thumbnailTitle = "Husband Harassing"
            ),
            VideoItem(
                id = "s_2",
                title = "Jab Meri Biwi Juth Bolke Paise Mangti Hai",
                type = ContentType.SHORT,
                duration = "0:22",
                publishedTimeAgo = "28 days ago",
                views = 3300,
                likes = 240,
                comments = 28,
                thumbnailTitle = "Jab Meri Biwi"
            ),
            VideoItem(
                id = "s_3",
                title = "Jab Meri Biwi Gussa ho Jaye #shorts",
                type = ContentType.SHORT,
                duration = "0:18",
                publishedTimeAgo = "28 days ago",
                views = 2300,
                likes = 180,
                comments = 15,
                thumbnailTitle = "Biwi Gussa"
            ),
            VideoItem(
                id = "s_4",
                title = "80 HAZAR KE MERE SHOES 👟 MEKO...",
                type = ContentType.SHORT,
                duration = "0:15",
                publishedTimeAgo = "1 month ago",
                views = 735,
                likes = 45,
                comments = 8,
                thumbnailTitle = "80 HAZAR"
            )
        )
    )

    var contentLiveStreams by mutableStateOf(
        listOf(
            VideoItem(
                id = "l_1",
                title = "Live Q&A With Fans | Amrit Ruhidas Official",
                type = ContentType.LIVE,
                duration = "45:12",
                publishedTimeAgo = "3 months ago",
                views = 12400,
                likes = 850,
                comments = 320,
                thumbnailTitle = "LIVE STREAM"
            ),
            VideoItem(
                id = "l_2",
                title = "Special Announcement & Video Teaser Live",
                type = ContentType.LIVE,
                duration = "28:04",
                publishedTimeAgo = "5 months ago",
                views = 8900,
                likes = 540,
                comments = 180,
                thumbnailTitle = "ANNOUNCEMENT"
            )
        )
    )

    // Analytics: Overview Tab (reactive)
    var overviewViewsMetric by mutableStateOf(
        MetricCardData(
            title = "Views",
            value = "90.7k",
            subtitle = "15.7k more than usual",
            isPositive = true,
            showArrow = true,
            trendData = listOf(28f, 29f, 28f, 29f, 29f, 28f, 28f, 29f, 32f, 31f, 30f, 28f, 29f, 31f, 31f, 30f, 29f, 30f, 35f, 62f, 57f),
            yMinLabel = "0",
            yMidLabel = "4.2k",
            yMaxLabel = "6.3k",
            startDateLabel = "27 Sept",
            endDateLabel = "24 Oct"
        )
    )

    var overviewWatchTimeMetric by mutableStateOf(
        MetricCardData(
            title = "Watch time (hours)",
            value = "1.1k",
            subtitle = "About the same as usual",
            isPositive = true,
            showArrow = false,
            isCheckmark = true,
            trendData = listOf(35f, 34f, 33f, 35f, 36f, 35f, 37f, 38f, 36f, 37f, 36f, 38f, 37f, 36f, 38f, 39f, 37f, 38f, 42f, 49f, 46f),
            yMinLabel = "0.0",
            yMidLabel = "32.7",
            yMaxLabel = "49.1",
            startDateLabel = "27 Sept",
            endDateLabel = "24 Oct"
        )
    )

    var overviewSubscribersMetric by mutableStateOf(
        MetricCardData(
            title = "Subscribers",
            value = "+3.8K",
            subtitle = "1.2K more than usual",
            isPositive = true,
            showArrow = true,
            trendData = listOf(12f, 15f, 18f, 14f, 16f, 20f, 19f, 22f, 24f, 21f, 23f, 25f, 28f, 32f, 35f, 40f, 45f, 52f, 58f, 65f),
            yMinLabel = "0",
            yMidLabel = "+2.0K",
            yMaxLabel = "+4.0K",
            startDateLabel = "27 Sept",
            endDateLabel = "24 Oct"
        )
    )

    var overviewEstimatedRevenueMetric by mutableStateOf(
        MetricCardData(
            title = "Estimated revenue",
            value = "$416.93",
            subtitle = "US$1.20 more than usual",
            isPositive = true,
            showArrow = true,
            trendData = listOf(25f, 30f, 35f, 45f, 42f, 50f, 48f, 55f, 52f, 40f, 44f, 48f, 52f, 58f, 62f, 59f, 65f, 68f, 70f, 75f),
            yMinLabel = "$0",
            yMidLabel = "$200",
            yMaxLabel = "$500",
            startDateLabel = "27 Sept",
            endDateLabel = "24 Oct",
            isSimulatedDemo = false
        )
    )

    val topContentList = listOf(
        VideoItem("tc_1", "Husband Harassing Loving Wif...", ContentType.SHORT, "0:15", "28 days", 73600, 5400),
        VideoItem("tc_2", "Jab Meri Biwi Juth Bolke Paise...", ContentType.SHORT, "0:22", "28 days", 3300, 240),
        VideoItem("tc_3", "Jab Meri Biwi Gussa ho Jaye #r...", ContentType.SHORT, "0:18", "28 days", 2300, 180),
        VideoItem("tc_4", "बीवी ने दिया धोखा अपने पति को | Hus...", ContentType.SHORT, "0:30", "28 days", 880, 75),
        VideoItem("tc_5", "Karwa Chauth Par Biwi Ki Dema...", ContentType.SHORT, "0:25", "28 days", 547, 42)
    )

    val realtimeVideos = listOf(
        RealtimeVideo("80 HAZAR KE MERE ...", 42, listOf(0.1f, 0.3f, 0.8f, 0.4f, 0.2f, 0.5f, 0.9f, 0.2f)),
        RealtimeVideo("MC STAN - Mere Ha...", 3, listOf(0.0f, 0.0f, 0.2f, 0.0f, 0.5f, 0.0f)),
        RealtimeVideo("295 - Sidhu Moose...", 2, listOf(0.0f, 0.2f, 0.0f, 0.3f, 0.0f, 0.0f)),
        RealtimeVideo("Abhi ke Abhi Ke Jak...", 1, listOf(0.0f, 0.0f, 0.1f, 0.0f, 0.0f, 0.0f)),
        RealtimeVideo("Phir Hera Pheri X M...", 1, listOf(0.0f, 0.1f, 0.0f, 0.0f, 0.0f, 0.0f))
    )

    val drilldownTopContentViews = listOf(
        DrilldownContentItem("80 HAZAR KE MERE SHOES 👟...", "735", 0.90f, "80 HAZAR", true),
        DrilldownContentItem("Mohabbat Mohabbat Mohabbat Hui...", "10", 0.12f, "Mohabbat", true),
        DrilldownContentItem("Abhi ke Abhi Ke Jake Peheli Ticket...", "8", 0.10f, "Abhi ke Abhi", true),
        DrilldownContentItem("MC STAN - Mere Haath Mein X Vija...", "6", 0.08f, "MC STAN", false),
        DrilldownContentItem("Mc Stan - Tujhe Bhula Diya Ft.Emiw...", "6", 0.08f, "Tujhe Bhula", false),
        DrilldownContentItem("VTEN - MAHA SHIVARATRI MASHU...", "5", 0.06f, "VTEN", false),
        DrilldownContentItem("Bride's Entry Dance for her Groom ||...", "4", 0.05f, "Bride", true),
        DrilldownContentItem("Tablig ejtema mosali#mosali #man...", "3", 0.04f, "Tablig", true),
        DrilldownContentItem("Phir Hera Pheri X MC STAN | Drill R...", "3", 0.04f, "Phir Hera Pheri", false),
        DrilldownContentItem("Cute Shayari Status in Girl Voice Fr...", "2", 0.03f, "Shayari", true),
        DrilldownContentItem("lanta photo 🔥#divine #freefire...", "2", 0.03f, "lanta", true),
        DrilldownContentItem("MC Stan Remix: Soniye Heeriye X P...", "2", 0.03f, "Remix", false),
        DrilldownContentItem("295 - Sidhu Moose Wala | New so...", "2", 0.03f, "295", false),
        DrilldownContentItem("Mc Stan X Bewafa Ft.Imran khan X...", "1", 0.02f, "Bewafa", false),
        DrilldownContentItem("Jamal Jamaloo - Bobby Deol Entr...", "1", 0.02f, "Jamal", false)
    )

    val drilldownTopContentWatchTime = listOf(
        DrilldownContentItem("80 HAZAR KE MERE SHOES 👟...", "1.3", 0.72f, "80 HAZAR", true),
        DrilldownContentItem("Mc Stan - Tujhe Bhula Diya Ft.Emiw...", "0.1", 0.10f, "Tujhe Bhula", false)
    )

    val drilldownTopContentSubscribers = listOf(
        DrilldownContentItem("shayari ho jailfree fire funny whatsa...", "0", 0.0f, "shayari", true),
        DrilldownContentItem("free Fire funny shayari 😂 #ffshorts...", "0", 0.0f, "free Fire", true),
        DrilldownContentItem("Abhi ke Abhi Ke Jake Peheli Ticket...", "0", 0.0f, "Abhi ke Abhi", true),
        DrilldownContentItem("Mc stan X Bewafa Ft.Imran khan X...", "0", 0.0f, "Bewafa", false),
        DrilldownContentItem("Cute Shayari Status in Girl Voice Fr...", "0", 0.0f, "Shayari", true),
        DrilldownContentItem("Jamal Jamaloo - Bobby Deol Entr...", "0", 0.0f, "Jamal", false),
        DrilldownContentItem("EMIWAY BANTAI - \"MUJHKO YAAD...", "0", 0.0f, "EMIWAY", false),
        DrilldownContentItem("JANAM JANAM FT.Vijay Dk X Mc sta...", "0", 0.0f, "JANAM", false)
    )

    val drilldownTopContentRevenue = listOf(
        DrilldownContentItem("80 HAZAR KE MERE SHOES 👟...", "$980.50", 0.78f, "80 HAZAR", true),
        DrilldownContentItem("Mc Stan - Tujhe Bhula Diya Ft.Emiw...", "$145.20", 0.12f, "Tujhe Bhula", false),
        DrilldownContentItem("VTEN - MAHA SHIVARATRI MASHU...", "$78.40", 0.06f, "VTEN", false),
        DrilldownContentItem("MC STAN - Mere Haath Mein X Vija...", "$43.22", 0.04f, "MC STAN", false)
    )

    // Analytics: Content Tab (Videos filter)
    val contentVideosViewsMetric = MetricCardData(
        title = "Views",
        value = "34",
        subtitle = "16 fewer than usual",
        isPositive = false,
        showArrow = true,
        trendData = listOf(1f, 0.5f, 2f, 1f, 1f, 1f, 3f, 3f, 1f, 0f, 0f, 3f, 2f, 2f, 1f, 0f, 0f, 1f, 0f, 1f, 1f, 4f, 3f, 1f),
        yMinLabel = "0",
        yMidLabel = "2",
        yMaxLabel = "6"
    )

    val contentVideosImpressionsMetric = MetricCardData(
        title = "Thumbnail impressions",
        value = "231",
        subtitle = "27% more than previous 28 days",
        isPositive = true,
        showArrow = true,
        trendData = listOf(8f, 12f, 18f, 5f, 8f, 14f, 11f, 12f, 7f, 3f, 8f, 11f, 14f, 15f, 1f, 3f, 10f, 7f, 13f, 8f, 11f, 10f, 26f),
        yMinLabel = "0",
        yMidLabel = "9",
        yMaxLabel = "27"
    )

    val contentVideosCtrMetric = MetricCardData(
        title = "Thumbnail click-through rate",
        value = "6.5%",
        subtitle = "",
        isPositive = true,
        showArrow = false,
        trendData = listOf(0.1f, 0.15f, 0.25f, 0.12f, 0.15f, 0.3f, 0.2f, 0.22f, 0.12f, 0.14f, 0.28f, 0.45f, 0.12f, 0.15f, 0.5f, 0.15f, 0.16f, 0.35f, 0.28f),
        yMinLabel = "0.0%",
        yMidLabel = "20.0%",
        yMaxLabel = "60.0%"
    )

    val contentVideosAvgDurationMetric = MetricCardData(
        title = "Average view duration",
        value = "0:56",
        subtitle = "",
        isPositive = true,
        showArrow = false,
        trendData = listOf(0.1f, 0.15f, 0.1f, 0.1f, 0.2f, 0.18f, 0.65f, 0.1f, 0.1f, 0.12f, 0.45f, 0.2f, 0.1f, 0.1f, 0.1f, 0.15f, 0.1f, 0.5f, 0.15f, 0.3f, 0.4f, 0.35f),
        yMinLabel = "0:00",
        yMidLabel = "1:09",
        yMaxLabel = "3:27"
    )

    val trafficSources = listOf(
        TrafficSourceItem("YouTube search", 58.8f),
        TrafficSourceItem("Browse features", 20.6f),
        TrafficSourceItem("Other YouTube features", 8.8f)
    )

    // Analytics: Audience Tab
    val audienceMonthlyMetric = MetricCardData(
        title = "Monthly audience",
        value = "368",
        subtitle = "",
        isPositive = true,
        showArrow = false,
        trendData = listOf(170f, 210f, 260f, 310f, 350f, 380f, 410f, 440f, 460f, 470f, 490f, 500f, 510f, 500f, 505f, 490f, 440f, 380f, 368f),
        yMinLabel = "0",
        yMidLabel = "170",
        yMaxLabel = "510"
    )

    val deviceBreakdown = listOf(
        DeviceTypeItem("Mobile", 91.8f),
        DeviceTypeItem("TV", 5.0f),
        DeviceTypeItem("Computer", 1.6f),
        DeviceTypeItem("Tablet", 1.6f)
    )

    val geographyList = listOf(
        SubtitleItem("India", 88.4f)
    )

    val subtitlesList = listOf(
        SubtitleItem("No subtitles/CC", 97.7f),
        SubtitleItem("Hindi", 2.0f),
        SubtitleItem("English", 0.1f)
    )

    // Trends Tab
    val topSearches = listOf(
        ExploreTopic("bengali bangladeshi short film", 4),
        ExploreTopic("bengali reels", 3)
    )

    val trendRecentVideos = listOf(
        TrendVideo(
            title = "বিজয়া দশমী ❤️ | #bonggos #ytshorts #shortvideo #shortsfeed...",
            channelName = "Bong Gos",
            viewsText = "8.2 lakh views",
            timeAgo = "17 hours ago",
            duration = "1:20"
        ),
        TrendVideo(
            title = "💔 বাংলা স্যাড স্ট্যাটাস | Shayari | Heart Touching Sad Status",
            channelName = "MR TOHID 44",
            viewsText = "4.1 lakh views",
            timeAgo = "1 day ago",
            duration = "0:59"
        )
    )

    private var realtimeChannel: SupabaseRealtimeChannel? = null
    private var cachePrefs: SharedPreferences? = null

    var lastSyncTime by mutableStateOf<Long?>(null)

    val syncStatusMessage: String
        get() = when {
            isConnectedToSupabase && lastSyncError == null -> "Connected to Supabase (Live)"
            lastSyncError != null -> "Sync error: $lastSyncError"
            SupabaseClient.anonKey.isBlank() -> "Supabase Anon Key is not set. Open Settings to connect."
            else -> "Connecting to Supabase..."
        }

    /**
     * Initializes repository with context to load cached data from device storage.
     * Prevents data from resetting to hardcoded defaults across app restarts.
     */
    fun init(context: Context) {
        val app = context.applicationContext
        com.example.data.local.LocalAdminStore.init(app)
        val p = app.getSharedPreferences("studio_data_cache", Context.MODE_PRIVATE)
        cachePrefs = p

        if (com.example.data.local.LocalAdminStore.hasCustomLocalData()) {
            Log.d(TAG, "Local admin customized data loaded; active on device.")
            return
        }

        // Restore cached Channel Profile if available
        val cachedName = p.getString("profile_name", null)
        if (!cachedName.isNullOrBlank()) {
            val restoredProfile = ChannelProfile(
                id = p.getString("profile_id", "channel_daniel") ?: "channel_daniel",
                name = cachedName,
                handle = p.getString("profile_handle", "@danieldalen") ?: "@danieldalen",
                subscribersCount = p.getString("profile_subs", "48,237") ?: "48,237",
                email = p.getString("profile_email", "creator@gmail.com") ?: "creator@gmail.com",
                isMonetized = p.getBoolean("profile_monetized", true)
            )
            danielProfile = restoredProfile
            currentProfile = restoredProfile
            Log.d(TAG, "Restored cached channel profile: ${restoredProfile.name}, subs=${restoredProfile.subscribersCount}")
        }

        // Restore cached Dashboard 2x2 Metrics
        val cachedViews = p.getString("metrics_views", null)
        if (!cachedViews.isNullOrBlank()) {
            dashboardViews = cachedViews
            dashboardWatchTime = p.getString("metrics_watch_time", dashboardWatchTime) ?: dashboardWatchTime
            dashboardSubscribers = p.getString("metrics_subscribers", dashboardSubscribers) ?: dashboardSubscribers
            dashboardRevenue = p.getString("metrics_revenue", dashboardRevenue) ?: dashboardRevenue
            dashboardPeriodLabel = p.getString("metrics_period", dashboardPeriodLabel) ?: dashboardPeriodLabel
            Log.d(TAG, "Restored cached metrics: views=$dashboardViews, revenue=$dashboardRevenue")
        }
    }

    fun resetToDefaults() {
        danielProfile = defaultDanielProfile
        currentProfile = defaultDanielProfile
        dashboardViews = "24.8 lakh"
        dashboardWatchTime = "16.8K"
        dashboardSubscribers = "+3.8K"
        dashboardRevenue = "$416.93"
        dashboardPeriodLabel = "Last 28 days"
        isMonetized = true
        monetizationWatchHours = "3,140"
        monetizationRequiredWatchHours = "3,000"
        monetizationShortsViews = "3.2M"
        monetizationRequiredShortsViews = "3.0M"
        monetizationVideoUploads = "5"
    }

    private fun saveProfileToCache(profile: ChannelProfile) {
        cachePrefs?.edit()
            ?.putString("profile_id", profile.id)
            ?.putString("profile_name", profile.name)
            ?.putString("profile_handle", profile.handle)
            ?.putString("profile_subs", profile.subscribersCount)
            ?.putString("profile_email", profile.email)
            ?.putBoolean("profile_monetized", profile.isMonetized)
            ?.apply()
    }

    private fun saveMetricsToCache(views: String, watchTime: String, subs: String, rev: String, period: String) {
        cachePrefs?.edit()
            ?.putString("metrics_views", views)
            ?.putString("metrics_watch_time", watchTime)
            ?.putString("metrics_subscribers", subs)
            ?.putString("metrics_revenue", rev)
            ?.putString("metrics_period", period)
            ?.apply()
    }

    /**
     * Initializes Supabase Realtime WebSocket listeners using the requested pattern:
     *   supabase.channel("app_realtime_changes").on(...).subscribe()
     * Automatically updates reactive Compose states when edits are made in the Web Admin Panel.
     */
    fun startRealtimeListener() {
        if (realtimeChannel != null && realtimeChannel?.status == "SUBSCRIBED") {
            return
        }

        realtimeChannel = supabase.channel("app_realtime_changes")
            .on("channel_profile") { record ->
                handleChannelProfileRealtime(record)
            }
            .on("dashboard_metrics") { record ->
                handleDashboardMetricsRealtime(record)
            }
            .on("videos") { record ->
                handleVideosRealtime(record)
            }
            .on("analytics_overview") { record ->
                handleAnalyticsRealtime(record)
            }
            .subscribe { status ->
                Log.d(TAG, "Supabase Realtime Channel Status: $status")
                if (status == "SUBSCRIBED") {
                    isConnectedToSupabase = true
                    lastSyncError = null
                }
            }
    }

    private fun handleChannelProfileRealtime(record: JSONObject) {
        CoroutineScope(Dispatchers.Main).launch {
            val current = danielProfile
            val updated = current.copy(
                name = record.optString("name", current.name),
                handle = record.optString("handle", current.handle),
                subscribersCount = record.optString("subscribers_count", current.subscribersCount),
                email = record.optString("email", current.email),
                isMonetized = if (record.has("is_monetized")) record.optBoolean("is_monetized") else current.isMonetized
            )
            danielProfile = updated
            currentProfile = updated
            isConnectedToSupabase = true
            saveProfileToCache(updated)
            Log.d(TAG, "Realtime update: channel_profile updated -> ${updated.name}, subs=${updated.subscribersCount}, monetized=${updated.isMonetized}")
        }
    }

    private fun handleDashboardMetricsRealtime(record: JSONObject) {
        CoroutineScope(Dispatchers.Main).launch {
            if (record.has("views")) dashboardViews = record.optString("views", dashboardViews)
            if (record.has("watch_time")) dashboardWatchTime = record.optString("watch_time", dashboardWatchTime)
            if (record.has("subscribers")) dashboardSubscribers = record.optString("subscribers", dashboardSubscribers)
            if (record.has("revenue")) dashboardRevenue = record.optString("revenue", dashboardRevenue)
            if (record.has("period_label")) dashboardPeriodLabel = record.optString("period_label", dashboardPeriodLabel)
            isConnectedToSupabase = true
            saveMetricsToCache(dashboardViews, dashboardWatchTime, dashboardSubscribers, dashboardRevenue, dashboardPeriodLabel)
            Log.d(TAG, "Realtime update: dashboard_metrics updated -> views=$dashboardViews, revenue=$dashboardRevenue")
        }
    }

    private fun handleVideosRealtime(record: JSONObject) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val remoteVideos = SupabaseClient.fetchVideos()
                if (!remoteVideos.isNullOrEmpty()) {
                    val videos = remoteVideos.filter { it.type == ContentType.VIDEO }
                    val shorts = remoteVideos.filter { it.type == ContentType.SHORT }
                    val lives = remoteVideos.filter { it.type == ContentType.LIVE }

                    if (videos.isNotEmpty()) contentVideos = videos
                    if (shorts.isNotEmpty()) contentShorts = shorts
                    if (lives.isNotEmpty()) contentLiveStreams = lives

                    val latest = remoteVideos.find { it.isExpanded } ?: remoteVideos.firstOrNull()
                    if (latest != null) {
                        dashboardLatestVideo = latest
                    }
                    isConnectedToSupabase = true
                    Log.d(TAG, "Realtime update: videos refreshed (${remoteVideos.size} items)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing videos on realtime event", e)
            }
        }
    }

    private fun handleAnalyticsRealtime(record: JSONObject) {
        CoroutineScope(Dispatchers.Main).launch {
            val title = record.optString("title", "")
            val value = record.optString("value", "")
            val subtitle = record.optString("subtitle", "")
            val isPositive = record.optBoolean("is_positive", true)

            if (title.contains("Views", ignoreCase = true)) {
                overviewViewsMetric = overviewViewsMetric.copy(value = value, subtitle = subtitle, isPositive = isPositive)
            } else if (title.contains("Watch", ignoreCase = true)) {
                overviewWatchTimeMetric = overviewWatchTimeMetric.copy(value = value, subtitle = subtitle, isPositive = isPositive)
            } else if (title.contains("Subscribers", ignoreCase = true)) {
                overviewSubscribersMetric = overviewSubscribersMetric.copy(value = value, subtitle = subtitle, isPositive = isPositive)
            } else if (title.contains("Revenue", ignoreCase = true)) {
                overviewEstimatedRevenueMetric = overviewEstimatedRevenueMetric.copy(value = value, subtitle = subtitle, isPositive = isPositive)
            }

            try {
                val remoteAnalytics = SupabaseClient.fetchAnalyticsOverview()
                if (!remoteAnalytics.isNullOrEmpty()) {
                    remoteAnalytics.find { it.title.equals("Views", ignoreCase = true) }?.let { overviewViewsMetric = it }
                    remoteAnalytics.find { it.title.contains("Watch", ignoreCase = true) }?.let { overviewWatchTimeMetric = it }
                    remoteAnalytics.find { it.title.equals("Subscribers", ignoreCase = true) }?.let { overviewSubscribersMetric = it }
                    remoteAnalytics.find { it.title.contains("Revenue", ignoreCase = true) }?.let { overviewEstimatedRevenueMetric = it }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing analytics on realtime event", e)
            }
            isConnectedToSupabase = true
            Log.d(TAG, "Realtime update: analytics updated -> $title: $value")
        }
    }

    /**
     * Synchronizes live data from Supabase PostgREST API and ensures Realtime listeners are active.
     * If offline, misconfigured, or tables do not exist yet,
     * falls back gracefully without modifying default values.
     */
    suspend fun syncWithSupabase() {
        // Ensure Realtime WebSocket listeners are started
        startRealtimeListener()

        if (isSyncing) return
        if (com.example.data.local.LocalAdminStore.hasCustomLocalData()) {
            Log.d(TAG, "Local custom admin data is active on device; preserving local overrides.")
            return
        }
        isSyncing = true
        lastSyncError = null

        try {
            // 1. Channel Profile
            val profile = SupabaseClient.fetchChannelProfile()
            if (profile != null) {
                danielProfile = profile
                currentProfile = profile
                isConnectedToSupabase = true
                saveProfileToCache(profile)
                Log.d(TAG, "Synced channel profile: ${profile.name}, subs: ${profile.subscribersCount}")
            }

            // 2. Dashboard 2x2 Metrics
            val metrics = SupabaseClient.fetchDashboardMetrics()
            if (metrics != null) {
                dashboardViews = metrics.views
                dashboardWatchTime = metrics.watchTime
                dashboardSubscribers = metrics.subscribers
                dashboardRevenue = metrics.revenue
                dashboardPeriodLabel = metrics.periodLabel
                isConnectedToSupabase = true
                saveMetricsToCache(dashboardViews, dashboardWatchTime, dashboardSubscribers, dashboardRevenue, dashboardPeriodLabel)
                Log.d(TAG, "Synced dashboard metrics: views=$dashboardViews, revenue=$dashboardRevenue")
            }

            // 3. Videos & Content
            val remoteVideos = SupabaseClient.fetchVideos()
            if (!remoteVideos.isNullOrEmpty()) {
                val videos = remoteVideos.filter { it.type == ContentType.VIDEO }
                val shorts = remoteVideos.filter { it.type == ContentType.SHORT }
                val lives = remoteVideos.filter { it.type == ContentType.LIVE }

                if (videos.isNotEmpty()) contentVideos = videos
                if (shorts.isNotEmpty()) contentShorts = shorts
                if (lives.isNotEmpty()) contentLiveStreams = lives

                // Update latest video if flagged
                val latest = remoteVideos.find { it.isExpanded } ?: remoteVideos.firstOrNull()
                if (latest != null) {
                    dashboardLatestVideo = latest
                }
                isConnectedToSupabase = true
                Log.d(TAG, "Synced ${remoteVideos.size} videos from Supabase")
            }

            // 4. Analytics Overview
            val remoteAnalytics = SupabaseClient.fetchAnalyticsOverview()
            if (!remoteAnalytics.isNullOrEmpty()) {
                remoteAnalytics.find { it.title.equals("Views", ignoreCase = true) }?.let {
                    overviewViewsMetric = it
                }
                remoteAnalytics.find { it.title.contains("Watch", ignoreCase = true) }?.let {
                    overviewWatchTimeMetric = it
                }
                remoteAnalytics.find { it.title.equals("Subscribers", ignoreCase = true) }?.let {
                    overviewSubscribersMetric = it
                }
                remoteAnalytics.find { it.title.contains("Revenue", ignoreCase = true) }?.let {
                    overviewEstimatedRevenueMetric = it
                }
                isConnectedToSupabase = true
            }

        } catch (e: Exception) {
            Log.e(TAG, "Supabase synchronization error", e)
            lastSyncError = e.message
        } finally {
            isSyncing = false
        }
    }
}

package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.StudioRepository
import com.example.data.local.LocalAdminStore
import com.example.model.CommentItem
import com.example.model.ContentType
import com.example.model.VideoItem
import com.example.ui.components.editableDoubleTap
import com.example.ui.theme.StudioTheme
import java.text.NumberFormat

enum class VideoViewMode {
    PERFORMANCE,
    ANALYTICS
}

enum class VideoAnalyticsSubTab(val label: String) {
    OVERVIEW("Overview"),
    REACH("Reach"),
    ENGAGEMENT("Engagement"),
    AUDIENCE("Audience"),
    REVENUE("Revenue")
}

/**
 * Robust dynamic analytical model tailored to any specific video item.
 */
data class VideoAnalyticsData(
    val video: VideoItem,
    val headlineText: String,
    val formattedViews: String,
    val watchTimeHoursText: String,
    val subscribersText: String,
    val avgRetentionDuration: String,
    val avgRetentionPercent: String,
    val realTimeViews: Long,
    val impressions: Long,
    val impressionsFormatted: String,
    val ctrText: String,
    val uniqueViewers: Long,
    val hypePoints: Int,
    val hypes: Int,
    val likesPercentText: String,
    val channelAverageLikesPercent: String,
    val channelAverageFraction: Float,
    val deviceMobilePercent: String,
    val deviceComputerPercent: String,
    val deviceMobileFraction: Float,
    val deviceComputerFraction: Float,
    val recentComments: List<CommentItem>,
    val publishedDateFormatted: String,
    val visibility: String,
    val notices: String,
    val estimatedRevenueUsd: String,
    val estimatedRevenueInr: String,
    val rpmUsd: String,
    val rpmInr: String,
    val cpmUsd: String,
    val cpmInr: String,
    val watchPageAdsPercent: String,
    val premiumPercent: String,
    val watchPageAdsFraction: Float,
    val premiumFraction: Float,
    val skippableAdsPercent: String,
    val bumperAdsPercent: String,
    val nonSkippableAdsPercent: String,
    val displayAdsPercent: String,
    val youtubePremiumAdPercent: String,
    val skippableAdsFraction: Float,
    val bumperAdsFraction: Float,
    val nonSkippableAdsFraction: Float,
    val displayAdsFraction: Float,
    val youtubePremiumAdFraction: Float
) {
    companion object {
        fun fromVideo(v: VideoItem): VideoAnalyticsData {
            val isLowView = v.views <= 25 || v.title.contains("Pizza Chains", ignoreCase = true)
            val viewsFmt = NumberFormat.getIntegerInstance().format(v.views)

            return if (isLowView) {
                VideoAnalyticsData(
                    video = v,
                    headlineText = "This video has had ${v.views} views since it was published",
                    formattedViews = viewsFmt,
                    watchTimeHoursText = "0.0",
                    subscribersText = "0",
                    avgRetentionDuration = "0:09",
                    avgRetentionPercent = "1.8%",
                    realTimeViews = 0,
                    impressions = 142,
                    impressionsFormatted = "142",
                    ctrText = "7.1%",
                    uniqueViewers = 5,
                    hypePoints = 0,
                    hypes = 0,
                    likesPercentText = if (v.likes > 0) "100.0%" else "N/A",
                    channelAverageLikesPercent = "79.6%",
                    channelAverageFraction = 0.796f,
                    deviceMobilePercent = "68.3%",
                    deviceComputerPercent = "31.7%",
                    deviceMobileFraction = 0.683f,
                    deviceComputerFraction = 0.317f,
                    recentComments = emptyList(),
                    publishedDateFormatted = "21 October 2026 • Published",
                    visibility = "Public",
                    notices = "—",
                    estimatedRevenueUsd = "$0.01",
                    estimatedRevenueInr = "₹0.83",
                    rpmUsd = "$0.40",
                    rpmInr = "₹33.20",
                    cpmUsd = "$1.15",
                    cpmInr = "₹95.45",
                    watchPageAdsPercent = "97.5%",
                    premiumPercent = "2.5%",
                    watchPageAdsFraction = 0.975f,
                    premiumFraction = 0.025f,
                    skippableAdsPercent = "62.4%",
                    bumperAdsPercent = "23.1%",
                    nonSkippableAdsPercent = "10.2%",
                    displayAdsPercent = "4.3%",
                    youtubePremiumAdPercent = "2.5%",
                    skippableAdsFraction = 0.624f,
                    bumperAdsFraction = 0.231f,
                    nonSkippableAdsFraction = 0.102f,
                    displayAdsFraction = 0.043f,
                    youtubePremiumAdFraction = 0.025f
                )
            } else {
                val watchHrs = String.format("%.1f", (v.views * 0.045).coerceAtLeast(0.1))
                val subsGain = "+${maxOf(1, (v.views * 0.007).toInt())}"
                val durationRetention = if (v.type == ContentType.SHORT) "0:42" else "4:15"
                val percentRetention = if (v.type == ContentType.SHORT) "78.2%" else "42.1%"
                val imps = v.views * 10
                val revUsdNum = (v.views * 0.00185).coerceAtLeast(0.05)
                val revInrNum = revUsdNum * 83.2
                val rpmUsdNum = (revUsdNum / maxOf(1L, v.views)) * 1000.0
                val rpmInrNum = rpmUsdNum * 83.2
                val cpmUsdNum = rpmUsdNum * 1.95
                val cpmInrNum = cpmUsdNum * 83.2

                VideoAnalyticsData(
                    video = v,
                    headlineText = "This video has had $viewsFmt views since it was published",
                    formattedViews = viewsFmt,
                    watchTimeHoursText = watchHrs,
                    subscribersText = subsGain,
                    avgRetentionDuration = durationRetention,
                    avgRetentionPercent = percentRetention,
                    realTimeViews = maxOf(0L, (v.views * 0.015).toLong()),
                    impressions = imps,
                    impressionsFormatted = NumberFormat.getIntegerInstance().format(imps),
                    ctrText = "6.4%",
                    uniqueViewers = maxOf(5L, (v.views * 0.42).toLong()),
                    hypePoints = if (v.views > 1000) 12 else 0,
                    hypes = if (v.views > 1000) 8 else 0,
                    likesPercentText = if (v.likes > 0) "98.7%" else "N/A",
                    channelAverageLikesPercent = "79.6%",
                    channelAverageFraction = 0.796f,
                    deviceMobilePercent = "74.2%",
                    deviceComputerPercent = "25.8%",
                    deviceMobileFraction = 0.742f,
                    deviceComputerFraction = 0.258f,
                    recentComments = if (v.comments > 0) {
                        listOf(
                            CommentItem(
                                id = "rc_1",
                                authorHandle = "@viewer_fan",
                                timeAgo = "1 day ago",
                                content = "Great video! Really enjoyed this breakdown 🙌",
                                videoTitle = v.title,
                                likesCount = 2
                            )
                        )
                    } else emptyList(),
                    publishedDateFormatted = "${v.publishedTimeAgo} • Published",
                    visibility = "Public",
                    notices = "—",
                    estimatedRevenueUsd = String.format("$%.2f", revUsdNum),
                    estimatedRevenueInr = String.format("₹%,.2f", revInrNum),
                    rpmUsd = String.format("$%.2f", rpmUsdNum),
                    rpmInr = String.format("₹%.2f", rpmInrNum),
                    cpmUsd = String.format("$%.2f", cpmUsdNum),
                    cpmInr = String.format("₹%.2f", cpmInrNum),
                    watchPageAdsPercent = "96.8%",
                    premiumPercent = "3.2%",
                    watchPageAdsFraction = 0.968f,
                    premiumFraction = 0.032f,
                    skippableAdsPercent = "58.4%",
                    bumperAdsPercent = "21.6%",
                    nonSkippableAdsPercent = "14.1%",
                    displayAdsPercent = "3.7%",
                    youtubePremiumAdPercent = "2.2%",
                    skippableAdsFraction = 0.584f,
                    bumperAdsFraction = 0.216f,
                    nonSkippableAdsFraction = 0.141f,
                    displayAdsFraction = 0.037f,
                    youtubePremiumAdFraction = 0.022f
                )
            }
        }
    }

    fun withOverrides(videoId: String): VideoAnalyticsData {
        val headline = LocalAdminStore.getVideoMetricOverride(videoId, "headlineText", headlineText)
        val viewsStr = LocalAdminStore.getVideoMetricOverride(videoId, "formattedViews", formattedViews)
        val watchTime = LocalAdminStore.getVideoMetricOverride(videoId, "watchTimeHoursText", watchTimeHoursText)
        val subs = LocalAdminStore.getVideoMetricOverride(videoId, "subscribersText", subscribersText)
        val avgDur = LocalAdminStore.getVideoMetricOverride(videoId, "avgRetentionDuration", avgRetentionDuration)
        val avgPct = LocalAdminStore.getVideoMetricOverride(videoId, "avgRetentionPercent", avgRetentionPercent)
        val rtViews = LocalAdminStore.getVideoMetricOverride(videoId, "realTimeViews", realTimeViews.toString()).toLongOrNull() ?: realTimeViews
        val imps = LocalAdminStore.getVideoMetricOverride(videoId, "impressionsFormatted", impressionsFormatted)
        val ctr = LocalAdminStore.getVideoMetricOverride(videoId, "ctrText", ctrText)
        val uv = LocalAdminStore.getVideoMetricOverride(videoId, "uniqueViewers", uniqueViewers.toString()).toLongOrNull() ?: uniqueViewers
        val hp = LocalAdminStore.getVideoMetricOverride(videoId, "hypePoints", hypePoints.toString()).toIntOrNull() ?: hypePoints
        val hy = LocalAdminStore.getVideoMetricOverride(videoId, "hypes", hypes.toString()).toIntOrNull() ?: hypes
        val lp = LocalAdminStore.getVideoMetricOverride(videoId, "likesPercentText", likesPercentText)
        val calp = LocalAdminStore.getVideoMetricOverride(videoId, "channelAverageLikesPercent", channelAverageLikesPercent)
        val dmp = LocalAdminStore.getVideoMetricOverride(videoId, "deviceMobilePercent", deviceMobilePercent)
        val dcp = LocalAdminStore.getVideoMetricOverride(videoId, "deviceComputerPercent", deviceComputerPercent)
        val pubDate = LocalAdminStore.getVideoMetricOverride(videoId, "publishedDateFormatted", publishedDateFormatted)
        val vis = LocalAdminStore.getVideoMetricOverride(videoId, "visibility", visibility)
        val notc = LocalAdminStore.getVideoMetricOverride(videoId, "notices", notices)
        val revUsd = LocalAdminStore.getVideoMetricOverride(videoId, "estimatedRevenueUsd", estimatedRevenueUsd).removePrefix("US")
        val revInr = LocalAdminStore.getVideoMetricOverride(videoId, "estimatedRevenueInr", estimatedRevenueInr)
        val rUsd = LocalAdminStore.getVideoMetricOverride(videoId, "rpmUsd", rpmUsd).removePrefix("US")
        val rInr = LocalAdminStore.getVideoMetricOverride(videoId, "rpmInr", rpmInr)
        val cUsd = LocalAdminStore.getVideoMetricOverride(videoId, "cpmUsd", cpmUsd).removePrefix("US")
        val cInr = LocalAdminStore.getVideoMetricOverride(videoId, "cpmInr", cpmInr)
        val wpa = LocalAdminStore.getVideoMetricOverride(videoId, "watchPageAdsPercent", watchPageAdsPercent)
        val prem = LocalAdminStore.getVideoMetricOverride(videoId, "premiumPercent", premiumPercent)
        val skip = LocalAdminStore.getVideoMetricOverride(videoId, "skippableAdsPercent", skippableAdsPercent)
        val bump = LocalAdminStore.getVideoMetricOverride(videoId, "bumperAdsPercent", bumperAdsPercent)
        val nonSkip = LocalAdminStore.getVideoMetricOverride(videoId, "nonSkippableAdsPercent", nonSkippableAdsPercent)
        val disp = LocalAdminStore.getVideoMetricOverride(videoId, "displayAdsPercent", displayAdsPercent)
        val ytp = LocalAdminStore.getVideoMetricOverride(videoId, "youtubePremiumAdPercent", youtubePremiumAdPercent)

        return this.copy(
            headlineText = headline,
            formattedViews = viewsStr,
            watchTimeHoursText = watchTime,
            subscribersText = subs,
            avgRetentionDuration = avgDur,
            avgRetentionPercent = avgPct,
            realTimeViews = rtViews,
            impressionsFormatted = imps,
            ctrText = ctr,
            uniqueViewers = uv,
            hypePoints = hp,
            hypes = hy,
            likesPercentText = lp,
            channelAverageLikesPercent = calp,
            deviceMobilePercent = dmp,
            deviceComputerPercent = dcp,
            publishedDateFormatted = pubDate,
            visibility = vis,
            notices = notc,
            estimatedRevenueUsd = revUsd,
            estimatedRevenueInr = revInr,
            rpmUsd = rUsd,
            rpmInr = rInr,
            cpmUsd = cUsd,
            cpmInr = cInr,
            watchPageAdsPercent = wpa,
            premiumPercent = prem,
            skippableAdsPercent = skip,
            bumperAdsPercent = bump,
            nonSkippableAdsPercent = nonSkip,
            displayAdsPercent = disp,
            youtubePremiumAdPercent = ytp
        )
    }
}

@Composable
fun IndividualVideoAnalyticsScreen(
    video: VideoItem,
    onBackClick: () -> Unit,
    initialViewMode: VideoViewMode = VideoViewMode.ANALYTICS,
    initialTab: VideoAnalyticsSubTab = VideoAnalyticsSubTab.OVERVIEW
) {
    var currentViewMode by rememberSaveable { mutableStateOf(initialViewMode) }
    var selectedAnalyticsTab by rememberSaveable { mutableStateOf(initialTab) }

    val metricsVersion = LocalAdminStore.videoMetricsVersion
    val analytics = remember(video.id, video.views, video.likes, metricsVersion) {
        VideoAnalyticsData.fromVideo(video).withOverrides(video.id)
    }

    // Seamless back handling: from performance view back to analytics, or back to previous screen
    BackHandler {
        if (currentViewMode == VideoViewMode.PERFORMANCE) {
            currentViewMode = VideoViewMode.ANALYTICS
        } else {
            onBackClick()
        }
    }

    if (currentViewMode == VideoViewMode.PERFORMANCE) {
        // =========================================================================
        // LEVEL 1: Video Performance Landing Page (Matches reference video at 0:04)
        // =========================================================================
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F0F0F)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Video",
                                tint = Color(0xFF0F0F0F)
                            )
                        }
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color(0xFF0F0F0F)
                            )
                        }
                        IconButton(onClick = { currentViewMode = VideoViewMode.ANALYTICS }) {
                            Icon(
                                imageVector = Icons.Default.Insights,
                                contentDescription = "Video Analytics",
                                tint = Color(0xFF0F0F0F)
                            )
                        }
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.SmartDisplay,
                                contentDescription = "Open in YouTube",
                                tint = Color(0xFF0F0F0F)
                            )
                        }
                    }
                }
            },
            containerColor = Color.White
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 48.dp)
            ) {
                // Top Video Thumbnail Hero Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(if (video.type == ContentType.SHORT) 9f / 16f else 16f / 9f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF212121)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!video.thumbnailUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = video.thumbnailUri,
                                    contentDescription = video.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    if (video.thumbnailTitle.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = video.thumbnailTitle,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Duration Badge in bottom-right corner
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xCC000000))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = video.duration.ifBlank { "8:53" },
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.editableDoubleTap(
                                        currentValue = video.duration,
                                        label = "Video Duration",
                                        onSave = { LocalAdminStore.updateVideoDuration(video.id, it) }
                                    )
                                )
                            }
                        }
                    }
                }

                // Video Title & Published Info Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = video.title.ifBlank { "Untitled Video" },
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F0F0F),
                                lineHeight = 22.sp,
                                modifier = Modifier.editableDoubleTap(
                                    currentValue = video.title,
                                    label = "Video Title",
                                    onSave = { LocalAdminStore.updateVideoTitle(video.id, it) }
                                )
                            )
                            Text(
                                text = analytics.publishedDateFormatted,
                                fontSize = 13.sp,
                                color = Color(0xFF606060),
                                modifier = Modifier.editableDoubleTap(
                                    currentValue = analytics.publishedDateFormatted,
                                    label = "Published Info",
                                    onSave = { LocalAdminStore.setVideoMetricOverride(video.id, "publishedDateFormatted", it) }
                                )
                            )
                        }
                    }
                }

                // Visibility and Notices Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            // Visibility Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Public,
                                    contentDescription = null,
                                    tint = Color(0xFF606060),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Visibility",
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F0F0F)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = analytics.visibility,
                                    fontSize = 14.sp,
                                    color = Color(0xFF606060),
                                    modifier = Modifier.editableDoubleTap(
                                        currentValue = analytics.visibility,
                                        label = "Visibility",
                                        onSave = { LocalAdminStore.setVideoMetricOverride(video.id, "visibility", it) }
                                    )
                                )
                            }

                            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))

                            // Notices Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Notices",
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F0F0F),
                                    modifier = Modifier.padding(start = 32.dp)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = analytics.notices,
                                    fontSize = 14.sp,
                                    color = Color(0xFF606060),
                                    modifier = Modifier.editableDoubleTap(
                                        currentValue = analytics.notices,
                                        label = "Notices",
                                        onSave = { LocalAdminStore.setVideoMetricOverride(video.id, "notices", it) }
                                    )
                                )
                            }
                        }
                    }
                }

                // Analytics Preview Card (Navigates to Detailed Video Analytics on tap)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currentViewMode = VideoViewMode.ANALYTICS }
                            .testTag("video_analytics_overview_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Analytics",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF0F0F0F)
                                    )
                                    Text(
                                        text = "Since published",
                                        fontSize = 12.sp,
                                        color = Color(0xFF606060)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "View Video Analytics",
                                    tint = Color(0xFF606060)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Separate elevated metric sub-cards
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                                    border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Views", fontSize = 12.sp, color = Color(0xFF606060))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = analytics.formattedViews,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F0F0F),
                                            modifier = Modifier.editableDoubleTap(
                                                currentValue = video.views.toString(),
                                                label = "Video Views",
                                                onSave = {
                                                    it.toLongOrNull()?.let { v ->
                                                        LocalAdminStore.updateVideoViews(video.id, v)
                                                    }
                                                }
                                            )
                                        )
                                    }
                                }

                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                                    border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Watch time", fontSize = 12.sp, color = Color(0xFF606060))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = analytics.watchTimeHoursText,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F0F0F),
                                            modifier = Modifier.editableDoubleTap(
                                                currentValue = analytics.watchTimeHoursText,
                                                label = "Watch Time (Hours)",
                                                onSave = { LocalAdminStore.setVideoMetricOverride(video.id, "watchTimeHoursText", it) }
                                            )
                                        )
                                    }
                                }

                                if (StudioRepository.isMonetized) {
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                                        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("Est. revenue", fontSize = 12.sp, color = Color(0xFF606060))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = analytics.estimatedRevenueUsd,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2E7D32),
                                                modifier = Modifier.editableDoubleTap(
                                                    currentValue = analytics.estimatedRevenueUsd,
                                                    label = "Estimated Revenue",
                                                    onSave = {
                                                        LocalAdminStore.setVideoMetricOverride(video.id, "estimatedRevenueUsd", it)
                                                    }
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Go to video analytics",
                                    color = Color(0xFF065FD4),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color(0xFF065FD4),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Dedicated Video Detail Revenue Card (Monetized Channels)
                if (StudioRepository.isMonetized) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedAnalyticsTab = VideoAnalyticsSubTab.REVENUE
                                    currentViewMode = VideoViewMode.ANALYTICS
                                }
                                .testTag("video_detail_revenue_card"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE8F5E9)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AttachMoney,
                                                contentDescription = null,
                                                tint = Color(0xFF2E7D32),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Estimated revenue",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = Color(0xFF0F0F0F)
                                            )
                                            Text(
                                                text = "Since published",
                                                fontSize = 12.sp,
                                                color = Color(0xFF606060)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text(
                                            text = analytics.estimatedRevenueUsd,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32),
                                            modifier = Modifier.editableDoubleTap(
                                                currentValue = analytics.estimatedRevenueUsd,
                                                label = "Estimated Revenue",
                                                onSave = {
                                                    LocalAdminStore.setVideoMetricOverride(video.id, "estimatedRevenueUsd", it)
                                                }
                                            )
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFE8F5E9)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF2E7D32))
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = "Monetized",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF2E7D32)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
                                Spacer(modifier = Modifier.height(10.dp))

                                // Sub-metrics: RPM & Playback-based CPM
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                                        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("RPM", fontSize = 11.sp, color = Color(0xFF606060))
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = analytics.rpmUsd,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F0F0F)
                                            )
                                        }
                                    }

                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                                        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("Playback-based CPM", fontSize = 11.sp, color = Color(0xFF606060))
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = analytics.cpmUsd,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F0F0F)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "See revenue analytics",
                                        color = Color(0xFF065FD4),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = Color(0xFF065FD4),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Comments Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Comments",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF0F0F0F)
                                )
                                TextButton(
                                    onClick = {},
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("View all", fontSize = 13.sp, color = Color(0xFF0F0F0F), fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (analytics.recentComments.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    analytics.recentComments.forEach { c ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                                                .padding(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFE0E0E0)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF757575), modifier = Modifier.size(18.dp))
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(c.authorHandle, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F0F0F))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(c.timeAgo, fontSize = 11.sp, color = Color(0xFF909090))
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(c.content, fontSize = 12.sp, color = Color(0xFF0F0F0F))
                                            }
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFAFAFA),
                                    border = BorderStroke(0.5.dp, Color(0xFFEEEEEE))
                                ) {
                                    Text(
                                        text = "No unresponded comments",
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                                        fontSize = 13.sp,
                                        color = Color(0xFF606060)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // =========================================================================
        // LEVEL 2: Detailed Video Analytics Screen (Matches video at 0:07 - 0:18)
        // =========================================================================
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .statusBarsPadding()
                ) {
                    // Header Bar with back arrow, mini thumbnail, and truncated title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF0F0F0F)
                            )
                        }

                        // Mini video thumbnail
                        Box(
                            modifier = Modifier
                                .size(width = 36.dp, height = 24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF212121)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!video.thumbnailUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = video.thumbnailUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = video.title.ifBlank { "Video Analytics" },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F0F0F),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .editableDoubleTap(
                                    currentValue = video.title,
                                    label = "Video Title",
                                    onSave = { LocalAdminStore.updateVideoTitle(video.id, it) }
                                )
                        )

                        IconButton(onClick = { currentViewMode = VideoViewMode.PERFORMANCE }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Video Details",
                                tint = Color(0xFF606060),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Sub-Tabs: Overview, Reach, Engagement, Audience, Revenue (Always all 5 tabs visible)
                    val visibleTabs = remember {
                        listOf(
                            VideoAnalyticsSubTab.OVERVIEW,
                            VideoAnalyticsSubTab.REACH,
                            VideoAnalyticsSubTab.ENGAGEMENT,
                            VideoAnalyticsSubTab.AUDIENCE,
                            VideoAnalyticsSubTab.REVENUE
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        visibleTabs.forEach { tab ->
                            val isSelected = tab == selectedAnalyticsTab
                            Column(
                                modifier = Modifier
                                    .clickable { selectedAnalyticsTab = tab }
                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                                    .testTag("analytics_subtab_${tab.name.lowercase()}"),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = tab.label,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF0F0F0F) else Color(0xFF606060)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .height(2.5.dp)
                                        .width(36.dp)
                                        .background(if (isSelected) Color(0xFF0F0F0F) else Color.Transparent, RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
                }
            },
            containerColor = Color.White
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp)
            ) {
                when (selectedAnalyticsTab) {
                    VideoAnalyticsSubTab.OVERVIEW -> {
                        // 1. Overview Headline Text (Matches video at 0:07)
                        item {
                            Text(
                                text = analytics.headlineText,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F0F0F),
                                lineHeight = 22.sp,
                                modifier = Modifier.editableDoubleTap(
                                    currentValue = analytics.headlineText,
                                    label = "Overview Headline",
                                    onSave = { LocalAdminStore.setVideoMetricOverride(video.id, "headlineText", it) }
                                )
                            )
                        }

                        // 2. Metric Swipeable Pager: Individual Distinct White Cards (Views, Watch Time, Subscribers)
                        item {
                            val pagerState = rememberPagerState(pageCount = { if (StudioRepository.isMonetized) 4 else 3 })

                            Column(modifier = Modifier.fillMaxWidth()) {
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.fillMaxWidth(),
                                    pageSpacing = 10.dp
                                ) { page ->
                                    when (page) {
                                        0 -> {
                                            // Card 1: Views Stepped Chart Card
                                             Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    Text("Views", fontSize = 13.sp, color = Color(0xFF606060))
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = analytics.formattedViews,
                                                        fontSize = 24.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF0F0F0F),
                                                        modifier = Modifier.editableDoubleTap(
                                                            currentValue = analytics.formattedViews,
                                                            label = "Views",
                                                            onSave = {
                                                                LocalAdminStore.setVideoMetricOverride(video.id, "formattedViews", it)
                                                                it.replace(",", "").toLongOrNull()?.let { v ->
                                                                    LocalAdminStore.updateVideoViews(video.id, v)
                                                                }
                                                            }
                                                        )
                                                    )
                                                    Spacer(modifier = Modifier.height(14.dp))

                                                    // Stepped Line Chart with horizontal grid lines
                                                    Row(modifier = Modifier.fillMaxWidth()) {
                                                        Column(
                                                            modifier = Modifier.height(100.dp),
                                                            verticalArrangement = Arrangement.SpaceBetween,
                                                            horizontalAlignment = Alignment.End
                                                        ) {
                                                            val maxV = video.views.coerceAtLeast(12)
                                                            Text("$maxV", fontSize = 10.sp, color = Color(0xFF909090))
                                                            Text("${(maxV * 2) / 3}", fontSize = 10.sp, color = Color(0xFF909090))
                                                            Text("${maxV / 3}", fontSize = 10.sp, color = Color(0xFF909090))
                                                            Text("0", fontSize = 10.sp, color = Color(0xFF909090))
                                                        }

                                                        Spacer(modifier = Modifier.width(8.dp))

                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Canvas(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .height(100.dp)
                                                            ) {
                                                                val w = size.width
                                                                val h = size.height

                                                                // Grid lines
                                                                drawLine(Color(0xFFEEEEEE), Offset(0f, 0f), Offset(w, 0f), strokeWidth = 1f)
                                                                drawLine(Color(0xFFEEEEEE), Offset(0f, h * 0.33f), Offset(w, h * 0.33f), strokeWidth = 1f)
                                                                drawLine(Color(0xFFEEEEEE), Offset(0f, h * 0.66f), Offset(w, h * 0.66f), strokeWidth = 1f)
                                                                drawLine(Color(0xFFEEEEEE), Offset(0f, h), Offset(w, h), strokeWidth = 1f)

                                                                // Stepped curve in cyan/blue (matches video at 0:07)
                                                                val stepPath = Path().apply {
                                                                    moveTo(0f, h * 0.85f)
                                                                    lineTo(w * 0.15f, h * 0.85f)
                                                                    lineTo(w * 0.15f, h * 0.60f)
                                                                    lineTo(w * 0.35f, h * 0.60f)
                                                                    lineTo(w * 0.35f, h * 0.40f)
                                                                    lineTo(w * 0.60f, h * 0.40f)
                                                                    lineTo(w * 0.60f, h * 0.15f)
                                                                    lineTo(w, h * 0.15f)
                                                                }

                                                                drawPath(
                                                                    path = stepPath,
                                                                    color = Color(0xFF00ACC1),
                                                                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                                                                )
                                                            }

                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.End
                                                            ) {
                                                                Text("5 days", fontSize = 10.sp, color = Color(0xFF909090))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        1 -> {
                                            // Card 2: Watch time Chart Card
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    Text("Watch time (hours)", fontSize = 13.sp, color = Color(0xFF606060))
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = analytics.watchTimeHoursText,
                                                        fontSize = 24.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF0F0F0F),
                                                        modifier = Modifier.editableDoubleTap(
                                                            currentValue = analytics.watchTimeHoursText,
                                                            label = "Watch Time (Hours)",
                                                            onSave = { LocalAdminStore.setVideoMetricOverride(video.id, "watchTimeHoursText", it) }
                                                        )
                                                    )
                                                    Spacer(modifier = Modifier.height(14.dp))

                                                    Row(modifier = Modifier.fillMaxWidth()) {
                                                        Column(
                                                            modifier = Modifier.height(100.dp),
                                                            verticalArrangement = Arrangement.SpaceBetween,
                                                            horizontalAlignment = Alignment.End
                                                        ) {
                                                            Text("0.0", fontSize = 10.sp, color = Color(0xFF909090))
                                                            Text("0.0", fontSize = 10.sp, color = Color(0xFF909090))
                                                            Text("0.0", fontSize = 10.sp, color = Color(0xFF909090))
                                                            Text("0.0", fontSize = 10.sp, color = Color(0xFF909090))
                                                        }

                                                        Spacer(modifier = Modifier.width(8.dp))

                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Canvas(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .height(100.dp)
                                                            ) {
                                                                val w = size.width
                                                                val h = size.height

                                                                drawLine(Color(0xFFEEEEEE), Offset(0f, 0f), Offset(w, 0f), strokeWidth = 1f)
                                                                drawLine(Color(0xFFEEEEEE), Offset(0f, h * 0.33f), Offset(w, h * 0.33f), strokeWidth = 1f)
                                                                drawLine(Color(0xFFEEEEEE), Offset(0f, h * 0.66f), Offset(w, h * 0.66f), strokeWidth = 1f)
                                                                drawLine(Color(0xFFEEEEEE), Offset(0f, h), Offset(w, h), strokeWidth = 1f)

                                                                drawLine(
                                                                    color = Color(0xFF00ACC1),
                                                                    start = Offset(0f, h * 0.85f),
                                                                    end = Offset(w, h * 0.85f),
                                                                    strokeWidth = 2.dp.toPx(),
                                                                    cap = StrokeCap.Round
                                                                )
                                                            }

                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.End
                                                            ) {
                                                                Text("5 days", fontSize = 10.sp, color = Color(0xFF909090))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        2 -> {
                                            // Card 3: Subscribers Chart Card
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    Text("Subscribers", fontSize = 13.sp, color = Color(0xFF606060))
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = analytics.subscribersText,
                                                        fontSize = 24.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF0F0F0F),
                                                        modifier = Modifier.editableDoubleTap(
                                                            currentValue = analytics.subscribersText,
                                                            label = "Subscribers Gain",
                                                            onSave = { LocalAdminStore.setVideoMetricOverride(video.id, "subscribersText", it) }
                                                        )
                                                    )
                                                    Spacer(modifier = Modifier.height(14.dp))

                                                    Row(modifier = Modifier.fillMaxWidth()) {
                                                        Column(
                                                            modifier = Modifier.height(100.dp),
                                                            verticalArrangement = Arrangement.SpaceBetween,
                                                            horizontalAlignment = Alignment.End
                                                        ) {
                                                            Text("3", fontSize = 10.sp, color = Color(0xFF909090))
                                                            Text("2", fontSize = 10.sp, color = Color(0xFF909090))
                                                            Text("1", fontSize = 10.sp, color = Color(0xFF909090))
                                                            Text("0", fontSize = 10.sp, color = Color(0xFF909090))
                                                        }

                                                        Spacer(modifier = Modifier.width(8.dp))

                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Canvas(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .height(100.dp)
                                                            ) {
                                                                val w = size.width
                                                                val h = size.height

                                                                drawLine(Color(0xFFEEEEEE), Offset(0f, 0f), Offset(w, 0f), strokeWidth = 1f)
                                                                drawLine(Color(0xFFEEEEEE), Offset(0f, h * 0.33f), Offset(w, h * 0.33f), strokeWidth = 1f)
                                                                drawLine(Color(0xFFEEEEEE), Offset(0f, h * 0.66f), Offset(w, h * 0.66f), strokeWidth = 1f)
                                                                drawLine(Color(0xFFEEEEEE), Offset(0f, h), Offset(w, h), strokeWidth = 1f)

                                                                drawLine(
                                                                    color = Color(0xFF00ACC1),
                                                                    start = Offset(0f, h * 0.85f),
                                                                    end = Offset(w, h * 0.85f),
                                                                    strokeWidth = 2.dp.toPx(),
                                                                    cap = StrokeCap.Round
                                                                )
                                                            }

                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.End
                                                            ) {
                                                                Text("5 days", fontSize = 10.sp, color = Color(0xFF909090))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        3 -> {
                                            // Card 4: Estimated Revenue Chart Card (Monetized)
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    Text("Estimated revenue", fontSize = 13.sp, color = Color(0xFF606060))
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = analytics.estimatedRevenueUsd,
                                                        fontSize = 24.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF2E7D32),
                                                        modifier = Modifier.editableDoubleTap(
                                                            currentValue = analytics.estimatedRevenueUsd,
                                                            label = "Estimated Revenue",
                                                            onSave = {
                                                                LocalAdminStore.setVideoMetricOverride(video.id, "estimatedRevenueUsd", it)
                                                            }
                                                        )
                                                    )
                                                    Spacer(modifier = Modifier.height(14.dp))

                                                    Row(modifier = Modifier.fillMaxWidth()) {
                                                        Column(
                                                            modifier = Modifier.height(100.dp),
                                                            verticalArrangement = Arrangement.SpaceBetween,
                                                            horizontalAlignment = Alignment.End
                                                        ) {
                                                            Text("$1.00", fontSize = 10.sp, color = Color(0xFF909090))
                                                            Text("$0.66", fontSize = 10.sp, color = Color(0xFF909090))
                                                            Text("$0.33", fontSize = 10.sp, color = Color(0xFF909090))
                                                            Text("0", fontSize = 10.sp, color = Color(0xFF909090))
                                                        }

                                                        Spacer(modifier = Modifier.width(8.dp))

                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Canvas(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .height(100.dp)
                                                            ) {
                                                                val w = size.width
                                                                val h = size.height

                                                                // Grid lines
                                                                drawLine(Color(0xFFEEEEEE), Offset(0f, 0f), Offset(w, 0f), strokeWidth = 1f)
                                                                drawLine(Color(0xFFEEEEEE), Offset(0f, h * 0.33f), Offset(w, h * 0.33f), strokeWidth = 1f)
                                                                drawLine(Color(0xFFEEEEEE), Offset(0f, h * 0.66f), Offset(w, h * 0.66f), strokeWidth = 1f)
                                                                drawLine(Color(0xFFEEEEEE), Offset(0f, h), Offset(w, h), strokeWidth = 1f)

                                                                // Green revenue trend curve
                                                                val revPath = Path().apply {
                                                                    moveTo(0f, h * 0.90f)
                                                                    cubicTo(w * 0.25f, h * 0.85f, w * 0.5f, h * 0.40f, w * 0.75f, h * 0.25f)
                                                                    lineTo(w, h * 0.15f)
                                                                }

                                                                drawPath(
                                                                    path = revPath,
                                                                    color = Color(0xFF2E7D32),
                                                                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                                                                )
                                                            }

                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.End
                                                            ) {
                                                                Text("Since published", fontSize = 10.sp, color = Color(0xFF909090))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Pager Indicator Dots (adapts to 3 or 4 dots)
                                val pageTotal = if (StudioRepository.isMonetized) 4 else 3
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(pageTotal) { index ->
                                        val isSelected = pagerState.currentPage == index
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color(0xFF0F0F0F) else Color(0xFFD0D0D0))
                                        )
                                        if (index < pageTotal - 1) Spacer(modifier = Modifier.width(6.dp))
                                    }
                                }
                            }
                        }

                        // 3. Audience Retention Card with Retention Curve (Matches video at 0:08)
                        item {
                            AudienceRetentionCard(
                                videoDuration = video.duration,
                                avgDuration = analytics.avgRetentionDuration,
                                avgPercent = analytics.avgRetentionPercent,
                                onSaveDuration = { LocalAdminStore.setVideoMetricOverride(video.id, "avgRetentionDuration", it) },
                                onSavePercent = { LocalAdminStore.setVideoMetricOverride(video.id, "avgRetentionPercent", it) }
                            )
                        }

                        // 4. Real Time Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Real time", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F0F0F))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${analytics.realTimeViews}",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F0F0F),
                                        modifier = Modifier.editableDoubleTap(
                                            currentValue = analytics.realTimeViews.toString(),
                                            label = "Real-time Views (48h)",
                                            onSave = { LocalAdminStore.setVideoMetricOverride(video.id, "realTimeViews", it) }
                                        )
                                    )
                                    Text("Views • 48 hours", fontSize = 12.sp, color = Color(0xFF606060))
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text("Nothing to show for these dates", fontSize = 13.sp, color = Color(0xFF909090))
                                }
                            }
                        }
                    }

                    VideoAnalyticsSubTab.REACH -> {
                        // 4 Separate Elevated Metric Cards in 2x2 Grid
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text("Impressions", fontSize = 12.sp, color = Color(0xFF606060))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = analytics.impressionsFormatted,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F0F0F),
                                                modifier = Modifier.editableDoubleTap(
                                                    currentValue = analytics.impressionsFormatted,
                                                    label = "Impressions",
                                                    onSave = { LocalAdminStore.setVideoMetricOverride(video.id, "impressionsFormatted", it) }
                                                )
                                            )
                                        }
                                    }

                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text("Impression CTR", fontSize = 12.sp, color = Color(0xFF606060))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = analytics.ctrText,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F0F0F),
                                                modifier = Modifier.editableDoubleTap(
                                                    currentValue = analytics.ctrText,
                                                    label = "Impression CTR",
                                                    onSave = { LocalAdminStore.setVideoMetricOverride(video.id, "ctrText", it) }
                                                )
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text("Views", fontSize = 12.sp, color = Color(0xFF606060))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = analytics.formattedViews,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F0F0F),
                                                modifier = Modifier.editableDoubleTap(
                                                    currentValue = analytics.formattedViews,
                                                    label = "Views",
                                                    onSave = { LocalAdminStore.setVideoMetricOverride(video.id, "formattedViews", it) }
                                                )
                                            )
                                        }
                                    }

                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text("Unique viewers", fontSize = 12.sp, color = Color(0xFF606060))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${analytics.uniqueViewers}",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F0F0F),
                                                modifier = Modifier.editableDoubleTap(
                                                    currentValue = analytics.uniqueViewers.toString(),
                                                    label = "Unique Viewers",
                                                    onSave = { LocalAdminStore.setVideoMetricOverride(video.id, "uniqueViewers", it) }
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("How viewers find this video", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F0F0F))
                        }

                        // 4 Separate Distinct White Cards for Traffic Reports (Matches video at 0:17)
                        item { TrafficSourceReportCard(title = "External sites or apps") }
                        item { TrafficSourceReportCard(title = "YouTube search terms") }
                        item { TrafficSourceReportCard(title = "Content suggesting this video") }
                        item { TrafficSourceReportCard(title = "Playlists featuring this video") }
                    }

                    VideoAnalyticsSubTab.ENGAGEMENT -> {
                        // 1. Hype Metric Card (Matches video at 0:16)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Hype", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F0F0F))
                                    Text("First 7 days", fontSize = 12.sp, color = Color(0xFF606060))
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${analytics.hypePoints}",
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F0F0F),
                                                modifier = Modifier.editableDoubleTap(
                                                    currentValue = analytics.hypePoints.toString(),
                                                    label = "Hype Points",
                                                    onSave = { LocalAdminStore.setVideoMetricOverride(video.id, "hypePoints", it) }
                                                )
                                            )
                                            Text("Hype points", fontSize = 12.sp, color = Color(0xFF606060))
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${analytics.hypes}",
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F0F0F),
                                                modifier = Modifier.editableDoubleTap(
                                                    currentValue = analytics.hypes.toString(),
                                                    label = "Hypes",
                                                    onSave = { LocalAdminStore.setVideoMetricOverride(video.id, "hypes", it) }
                                                )
                                            )
                                            Text("Hypes", fontSize = 12.sp, color = Color(0xFF606060))
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Watch Time Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Watch time (hours)", fontSize = 13.sp, color = Color(0xFF606060))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = analytics.watchTimeHoursText,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F0F0F),
                                        modifier = Modifier.editableDoubleTap(
                                            currentValue = analytics.watchTimeHoursText,
                                            label = "Watch Time (Hours)",
                                            onSave = { LocalAdminStore.setVideoMetricOverride(video.id, "watchTimeHoursText", it) }
                                        )
                                    )
                                }
                            }
                        }

                        // 3. Audience Retention Card
                        item {
                            AudienceRetentionCard(
                                videoDuration = video.duration,
                                avgDuration = analytics.avgRetentionDuration,
                                avgPercent = analytics.avgRetentionPercent,
                                onSaveDuration = { LocalAdminStore.setVideoMetricOverride(video.id, "avgRetentionDuration", it) },
                                onSavePercent = { LocalAdminStore.setVideoMetricOverride(video.id, "avgRetentionPercent", it) }
                            )
                        }

                        // 4. Likes vs Dislikes Card (Matches video at 0:16)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Likes (vs dislikes)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F0F0F))
                                    Text("Since published", fontSize = 12.sp, color = Color(0xFF606060))
                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = video.title.ifBlank { "This video" },
                                            fontSize = 13.sp,
                                            color = Color(0xFF0F0F0F),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = analytics.likesPercentText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F0F0F),
                                            modifier = Modifier.editableDoubleTap(
                                                currentValue = analytics.likesPercentText,
                                                label = "Likes %",
                                                onSave = { LocalAdminStore.setVideoMetricOverride(video.id, "likesPercentText", it) }
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Channel average", fontSize = 13.sp, color = Color(0xFF606060))
                                        Text(
                                            text = analytics.channelAverageLikesPercent,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F0F0F),
                                            modifier = Modifier.editableDoubleTap(
                                                currentValue = analytics.channelAverageLikesPercent,
                                                label = "Channel Average Likes %",
                                                onSave = { LocalAdminStore.setVideoMetricOverride(video.id, "channelAverageLikesPercent", it) }
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Purple Progress Bar (Channel average)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFFEEEEEE))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(analytics.channelAverageFraction)
                                                .fillMaxHeight()
                                                .background(Color(0xFF7E57C2), RoundedCornerShape(3.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }

                    VideoAnalyticsSubTab.AUDIENCE -> {
                        // 1. Metric Cards: Unique Viewers & Subscribers (Separate elevated white cards)
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("Unique viewers", fontSize = 12.sp, color = Color(0xFF606060))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${analytics.uniqueViewers}",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F0F0F),
                                            modifier = Modifier.editableDoubleTap(
                                                currentValue = "${analytics.uniqueViewers}",
                                                label = "Unique Viewers",
                                                onSave = { LocalAdminStore.setVideoMetricOverride(video.id, "uniqueViewers", it) }
                                            )
                                        )
                                    }
                                }

                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("Subscribers", fontSize = 12.sp, color = Color(0xFF606060))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = analytics.subscribersText,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F0F0F),
                                            modifier = Modifier.editableDoubleTap(
                                                currentValue = analytics.subscribersText,
                                                label = "Subscribers",
                                                onSave = { LocalAdminStore.setVideoMetricOverride(video.id, "subscribersText", it) }
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Audience by Watch Behaviour Card
                        item {
                            TrafficSourceReportCard(title = "Audience by watch behaviour", subtitle = "Unique viewers • Since published")
                        }

                        // 3. Viewers Also Watch Card
                        item {
                            TrafficSourceReportCard(title = "Viewers also watch", subtitle = "Last 90 days")
                        }

                        // 4. Device Type Card (Matches video at 0:13)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Device type", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F0F0F))
                                    Text("Watch time (hours) • Since published", fontSize = 12.sp, color = Color(0xFF606060))
                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Segmented Progress Bar: Mobile + Computer
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(analytics.deviceMobileFraction)
                                                .fillMaxHeight()
                                                .background(Color(0xFF512DA8))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .weight(analytics.deviceComputerFraction)
                                                .fillMaxHeight()
                                                .background(Color(0xFF9575CD))
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF512DA8)))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Mobile", fontSize = 13.sp, color = Color(0xFF0F0F0F))
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(
                                            text = analytics.deviceMobilePercent,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF0F0F0F),
                                            modifier = Modifier.editableDoubleTap(
                                                currentValue = analytics.deviceMobilePercent,
                                                label = "Mobile Watch Time %",
                                                onSave = { LocalAdminStore.setVideoMetricOverride(video.id, "deviceMobilePercent", it) }
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF9575CD)))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Computer", fontSize = 13.sp, color = Color(0xFF0F0F0F))
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(
                                            text = analytics.deviceComputerPercent,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF0F0F0F),
                                            modifier = Modifier.editableDoubleTap(
                                                currentValue = analytics.deviceComputerPercent,
                                                label = "Computer Watch Time %",
                                                onSave = { LocalAdminStore.setVideoMetricOverride(video.id, "deviceComputerPercent", it) }
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // 5. Demographic Breakdown Cards (Matches video at 0:14)
                        item { TrafficSourceReportCard(title = "Age", subtitle = "Views • Since published") }
                        item { TrafficSourceReportCard(title = "Gender", subtitle = "Views • Since published") }
                        item { TrafficSourceReportCard(title = "Top geographies", subtitle = "Views • Since published") }
                        item { TrafficSourceReportCard(title = "Top subtitle/CC languages", subtitle = "Views • Since published") }
                    }

                    VideoAnalyticsSubTab.REVENUE -> {
                        // 1. Estimated Revenue Card with trend graph
                        item {
                            RevenueEstimatedEarningsCard(
                                analytics = analytics,
                                onSavePrimary = { newVal ->
                                    LocalAdminStore.setVideoMetricOverride(video.id, "estimatedRevenueUsd", newVal)
                                }
                            )
                        }

                        // 2. RPM (Revenue Per Mille) Card with explanatory info icon/dialog
                        item {
                            RevenueRpmCard(
                                analytics = analytics,
                                onSaveRpm = { newVal ->
                                    LocalAdminStore.setVideoMetricOverride(video.id, "rpmUsd", newVal)
                                }
                            )
                        }

                        // 3. Playback-based CPM Card with explanatory info icon/dialog
                        item {
                            RevenueCpmCard(
                                analytics = analytics,
                                onSaveCpm = { newVal ->
                                    LocalAdminStore.setVideoMetricOverride(video.id, "cpmUsd", newVal)
                                }
                            )
                        }

                        // 4. How you make money / Revenue Sources Card (Watch Page Ads, YouTube Premium)
                        item {
                            RevenueSourcesCard(
                                analytics = analytics,
                                onSaveWatchPageAds = { LocalAdminStore.setVideoMetricOverride(video.id, "watchPageAdsPercent", it) },
                                onSavePremium = { LocalAdminStore.setVideoMetricOverride(video.id, "premiumPercent", it) }
                            )
                        }

                        // 5. Ad Types / Formats Card (Skippable, Bumper, Non-skippable, Display, Premium)
                        item {
                            RevenueAdTypesCard(
                                analytics = analytics,
                                onSaveAdType = { key, value ->
                                    LocalAdminStore.setVideoMetricOverride(video.id, key, value)
                                }
                            )
                        }

                        // 6. Monthly Estimated Revenue Card
                        item {
                            RevenueMonthlyCard(
                                analytics = analytics,
                                onSaveMonthlyRevenue = { newVal ->
                                    LocalAdminStore.setVideoMetricOverride(video.id, "estimatedRevenueUsd", newVal)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AudienceRetentionCard(
    videoDuration: String,
    avgDuration: String = "0:09",
    avgPercent: String = "1.8%",
    onSaveDuration: (String) -> Unit = {},
    onSavePercent: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Audience retention", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F0F0F))
                    Text("Average view duration • Lifetime", fontSize = 12.sp, color = Color(0xFF606060))
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFF0F2F5)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = avgDuration,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F0F0F),
                            modifier = Modifier.editableDoubleTap(
                                currentValue = avgDuration,
                                label = "Avg Retention Duration",
                                onSave = onSaveDuration
                            )
                        )
                        Text(
                            text = " (",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F0F0F)
                        )
                        Text(
                            text = avgPercent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F0F0F),
                            modifier = Modifier.editableDoubleTap(
                                currentValue = avgPercent,
                                label = "Avg Retention Percent",
                                onSave = onSavePercent
                            )
                        )
                        Text(
                            text = ")",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F0F0F)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Graph with Y and X axis labels
            Row(modifier = Modifier.fillMaxWidth()) {
                // Y-axis labels (100%, 66%, 33%, 0%)
                Column(
                    modifier = Modifier.height(120.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text("100%", fontSize = 10.sp, color = Color(0xFF909090))
                    Text("66%", fontSize = 10.sp, color = Color(0xFF909090))
                    Text("33%", fontSize = 10.sp, color = Color(0xFF909090))
                    Text("0%", fontSize = 10.sp, color = Color(0xFF909090))
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Canvas retention curve
                Column(modifier = Modifier.weight(1f)) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        val w = size.width
                        val h = size.height

                        // Horizontal grid lines
                        drawLine(Color(0xFFEEEEEE), Offset(0f, 0f), Offset(w, 0f), strokeWidth = 1f)
                        drawLine(Color(0xFFEEEEEE), Offset(0f, h * 0.33f), Offset(w, h * 0.33f), strokeWidth = 1f)
                        drawLine(Color(0xFFEEEEEE), Offset(0f, h * 0.66f), Offset(w, h * 0.66f), strokeWidth = 1f)
                        drawLine(Color(0xFFEEEEEE), Offset(0f, h), Offset(w, h), strokeWidth = 1f)

                        // Retention drop curve path
                        val curvePath = Path().apply {
                            moveTo(0f, 0f) // 100%
                            cubicTo(
                                w * 0.05f, h * 0.60f,
                                w * 0.08f, h * 0.88f,
                                w * 0.12f, h * 0.95f
                            )
                            cubicTo(
                                w * 0.20f, h * 0.98f,
                                w * 0.50f, h * 0.98f,
                                w, h * 0.99f
                            )
                        }

                        // Gradient fill under curve
                        val fillPath = Path().apply {
                            addPath(curvePath)
                            lineTo(w, h)
                            lineTo(0f, h)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0x33E91E63), Color(0x05E91E63))
                            )
                        )

                        // Line stroke
                        drawPath(
                            path = curvePath,
                            color = Color(0xFFE91E63),
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // X-axis labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0:00", fontSize = 10.sp, color = Color(0xFF909090))
                        Text(videoDuration.ifBlank { "8:53" }, fontSize = 10.sp, color = Color(0xFF909090))
                    }
                }
            }
        }
    }
}

@Composable
fun TrafficSourceReportCard(
    title: String,
    subtitle: String = "Views • Since published"
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F0F0F))
            Text(subtitle, fontSize = 12.sp, color = Color(0xFF606060))
            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = Color(0xFF909090),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Not enough traffic data to show this report", fontSize = 13.sp, color = Color(0xFF909090))
            }
        }
    }
}

@Composable
fun RevenueEstimatedEarningsCard(
    analytics: VideoAnalyticsData,
    onSavePrimary: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("revenue_estimated_earnings_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Estimated revenue",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF0F0F0F)
            )
            Text(
                text = "Since published",
                fontSize = 12.sp,
                color = Color(0xFF606060)
            )

            Spacer(modifier = Modifier.height(10.dp))

            val primaryValue = analytics.estimatedRevenueUsd.removePrefix("US")

            Text(
                text = primaryValue,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F0F0F),
                modifier = Modifier.editableDoubleTap(
                    currentValue = primaryValue,
                    label = "Estimated Revenue",
                    onSave = onSavePrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Revenue trend curve with green line and soft gradient fill
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.height(100.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text("$1.00", fontSize = 10.sp, color = Color(0xFF909090))
                    Text("$0.66", fontSize = 10.sp, color = Color(0xFF909090))
                    Text("$0.33", fontSize = 10.sp, color = Color(0xFF909090))
                    Text("0", fontSize = 10.sp, color = Color(0xFF909090))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        val w = size.width
                        val h = size.height

                        // Horizontal grid lines
                        drawLine(Color(0xFFEEEEEE), Offset(0f, 0f), Offset(w, 0f), strokeWidth = 1f)
                        drawLine(Color(0xFFEEEEEE), Offset(0f, h * 0.33f), Offset(w, h * 0.33f), strokeWidth = 1f)
                        drawLine(Color(0xFFEEEEEE), Offset(0f, h * 0.66f), Offset(w, h * 0.66f), strokeWidth = 1f)
                        drawLine(Color(0xFFEEEEEE), Offset(0f, h), Offset(w, h), strokeWidth = 1f)

                        // Stepped green line representing revenue accretion
                        val revPath = Path().apply {
                            moveTo(0f, h * 0.95f)
                            lineTo(w * 0.20f, h * 0.95f)
                            lineTo(w * 0.20f, h * 0.70f)
                            lineTo(w * 0.45f, h * 0.70f)
                            lineTo(w * 0.45f, h * 0.45f)
                            lineTo(w * 0.70f, h * 0.45f)
                            lineTo(w * 0.70f, h * 0.20f)
                            lineTo(w, h * 0.20f)
                        }

                        val fillPath = Path().apply {
                            addPath(revPath)
                            lineTo(w, h)
                            lineTo(0f, h)
                            close()
                        }

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0x332BA640), Color(0x052BA640))
                            )
                        )

                        drawPath(
                            path = revPath,
                            color = Color(0xFF2BA640),
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text("Since published", fontSize = 10.sp, color = Color(0xFF909090))
                    }
                }
            }
        }
    }
}

@Composable
fun RevenueRpmCard(
    analytics: VideoAnalyticsData,
    onSaveRpm: (String) -> Unit = {}
) {
    var showRpmInfoDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("revenue_rpm_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "RPM",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F0F0F)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { showRpmInfoDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "RPM Info",
                            tint = Color(0xFF606060),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = "Monetized",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Revenue for every 1K views • Since published",
                fontSize = 12.sp,
                color = Color(0xFF606060)
            )

            Spacer(modifier = Modifier.height(10.dp))

            val rpmVal = analytics.rpmUsd
            Text(
                text = rpmVal,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F0F0F),
                modifier = Modifier.editableDoubleTap(
                    currentValue = rpmVal,
                    label = "RPM",
                    onSave = onSaveRpm
                )
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "How you earn: Watch Page Ads, YouTube Premium",
                fontSize = 12.sp,
                color = Color(0xFF606060)
            )
        }
    }

    if (showRpmInfoDialog) {
        AlertDialog(
            onDismissRequest = { showRpmInfoDialog = false },
            title = {
                Text(
                    text = "Revenue per mille (RPM)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F0F0F)
                )
            },
            text = {
                Text(
                    text = "Revenue per mille (RPM) shows how much money you made per 1,000 video views. It includes ad revenue, YouTube Premium, channel memberships, and fan funding.",
                    fontSize = 13.sp,
                    color = Color(0xFF606060),
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showRpmInfoDialog = false }) {
                    Text("Got it", color = Color(0xFF065FD4), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun RevenueCpmCard(
    analytics: VideoAnalyticsData,
    onSaveCpm: (String) -> Unit = {}
) {
    var showCpmInfoDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("revenue_cpm_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Playback-based CPM",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F0F0F)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { showCpmInfoDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "CPM Info",
                            tint = Color(0xFF606060),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = "Cost per 1,000 monetized playbacks • Since published",
                fontSize = 12.sp,
                color = Color(0xFF606060)
            )

            Spacer(modifier = Modifier.height(10.dp))

            val cpmVal = analytics.cpmUsd
            Text(
                text = cpmVal,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F0F0F),
                modifier = Modifier.editableDoubleTap(
                    currentValue = cpmVal,
                    label = "CPM",
                    onSave = onSaveCpm
                )
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Advertiser spend on 1,000 monetized playbacks",
                fontSize = 12.sp,
                color = Color(0xFF606060)
            )
        }
    }

    if (showCpmInfoDialog) {
        AlertDialog(
            onDismissRequest = { showCpmInfoDialog = false },
            title = {
                Text(
                    text = "Playback-based CPM",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F0F0F)
                )
            },
            text = {
                Text(
                    text = "Playback-based Cost per mille (CPM) is the effective rate that advertisers paid for every 1,000 video playbacks where one or more ads were shown.",
                    fontSize = 13.sp,
                    color = Color(0xFF606060),
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showCpmInfoDialog = false }) {
                    Text("Got it", color = Color(0xFF065FD4), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun RevenueSourcesCard(
    analytics: VideoAnalyticsData,
    onSaveWatchPageAds: (String) -> Unit = {},
    onSavePremium: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("revenue_sources_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("How you make money", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F0F0F))
            Text("Revenue sources • Since published", fontSize = 12.sp, color = Color(0xFF606060))
            Spacer(modifier = Modifier.height(14.dp))

            // Segmented bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(analytics.watchPageAdsFraction)
                        .fillMaxHeight()
                        .background(Color(0xFF2BA640))
                )
                Box(
                    modifier = Modifier
                        .weight(analytics.premiumFraction)
                        .fillMaxHeight()
                        .background(Color(0xFF81C784))
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2BA640)))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Watch page ads", fontSize = 13.sp, color = Color(0xFF0F0F0F))
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = analytics.watchPageAdsPercent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F0F0F),
                    modifier = Modifier.editableDoubleTap(
                        currentValue = analytics.watchPageAdsPercent,
                        label = "Watch Page Ads %",
                        onSave = onSaveWatchPageAds
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF81C784)))
                Spacer(modifier = Modifier.width(8.dp))
                Text("YouTube Premium", fontSize = 13.sp, color = Color(0xFF0F0F0F))
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = analytics.premiumPercent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F0F0F),
                    modifier = Modifier.editableDoubleTap(
                        currentValue = analytics.premiumPercent,
                        label = "YouTube Premium %",
                        onSave = onSavePremium
                    )
                )
            }
        }
    }
}

@Composable
fun RevenueAdTypesCard(
    analytics: VideoAnalyticsData,
    onSaveAdType: (String, String) -> Unit = { _, _ -> }
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("revenue_ad_types_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Ad types", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F0F0F))
            Text("Ad revenue • Since published", fontSize = 12.sp, color = Color(0xFF606060))
            Spacer(modifier = Modifier.height(14.dp))

            // Multi-segment horizontal progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                Box(modifier = Modifier.weight(analytics.skippableAdsFraction).fillMaxHeight().background(Color(0xFF1B5E20)))
                Box(modifier = Modifier.weight(analytics.bumperAdsFraction).fillMaxHeight().background(Color(0xFF2E7D32)))
                Box(modifier = Modifier.weight(analytics.nonSkippableAdsFraction).fillMaxHeight().background(Color(0xFF43A047)))
                Box(modifier = Modifier.weight(analytics.displayAdsFraction).fillMaxHeight().background(Color(0xFF81C784)))
                Box(modifier = Modifier.weight(analytics.youtubePremiumAdFraction).fillMaxHeight().background(Color(0xFFC8E6C9)))
            }

            Spacer(modifier = Modifier.height(14.dp))

            data class AdTypeItem(val label: String, val percent: String, val color: Color, val key: String)

            val adTypes = listOf(
                AdTypeItem("Skippable video ads (auction)", analytics.skippableAdsPercent, Color(0xFF1B5E20), "skippableAdsPercent"),
                AdTypeItem("Bumper ads (auction)", analytics.bumperAdsPercent, Color(0xFF2E7D32), "bumperAdsPercent"),
                AdTypeItem("Non-skippable video ads", analytics.nonSkippableAdsPercent, Color(0xFF43A047), "nonSkippableAdsPercent"),
                AdTypeItem("Display ads", analytics.displayAdsPercent, Color(0xFF81C784), "displayAdsPercent"),
                AdTypeItem("YouTube Premium revenue", analytics.youtubePremiumAdPercent, Color(0xFFC8E6C9), "youtubePremiumAdPercent")
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                adTypes.forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(item.color))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(item.label, fontSize = 13.sp, color = Color(0xFF0F0F0F))
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = item.percent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F0F0F),
                            modifier = Modifier.editableDoubleTap(
                                currentValue = item.percent,
                                label = "${item.label} %",
                                onSave = { onSaveAdType(item.key, it) }
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RevenueMonthlyCard(
    analytics: VideoAnalyticsData,
    onSaveMonthlyRevenue: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("revenue_monthly_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Monthly estimated revenue", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F0F0F))
            Text("Last 6 months • Since published", fontSize = 12.sp, color = Color(0xFF606060))
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("October 2026", fontSize = 13.5.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F0F0F))
                    Text("Current billing cycle", fontSize = 11.sp, color = Color(0xFF909090))
                }
                val monthRev = analytics.estimatedRevenueUsd
                Text(
                    text = monthRev,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.editableDoubleTap(
                        currentValue = monthRev,
                        label = "Monthly Revenue",
                        onSave = onSaveMonthlyRevenue
                    )
                )
            }
        }
    }
}

// =========================================================================
// PREVIEWS FOR INDIVIDUAL VIDEO ANALYTICS SCREEN & ALL TABS
// =========================================================================

private val previewVideoItem = VideoItem(
    id = "sample_preview_vid",
    title = "How  Pizza Chains Actually Make Money?",
    type = ContentType.VIDEO,
    duration = "8:53",
    publishedTimeAgo = "First 5 days, 15 hours",
    views = 12,
    likes = 1,
    comments = 0,
    rankingText = "1 of 10",
    avgPercentageViewed = "1.8%",
    thumbnailTitle = "CHEAP PIZZA TRAP"
)

private val previewMonetizedVideoItem = VideoItem(
    id = "sample_monetized_vid",
    title = "Motivational Story Of Every IAS Aspirant | Rahul Kumar",
    type = ContentType.VIDEO,
    duration = "32:28",
    publishedTimeAgo = "5 years ago",
    views = 15000000,
    likes = 168000,
    comments = 4400,
    rankingText = "1 of 10",
    avgPercentageViewed = "42.5%",
    thumbnailTitle = "STRUGGLE OF IAS ASPIRANT"
)

@Preview(showBackground = true, name = "Video Analytics - Overview Tab")
@Composable
fun IndividualVideoAnalyticsPreviewOverview() {
    StudioTheme {
        IndividualVideoAnalyticsScreen(
            video = previewVideoItem,
            onBackClick = {},
            initialViewMode = VideoViewMode.ANALYTICS,
            initialTab = VideoAnalyticsSubTab.OVERVIEW
        )
    }
}

@Preview(showBackground = true, name = "Video Analytics - Reach Tab")
@Composable
fun IndividualVideoAnalyticsPreviewReach() {
    StudioTheme {
        IndividualVideoAnalyticsScreen(
            video = previewVideoItem,
            onBackClick = {},
            initialViewMode = VideoViewMode.ANALYTICS,
            initialTab = VideoAnalyticsSubTab.REACH
        )
    }
}

@Preview(showBackground = true, name = "Video Analytics - Engagement Tab")
@Composable
fun IndividualVideoAnalyticsPreviewEngagement() {
    StudioTheme {
        IndividualVideoAnalyticsScreen(
            video = previewVideoItem,
            onBackClick = {},
            initialViewMode = VideoViewMode.ANALYTICS,
            initialTab = VideoAnalyticsSubTab.ENGAGEMENT
        )
    }
}

@Preview(showBackground = true, name = "Video Analytics - Audience Tab")
@Composable
fun IndividualVideoAnalyticsPreviewAudience() {
    StudioTheme {
        IndividualVideoAnalyticsScreen(
            video = previewVideoItem,
            onBackClick = {},
            initialViewMode = VideoViewMode.ANALYTICS,
            initialTab = VideoAnalyticsSubTab.AUDIENCE
        )
    }
}

@Preview(showBackground = true, name = "Video Analytics - Revenue Tab")
@Composable
fun IndividualVideoAnalyticsPreviewRevenue() {
    StudioTheme {
        IndividualVideoAnalyticsScreen(
            video = previewMonetizedVideoItem,
            onBackClick = {},
            initialViewMode = VideoViewMode.ANALYTICS,
            initialTab = VideoAnalyticsSubTab.REVENUE
        )
    }
}

@Preview(showBackground = true, name = "Video Performance - Landing Page")
@Composable
fun IndividualVideoAnalyticsPreviewPerformance() {
    StudioTheme {
        IndividualVideoAnalyticsScreen(
            video = previewVideoItem,
            onBackClick = {},
            initialViewMode = VideoViewMode.PERFORMANCE
        )
    }
}


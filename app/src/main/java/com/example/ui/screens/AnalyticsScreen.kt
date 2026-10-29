package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StudioRepository
import com.example.data.local.LocalAdminStore
import com.example.model.ContentType
import com.example.model.DrilldownContentItem
import com.example.model.MetricCardData
import com.example.model.VideoItem
import com.example.ui.components.*
import com.example.ui.theme.*

enum class AnalyticsSubTab(val label: String) {
    OVERVIEW("Overview"),
    CONTENT("Content"),
    AUDIENCE("Audience"),
    REVENUE("Revenue"),
    TRENDS("Trends")
}

@Composable
fun AnalyticsScreen(
    onVideoClick: (VideoItem) -> Unit,
    activeDrilldownMetric: MetricCardData? = null,
    onDrilldownChange: (MetricCardData?) -> Unit = {}
) {
    var selectedSubTab by remember { mutableStateOf(AnalyticsSubTab.OVERVIEW) }
    var selectedContentFilter by remember { mutableStateOf("All") }

    val colors = StudioTheme.colors

    if (activeDrilldownMetric != null) {
        MetricDrilldownScreen(
            metric = activeDrilldownMetric,
            onBackClick = { onDrilldownChange(null) },
            onVideoClick = onVideoClick
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("analytics_screen")
    ) {
        // Sub-tabs Horizontal Scrollable Row (Overview, Content, Audience, Revenue, Trends)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnalyticsSubTab.values().forEach { subTab ->
                val isSelected = subTab == selectedSubTab
                Column(
                    modifier = Modifier
                        .clickable { selectedSubTab = subTab }
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = subTab.label,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color(0xFF0F0F0F) else Color(0xFF606060)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(52.dp)
                            .height(2.5.dp)
                            .background(if (isSelected) Color(0xFF0F0F0F) else Color.Transparent)
                    )
                }
            }
        }

        HorizontalDivider(thickness = 0.8.dp, color = Color(0xFFEEEEEE))

        // SubTab content
        when (selectedSubTab) {
            AnalyticsSubTab.OVERVIEW -> OverviewTabContent(
                onMetricClick = { onDrilldownChange(it) },
                onVideoClick = onVideoClick
            )
            AnalyticsSubTab.CONTENT -> ContentTabAnalytics(
                selectedFilter = selectedContentFilter,
                onFilterSelected = { selectedContentFilter = it },
                onVideoClick = onVideoClick
            )
            AnalyticsSubTab.AUDIENCE -> AudienceTabContent(onVideoClick)
            AnalyticsSubTab.REVENUE -> RevenueTabContent()
            AnalyticsSubTab.TRENDS -> TrendsTabContent()
        }
    }
}

@Composable
private fun OverviewTabContent(
    onMetricClick: (MetricCardData) -> Unit,
    onVideoClick: (VideoItem) -> Unit
) {
    val colors = StudioTheme.colors
    val overviewCards = listOf(
        StudioRepository.overviewViewsMetric,
        StudioRepository.overviewWatchTimeMetric,
        StudioRepository.overviewSubscribersMetric,
        StudioRepository.overviewEstimatedRevenueMetric // Explicit addition: Estimated revenue card
    )
    val pagerState = rememberPagerState(pageCount = { overviewCards.size })

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Swipeable Metric Cards Pager - Entire Card slides horizontally as one complete unit
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                    pageSpacing = 12.dp
                ) { page ->
                    val metric = overviewCards[page]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
                            .clickable { onMetricClick(metric) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            MetricCarouselCard(metric = metric)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Pager Dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(overviewCards.size) { i ->
                        val isCurrent = pagerState.currentPage == i
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (isCurrent) 6.dp else 5.dp)
                                .clip(CircleShape)
                                .background(if (isCurrent) colors.textPrimary else colors.textTertiary)
                        )
                    }
                }
            }
        }

        // Top content section (Views · Last 28 days)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Top content",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Views · Last 28 days",
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    StudioRepository.topContentList.forEach { video ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onVideoClick(video) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            VideoThumbnailView(
                                title = video.title,
                                isShort = video.type == ContentType.SHORT,
                                width = 38.dp,
                                height = if (video.type == ContentType.SHORT) 52.dp else 30.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = video.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary,
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${video.views}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            }
        }

        // Real time section (Views · 48 hours)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Real time",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "49",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Views · 48 hours",
                                fontSize = 13.sp,
                                color = colors.textSecondary
                            )
                        }

                        RealtimeBarChart(
                            viewsCount = 49,
                            modifier = Modifier.width(140.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(thickness = 0.8.dp, color = colors.divider)
                    Spacer(modifier = Modifier.height(8.dp))

                    StudioRepository.realtimeVideos.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            VideoThumbnailView(
                                title = item.title,
                                isShort = true,
                                width = 36.dp,
                                height = 48.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = item.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary,
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )
                            Text(
                                text = "${item.views}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(14.dp)
                            ) {
                                RealtimeBarChart(viewsCount = item.views)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricDrilldownScreen(
    metric: MetricCardData,
    onBackClick: () -> Unit,
    onVideoClick: (VideoItem) -> Unit
) {
    BackHandler { onBackClick() }

    val colors = StudioTheme.colors
    val dateChips = listOf("7D", "28D", "90D", "365D", "Oct", "Sept", "Aug", "2026", "2025", "Lifetime")
    var selectedDateRange by remember { mutableStateOf("28D") }

    val headlineText = when (metric.title) {
        "Views" -> {
            val count = when (selectedDateRange) {
                "7D" -> "195"
                "90D" -> "1,840"
                "365D" -> "12.4K"
                "Lifetime" -> "887.7K"
                else -> metric.value.ifEmpty { "812" }
            }
            "Your channel got $count views in the last $selectedDateRange"
        }
        "Watch time (hours)" -> "Your channel got ${metric.value} hours in the last $selectedDateRange"
        "Subscribers" -> "Your channel got ${metric.value} in the last $selectedDateRange"
        "Estimated revenue" -> "Your estimated revenue was ${metric.value} in the last $selectedDateRange (Simulated)"
        else -> "${metric.title} in the last $selectedDateRange"
    }

    val displayTrendData = remember(selectedDateRange, metric.trendData) {
        when (selectedDateRange) {
            "7D" -> if (metric.trendData.size >= 7) metric.trendData.takeLast(7) else metric.trendData
            "90D" -> metric.trendData.map { it * 1.4f }
            else -> metric.trendData
        }
    }

    val (startDateLabel, endDateLabel) = when (selectedDateRange) {
        "7D" -> "14 Oct" to "21 Oct"
        "90D" -> "23 Jul" to "21 Oct"
        "365D" -> "Oct 2025" to "Oct 2026"
        "Lifetime" -> "Channel start" to "Today"
        else -> "24 Sept" to "21 Oct"
    }

    val topItems: List<DrilldownContentItem> = when (metric.title) {
        "Views" -> StudioRepository.drilldownTopContentViews
        "Watch time (hours)" -> StudioRepository.drilldownTopContentWatchTime
        "Subscribers" -> StudioRepository.drilldownTopContentSubscribers
        "Estimated revenue" -> StudioRepository.drilldownTopContentRevenue
        else -> emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Date-range buttons directly below the app bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            dateChips.forEach { chip ->
                val isSelected = chip == selectedDateRange
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) (if (colors.isDark) Color.White else Color.Black) else colors.chipBackground)
                        .border(1.dp, if (isSelected) Color.Transparent else colors.chipBorder, RoundedCornerShape(8.dp))
                        .clickable { selectedDateRange = chip }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chip,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) (if (colors.isDark) Color.Black else Color.White) else colors.textPrimary
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Summary text directly under the date selector (compact, no excessive vertical gap)
            item {
                Text(
                    text = headlineText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 8.dp)
                )
            }

            // Big Drilldown Chart (compact padding and card spacing)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        TrendLineChart(
                            data = displayTrendData,
                            lineColor = if (metric.title == "Estimated revenue") StudioGreen else Color(0xFF00ACC1),
                            yMinLabel = metric.yMinLabel,
                            yMidLabel = metric.yMidLabel,
                            yMaxLabel = metric.yMaxLabel,
                            startDateLabel = startDateLabel,
                            endDateLabel = endDateLabel
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(thickness = 0.8.dp, color = colors.divider)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Metric Legend and Comparison
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (metric.title == "Estimated revenue") StudioGreen else Color(0xFF00ACC1))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = metric.title,
                                    fontSize = 13.sp,
                                    color = colors.textPrimary
                                )
                            }
                            Text(
                                text = if (metric.title == "Views" && selectedDateRange == "7D") "195" else metric.value,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }

                        if (metric.subtitle.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (metric.title == "Views") "612 more than the usual 140–200" else metric.subtitle,
                                fontSize = 12.sp,
                                color = if (metric.isPositive) StudioGreen else colors.textSecondary
                            )
                        }
                    }
                }
            }

            // Top Content Breakdown List (matching recording)
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Top content",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            items(topItems.size) { i ->
                val item = topItems[i]
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VideoThumbnailView(
                            title = item.title,
                            isShort = item.isShort,
                            width = 38.dp,
                            height = if (item.isShort) 48.dp else 28.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = item.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.valueText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }

                    if (item.sharePercent > 0f) {
                        Spacer(modifier = Modifier.height(4.dp))
                        StudioLinearProgress(
                            percentage = item.sharePercent * 100f,
                            barColor = Color(0xFF00ACC1),
                            modifier = Modifier.padding(start = 50.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentTabAnalytics(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    onVideoClick: (VideoItem) -> Unit
) {
    val colors = StudioTheme.colors
    val filters = listOf("All", "Videos", "Shorts", "Live", "Posts")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Filter pills row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = filter == selectedFilter
                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) (if (colors.isDark) Color.White else Color.Black) else colors.chipBackground)
                            .border(1.dp, if (isSelected) Color.Transparent else colors.chipBorder, RoundedCornerShape(8.dp))
                            .clickable { onFilterSelected(filter) }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) (if (colors.isDark) Color.Black else Color.White) else colors.textPrimary
                        )
                    }
                }
            }
        }

        when (selectedFilter) {
            "Shorts" -> {
                // Shorts cards (matching screen recording 00:26 - 00:28 & 00:48 - 00:52)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Views", fontSize = 13.sp, color = colors.textSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "772", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                MetricIndicatorBadge(isPositive = true, showArrow = true)
                            }
                            Text(text = "662 more than usual", fontSize = 13.sp, color = colors.textSecondary)
                            Spacer(modifier = Modifier.height(14.dp))
                            TrendLineChart(
                                data = listOf(10f, 20f, 60f, 15f, 25f, 40f, 30f, 35f, 20f, 25f, 30f, 25f, 40f),
                                lineColor = Color(0xFF673AB7),
                                yMinLabel = "0",
                                yMaxLabel = "60"
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "How viewers engaged", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Text(text = "Last 28 days", fontSize = 13.sp, color = colors.textSecondary)
                            Spacer(modifier = Modifier.height(14.dp))
                            StudioLinearProgress(percentage = 83.2f, barColor = Color(0xFF673AB7))
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(text = "83.2%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                    Text(text = "Stayed to watch", fontSize = 12.sp, color = colors.textSecondary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "16.8%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                    Text(text = "Swiped away", fontSize = 12.sp, color = colors.textSecondary)
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Top Shorts", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Text(text = "Views · Last 28 days", fontSize = 13.sp, color = colors.textSecondary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                VideoThumbnailView(title = "80 HAZAR", isShort = true, width = 36.dp, height = 48.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "80 HAZAR KE MERE SHOES 👟 MEKO...",
                                    fontSize = 13.sp,
                                    color = colors.textPrimary,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1
                                )
                                Text(text = "735", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "How viewers find your Shorts", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Text(text = "Views · Last 28 days", fontSize = 13.sp, color = colors.textSecondary)
                            Spacer(modifier = Modifier.height(14.dp))
                            listOf(
                                "YouTube search" to "95.0%",
                                "Shorts feed" to "1.7%",
                                "Sound pages" to "1.3%"
                            ).forEach { (src, pct) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = src, fontSize = 14.sp, color = colors.textPrimary)
                                    Text(text = pct, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                }
                            }
                        }
                    }
                }
            }

            "Videos" -> {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Views", fontSize = 13.sp, color = colors.textSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "39", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                MetricIndicatorBadge(isPositive = false, showArrow = true)
                            }
                            Text(text = "11 fewer than usual", fontSize = 13.sp, color = colors.textSecondary)
                            Spacer(modifier = Modifier.height(14.dp))
                            TrendLineChart(
                                data = listOf(1f, 0.5f, 2f, 1f, 1f, 3f, 1f, 0f, 2f, 1f, 4f, 1f),
                                lineColor = Color(0xFF673AB7),
                                yMinLabel = "0",
                                yMaxLabel = "6"
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "How viewers find your videos", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Text(text = "Views · Last 28 days", fontSize = 13.sp, color = colors.textSecondary)
                            Spacer(modifier = Modifier.height(14.dp))
                            listOf(
                                "YouTube search" to "56.4%",
                                "Browse features" to "20.5%",
                                "Other YouTube features" to "12.8%"
                            ).forEach { (src, pct) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = src, fontSize = 14.sp, color = colors.textPrimary)
                                    Text(text = pct, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                // All, Live, Posts
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Views", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Text(text = "Last 28 days", fontSize = 13.sp, color = colors.textSecondary)
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Shorts", fontSize = 14.sp, color = colors.textPrimary)
                                Text(text = "772", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            StudioLinearProgress(percentage = 95f, barColor = Color(0xFF673AB7))

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Videos", fontSize = 14.sp, color = colors.textPrimary)
                                Text(text = "39", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            StudioLinearProgress(percentage = 5f, barColor = Color(0xFF673AB7))
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Published content", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Text(text = "Last 28 days", fontSize = 13.sp, color = colors.textSecondary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.chipBackground)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Nothing to show for these dates",
                                        fontSize = 13.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AudienceTabContent(onVideoClick: (VideoItem) -> Unit) {
    val colors = StudioTheme.colors

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Monthly audience Card (Matching reference IMG_3104)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Monthly audience",
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "39.6k",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TrendLineChart(
                        data = StudioRepository.audienceMonthlyMetric.trendData,
                        lineColor = Color(0xFF7E57C2),
                        yLabels = listOf("42.0k", "28.0k", "14.0k", "0"),
                        fixedMin = 0f,
                        fixedMax = 42.0f,
                        startDateLabel = "27 Sept",
                        endDateLabel = "24 Oct"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pager dots (3 dots)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(colors.textPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD1D5DB))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD1D5DB))
                        )
                    }
                }
            }
        }

        // Audience by watch behaviour (Matching reference IMG_3104)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Audience by watch behaviour",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Monthly audience · 24 Oct 2026",
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    AudienceSegmentBar(newPct = 82.6f, casualPct = 16.6f, regularPct = 0.8f)
                    Spacer(modifier = Modifier.height(14.dp))

                    listOf(
                        Triple("New viewers", "82.6%", Color(0xFF311B92)),
                        Triple("Casual viewers", "16.6%", Color(0xFF7E57C2)),
                        Triple("Regular viewers", "0.8%", Color(0xFFBA68C8))
                    ).forEach { (label, pct, col) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(col))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = label, fontSize = 14.sp, color = colors.textPrimary)
                            }
                            Text(text = pct, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                        }
                    }
                }
            }
        }

        // Popular with new viewers (Matching reference IMG_3104)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Popular with new viewers",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Views · Last 28 days",
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val newPopularVideos = listOf(
                        Triple("Husband Harassing Lovi...", "61.2k", true),
                        Triple("Jab Meri Biwi Gussa ho...", "1.9k", true),
                        Triple("Jab Meri Biwi Juth Bolk...", "1.3k", true),
                        Triple("Karwa Chauth Par Biwi...", "929", true),
                        Triple("Corrupt Policeman Humi...", "567", false)
                    )

                    newPopularVideos.forEach { (title, views, isShort) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val videoItem = VideoItem(
                                        id = "new_viewers_${title.hashCode()}",
                                        title = title,
                                        type = if (isShort) ContentType.SHORT else ContentType.VIDEO,
                                        duration = if (isShort) "0:45" else "12:30",
                                        publishedTimeAgo = "28 days ago",
                                        views = views.replace("k", "").toFloatOrNull()?.let { (it * 1000).toLong() } ?: views.toLongOrNull() ?: 1000L,
                                        likes = 120,
                                        comments = 15,
                                        thumbnailTitle = title
                                    )
                                    onVideoClick(videoItem)
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            VideoThumbnailView(
                                title = title,
                                isShort = isShort,
                                width = 38.dp,
                                height = if (isShort) 48.dp else 26.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary,
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )
                            Text(
                                text = views,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            }
        }

        // When your viewers are on YouTube (Frame 020)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "When your viewers are on YouTube",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Your local time (GMT +0530) · Last 28 days",
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val days = listOf("M", "T", "W", "T", "F", "S", "S")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        days.forEach { day ->
                            Text(text = day, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.textSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Heatmap grid (6 rows of hours)
                    val intensities = listOf(
                        listOf(0.1f, 0.1f, 0.2f, 0.1f, 0.1f, 0.2f, 0.2f),
                        listOf(0.2f, 0.3f, 0.3f, 0.2f, 0.3f, 0.4f, 0.4f),
                        listOf(0.4f, 0.5f, 0.6f, 0.5f, 0.6f, 0.7f, 0.7f),
                        listOf(0.7f, 0.8f, 0.9f, 0.8f, 0.9f, 0.95f, 0.95f),
                        listOf(0.9f, 0.95f, 0.95f, 0.9f, 0.95f, 0.9f, 0.9f),
                        listOf(0.5f, 0.6f, 0.6f, 0.5f, 0.6f, 0.6f, 0.5f)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        intensities.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                row.forEach { alpha ->
                                    Box(
                                        modifier = Modifier
                                            .size(width = 36.dp, height = 12.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color(0xFF311B92).copy(alpha = alpha.coerceIn(0.12f, 1f)))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Device type (Recording 01:01)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Device type", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Text(text = "Watch time (hours) · Last 28 days", fontSize = 13.sp, color = colors.textSecondary)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    ) {
                        Box(modifier = Modifier.weight(91.7f).fillMaxHeight().background(Color(0xFF311B92)))
                        Box(modifier = Modifier.weight(5.1f).fillMaxHeight().background(Color(0xFF7E57C2)))
                        Box(modifier = Modifier.weight(1.6f).fillMaxHeight().background(Color(0xFFBA68C8)))
                        Box(modifier = Modifier.weight(1.6f).fillMaxHeight().background(Color(0xFFE91E63)))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    listOf(
                        "Mobile" to "91.7%",
                        "TV" to "5.1%",
                        "Computer" to "1.6%",
                        "Tablet" to "1.6%"
                    ).forEach { (dev, pct) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = dev, fontSize = 14.sp, color = colors.textPrimary)
                            Text(text = pct, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                        }
                    }
                }
            }
        }

        // Geography
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Geography", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Text(text = "Views · Last 28 days", fontSize = 13.sp, color = colors.textSecondary)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "India", fontSize = 14.sp, color = colors.textPrimary)
                        Text(text = "86.1%", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    StudioLinearProgress(percentage = 86.1f, barColor = Color(0xFF673AB7))
                }
            }
        }
    }
}

@Composable
private fun RevenueTabContent() {
    val colors = StudioTheme.colors
    val timeChips = listOf("Since published", "First 24H", "7D", "28D", "90D")
    var selectedChip by remember { mutableStateOf("Since published") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Time filter chips row (Frame 048)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timeChips.forEach { chip ->
                    val isSelected = chip == selectedChip
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF0F0F0F) else Color(0xFFF2F4F7))
                            .border(1.dp, if (isSelected) Color.Transparent else Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                            .clickable { selectedChip = chip }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chip,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF0F0F0F)
                        )
                    }
                }
            }
        }

        // Top Estimated Revenue Card (Frame 048)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Title and value row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00ACC1))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Estimated revenue",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }

                        Text(
                            text = "$6,768.14",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Smooth Teal Revenue Line Chart (Frame 048: $6.9k to 0, 21 Jul to 25 Oct)
                    TrendLineChart(
                        data = listOf(0f, 15f, 35f, 52f, 62f, 66f, 68f, 68.5f, 68.8f, 69f, 69f, 69.1f),
                        lineColor = Color(0xFF00ACC1),
                        yMinLabel = "$0",
                        yMidLabel = "$4.6k",
                        yMaxLabel = "$6.9k",
                        startDateLabel = "21 Jul",
                        endDateLabel = "25 Oct"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Notice pill: 2 days of data still processing (Frame 048)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE8ECEF)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color(0xFF606060),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "2 days of data still processing",
                                fontSize = 13.sp,
                                color = Color(0xFF404040)
                            )
                        }
                    }
                }
            }
        }

        // How Supers and gifts make money Card (Frame 048)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "How Supers and gifts make money",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Notice pill: 2 days of data still processing
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE8ECEF)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color(0xFF606060),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "2 days of data still processing",
                                fontSize = 13.sp,
                                color = Color(0xFF404040)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Watch page ads
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Watch page ads", fontSize = 14.sp, color = colors.textPrimary)
                        Text(text = "$6,763.85", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    StudioLinearProgress(percentage = 99.5f, barColor = Color(0xFF00ACC1))

                    Spacer(modifier = Modifier.height(16.dp))

                    // YouTube Premium
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "YouTube Premium", fontSize = 14.sp, color = colors.textPrimary)
                        Text(text = "$4.28", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    StudioLinearProgress(percentage = 1.5f, barColor = Color(0xFF00ACC1))
                }
            }
        }

        // How much advertisers pay Card (Frame 036)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "How much advertisers pay",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Last 28 days",
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "$0.74",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Playback-based cost for every 1K views (CPM)",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }

        // Earnings by ad type Card (Frame 036)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Earnings by ad type",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "YouTube ad revenue · Last 28 days",
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Non-skippable
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Non-skippable video ads (Auction)", fontSize = 14.sp, color = colors.textPrimary)
                        Text(text = "59.1%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    StudioLinearProgress(percentage = 59.1f, barColor = Color(0xFF26A69A))

                    Spacer(modifier = Modifier.height(14.dp))

                    // Skippable
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Skippable video ads (Auction)", fontSize = 14.sp, color = colors.textPrimary)
                        Text(text = "29.3%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    StudioLinearProgress(percentage = 29.3f, barColor = Color(0xFF26A69A))

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bumper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Bumper ads (Auction)", fontSize = 14.sp, color = colors.textPrimary)
                        Text(text = "8.5%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    StudioLinearProgress(percentage = 8.5f, barColor = Color(0xFF26A69A))
                }
            }
        }
    }
}

@Composable
private fun TrendsTabContent() {
    val colors = StudioTheme.colors
    var searchQuery by remember { mutableStateOf("") }
    var topicSaves by remember { mutableStateOf(mapOf("haryanvi short movies haryanavi" to false, "crime alert" to false)) }
    var showInspiringCard by remember { mutableStateOf(true) }
    var selectedReactionIndex by remember { mutableStateOf<Int?>(null) }

    val topSearchesList = listOf(
        "haryanvi short movies haryanavi",
        "crime alert",
        "ias motivational video",
        "hindi short films"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Explore topics header (Frame 032)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Explore topics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Help",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "0",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                }
            }
        }

        // Search Input (Frame 032)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF2F4F7))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF606060),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (searchQuery.isEmpty()) "Search" else searchQuery,
                        fontSize = 15.sp,
                        color = if (searchQuery.isEmpty()) Color(0xFF606060) else Color(0xFF0F0F0F)
                    )
                }
            }
        }

        // Top searches header (Frame 032)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Top searches",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = colors.textPrimary
                )
            }
        }

        // Top search cards (Frame 032: haryanvi short movies haryanavi, crime alert)
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(topSearchesList.size) { i ->
                    val topic = topSearchesList[i]
                    val isSaved = topicSaves[topic] ?: false
                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = topic,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                minLines = 2,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        topicSaves = topicSaves.toMutableMap().apply {
                                            put(topic, !isSaved)
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Save",
                                        tint = if (isSaved) StudioRed else colors.textPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Feedback Card: How inspiring is this? (Frame 032)
        if (showInspiringCard) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF2F4F7)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "How inspiring is this?",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F0F0F)
                            )
                            IconButton(
                                onClick = { showInspiringCard = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color(0xFF606060),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val emojiLabels = listOf("😞", "🙁", "😐", "🙂", "😄")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            emojiLabels.forEachIndexed { index, emoji ->
                                val isSelected = selectedReactionIndex == index
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFF0F0F0F) else Color.White)
                                        .clickable { selectedReactionIndex = index },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 22.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Not at all", fontSize = 11.sp, color = Color(0xFF606060))
                            Text(text = "Extremely", fontSize = 11.sp, color = Color(0xFF606060))
                        }
                    }
                }
            }
        }

        // Breakout videos (? >) (Frame 032)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Breakout videos",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Help",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = colors.textPrimary
                )
            }
        }

        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val breakoutVideos = listOf(
                    Triple("Police Officer Action Scene", "Desh Crime Series", "1.2M views"),
                    Triple("Family Drama Struggle Episode 4", "Drama World", "840K views"),
                    Triple("Villager inspiring IAS journey", "Inspiration Hub", "620K views")
                )

                items(breakoutVideos.size) { i ->
                    val (title, channel, views) = breakoutVideos[i]
                    Column(modifier = Modifier.width(220.dp)) {
                        VideoThumbnailView(
                            title = title,
                            duration = "14:20",
                            width = 220.dp,
                            height = 124.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                modifier = Modifier.weight(1f),
                                maxLines = 2
                            )
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$channel · $views · 2 weeks ago",
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCarouselCard(
    metric: MetricCardData,
    lineColor: Color = Color(0xFF00ACC1)
) {
    val colors = StudioTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = metric.title,
            fontSize = 13.sp,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = metric.value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier.editableDoubleTap(
                    currentValue = metric.value,
                    label = metric.title,
                    onSave = { newVal ->
                        when (metric.title) {
                            "Views" -> LocalAdminStore.updateDashboardViews(newVal)
                            "Watch time (hours)" -> LocalAdminStore.updateDashboardWatchTime(newVal)
                            "Subscribers" -> LocalAdminStore.updateDashboardSubscribers(newVal)
                            "Estimated revenue" -> LocalAdminStore.updateDashboardRevenue(newVal)
                        }
                    }
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            MetricIndicatorBadge(
                isPositive = metric.isPositive,
                isCheckmark = metric.isCheckmark,
                showArrow = metric.showArrow
            )
        }
        if (metric.subtitle.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = metric.subtitle,
                fontSize = 13.sp,
                color = if (metric.isSimulatedDemo) StudioGreen else colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        TrendLineChart(
            data = metric.trendData,
            lineColor = if (metric.title == "Estimated revenue") StudioGreen else lineColor,
            yMinLabel = metric.yMinLabel,
            yMidLabel = metric.yMidLabel,
            yMaxLabel = metric.yMaxLabel,
            startDateLabel = metric.startDateLabel,
            endDateLabel = metric.endDateLabel
        )
    }
}

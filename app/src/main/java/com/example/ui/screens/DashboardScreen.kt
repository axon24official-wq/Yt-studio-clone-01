package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.StudioRepository
import com.example.model.VideoItem
import com.example.ui.components.VideoThumbnailView
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    onVideoClick: (VideoItem) -> Unit,
    onAnalyticsClick: () -> Unit
) {
    val profile = StudioRepository.danielProfile
    var expandedVideoId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
    ) {
        // Channel Summary: Profile picture, Channel name, Subscriber count, Subtitle
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular channel profile picture (72dp)
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF1E3A8A), Color(0xFF0F172A), Color(0xFF020617))
                            )
                        )
                        .border(1.5.dp, Color(0xFF3B82F6), CircleShape)
                        .testTag("channel_summary_avatar"),
                    contentAlignment = Alignment.Center
                ) {
                    if (!profile.avatarUri.isNullOrBlank()) {
                        AsyncImage(
                            model = profile.avatarUri,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Channel Avatar",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "OFFICIAL",
                                color = Color(0xFF60A5FA),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = profile.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioBlack
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = profile.subscribersCount,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioBlack,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Total subscribers",
                        fontSize = 13.sp,
                        color = StudioSecondaryText
                    )
                }
            }
        }

        // Channel Analytics Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAnalyticsClick() }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Channel analytics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioBlack
                    )
                    Text(
                        text = StudioRepository.dashboardPeriodLabel,
                        fontSize = 13.sp,
                        color = StudioSecondaryText
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2x2 Grid of distinct, visible rounded rectangular cards
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Row 1: Views & Watch time (hours)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnalyticsCard(
                            modifier = Modifier.weight(1f),
                            label = "Views",
                            value = StudioRepository.dashboardViews,
                            indicator = DashboardCardIndicator.GREEN_UP_ARROW
                        )

                        AnalyticsCard(
                            modifier = Modifier.weight(1f),
                            label = "Watch time (hours)",
                            value = StudioRepository.dashboardWatchTime,
                            indicator = DashboardCardIndicator.GREEN_CHECKMARK
                        )
                    }

                    // Row 2: Subscribers & Estimated revenue
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnalyticsCard(
                            modifier = Modifier.weight(1f),
                            label = "Subscribers",
                            value = StudioRepository.dashboardSubscribers,
                            indicator = DashboardCardIndicator.NONE
                        )

                        AnalyticsCard(
                            modifier = Modifier.weight(1f),
                            label = "Estimated revenue",
                            value = StudioRepository.dashboardRevenue,
                            indicator = DashboardCardIndicator.NONE
                        )
                    }
                }
            }
        }

        // Latest published content Section
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Latest published content",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = StudioBlack
            )
        }

        val contentList = StudioRepository.latestPublishedContent
        itemsIndexed(contentList) { index, video ->
            val isExpanded = expandedVideoId == video.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("latest_content_item_$index"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    // Video Row: Thumbnail on left, title & elapsed time on right
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onVideoClick(video) },
                        verticalAlignment = Alignment.Top
                    ) {
                        VideoThumbnailView(
                            title = video.title,
                            duration = video.duration,
                            isShort = true,
                            width = 46.dp,
                            height = 68.dp,
                            thumbnailUri = video.thumbnailUri
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = video.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = StudioBlack,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 19.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = video.publishedTimeAgo,
                                fontSize = 12.sp,
                                color = StudioSecondaryText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stats row: Views count, Likes count, Comments count, Expansion chevron
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Analytics Views count
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Views",
                                tint = StudioSecondaryText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatCount(video.views),
                                fontSize = 12.sp,
                                color = StudioSecondaryText
                            )
                        }

                        // Likes count
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ThumbUp,
                                contentDescription = "Likes",
                                tint = StudioSecondaryText,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatCount(video.likes),
                                fontSize = 12.sp,
                                color = StudioSecondaryText
                            )
                        }

                        // Comments count
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Comment,
                                contentDescription = "Comments",
                                tint = StudioSecondaryText,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatCount(video.comments),
                                fontSize = 12.sp,
                                color = StudioSecondaryText
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Expansion chevron arrow on the right
                        IconButton(
                            onClick = {
                                expandedVideoId = if (isExpanded) null else video.id
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = StudioSecondaryText,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Expandable detailed metrics dropdown
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFAFAFA))
                                .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Ranking by views", fontSize = 13.sp, color = StudioDarkText)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = video.rankingText, fontSize = 13.sp, color = StudioDarkText)
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = StudioSecondaryText, modifier = Modifier.size(16.dp))
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Views", fontSize = 13.sp, color = StudioDarkText)
                                Text(text = "${formatCount(video.views)} ↑", fontSize = 13.sp, color = StudioGreen, fontWeight = FontWeight.SemiBold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Average view duration", fontSize = 13.sp, color = StudioDarkText)
                                Text(text = video.avgPercentageViewed, fontSize = 13.sp, color = StudioDarkText)
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class DashboardCardIndicator {
    NONE,
    GREEN_UP_ARROW,
    GREEN_CHECKMARK
}

@Composable
private fun AnalyticsCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    indicator: DashboardCardIndicator = DashboardCardIndicator.NONE
) {
    Card(
        modifier = modifier.height(86.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = StudioSecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioBlack
                )

                if (indicator != DashboardCardIndicator.NONE) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(17.dp)
                            .clip(CircleShape)
                            .background(StudioGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        if (indicator == DashboardCardIndicator.GREEN_UP_ARROW) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Up",
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        } else if (indicator == DashboardCardIndicator.GREEN_CHECKMARK) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Check",
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatCount(count: Long): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> {
            val v = count / 1000.0
            if (v % 1.0 == 0.0) String.format("%.0fk", v) else String.format("%.1fk", v)
        }
        else -> count.toString()
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ThumbUp
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
import com.example.model.ContentType
import com.example.model.VideoItem
import com.example.ui.components.MetricIndicatorBadge
import com.example.ui.components.TrendLineChart
import com.example.ui.components.VideoThumbnailView
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailModal(
    video: VideoItem,
    onDismiss: () -> Unit
) {
    val colors = StudioTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBackground,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.chipBorder)
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .testTag("video_detail_modal"),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top action bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Video performance",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )

                    Row {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = colors.textPrimary)
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = colors.textPrimary)
                        }
                    }
                }
            }

            // Video preview hero
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    VideoThumbnailView(
                        title = video.title,
                        isShort = video.type == ContentType.SHORT,
                        width = 110.dp,
                        height = if (video.type == ContentType.SHORT) 140.dp else 70.dp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = video.publishedTimeAgo,
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.ThumbUp, contentDescription = null, modifier = Modifier.size(16.dp), tint = colors.textSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${video.likes}", fontSize = 13.sp, color = colors.textSecondary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = colors.textSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${video.comments}", fontSize = 13.sp, color = colors.textSecondary)
                            }
                        }
                    }
                }
            }

            // Views metric card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = if (colors.isDark) Color(0xFF1E1E1E) else colors.cardBackground),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Views", fontSize = 13.sp, color = colors.textSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "${video.views}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            MetricIndicatorBadge(isPositive = true, showArrow = true)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TrendLineChart(
                            data = listOf(0.1f, 0.4f, 0.6f, 0.5f, 0.8f, 0.7f, 1.0f),
                            lineColor = Color(0xFF00ACC1),
                            yMinLabel = "0",
                            yMaxLabel = "${video.views.coerceAtLeast(10)}"
                        )
                    }
                }
            }

            // Key metrics breakdown
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = if (colors.isDark) Color(0xFF1E1E1E) else colors.cardBackground),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ranking by views", fontSize = 14.sp, color = colors.textPrimary)
                            Text(video.rankingText.ifEmpty { "1 of 10" }, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = colors.textPrimary)
                        }
                        HorizontalDivider(color = colors.divider)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Average percentage viewed", fontSize = 14.sp, color = colors.textPrimary)
                            Text(video.avgPercentageViewed.ifEmpty { "88.5%" }, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = colors.textPrimary)
                        }
                        HorizontalDivider(color = colors.divider)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Audience retention", fontSize = 14.sp, color = colors.textPrimary)
                            Text("Above average", color = StudioGreen, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

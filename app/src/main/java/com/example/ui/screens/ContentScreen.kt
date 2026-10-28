package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StudioRepository
import com.example.data.local.LocalAdminStore
import com.example.model.ContentType
import com.example.model.VideoItem
import com.example.ui.components.VideoThumbnailView
import com.example.ui.components.editableDoubleTap
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentScreen(
    onVideoClick: (VideoItem) -> Unit
) {
    val colors = StudioTheme.colors
    var selectedCategory by remember { mutableStateOf("Videos") }
    val categories = listOf("All", "Videos", "Shorts", "Live", "Posts", "Playlists")

    var selectedSortBy by remember { mutableStateOf("Most viewed") }
    var selectedVisibility by remember { mutableStateOf<String?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }
    var showVisibilitySheet by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var menuVideo by remember { mutableStateOf<VideoItem?>(null) }

    // Format count helper (e.g. 30000000 -> 30m, 190000 -> 190k, 4100 -> 4.1k)
    fun formatMetricCount(count: Long): String {
        return when {
            count >= 1_000_000 -> {
                val value = count / 1_000_000.0
                if (value >= 10.0) "${value.toInt()}m" else String.format("%.1fm", value).replace(".0m", "m")
            }
            count >= 1_000 -> {
                val value = count / 1_000.0
                if (value >= 10.0) "${value.toInt()}k" else String.format("%.1fk", value).replace(".0k", "k")
            }
            else -> count.toString()
        }
    }

    // Filter videos based on category, search, and sort
    val displayedVideos = remember(selectedCategory, searchQuery, selectedSortBy, StudioRepository.contentVideos, StudioRepository.contentShorts, StudioRepository.contentLiveStreams) {
        val baseList = when (selectedCategory) {
            "Videos" -> StudioRepository.contentVideos
            "Shorts" -> StudioRepository.contentShorts
            "Live" -> StudioRepository.contentLiveStreams
            "All" -> StudioRepository.contentVideos + StudioRepository.contentShorts + StudioRepository.contentLiveStreams
            else -> StudioRepository.contentVideos
        }

        val filtered = if (searchQuery.isNotBlank()) {
            baseList.filter { it.title.contains(searchQuery, ignoreCase = true) }
        } else {
            baseList
        }

        when (selectedSortBy) {
            "Most viewed" -> filtered.sortedByDescending { it.views }
            "Latest" -> filtered
            else -> filtered
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("content_screen")
    ) {
        // Top Action Bar (< Videos and Search)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSearchActive) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search videos...", fontSize = 14.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF2F4F7),
                        unfocusedContainerColor = Color(0xFFF2F4F7),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(20.dp),
                    trailingIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            searchQuery = ""
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Search", tint = Color(0xFF0F0F0F))
                        }
                    }
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showFilterSheet = true }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF0F0F0F),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = selectedCategory,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F0F0F)
                    )
                }

                IconButton(onClick = { isSearchActive = true }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF0F0F0F),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Horizontal Category Tabs (All, Videos, Shorts, Live, Posts, Playlists)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF0F0F0F) else Color(0xFFF2F4F7))
                        .border(1.dp, if (isSelected) Color.Transparent else Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                        .clickable { selectedCategory = category }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF0F0F0F)
                    )
                }
            }
        }

        // Horizontal Filter & Sort Bar (Filter Button with "1", Sort by, Visibility, Views)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filter icon pill with badge "1"
            Surface(
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showFilterSheet = true },
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0F0F0F),
                border = null
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filters",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "1",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Sort by: Most viewed chip
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F0F0F))
                    .clickable { showSortSheet = true }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sort by: $selectedSortBy ∨",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            // Visibility chip
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF2F4F7))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                    .clickable { showVisibilitySheet = true }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedVisibility != null) "Visibility: $selectedVisibility ∨" else "Visibility ∨",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0F0F0F)
                )
            }

            // Views chip
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF2F4F7))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                    .clickable { showFilterSheet = true }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Views ∨",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0F0F0F)
                )
            }

            // Monetization chip
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF2F4F7))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                    .clickable { showFilterSheet = true }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Monetization ∨",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0F0F0F)
                )
            }
        }

        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE5E7EB))

        // Content Video Rows List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(displayedVideos, key = { it.id }) { video ->
                val isIneligible = video.title.contains("Waqt Sabka", ignoreCase = true) || video.id == "c_vid_ias_2"
                val isShort = video.type == ContentType.SHORT

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onVideoClick(video) }
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Video Thumbnail
                    VideoThumbnailView(
                        title = video.title,
                        duration = video.duration,
                        isShort = isShort,
                        width = if (isShort) 68.dp else 126.dp,
                        height = if (isShort) 100.dp else 72.dp,
                        thumbnailUri = video.thumbnailUri
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Video Info Details
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                    ) {
                        // Title + 3-dots row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = video.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF0F0F0F),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 18.sp,
                                modifier = Modifier
                                    .weight(1f)
                                    .editableDoubleTap(
                                        currentValue = video.title,
                                        label = "Video Title",
                                        onSave = { LocalAdminStore.updateVideoTitle(video.id, it) },
                                        onSingleClick = { onVideoClick(video) }
                                    )
                            )

                            IconButton(
                                onClick = { menuVideo = video },
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(start = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = Color(0xFF606060),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Status Line: Globe icon • publishedTimeAgo
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.editableDoubleTap(
                                currentValue = video.publishedTimeAgo,
                                label = "Published Time (${video.title})",
                                onSave = { LocalAdminStore.updateVideoPublishedTimeAgo(video.id, it) }
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Public,
                                contentDescription = "Public",
                                tint = Color(0xFF606060),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "• ${video.publishedTimeAgo}",
                                fontSize = 12.sp,
                                color = Color(0xFF606060)
                            )
                        }

                        // Ineligible to earn indicator
                        if (isIneligible) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Cancel,
                                    contentDescription = "Ineligible",
                                    tint = StudioRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Ineligible to earn",
                                    fontSize = 12.sp,
                                    color = Color(0xFF606060)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Metrics Row: Views, Likes, Comments
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.editableDoubleTap(
                                    currentValue = video.views.toString(),
                                    label = "Views (${video.title})",
                                    onSave = {
                                        it.toLongOrNull()?.let { v ->
                                            LocalAdminStore.updateVideoViews(video.id, v)
                                        }
                                    }
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.BarChart,
                                    contentDescription = "Views",
                                    tint = Color(0xFF606060),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = formatMetricCount(video.views),
                                    fontSize = 12.sp,
                                    color = Color(0xFF606060)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.editableDoubleTap(
                                    currentValue = video.likes.toString(),
                                    label = "Likes (${video.title})",
                                    onSave = {
                                        it.toLongOrNull()?.let { l ->
                                            LocalAdminStore.updateVideoLikes(video.id, l)
                                        }
                                    }
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ThumbUp,
                                    contentDescription = "Likes",
                                    tint = Color(0xFF606060),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = formatMetricCount(video.likes),
                                    fontSize = 12.sp,
                                    color = Color(0xFF606060)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.editableDoubleTap(
                                    currentValue = video.comments.toString(),
                                    label = "Comments (${video.title})",
                                    onSave = {
                                        it.toLongOrNull()?.let { c ->
                                            LocalAdminStore.updateVideoComments(video.id, c)
                                        }
                                    }
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ChatBubbleOutline,
                                    contentDescription = "Comments",
                                    tint = Color(0xFF606060),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = formatMetricCount(video.comments),
                                    fontSize = 12.sp,
                                    color = Color(0xFF606060)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Sort By Modal Bottom Sheet
    if (showSortSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSortSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Sort by",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F0F0F)
                )

                listOf("Most viewed", "Latest", "Date added (newest)", "Date added (oldest)").forEach { sortOption ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedSortBy = sortOption
                                showSortSheet = false
                            }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sortOption,
                            fontSize = 15.sp,
                            fontWeight = if (selectedSortBy == sortOption) FontWeight.Bold else FontWeight.Normal,
                            color = Color(0xFF0F0F0F)
                        )
                        RadioButton(
                            selected = selectedSortBy == sortOption,
                            onClick = {
                                selectedSortBy = sortOption
                                showSortSheet = false
                            }
                        )
                    }
                }
            }
        }
    }

    // Visibility Modal Bottom Sheet
    if (showVisibilitySheet) {
        ModalBottomSheet(
            onDismissRequest = { showVisibilitySheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Visibility",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F0F0F)
                )

                listOf("All", "Public", "Unlisted", "Private", "Draft").forEach { option ->
                    val isSelected = selectedVisibility == option || (selectedVisibility == null && option == "All")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedVisibility = if (option == "All") null else option
                                showVisibilitySheet = false
                            }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = Color(0xFF0F0F0F)
                        )
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                selectedVisibility = if (option == "All") null else option
                                showVisibilitySheet = false
                            }
                        )
                    }
                }
            }
        }
    }

    // Filter Dialog
    if (showFilterSheet) {
        AlertDialog(
            onDismissRequest = { showFilterSheet = false },
            containerColor = Color.White,
            title = { Text(text = "Filters", fontWeight = FontWeight.Bold, color = Color(0xFF0F0F0F)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("• Sort: $selectedSortBy", color = Color(0xFF0F0F0F))
                    Text("• Visibility: ${selectedVisibility ?: "All"}", color = Color(0xFF0F0F0F))
                    Text("• Views: Any", color = Color(0xFF0F0F0F))
                    Text("• Restrictions: None", color = Color(0xFF0F0F0F))
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilterSheet = false }) {
                    Text("Apply", color = Color(0xFF0F0F0F), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 3-dots Video Action Sheet
    menuVideo?.let { video ->
        ModalBottomSheet(
            onDismissRequest = { menuVideo = null },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = video.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F0F0F),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE5E7EB))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            menuVideo = null
                            onVideoClick(video)
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF0F0F0F))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Play video", fontSize = 15.sp, color = Color(0xFF0F0F0F))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { menuVideo = null }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF0F0F0F))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Share video", fontSize = 15.sp, color = Color(0xFF0F0F0F))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { menuVideo = null }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF0F0F0F))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Edit video", fontSize = 15.sp, color = Color(0xFF0F0F0F))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { menuVideo = null }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color(0xFF0F0F0F))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Save to device", fontSize = 15.sp, color = Color(0xFF0F0F0F))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { menuVideo = null }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = StudioRed)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Delete", fontSize = 15.sp, color = StudioRed)
                }
            }
        }
    }
}

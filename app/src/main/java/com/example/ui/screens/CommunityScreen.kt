package com.example.ui.screens

import com.example.data.StudioRepository

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ThumbDown
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
import com.example.ui.components.VideoThumbnailView
import com.example.ui.theme.*

data class UserComment(
    val id: String,
    val authorName: String,
    val authorHandle: String,
    val avatarLetter: String,
    val avatarColor: Color,
    val timeAgo: String,
    val content: String,
    val videoTitle: String,
    val initialLikes: Int = 0,
    val initialLiked: Boolean = false,
    val initialHearted: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen() {
    val colors = StudioTheme.colors

    var isAddHeartsDismissed by remember { mutableStateOf(false) }

    val commentsList = remember {
        mutableStateListOf(
            UserComment(
                id = "c_1",
                authorName = "Abdul Wasiq Syed",
                authorHandle = "@abdulwasiqsyed",
                avatarLetter = "A",
                avatarColor = Color(0xFF4E342E),
                timeAgo = "8 hours ago",
                content = "Who is the new actress full name please please please please please please please please please please",
                videoTitle = "Karwa Chauth Par Biwi Ki Demand | Husband Wife Funny Reel | Rahul R...",
                initialLikes = 0,
                initialLiked = false,
                initialHearted = false
            ),
            UserComment(
                id = "c_2",
                authorName = "Shreeji",
                authorHandle = "@Shreeji00024",
                avatarLetter = "🕉️",
                avatarColor = Color(0xFFD84315),
                timeAgo = "3 days ago",
                content = "😂😂😂😂😂😂",
                videoTitle = "Jab Meri Biwi Gussa ho Jaye #rahulrana #wife #husbandwife #h...",
                initialLikes = 2,
                initialLiked = true,
                initialHearted = false
            )
        )
    }

    var selectedCommentForOptions by remember { mutableStateOf<UserComment?>(null) }
    var replyingToComment by remember { mutableStateOf<UserComment?>(null) }
    var replyText by remember { mutableStateOf("") }
    var showContentControlsDialog by remember { mutableStateOf(false) }
    var showCommentSettingsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
            .testTag("community_screen"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Summary Cards: "Comments 28D" (4) & "Monthly audience 28D" (39.6k)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Comments 28D card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Comments 28D",
                            fontSize = 13.sp,
                            color = StudioSecondaryText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "4",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioBlack
                        )
                    }
                }

                // Monthly audience 28D card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Monthly audience 28D",
                            fontSize = 13.sp,
                            color = StudioSecondaryText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "39.6k",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioBlack
                        )
                    }
                }
            }
        }

        // Comments Section Header with "View all" button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comments",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioBlack
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(20.dp))
                        .clickable { }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "View all",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = StudioBlack
                    )
                }
            }
        }

        // "Add hearts ❤️" Banner Card
        if (!isAddHeartsDismissed) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Add hearts ❤️",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioBlack
                            )

                            IconButton(
                                onClick = { isAddHeartsDismissed = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = StudioBlack,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Overlapping avatars "T" and "P"
                            Box(modifier = Modifier.width(46.dp).height(30.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFBF360C)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "T", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .offset(x = 18.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, Color.White, CircleShape)
                                        .background(Color(0xFF00796B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "P", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "@tasvinbundhun1725 and 9 more sent their love and support to you.",
                                fontSize = 13.5.sp,
                                color = StudioBlack,
                                lineHeight = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // "View comments" filled button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clip(RoundedCornerShape(19.dp))
                                .background(Color(0xFFF2F2F2))
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "View comments",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = StudioBlack
                            )
                        }
                    }
                }
            }
        }

        // Comments List Container Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    commentsList.forEachIndexed { index, comment ->
                        var liked by remember { mutableStateOf(comment.initialLiked) }
                        var disliked by remember { mutableStateOf(false) }
                        var hearted by remember { mutableStateOf(comment.initialHearted) }
                        var likesCount by remember { mutableStateOf(comment.initialLikes) }
                        var translated by remember { mutableStateOf(false) }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Video title & Thumbnail row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = comment.videoTitle,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = StudioBlack,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 19.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                VideoThumbnailView(
                                    title = comment.videoTitle,
                                    isShort = true,
                                    width = 50.dp,
                                    height = 34.dp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Author info & comment content
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(comment.avatarColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = comment.avatarLetter,
                                        color = Color.White,
                                        fontSize = if (comment.avatarLetter.length > 1) 18.sp else 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${comment.authorHandle} · ${comment.timeAgo}",
                                            fontSize = 12.5.sp,
                                            color = StudioSecondaryText
                                        )

                                        IconButton(
                                            onClick = { selectedCommentForOptions = comment },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Options",
                                                tint = StudioSecondaryText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (translated) "${comment.content} (Translated)" else comment.content,
                                        fontSize = if (comment.content.contains("😂")) 16.sp else 14.sp,
                                        color = StudioBlack,
                                        lineHeight = 19.sp
                                    )

                                    if (comment.id == "c_2") {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (translated) "Show original" else "Translate to English",
                                            fontSize = 12.sp,
                                            color = StudioSecondaryText,
                                            modifier = Modifier.clickable { translated = !translated }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Action icons row (ThumbUp, ThumbDown, Reply, Heart)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(22.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Thumbs up
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable {
                                                if (liked) {
                                                    liked = false
                                                    likesCount = (likesCount - 1).coerceAtLeast(0)
                                                } else {
                                                    liked = true
                                                    disliked = false
                                                    likesCount++
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (liked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                                contentDescription = "Like",
                                                tint = if (liked) StudioBlack else StudioSecondaryText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            if (likesCount > 0) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "$likesCount",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (liked) StudioBlack else StudioSecondaryText
                                                )
                                            }
                                        }

                                        // Thumbs down
                                        IconButton(
                                            onClick = {
                                                disliked = !disliked
                                                if (disliked && liked) {
                                                    liked = false
                                                    likesCount = (likesCount - 1).coerceAtLeast(0)
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.ThumbDown,
                                                contentDescription = "Dislike",
                                                tint = if (disliked) StudioBlack else StudioSecondaryText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Comment reply
                                        IconButton(
                                            onClick = { replyingToComment = comment },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Outlined.Comment,
                                                contentDescription = "Reply",
                                                tint = StudioSecondaryText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Heart
                                        IconButton(
                                            onClick = { hearted = !hearted },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (hearted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Heart",
                                                tint = if (hearted) StudioRed else StudioSecondaryText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (index < commentsList.size - 1) {
                            HorizontalDivider(
                                thickness = 0.8.dp,
                                color = Color(0xFFF0F0F0),
                                modifier = Modifier.padding(vertical = 14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Viewer posts Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Viewer posts",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioBlack
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(20.dp))
                        .clickable { }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "View all",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = StudioBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF2F2F2))
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Your community is off",
                        fontSize = 14.sp,
                        color = StudioSecondaryText
                    )
                }
            }
        }

        // Community spotlight Section
        item {
            Text(
                text = "Community spotlight",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = StudioBlack
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Weekly highlight of your most engaged audience members, including your top commenters",
                fontSize = 13.sp,
                color = StudioSecondaryText,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .width(145.dp)
                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile avatar with circular photo/portrait
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                androidx.compose.ui.graphics.Brush.radialGradient(
                                    listOf(Color(0xFF455A64), Color(0xFF263238))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👨🏻‍💼", fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Siddharth Ba...",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioBlack,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(13.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(StudioRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "▶", fontSize = 7.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "@siddharthbajpai9...",
                        fontSize = 11.sp,
                        color = StudioSecondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Comment,
                            contentDescription = "Comments",
                            tint = StudioSecondaryText,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "1",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioBlack
                        )
                    }
                }
            }
        }

        // Moderation Section
        item {
            Text(
                text = "Moderation",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = StudioBlack
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showContentControlsDialog = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = StudioBlack,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Content controls",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioBlack
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCommentSettingsDialog = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = StudioBlack,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Learn about comment settings",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioBlack
                        )
                    }
                }
            }
        }
    }

    // Comment Options Bottom Sheet
    selectedCommentForOptions?.let { comment ->
        ModalBottomSheet(
            onDismissRequest = { selectedCommentForOptions = null },
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
                    text = "Comment options",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioBlack
                )

                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE5E7EB))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCommentForOptions = null }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pin this comment", fontSize = 15.sp, color = StudioBlack)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            commentsList.removeAll { it.id == comment.id }
                            selectedCommentForOptions = null
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Remove comment", fontSize = 15.sp, color = StudioRed)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCommentForOptions = null }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Report spam or abuse", fontSize = 15.sp, color = StudioBlack)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCommentForOptions = null }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hide user from channel", fontSize = 15.sp, color = StudioBlack)
                }
            }
        }
    }

    // Reply Dialog
    replyingToComment?.let { target ->
        AlertDialog(
            onDismissRequest = { replyingToComment = null },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Reply to ${target.authorHandle}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioBlack
                )
            },
            text = {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    placeholder = { Text("Add a public reply...", fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        replyingToComment = null
                        replyText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioBlack)
                ) {
                    Text("Reply", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { replyingToComment = null }) {
                    Text("Cancel", color = StudioSecondaryText)
                }
            }
        )
    }

    // Content Controls Dialog
    if (showContentControlsDialog) {
        AlertDialog(
            onDismissRequest = { showContentControlsDialog = false },
            containerColor = Color.White,
            title = { Text("Content controls", fontWeight = FontWeight.Bold, color = StudioBlack) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Hold potentially inappropriate comments for review: Enabled", color = StudioBlack)
                    Text("• Increase strictness filter: Standard", color = StudioBlack)
                    Text("• Block links in comments: Enabled", color = StudioBlack)
                }
            },
            confirmButton = {
                TextButton(onClick = { showContentControlsDialog = false }) {
                    Text("Done", color = StudioBlack, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Comment Settings Dialog
    if (showCommentSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showCommentSettingsDialog = false },
            containerColor = Color.White,
            title = { Text("Comment Settings", fontWeight = FontWeight.Bold, color = StudioBlack) },
            text = {
                Text(
                    "You can choose how comments appear on your channel. Learn more about channel moderation tools and automated filters.",
                    color = StudioSecondaryText,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showCommentSettingsDialog = false }) {
                    Text("Got it", color = StudioBlack, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

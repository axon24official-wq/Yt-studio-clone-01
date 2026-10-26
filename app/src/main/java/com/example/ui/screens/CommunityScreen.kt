package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.ChatBubbleOutline
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
import com.example.data.StudioRepository
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

    val commentsList = remember {
        mutableStateListOf(
            UserComment(
                id = "c_1",
                authorName = "Siddharth Bajpai",
                authorHandle = "@siddharthbajpai9...",
                avatarLetter = "S",
                avatarColor = Color(0xFF1976D2),
                timeAgo = "1 month ago",
                content = "Ye kosa gana hai bhai ??☠️🧐",
                videoTitle = "80 HAZAR KE MERE SHOES 👟 MEKO MUT TU KUDH KO PUCH#mcstan #f...",
                initialLikes = 0,
                initialLiked = false,
                initialHearted = false
            ),
            UserComment(
                id = "c_2",
                authorName = "Priya Sharma",
                authorHandle = "@priyasharma_ias",
                avatarLetter = "P",
                avatarColor = Color(0xFFC2185B),
                timeAgo = "2 weeks ago",
                content = "Very motivational video sir! Really touched my heart ❤️🙏",
                videoTitle = "गरीब लड़की बनी IAS OFFICER | Waqt Sabka Badalta Hai",
                initialLikes = 12,
                initialLiked = true,
                initialHearted = true
            ),
            UserComment(
                id = "c_3",
                authorName = "Rahul Kumar",
                authorHandle = "@rahulkumar_vlogs",
                avatarLetter = "R",
                avatarColor = Color(0xFF388E3C),
                timeAgo = "3 days ago",
                content = "Awesome content brother, keep making more videos like this!",
                videoTitle = "Biwi Ho Toh Aisi | HUSBAND HARRASING LOVING WIFE",
                initialLikes = 4,
                initialLiked = false,
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
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Summary Cards (Comments 28D, Monthly audience 28D - Frame 033)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Comments",
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Last 28 days",
                            fontSize = 11.sp,
                            color = colors.textTertiary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "1,420",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Monthly audience",
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Last 28 days",
                            fontSize = 11.sp,
                            color = colors.textTertiary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "90.7k",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }

        // Comments Section Header (Frame 034)
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
                    color = colors.textPrimary
                )
                Text(
                    text = "View all",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
                    modifier = Modifier.clickable { }
                )
            }
        }

        // Comments List Items (Frame 034)
        items(commentsList.size, key = { commentsList[it].id }) { index ->
            val comment = commentsList[index]
            var liked by remember { mutableStateOf(comment.initialLiked) }
            var disliked by remember { mutableStateOf(false) }
            var hearted by remember { mutableStateOf(comment.initialHearted) }
            var likesCount by remember { mutableStateOf(comment.initialLikes) }
            var translated by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Video reference bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = comment.videoTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        VideoThumbnailView(
                            title = comment.videoTitle,
                            width = 46.dp,
                            height = 28.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFD8DCE3))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Author info & comment text
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
                                fontSize = 16.sp,
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
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                                IconButton(
                                    onClick = { selectedCommentForOptions = comment },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Options",
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (translated) "${comment.content} (Translated: Beautiful song / video brother!)" else comment.content,
                                fontSize = 14.sp,
                                color = colors.textPrimary,
                                lineHeight = 19.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (translated) "Show original" else "Translate to English",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF065FD4),
                                modifier = Modifier.clickable { translated = !translated }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons (Thumbs up, Thumbs down, Reply, Creator Heart)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Like
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
                                imageVector = Icons.Outlined.ThumbUp,
                                contentDescription = "Like",
                                tint = if (liked) Color(0xFF065FD4) else colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            if (likesCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$likesCount",
                                    fontSize = 12.sp,
                                    color = if (liked) Color(0xFF065FD4) else colors.textSecondary
                                )
                            }
                        }

                        // Dislike
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
                                tint = if (disliked) Color(0xFF065FD4) else colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Reply
                        IconButton(
                            onClick = { replyingToComment = comment },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Reply",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Creator Heart (Frame 034)
                        Box(
                            modifier = Modifier
                                .clickable { hearted = !hearted }
                                .padding(2.dp)
                        ) {
                            Icon(
                                imageVector = if (hearted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Creator Heart",
                                tint = if (hearted) StudioRed else colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            if (hearted) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(StudioRed)
                                        .border(1.dp, Color.White, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Viewer posts section (Frame 034)
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
                    color = colors.textPrimary
                )
                Text(
                    text = "View all",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
                    modifier = Modifier.clickable { }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF2F4F7)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Your community is off",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF606060)
                    )
                }
            }
        }

        // Community spotlight section (Frame 034)
        item {
            Text(
                text = "Community spotlight",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Weekly highlight of your most engaged audience members, including your top commenters",
                fontSize = 13.sp,
                color = colors.textSecondary,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .width(180.dp)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile avatar with circular photo/letter
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF37474F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👨🏻", fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Siddharth Ba...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(StudioRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "▶", fontSize = 8.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "@siddharthbajpai9...",
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comments",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "1",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }

        // Moderation section (Frame 034)
        item {
            Text(
                text = "Moderation",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(14.dp)
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
                            tint = colors.textPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Content controls",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE5E7EB))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCommentSettingsDialog = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = colors.textPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Learn about comment settings",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }
    }

    // Comment Options Bottom Sheet (Frame 034)
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
                    color = Color(0xFF0F0F0F)
                )

                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE5E7EB))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCommentForOptions = null }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pin this comment", fontSize = 15.sp, color = Color(0xFF0F0F0F))
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
                    Text("Report spam or abuse", fontSize = 15.sp, color = Color(0xFF0F0F0F))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCommentForOptions = null }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hide user from channel", fontSize = 15.sp, color = Color(0xFF0F0F0F))
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
                    color = Color(0xFF0F0F0F)
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F0F0F))
                ) {
                    Text("Reply", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { replyingToComment = null }) {
                    Text("Cancel", color = Color(0xFF606060))
                }
            }
        )
    }

    // Content Controls Dialog
    if (showContentControlsDialog) {
        AlertDialog(
            onDismissRequest = { showContentControlsDialog = false },
            containerColor = Color.White,
            title = { Text("Content controls", fontWeight = FontWeight.Bold, color = Color(0xFF0F0F0F)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Hold potentially inappropriate comments for review: Enabled", color = Color(0xFF0F0F0F))
                    Text("• Increase strictness filter: Standard", color = Color(0xFF0F0F0F))
                    Text("• Block links in comments: Enabled", color = Color(0xFF0F0F0F))
                }
            },
            confirmButton = {
                TextButton(onClick = { showContentControlsDialog = false }) {
                    Text("Done", color = Color(0xFF0F0F0F), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Comment Settings Dialog
    if (showCommentSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showCommentSettingsDialog = false },
            containerColor = Color.White,
            title = { Text("Comment Settings", fontWeight = FontWeight.Bold, color = Color(0xFF0F0F0F)) },
            text = {
                Text(
                    "You can choose how comments appear on your channel. Learn more about channel moderation tools and automated filters.",
                    color = Color(0xFF606060),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showCommentSettingsDialog = false }) {
                    Text("Got it", color = Color(0xFF0F0F0F), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.VideoLibrary
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
import androidx.compose.ui.window.Dialog
import com.example.data.local.LocalAdminStore
import com.example.model.ContentType
import com.example.model.VideoItem
import com.example.ui.theme.StudioRed

@Composable
fun AddVideoDialog(
    onDismiss: () -> Unit,
    onVideoAdded: (VideoItem) -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ContentType.VIDEO) }
    var duration by remember { mutableStateOf("10:00") }
    var thumbnailUri by remember { mutableStateOf("") }
    var viewsText by remember { mutableStateOf("1500") }
    var likesText by remember { mutableStateOf("120") }
    var showOnDashboard by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_video_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with YouTube badge and title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(StudioRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Add Video to Channel",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F0F0F)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF606060),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = "Saved instantly to local storage and populates Dashboard & Content tabs without server sync.",
                    fontSize = 12.sp,
                    color = Color(0xFF606060),
                    lineHeight = 16.sp
                )

                // Video Type Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        ContentType.VIDEO to "Video",
                        ContentType.SHORT to "Short",
                        ContentType.LIVE to "Live"
                    ).forEach { (type, label) ->
                        val isSelected = selectedType == type
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedType = type
                                    if (type == ContentType.SHORT && duration == "10:00") {
                                        duration = "0:30"
                                    } else if (type == ContentType.VIDEO && duration == "0:30") {
                                        duration = "10:00"
                                    }
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF0F0F0F) else Color(0xFFF2F4F7),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF0F0F0F) else Color(0xFFE5E7EB))
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF0F0F0F)
                                )
                            }
                        }
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Video Title *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_video_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0F0F0F),
                        unfocusedBorderColor = Color(0xFFCCCCCC),
                        focusedLabelColor = Color(0xFF0F0F0F),
                        cursorColor = StudioRed
                    )
                )

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_video_desc_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0F0F0F),
                        unfocusedBorderColor = Color(0xFFCCCCCC),
                        focusedLabelColor = Color(0xFF0F0F0F),
                        cursorColor = StudioRed
                    )
                )

                // Duration & Views Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duration") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0F0F0F),
                            unfocusedBorderColor = Color(0xFFCCCCCC),
                            focusedLabelColor = Color(0xFF0F0F0F),
                            cursorColor = StudioRed
                        )
                    )

                    OutlinedTextField(
                        value = viewsText,
                        onValueChange = { viewsText = it },
                        label = { Text("Views") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0F0F0F),
                            unfocusedBorderColor = Color(0xFFCCCCCC),
                            focusedLabelColor = Color(0xFF0F0F0F),
                            cursorColor = StudioRed
                        )
                    )
                }

                // Thumbnail URL or Local URI
                OutlinedTextField(
                    value = thumbnailUri,
                    onValueChange = { thumbnailUri = it },
                    label = { Text("Thumbnail Image URI / URL") },
                    singleLine = true,
                    placeholder = { Text("https://... or file URI") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0F0F0F),
                        unfocusedBorderColor = Color(0xFFCCCCCC),
                        focusedLabelColor = Color(0xFF0F0F0F),
                        cursorColor = StudioRed
                    )
                )

                // Show on Dashboard Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFAFAFA))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Pin to Dashboard",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F0F0F)
                        )
                        Text(
                            text = "Display in Latest published video card",
                            fontSize = 11.sp,
                            color = Color(0xFF606060)
                        )
                    }

                    Switch(
                        checked = showOnDashboard,
                        onCheckedChange = { showOnDashboard = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = StudioRed
                        )
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF606060))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val cleanTitle = title.trim().ifEmpty { "New Video" }
                            val cleanDuration = duration.trim().ifEmpty {
                                if (selectedType == ContentType.SHORT) "0:30" else "10:00"
                            }
                            val cleanViews = viewsText.replace(",", "").toLongOrNull() ?: 1500L
                            val cleanLikes = likesText.replace(",", "").toLongOrNull() ?: 120L

                            val newVideo = VideoItem(
                                id = "local_vid_${System.currentTimeMillis()}",
                                title = cleanTitle,
                                type = selectedType,
                                duration = cleanDuration,
                                publishedTimeAgo = "Just now",
                                views = cleanViews,
                                likes = cleanLikes,
                                comments = 0,
                                rankingText = "1 of 10",
                                avgPercentageViewed = "72.4%",
                                thumbnailUri = thumbnailUri.trim().ifEmpty { null },
                                thumbnailTitle = cleanTitle.take(18)
                            )

                            LocalAdminStore.addOrUpdateVideo(newVideo, showOnDashboard = showOnDashboard)
                            onVideoAdded(newVideo)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_video_confirm_button")
                    ) {
                        Text("Add Video", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

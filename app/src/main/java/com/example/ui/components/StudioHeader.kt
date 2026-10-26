package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*

@Composable
fun StudioHeader(
    onAvatarClick: () -> Unit,
    onCreateClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onAdminClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
            .testTag("studio_header")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // YouTube Studio Logo and Wordmark
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("studio_brand_logo").clickable { onAdminClick() }
            ) {
                // YouTube Red Play Button Badge
                Box(
                    modifier = Modifier
                        .size(width = 30.dp, height = 21.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(StudioRed),
                    contentAlignment = Alignment.Center
                ) {
                    // White Play Triangle
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(9.dp)) {
                        val path = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width, size.height / 2f)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(path, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Studio",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioBlack,
                    letterSpacing = (-0.5).sp
                )
            }

            // Right side action icons: (+) Bell Avatar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Create (+) Button in circle
                IconButton(
                    onClick = onCreateClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("create_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .border(1.2.dp, StudioBlack, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create",
                            tint = StudioBlack,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Notification Bell with red indicator badge
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("notifications_button")
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = StudioBlack,
                            modifier = Modifier.size(24.dp)
                        )
                        // Notification indicator dot (red dot on top-right)
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(StudioNotificationBadgeRed)
                        )
                    }
                }

                // Channel Avatar (Circular avatar on the right)
                val avatarUri = com.example.data.StudioRepository.danielProfile.avatarUri
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF1E88E5), Color(0xFF0D47A1))
                            )
                        )
                        .clickable(onClick = onAvatarClick)
                        .testTag("channel_avatar_header"),
                    contentAlignment = Alignment.Center
                ) {
                    if (!avatarUri.isNullOrBlank()) {
                        AsyncImage(
                            model = avatarUri,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Thin divider below the app bar
        HorizontalDivider(thickness = 0.8.dp, color = StudioDivider)
    }
}

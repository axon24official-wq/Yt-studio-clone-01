package com.example.ui.screens

import com.example.data.StudioRepository

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Copyright
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun EarnScreen(
    onOpenAdminPanel: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
            .testTag("earn_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header: Large bold title "Earn"
        item {
            Text(
                text = "Earn",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = StudioBlack
            )
        }

        // Top Card (Payment Status)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("earn_payment_status_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = StudioRepository.payoutHeadline,
                            fontSize = 13.5.sp,
                            color = StudioDarkText,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Payment info",
                            tint = StudioSecondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = StudioRepository.payoutAmount,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioBlack,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "minus taxes",
                            fontSize = 13.sp,
                            color = StudioSecondaryText,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
            }
        }

        // Second Card (Monthly Earnings)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("earn_monthly_earnings_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = StudioRepository.payoutMonthlyMonth,
                            fontSize = 13.5.sp,
                            color = StudioSecondaryText
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Monthly earnings info",
                            tint = StudioSecondaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = StudioRepository.payoutMonthlyAmount,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioBlack
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress / threshold bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFD6DBE1))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .background(Color(0xFF007A87))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = StudioRepository.payoutMinimumText,
                        fontSize = 12.sp,
                        color = StudioSecondaryText
                    )
                }
            }
        }

        // Section Header: "Ways that you're earning"
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ways that you're earning",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = StudioBlack
            )
        }

        // 1. Watch page ads (orange vertical bars)
        item {
            EarnListItem(
                title = "Watch page ads",
                icon = { WatchPageAdsIcon() }
            )
        }

        // 2. Shorts Feed ads (yellow lightning bolt)
        item {
            EarnListItem(
                title = "Shorts Feed ads",
                icon = { ShortsFeedAdsIcon() }
            )
        }

        // 3. Memberships (green star)
        item {
            EarnListItem(
                title = "Memberships",
                icon = { MembershipsStarIcon() }
            )
        }

        // 4. Gifts (green gift box)
        item {
            EarnListItem(
                title = "Gifts",
                icon = { GiftsIcon() }
            )
        }

        // 5. Supers (purple heart/key)
        item {
            EarnListItem(
                title = "Supers",
                icon = { SupersIcon() }
            )
        }

        // 6. Creator Partnerships (blue hands)
        item {
            EarnListItem(
                title = "Creator Partnerships",
                icon = { CreatorPartnershipsIcon() }
            )
        }

        // 7. Shopping (blue shopping bag)
        item {
            EarnListItem(
                title = "Shopping",
                icon = { ShoppingIcon() }
            )
        }

        // Button: "More ways to earn" pill-shaped button
        item {
            Spacer(modifier = Modifier.height(2.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(21.dp))
                    .clickable { },
                color = Color(0xFFF0F2F5),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE1E5EA)),
                shape = RoundedCornerShape(21.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "More ways to earn",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = StudioBlack
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Section Header: "Creator resources and tools"
        item {
            Text(
                text = "Creator resources and tools",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = StudioBlack
            )
        }

        // List of Creator resources and tools
        item {
            CreatorResourceRow(title = "Your payment activity", icon = Icons.Default.Paid)
        }
        item {
            CreatorResourceRow(title = "YouTube Creator Support", icon = Icons.Default.ChatBubbleOutline)
        }
        item {
            CreatorResourceRow(title = "Copyright Match Tool", icon = Icons.Default.Copyright)
        }
        item {
            CreatorResourceRow(title = "Video end screens", icon = Icons.Default.SmartDisplay)
        }

        // Announcement & Shopping Section (from screenshot IMG_3086)
        item {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.8.dp, color = StudioDivider)
            Spacer(modifier = Modifier.height(12.dp))

            // Flipkart Promo card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "₹80L Prize Pool up for grabs this BBD",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioBlack
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tag Flipkart products in 5+ Shorts & videos to win international trips & Ray-Ban glasses",
                            fontSize = 12.5.sp,
                            color = StudioSecondaryText,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "2 minutes ago",
                            fontSize = 11.5.sp,
                            color = StudioTertiaryText
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "🪙", fontSize = 34.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Shopping block
            Column {
                Text(
                    text = "Shopping",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioBlack
                )
                Text(
                    text = "Past 28 days",
                    fontSize = 12.sp,
                    color = StudioSecondaryText
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Affiliate revenue", fontSize = 12.sp, color = StudioSecondaryText)
                        Text(text = "$0", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StudioBlack)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Affiliate orders", fontSize = 12.sp, color = StudioSecondaryText)
                        Text(text = "0", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StudioBlack)
                    }
                }
            }
        }

        // Section: Restored small Admin Panel entry at the bottom of the last page
        item {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 0.8.dp, color = StudioDivider)
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenAdminPanel() }
                    .testTag("open_admin_panel_button"),
                color = StudioCardBackground,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8EAF6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Panel",
                            tint = Color(0xFF1A237E),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Admin Panel",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StudioBlack
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Local device customizations and demo controls",
                            fontSize = 12.sp,
                            color = StudioSecondaryText
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open Admin Panel",
                        tint = StudioSecondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun EarnListItem(
    title: String,
    icon: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(26.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = StudioBlack,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF9E9E9E),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun CreatorResourceRow(
    title: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = StudioSecondaryText,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            color = StudioBlack,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF9E9E9E),
            modifier = Modifier.size(20.dp)
        )
    }
}

// --- Official YouTube Studio Colored Icons ---

/**
 * Watch page ads: 3 vertical rounded bars in orange (as in reference screenshot)
 */
@Composable
fun WatchPageAdsIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val orange = Color(0xFFFF6D00)
        val barWidth = w * 0.22f
        val radius = CornerRadius(barWidth / 2f, barWidth / 2f)

        // Left bar
        drawRoundRect(
            color = orange,
            topLeft = Offset(w * 0.08f, h * 0.22f),
            size = Size(barWidth, h * 0.56f),
            cornerRadius = radius
        )
        // Middle bar (taller)
        drawRoundRect(
            color = orange,
            topLeft = Offset(w * 0.39f, h * 0.12f),
            size = Size(barWidth, h * 0.76f),
            cornerRadius = radius
        )
        // Right bar
        drawRoundRect(
            color = orange,
            topLeft = Offset(w * 0.70f, h * 0.22f),
            size = Size(barWidth, h * 0.56f),
            cornerRadius = radius
        )
    }
}

/**
 * Shorts Feed ads: Chunky yellow lightning bolt
 */
@Composable
fun ShortsFeedAdsIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val yellow = Color(0xFFFFB300)
        val path = Path().apply {
            moveTo(w * 0.60f, h * 0.04f)
            lineTo(w * 0.16f, h * 0.52f)
            lineTo(w * 0.46f, h * 0.52f)
            lineTo(w * 0.38f, h * 0.96f)
            lineTo(w * 0.84f, h * 0.44f)
            lineTo(w * 0.54f, h * 0.44f)
            close()
        }
        drawPath(path, color = yellow)
    }
}

/**
 * Memberships: Bright green 5-pointed star
 */
@Composable
fun MembershipsStarIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Icon(
        imageVector = Icons.Default.Star,
        contentDescription = "Memberships",
        tint = Color(0xFF2BA640),
        modifier = modifier
    )
}

/**
 * Gifts: Teal/green gift box with ribbon
 */
@Composable
fun GiftsIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Icon(
        imageVector = Icons.Default.CardGiftcard,
        contentDescription = "Gifts",
        tint = Color(0xFF00897B),
        modifier = modifier
    )
}

/**
 * Supers: Purple heart/key icon (official YouTube Supers logo)
 */
@Composable
fun SupersIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val purple = Color(0xFF8E24AA)

        // Upper heart shape
        val heartPath = Path().apply {
            moveTo(w * 0.5f, h * 0.50f)
            cubicTo(w * 0.16f, h * 0.30f, w * 0.06f, h * 0.08f, w * 0.28f, h * 0.05f)
            cubicTo(w * 0.42f, h * 0.05f, w * 0.48f, h * 0.18f, w * 0.5f, h * 0.24f)
            cubicTo(w * 0.52f, h * 0.18f, w * 0.58f, h * 0.05f, w * 0.72f, h * 0.05f)
            cubicTo(w * 0.94f, h * 0.08f, w * 0.84f, h * 0.30f, w * 0.5f, h * 0.50f)
            close()
        }
        drawPath(heartPath, color = purple)

        // Key stem underneath
        val stemWidth = w * 0.18f
        drawRoundRect(
            color = purple,
            topLeft = Offset(w * 0.41f, h * 0.42f),
            size = Size(stemWidth, h * 0.52f),
            cornerRadius = CornerRadius(stemWidth / 3f, stemWidth / 3f)
        )
        // Key notch
        drawRoundRect(
            color = purple,
            topLeft = Offset(w * 0.55f, h * 0.64f),
            size = Size(w * 0.20f, h * 0.14f),
            cornerRadius = CornerRadius(4f, 4f)
        )
    }
}

/**
 * Creator Partnerships: Blue hands (handshake)
 */
@Composable
fun CreatorPartnershipsIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Icon(
        imageVector = Icons.Default.Handshake,
        contentDescription = "Creator Partnerships",
        tint = Color(0xFF1E88E5),
        modifier = modifier
    )
}

/**
 * Shopping: Blue shopping bag
 */
@Composable
fun ShoppingIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Icon(
        imageVector = Icons.Default.ShoppingBag,
        contentDescription = "Shopping",
        tint = Color(0xFF0288D1),
        modifier = modifier
    )
}

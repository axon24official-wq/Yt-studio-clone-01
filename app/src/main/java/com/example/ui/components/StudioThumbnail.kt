package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun VideoThumbnailView(
    title: String,
    duration: String = "",
    isShort: Boolean = false,
    width: Dp = 120.dp,
    height: Dp = if (isShort) 160.dp else 68.dp,
    thumbnailUri: String? = null,
    modifier: Modifier = Modifier
) {
    // Generate distinct background color/gradients based on title
    val (brush, textOverlay) = when {
        title.contains("IAS", ignoreCase = true) || title.contains("लड़की बनी", ignoreCase = true) -> {
            Brush.verticalGradient(listOf(Color(0xFFB71C1C), Color(0xFF1B5E20), Color(0xFF263238))) to "STRUGGLE OF\nIAS ASPIRANT"
        }
        title.contains("Biwi", ignoreCase = true) || title.contains("बीवी", ignoreCase = true) || title.contains("Husband", ignoreCase = true) -> {
            Brush.verticalGradient(listOf(Color(0xFF880E4F), Color(0xFF4A148C), Color(0xFF1A237E))) to "HUSBAND\nHARASSING"
        }
        title.contains("Motu Patlu", ignoreCase = true) -> {
            Brush.verticalGradient(listOf(Color(0xFF0277BD), Color(0xFF004D40), Color(0xFF263238))) to "MOTU\nPATLU"
        }
        title.contains("Jungle Book", ignoreCase = true) -> {
            Brush.verticalGradient(listOf(Color(0xFF2E7D32), Color(0xFFF57F17), Color(0xFF1B5E20))) to "JUNGLE\nBOOK"
        }
        title.contains("pov", ignoreCase = true) || title.contains("entrepreneur", ignoreCase = true) -> {
            Brush.verticalGradient(listOf(Color(0xFF5D4037), Color(0xFF1976D2), Color(0xFF263238))) to "POV"
        }
        title.contains("80 HAZAR", ignoreCase = true) -> {
            Brush.verticalGradient(listOf(Color(0xFF880E4F), Color(0xFF1A0010))) to "👟 MC STAN"
        }
        title.contains("सऊदी", ignoreCase = true) || title.contains("eid", ignoreCase = true) -> {
            Brush.verticalGradient(listOf(Color(0xFFB71C1C), Color(0xFF0D47A1))) to "चाँद हुआ या नहीं?\n2026"
        }
        title.contains("unfrezz", ignoreCase = true) || title.contains("Red", ignoreCase = true) -> {
            Brush.verticalGradient(listOf(Color(0xFFFF5252), Color(0xFF37474F))) to "👦🏻"
        }
        title.contains("26 March", ignoreCase = true) || title.contains("Striped", ignoreCase = true) -> {
            Brush.verticalGradient(listOf(Color(0xFF5D4037), Color(0xFF263238))) to "26 MAR"
        }
        title.contains("Pigeon", ignoreCase = true) || title.contains("Parrot", ignoreCase = true) -> {
            Brush.verticalGradient(listOf(Color(0xFF66BB6A), Color(0xFF2E7D32))) to "🕊️ 🦜"
        }
        title.contains("Track", ignoreCase = true) -> {
            Brush.verticalGradient(listOf(Color(0xFFFFF9C4), Color(0xFFF57F17))) to "🕊️ 🛤️"
        }
        title.contains("Rabbit", ignoreCase = true) -> {
            Brush.verticalGradient(listOf(Color(0xFF81D4FA), Color(0xFF0288D1))) to "🐇 ⛰️"
        }
        title.contains("Mohabbat", ignoreCase = true) -> {
            Brush.verticalGradient(listOf(Color(0xFF212121), Color(0xFF000000))) to "🖤"
        }
        title.contains("Abhi ke Abhi", ignoreCase = true) -> {
            Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF1565C0))) to "🎬"
        }
        title.contains("Tujhe Bhula", ignoreCase = true) -> {
            Brush.verticalGradient(listOf(Color(0xFFC2185B), Color(0xFF311B92))) to "BHULA DIYA"
        }
        title.contains("VTEN", ignoreCase = true) -> {
            Brush.verticalGradient(listOf(Color(0xFF00838F), Color(0xFF004D40))) to "VTEN"
        }
        title.contains("free fire", ignoreCase = true) -> {
            Brush.verticalGradient(listOf(Color(0xFFFF6F00), Color(0xFF212121))) to "FREE FIRE"
        }
        else -> {
            Brush.verticalGradient(listOf(Color(0xFF37474F), Color(0xFF102027))) to "HD"
        }
    }

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(8.dp))
            .background(brush)
            .border(0.5.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (!thumbnailUri.isNullOrBlank()) {
            AsyncImage(
                model = thumbnailUri,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Thumbnail central badge / graphic text
            Text(
                text = textOverlay,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = if (isShort) 14.sp else 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // Duration badge on bottom right
        if (duration.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = duration,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

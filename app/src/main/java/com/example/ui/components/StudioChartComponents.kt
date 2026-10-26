package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun MetricIndicatorBadge(
    isPositive: Boolean,
    isCheckmark: Boolean = false,
    showArrow: Boolean = true,
    size: Dp = 18.dp
) {
    val bgColor = if (isPositive) StudioGreen else Color(0xFF757575)

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (isCheckmark) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Normal",
                tint = Color.White,
                modifier = Modifier.size(size * 0.7f)
            )
        } else if (showArrow) {
            Icon(
                imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = if (isPositive) "Up" else "Down",
                tint = Color.White,
                modifier = Modifier.size(size * 0.7f)
            )
        }
    }
}

@Composable
fun TrendLineChart(
    data: List<Float>,
    lineColor: Color = Color(0xFF00ACC1),
    yMinLabel: String = "0",
    yMidLabel: String = "",
    yMaxLabel: String = "",
    startDateLabel: String = "23 Sept",
    endDateLabel: String = "20 Oct",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            // Y-Axis labels
            Column(
                modifier = Modifier
                    .width(36.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(text = yMaxLabel, fontSize = 10.sp, color = StudioSecondaryText)
                if (yMidLabel.isNotEmpty()) {
                    Text(text = yMidLabel, fontSize = 10.sp, color = StudioSecondaryText)
                }
                Text(text = yMinLabel, fontSize = 10.sp, color = StudioSecondaryText)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Chart Canvas
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp)
            ) {
                val width = size.width
                val height = size.height

                // Draw 3 horizontal guidelines
                val gridColor = Color(0xFFEEEEEE)
                drawLine(gridColor, Offset(0f, 0f), Offset(width, 0f), strokeWidth = 1f)
                drawLine(gridColor, Offset(0f, height / 2f), Offset(width, height / 2f), strokeWidth = 1f)
                drawLine(gridColor, Offset(0f, height), Offset(width, height), strokeWidth = 1f)

                if (data.size < 2) return@Canvas

                val minVal = data.minOrNull() ?: 0f
                val maxVal = data.maxOrNull() ?: 1f
                val range = if (maxVal == minVal) 1f else (maxVal - minVal)

                val points = data.mapIndexed { index, value ->
                    val x = (index.toFloat() / (data.size - 1)) * width
                    val normalizedY = (value - minVal) / range
                    val y = height - (normalizedY * (height - 8f)) - 4f
                    Offset(x, y)
                }

                val path = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // X-Axis labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 44.dp, top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = startDateLabel, fontSize = 11.sp, color = StudioSecondaryText)
            Text(text = endDateLabel, fontSize = 11.sp, color = StudioSecondaryText)
        }
    }
}

@Composable
fun RealtimeBarChart(
    viewsCount: Int = 49,
    modifier: Modifier = Modifier
) {
    val barHeights = listOf(
        0.05f, 0.08f, 0.6f, 0.1f, 0.05f, 0.45f, 0.25f, 0.05f,
        0.1f, 0.15f, 0.1f, 0.2f, 0.15f, 0.25f, 0.7f, 0.15f,
        0.05f, 0.1f, 0.05f, 0.15f, 0.2f, 0.4f, 0.85f, 0.18f,
        0.05f, 0.05f, 0.15f, 0.1f, 0.05f, 0.05f, 0.18f, 0.05f
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        val count = barHeights.size
        val barWidth = 3.dp.toPx()
        val spacing = (size.width - (count * barWidth)) / (count - 1)
        val barColor = Color(0xFF00ACC1)

        barHeights.forEachIndexed { i, ratio ->
            val h = size.height * ratio
            val x = i * (barWidth + spacing)
            val y = size.height - h

            drawRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, h)
            )
        }
    }
}

@Composable
fun AudienceSegmentBar(
    newPct: Float = 97.6f,
    casualPct: Float = 2.4f,
    regularPct: Float = 0.1f,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
    ) {
        Box(
            modifier = Modifier
                .weight(newPct)
                .fillMaxHeight()
                .background(Color(0xFF311B92)) // Dark purple
        )
        Box(
            modifier = Modifier
                .weight(casualPct)
                .fillMaxHeight()
                .background(Color(0xFF7E57C2)) // Medium purple
        )
        Box(
            modifier = Modifier
                .weight(regularPct)
                .fillMaxHeight()
                .background(Color(0xFFBA68C8)) // Light violet
        )
    }
}

@Composable
fun StudioLinearProgress(
    percentage: Float,
    barColor: Color = Color(0xFF673AB7),
    modifier: Modifier = Modifier
) {
    val fillWeight = percentage.coerceIn(0.1f, 100f)
    val emptyWeight = (100f - fillWeight).coerceAtLeast(0.01f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFFF0F0F0))
    ) {
        Box(
            modifier = Modifier
                .weight(fillWeight)
                .fillMaxHeight()
                .background(barColor)
        )
        Box(
            modifier = Modifier
                .weight(emptyWeight)
                .fillMaxHeight()
        )
    }
}

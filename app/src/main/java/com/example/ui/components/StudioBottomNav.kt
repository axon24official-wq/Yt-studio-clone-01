package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class StudioTab(val label: String) {
    DASHBOARD("Dashboard"),
    CONTENT("Content"),
    ANALYTICS("Analytics"),
    COMMUNITY("Comments"),
    EARN("Earn")
}

@Composable
fun StudioBottomNav(
    selectedTab: StudioTab,
    onTabSelected: (StudioTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .testTag("studio_bottom_nav")
    ) {
        HorizontalDivider(thickness = 0.8.dp, color = StudioDivider)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudioTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                val icon: ImageVector = when (tab) {
                    StudioTab.DASHBOARD -> if (isSelected) Icons.Filled.GridView else Icons.Outlined.GridView
                    StudioTab.CONTENT -> if (isSelected) Icons.Filled.VideoLibrary else Icons.Outlined.VideoLibrary
                    StudioTab.ANALYTICS -> if (isSelected) Icons.Filled.BarChart else Icons.Outlined.BarChart
                    StudioTab.COMMUNITY -> if (isSelected) Icons.AutoMirrored.Filled.Comment else Icons.AutoMirrored.Outlined.Comment
                    StudioTab.EARN -> if (isSelected) Icons.Filled.MonetizationOn else Icons.Outlined.MonetizationOn
                }

                val itemColor = if (isSelected) StudioBlack else StudioSecondaryText

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTabSelected(tab) }
                        )
                        .testTag("tab_${tab.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab.label,
                        tint = itemColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = tab.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = itemColor
                    )
                }
            }
        }
    }
}

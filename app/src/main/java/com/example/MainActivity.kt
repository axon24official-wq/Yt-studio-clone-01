package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.local.LocalAdminStore
import com.example.model.MetricCardData
import com.example.model.VideoItem
import com.example.ui.components.AddVideoDialog
import com.example.ui.components.InlineEditDialogHost
import com.example.ui.components.InlineEditManager
import com.example.ui.components.LocalInlineEditManager
import com.example.ui.components.StudioBottomNav
import com.example.ui.components.StudioHeader
import com.example.ui.components.StudioTab
import com.example.ui.screens.*
import com.example.ui.screens.IndividualVideoAnalyticsScreen
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.example.data.SupabaseClient.init(this)
        com.example.data.StudioRepository.init(this)
        setContent {
            MyApplicationTheme {
                StudioApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioApp() {
    var currentTab by rememberSaveable { mutableStateOf(StudioTab.DASHBOARD) }
    var isAccountScreenOpen by rememberSaveable { mutableStateOf(false) }
    var showAddVideoDialog by rememberSaveable { mutableStateOf(false) }
    val inlineEditManager = remember { InlineEditManager() }
    var selectedVideoId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedVideoForDetail by remember { mutableStateOf<VideoItem?>(null) }
    var showCreateSheet by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var activeAnalyticsDrilldown by remember { mutableStateOf<MetricCardData?>(null) }

    // Automatic restoration of selected video across orientation or process recreation
    LaunchedEffect(selectedVideoId) {
        if (selectedVideoId != null && selectedVideoForDetail == null) {
            selectedVideoForDetail = com.example.data.StudioRepository.findVideoById(selectedVideoId)
                ?: com.example.data.StudioRepository.dashboardLatestVideo
        }
    }

    val colors = StudioTheme.colors

    // Sync live data from Supabase and listen for Realtime WebSocket updates
    LaunchedEffect(Unit) {
        com.example.data.StudioRepository.syncWithSupabase()
        com.example.data.StudioRepository.startRealtimeListener()
    }

    // Hardware back navigation handler
    BackHandler(enabled = isAccountScreenOpen || selectedVideoForDetail != null || activeAnalyticsDrilldown != null || currentTab != StudioTab.DASHBOARD) {
        when {
            selectedVideoForDetail != null -> {
                selectedVideoForDetail = null
                selectedVideoId = null
            }
            isAccountScreenOpen -> isAccountScreenOpen = false
            activeAnalyticsDrilldown != null -> activeAnalyticsDrilldown = null
            currentTab != StudioTab.DASHBOARD -> currentTab = StudioTab.DASHBOARD
        }
    }

    CompositionLocalProvider(LocalInlineEditManager provides inlineEditManager) {
        val currentDetailVideo = selectedVideoForDetail
        if (currentDetailVideo != null) {
            IndividualVideoAnalyticsScreen(
                video = currentDetailVideo,
                onBackClick = {
                    selectedVideoForDetail = null
                    selectedVideoId = null
                }
            )
        } else if (isAccountScreenOpen) {
            AccountScreen(
                onBackClick = { isAccountScreenOpen = false },
                onChannelSelected = { },
                onOpenAdminPanel = {
                    isAccountScreenOpen = false
                    showAddVideoDialog = true
                }
            )
        } else {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("studio_root_scaffold"),
                containerColor = colors.background,
                topBar = {
                    if (currentTab == StudioTab.ANALYTICS && activeAnalyticsDrilldown != null) {
                        // Top app bar with back arrow and "Views" title
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.background)
                                .statusBarsPadding()
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { activeAnalyticsDrilldown = null }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = colors.textPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = activeAnalyticsDrilldown!!.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }
                    } else {
                        StudioHeader(
                            onAvatarClick = { isAccountScreenOpen = true },
                            onCreateClick = { showCreateSheet = true },
                            onNotificationClick = { showNotificationsDialog = true },
                            onAdminClick = { showAddVideoDialog = true }
                        )
                    }
                },
                bottomBar = {
                    StudioBottomNav(
                        selectedTab = currentTab,
                        onTabSelected = {
                            currentTab = it
                            activeAnalyticsDrilldown = null
                            selectedVideoForDetail = null
                            selectedVideoId = null
                        }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentTab) {
                        StudioTab.DASHBOARD -> DashboardScreen(
                            onVideoClick = { video ->
                                selectedVideoId = video.id
                                selectedVideoForDetail = video
                            },
                            onAnalyticsClick = {
                                currentTab = StudioTab.ANALYTICS
                                activeAnalyticsDrilldown = null
                            }
                        )
                        StudioTab.CONTENT -> ContentScreen(
                            onVideoClick = { video ->
                                selectedVideoId = video.id
                                selectedVideoForDetail = video
                            }
                        )
                        StudioTab.ANALYTICS -> AnalyticsScreen(
                            onVideoClick = { video ->
                                selectedVideoId = video.id
                                selectedVideoForDetail = video
                            },
                            activeDrilldownMetric = activeAnalyticsDrilldown,
                            onDrilldownChange = { activeAnalyticsDrilldown = it }
                        )
                        StudioTab.COMMUNITY -> CommunityScreen()
                        StudioTab.EARN -> EarnScreen(
                            onOpenAdminPanel = { showAddVideoDialog = true }
                        )
                    }
                }
            }
        }

        if (showAddVideoDialog) {
            AddVideoDialog(onDismiss = { showAddVideoDialog = false })
        }

        InlineEditDialogHost(manager = inlineEditManager)
    }

    // Individual video analytics is rendered as a dedicated screen above

    // Create Modal Bottom Sheet
    if (showCreateSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCreateSheet = false },
            containerColor = colors.cardBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Create",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showCreateSheet = false
                            showAddVideoDialog = true
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, tint = colors.textPrimary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Upload a video", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showCreateSheet = false
                            showAddVideoDialog = true
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = colors.textPrimary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Create a Short", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showCreateSheet = false
                            showAddVideoDialog = true
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, tint = colors.textPrimary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Go live", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                }
            }
        }
    }

    // Notifications Dialog
    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            containerColor = colors.cardBackground,
            title = { Text("Notifications", fontWeight = FontWeight.Bold, color = colors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Your Short reached 749 views!", color = colors.textPrimary)
                    Text("• @gautamm24 commented on your video.", color = colors.textPrimary)
                    Text("• Check your monthly performance summary.", color = colors.textPrimary)
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationsDialog = false }) {
                    Text("Dismiss", color = colors.accentRed, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Preview(showBackground = true, name = "Studio App Main Preview")
@Composable
fun StudioAppPreview() {
    StudioTheme {
        StudioApp()
    }
}

package com.example.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.StudioRepository
import com.example.data.local.LocalAdminStore
import com.example.model.ContentType
import com.example.model.VideoItem
import com.example.ui.theme.StudioTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

enum class AdminSectionTab(val label: String, val icon: String) {
    PROFILE("Profile", "👤"),
    STATISTICS("Stats", "📊"),
    ANALYTICS("Analytics", "📈"),
    VIDEOS("Videos & Content", "🎬"),
    MONETIZATION("Earn & Payout", "💰")
}

enum class VideoManagementTab(val label: String) {
    VIDEOS("Videos"),
    SHORTS("Shorts"),
    LIVE("Live"),
    DASHBOARD("Dashboard")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalAdminDashboardScreen(
    onClose: () -> Unit,
    onLogout: () -> Unit,
    onSessionExpired: () -> Unit = {}
) {
    val colors = StudioTheme.colors
    val context = LocalContext.current
    var currentSection by remember { mutableStateOf(AdminSectionTab.PROFILE) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    fun checkAccessOrLock(): Boolean {
        if (!LocalAdminStore.isSessionActive()) {
            onSessionExpired()
            return false
        }
        return true
    }

    // 1-second active session tick to keep countdown updated and lock immediately on expiry
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(1000L)
            LocalAdminStore.refreshSessionTick()
            if (!LocalAdminStore.isSessionActive()) {
                onSessionExpired()
                break
            }
        }
    }

    // --- Channel Profile State ---
    var profileName by remember { mutableStateOf(StudioRepository.currentProfile.name) }
    var profileHandle by remember { mutableStateOf(StudioRepository.currentProfile.handle) }
    var profileSubs by remember { mutableStateOf(StudioRepository.currentProfile.subscribersCount) }
    var profileEmail by remember { mutableStateOf(StudioRepository.currentProfile.email) }
    var profileDesc by remember { mutableStateOf(StudioRepository.currentProfile.description) }
    var profileAvatarUri by remember { mutableStateOf(StudioRepository.currentProfile.avatarUri) }
    var profileAvatarUrlInput by remember { mutableStateOf("") }
    var profileMonetized by remember { mutableStateOf(StudioRepository.currentProfile.isMonetized) }

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            profileAvatarUri = uri.toString()
            snackbarMessage = "Profile picture selected from gallery!"
        }
    }

    // --- Channel Statistics State ---
    var statsViews by remember { mutableStateOf(StudioRepository.dashboardViews) }
    var statsWatchTime by remember { mutableStateOf(StudioRepository.dashboardWatchTime) }
    var statsSubs by remember { mutableStateOf(StudioRepository.dashboardSubscribers) }
    var statsRevenue by remember { mutableStateOf(StudioRepository.dashboardRevenue) }
    var statsPeriod by remember { mutableStateOf(StudioRepository.dashboardPeriodLabel) }

    // --- Analytics Exact-Sum State ---
    var analyticsDays by remember { mutableIntStateOf(28) }
    var analyticsTotalViewsInput by remember { mutableStateOf("5000") }
    var analyticsWatchHoursInput by remember { mutableStateOf("150.0") }
    var analyticsRevenueDollarsInput by remember { mutableStateOf("250.00") }
    var analyticsMonthlyAudienceInput by remember { mutableStateOf(StudioRepository.communityMonthlyAudience) }
    var generatedViewsDistribution by remember { mutableStateOf<List<Float>>(emptyList()) }
    var isAnalyticsCalculated by remember { mutableStateOf(false) }

    fun recalculateDistribution() {
        val total = analyticsTotalViewsInput.toLongOrNull() ?: 5000L
        generatedViewsDistribution = LocalAdminStore.distributeExactSum(total, analyticsDays)
        isAnalyticsCalculated = true
    }

    LaunchedEffect(analyticsDays, analyticsTotalViewsInput) {
        recalculateDistribution()
    }

    // --- Videos and Shorts State ---
    var selectedVideoCategory by remember { mutableStateOf(VideoManagementTab.VIDEOS) }
    var videoToEdit by remember { mutableStateOf<VideoItem?>(null) }
    var showVideoDialog by remember { mutableStateOf(false) }
    var videoToDelete by remember { mutableStateOf<VideoItem?>(null) }

    // --- Monetization & Payout State ---
    var earnIsMonetized by remember { mutableStateOf(StudioRepository.isMonetized) }
    var earnWatchHours by remember { mutableStateOf(StudioRepository.monetizationWatchHours) }
    var earnReqWatchHours by remember { mutableStateOf(StudioRepository.monetizationRequiredWatchHours) }
    var earnShortsViews by remember { mutableStateOf(StudioRepository.monetizationShortsViews) }
    var earnReqShortsViews by remember { mutableStateOf(StudioRepository.monetizationRequiredShortsViews) }
    var earnVideoUploads by remember { mutableStateOf(StudioRepository.monetizationVideoUploads) }
    var earnPayoutHeadline by remember { mutableStateOf(StudioRepository.payoutHeadline) }
    var earnPayoutAmount by remember { mutableStateOf(StudioRepository.payoutAmount) }
    var earnPayoutMonthlyMonth by remember { mutableStateOf(StudioRepository.payoutMonthlyMonth) }
    var earnPayoutMonthlyAmount by remember { mutableStateOf(StudioRepository.payoutMonthlyAmount) }
    var earnPayoutMinimumText by remember { mutableStateOf(StudioRepository.payoutMinimumText) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Studio Admin Panel",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Live Data Customization & On-Device Storage",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close", tint = colors.textPrimary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            LocalAdminStore.lockSession()
                            onSessionExpired()
                        },
                        modifier = Modifier.testTag("admin_lock_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Lock", tint = colors.textSecondary)
                    }
                    IconButton(
                        onClick = {
                            if (checkAccessOrLock()) {
                                showResetConfirmDialog = true
                            }
                        },
                        modifier = Modifier.testTag("admin_reset_button")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset Demo Data", tint = Color(0xFFFF9800))
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("admin_logout_button")
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = colors.textSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.cardBackground,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary)
                    ) {
                        Text("Done / Close")
                    }
                    Button(
                        onClick = {
                            if (!checkAccessOrLock()) return@Button

                            when (currentSection) {
                                AdminSectionTab.PROFILE -> {
                                    LocalAdminStore.saveChannelProfile(
                                        name = profileName.trim(),
                                        handle = profileHandle.trim(),
                                        subscribersCount = profileSubs.trim(),
                                        email = profileEmail.trim(),
                                        description = profileDesc.trim(),
                                        avatarUri = profileAvatarUri,
                                        isMonetized = profileMonetized
                                    )
                                    snackbarMessage = "Channel profile saved immediately!"
                                }
                                AdminSectionTab.STATISTICS -> {
                                    LocalAdminStore.saveChannelStatistics(
                                        views = statsViews.trim(),
                                        watchTime = statsWatchTime.trim(),
                                        subscribers = statsSubs.trim(),
                                        revenue = statsRevenue.trim(),
                                        period = statsPeriod.trim()
                                    )
                                    snackbarMessage = "Dashboard statistics updated immediately!"
                                }
                                AdminSectionTab.ANALYTICS -> {
                                    val totalV = analyticsTotalViewsInput.toLongOrNull() ?: 5000L
                                    val totalW = analyticsWatchHoursInput.toFloatOrNull() ?: 150f
                                    val totalR = analyticsRevenueDollarsInput.toFloatOrNull() ?: 250f
                                    LocalAdminStore.generateAndSaveAnalytics(
                                        days = analyticsDays,
                                        totalViews = totalV,
                                        totalWatchTimeHours = totalW,
                                        totalRevenueDollars = totalR
                                    )
                                    LocalAdminStore.saveMonthlyAudience(analyticsMonthlyAudienceInput.trim())
                                    snackbarMessage = "Analytics and audience ($totalV views across $analyticsDays days) saved!"
                                }
                                AdminSectionTab.VIDEOS -> {
                                    snackbarMessage = "Videos & Content are synchronized!"
                                }
                                AdminSectionTab.MONETIZATION -> {
                                    LocalAdminStore.saveMonetization(
                                        isMonetized = earnIsMonetized,
                                        watchHours = earnWatchHours.trim(),
                                        requiredWatchHours = earnReqWatchHours.trim(),
                                        shortsViews = earnShortsViews.trim(),
                                        requiredShortsViews = earnReqShortsViews.trim(),
                                        videoUploads = earnVideoUploads.trim()
                                    )
                                    LocalAdminStore.savePayout(
                                        headline = earnPayoutHeadline.trim(),
                                        amount = earnPayoutAmount.trim(),
                                        monthlyMonth = earnPayoutMonthlyMonth.trim(),
                                        monthlyAmount = earnPayoutMonthlyAmount.trim(),
                                        minimumText = earnPayoutMinimumText.trim()
                                    )
                                    snackbarMessage = "Monetization & Payout updated immediately!"
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_save_section_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.navSelected)
                    ) {
                        Text("Save ${currentSection.label}", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = colors.background,
        snackbarHost = {
            snackbarMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { snackbarMessage = null }) {
                            Text("OK", color = Color.White)
                        }
                    }
                ) {
                    Text(msg)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isUnlimited = LocalAdminStore.isUnlimitedSession()
            val remainingSec = LocalAdminStore.getRemainingSeconds()
            val minutes = remainingSec / 60
            val seconds = remainingSec % 60
            val timeString = String.format("%02d:%02d", minutes, seconds)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_session_bar"),
                color = colors.cardBackground,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isUnlimited) Color(0xFF4CAF50) else Color(0xFFFF9800))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isUnlimited) "Unlimited Access Active" else "Session Active: $timeString",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                LocalAdminStore.lockSession()
                                onSessionExpired()
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Now",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lock", fontSize = 11.sp, color = colors.textSecondary)
                    }
                }
            }

            HorizontalDivider(color = colors.divider, thickness = 0.5.dp)

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AdminSectionTab.values()) { tab ->
                    val isSelected = currentSection == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (checkAccessOrLock()) {
                                currentSection = tab
                            }
                        },
                        label = {
                            Text(
                                text = "${tab.icon} ${tab.label}",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.navSelected,
                            selectedLabelColor = Color.White,
                            containerColor = colors.chipBackground,
                            labelColor = colors.textPrimary
                        )
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp)
            ) {
                when (currentSection) {
                    AdminSectionTab.PROFILE -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text("Channel Profile Editor", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textPrimary)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                profileName = "shain"
                                                profileHandle = "@shain"
                                                profileSubs = "128,490"
                                                snackbarMessage = "Applied preset: 'shain'"
                                            },
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("Set 'shain'", fontSize = 12.sp)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                profileName = "Daniel Dalen"
                                                profileHandle = "@danieldalen"
                                                profileSubs = "48,237"
                                                snackbarMessage = "Applied preset: 'Daniel Dalen'"
                                            },
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("Set 'Daniel Dalen'", fontSize = 12.sp)
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFFEB3B)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (!profileAvatarUri.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = profileAvatarUri,
                                                    contentDescription = "Avatar",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Text(text = "👦🏻", fontSize = 38.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Button(
                                                onClick = {
                                                    avatarPickerLauncher.launch(
                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                    )
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = colors.chipBackground),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Choose from Gallery", color = colors.textPrimary, fontSize = 12.sp)
                                            }

                                            if (!profileAvatarUri.isNullOrBlank()) {
                                                TextButton(
                                                    onClick = {
                                                        profileAvatarUri = null
                                                        snackbarMessage = "Reset avatar to default emoji"
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text("Remove Custom Image", color = Color(0xFFFF5252), fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = profileAvatarUrlInput,
                                            onValueChange = { profileAvatarUrlInput = it },
                                            label = { Text("Or Paste Image URL") },
                                            placeholder = { Text("https://...") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Button(
                                            onClick = {
                                                if (profileAvatarUrlInput.isNotBlank()) {
                                                    profileAvatarUri = profileAvatarUrlInput.trim()
                                                    snackbarMessage = "Profile picture URL applied!"
                                                }
                                            },
                                            enabled = profileAvatarUrlInput.isNotBlank()
                                        ) {
                                            Text("Apply")
                                        }
                                    }

                                    OutlinedTextField(
                                        value = profileName,
                                        onValueChange = { profileName = it },
                                        label = { Text("Channel Name (e.g. shain)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = profileHandle,
                                        onValueChange = { profileHandle = it },
                                        label = { Text("Handle (e.g. @shain)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = profileSubs,
                                        onValueChange = { profileSubs = it },
                                        label = { Text("Total Subscribers Display (e.g. 128,490)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = profileEmail,
                                        onValueChange = { profileEmail = it },
                                        label = { Text("Email Address") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = profileDesc,
                                        onValueChange = { profileDesc = it },
                                        label = { Text("Channel Description") },
                                        maxLines = 3,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Channel Monetization Enabled", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.textPrimary)
                                            Text("Displays partner badge and monetized metrics", fontSize = 12.sp, color = colors.textSecondary)
                                        }
                                        Switch(
                                            checked = profileMonetized,
                                            onCheckedChange = { profileMonetized = it }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    AdminSectionTab.STATISTICS -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text("Home Dashboard Metric Cards", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textPrimary)
                                    Text("Modifies the 4 primary rounded cards on the Home Dashboard:", fontSize = 12.sp, color = colors.textSecondary)

                                    Text("Quick Period Label:", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf("Last 28 days", "Last 90 days", "Last 365 days", "Lifetime").forEach { p ->
                                            FilterChip(
                                                selected = statsPeriod == p,
                                                onClick = { statsPeriod = p },
                                                label = { Text(p, fontSize = 11.sp) }
                                            )
                                        }
                                    }

                                    OutlinedTextField(
                                        value = statsViews,
                                        onValueChange = { statsViews = it },
                                        label = { Text("Views Card (e.g. 2.4k)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = statsWatchTime,
                                        onValueChange = { statsWatchTime = it },
                                        label = { Text("Watch Time Card (e.g. 84.2)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = statsSubs,
                                        onValueChange = { statsSubs = it },
                                        label = { Text("Subscribers Card (e.g. -1.0k or +1.2k)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = statsRevenue,
                                        onValueChange = { statsRevenue = it },
                                        label = { Text("Estimated Revenue Card (e.g. US$8.65)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = statsPeriod,
                                        onValueChange = { statsPeriod = it },
                                        label = { Text("Custom Period Text") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    AdminSectionTab.ANALYTICS -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text("Analytics Chart & Audience Generator", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textPrimary)
                                    Text("Generates realistic daily graph curves whose points sum up EXACTLY to your target views:", fontSize = 12.sp, color = colors.textSecondary)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(7, 28, 90, 365).forEach { d ->
                                            FilterChip(
                                                selected = analyticsDays == d,
                                                onClick = { analyticsDays = d },
                                                label = { Text("$d Days") }
                                            )
                                        }
                                    }

                                    OutlinedTextField(
                                        value = analyticsTotalViewsInput,
                                        onValueChange = { analyticsTotalViewsInput = it },
                                        label = { Text("Target Total Views (Exact Sum)") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = analyticsWatchHoursInput,
                                        onValueChange = { analyticsWatchHoursInput = it },
                                        label = { Text("Target Total Watch Time (Hours)") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = analyticsRevenueDollarsInput,
                                        onValueChange = { analyticsRevenueDollarsInput = it },
                                        label = { Text("Target Estimated Revenue ($)") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = analyticsMonthlyAudienceInput,
                                        onValueChange = { analyticsMonthlyAudienceInput = it },
                                        label = { Text("Community Monthly Audience (e.g. 39.6k)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    if (generatedViewsDistribution.isNotEmpty()) {
                                        val calculatedSum = generatedViewsDistribution.sum().toLong()
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            color = colors.chipBackground,
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("Mathematical Sum:", fontSize = 12.sp, color = colors.textSecondary)
                                                    Text(
                                                        text = "$calculatedSum views",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = Color(0xFF2E7D32)
                                                    )
                                                }

                                                val maxBar = (generatedViewsDistribution.maxOrNull() ?: 1f).coerceAtLeast(1f)
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(36.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                    verticalAlignment = Alignment.Bottom
                                                ) {
                                                    generatedViewsDistribution.take(30).forEach { pt ->
                                                        val frac = (pt / maxBar).coerceIn(0.1f, 1f)
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .fillMaxHeight(frac)
                                                                .background(colors.navSelected, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AdminSectionTab.VIDEOS -> {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(VideoManagementTab.values()) { vTab ->
                                            val count = when (vTab) {
                                                VideoManagementTab.VIDEOS -> StudioRepository.contentVideos.size
                                                VideoManagementTab.SHORTS -> StudioRepository.contentShorts.size
                                                VideoManagementTab.LIVE -> StudioRepository.contentLiveStreams.size
                                                VideoManagementTab.DASHBOARD -> StudioRepository.latestPublishedContent.size
                                            }
                                            FilterChip(
                                                selected = selectedVideoCategory == vTab,
                                                onClick = { selectedVideoCategory = vTab },
                                                label = { Text("${vTab.label} ($count)", fontSize = 12.sp) }
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            val cType = when (selectedVideoCategory) {
                                                VideoManagementTab.SHORTS -> ContentType.SHORT
                                                VideoManagementTab.LIVE -> ContentType.LIVE
                                                else -> ContentType.VIDEO
                                            }
                                            videoToEdit = VideoItem(
                                                id = "custom_${System.currentTimeMillis()}",
                                                title = "",
                                                type = cType,
                                                duration = if (cType == ContentType.SHORT) "0:30" else if (cType == ContentType.LIVE) "45:00" else "8:45",
                                                publishedTimeAgo = "Just now",
                                                views = 1200L,
                                                likes = 95L,
                                                comments = 14L
                                            )
                                            showVideoDialog = true
                                        },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = colors.navSelected)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        val activeList = when (selectedVideoCategory) {
                            VideoManagementTab.VIDEOS -> StudioRepository.contentVideos
                            VideoManagementTab.SHORTS -> StudioRepository.contentShorts
                            VideoManagementTab.LIVE -> StudioRepository.contentLiveStreams
                            VideoManagementTab.DASHBOARD -> StudioRepository.latestPublishedContent
                        }

                        if (activeList.isEmpty()) {
                            item {
                                Text("No items in this section. Tap 'Add' above to create one.", color = colors.textSecondary, fontSize = 13.sp)
                            }
                        } else {
                            items(activeList) { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 64.dp, height = if (item.type == ContentType.SHORT) 80.dp else 44.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF212121)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (!item.thumbnailUri.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = item.thumbnailUri,
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Text(
                                                    text = if (item.type == ContentType.SHORT) "⚡ Short" else if (item.type == ContentType.LIVE) "🔴 Live" else "▶ Video",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.title.ifBlank { "Untitled" },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                maxLines = 2,
                                                color = colors.textPrimary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${item.views} views • ${item.publishedTimeAgo}",
                                                fontSize = 11.sp,
                                                color = colors.textSecondary
                                            )
                                            Text(
                                                text = "Likes: ${item.likes} • Comments: ${item.comments}",
                                                fontSize = 11.sp,
                                                color = colors.textSecondary
                                            )
                                        }

                                        Row {
                                            IconButton(
                                                onClick = {
                                                    videoToEdit = item
                                                    showVideoDialog = true
                                                }
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(
                                                onClick = {
                                                    videoToDelete = item
                                                }
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AdminSectionTab.MONETIZATION -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text("Monetization & Payout Editor", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textPrimary)
                                    Text("Controls Partner status and real-time Payout numbers on the Earn screen:", fontSize = 12.sp, color = colors.textSecondary)

                                    OutlinedButton(
                                        onClick = {
                                            earnPayoutHeadline = "A payment is headed your way.\nExpect a payment 21–26 October"
                                            earnPayoutAmount = "$355.13"
                                            earnPayoutMonthlyMonth = "October earnings"
                                            earnPayoutMonthlyAmount = "$112.19"
                                            earnPayoutMinimumText = "Payment minimum: $100.00"
                                            earnIsMonetized = true
                                            snackbarMessage = "Applied official reference payout ($355.13)!"
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Apply Reference Payout ($355.13, Oct 21–26)")
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Partner Program Monetization", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.textPrimary)
                                            Text(if (earnIsMonetized) "Active (Shows Payout & Ads)" else "Inactive (Shows Milestones)", fontSize = 12.sp, color = colors.textSecondary)
                                        }
                                        Switch(
                                            checked = earnIsMonetized,
                                            onCheckedChange = { earnIsMonetized = it }
                                        )
                                    }

                                    HorizontalDivider(color = colors.divider)
                                    Text("Payout & Earnings Details (Earn Tab):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)

                                    OutlinedTextField(
                                        value = earnPayoutHeadline,
                                        onValueChange = { earnPayoutHeadline = it },
                                        label = { Text("Payout Headline Banner") },
                                        maxLines = 3,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = earnPayoutAmount,
                                        onValueChange = { earnPayoutAmount = it },
                                        label = { Text("Total Payout Amount (e.g. $355.13)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = earnPayoutMonthlyMonth,
                                        onValueChange = { earnPayoutMonthlyMonth = it },
                                        label = { Text("Monthly Earnings Month (e.g. October earnings)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = earnPayoutMonthlyAmount,
                                        onValueChange = { earnPayoutMonthlyAmount = it },
                                        label = { Text("Monthly Earnings Amount (e.g. $112.19)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = earnPayoutMinimumText,
                                        onValueChange = { earnPayoutMinimumText = it },
                                        label = { Text("Payment Minimum Text (e.g. Payment minimum: $100.00)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    HorizontalDivider(color = colors.divider)
                                    Text("Milestone / Progress Figures (When not monetized):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)

                                    OutlinedTextField(
                                        value = earnWatchHours,
                                        onValueChange = { earnWatchHours = it },
                                        label = { Text("Qualified Watch Hours (e.g. 3,140)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = earnReqWatchHours,
                                        onValueChange = { earnReqWatchHours = it },
                                        label = { Text("Required Watch Hours (e.g. 3,000)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = earnShortsViews,
                                        onValueChange = { earnShortsViews = it },
                                        label = { Text("Qualified Shorts Views (e.g. 3.2M)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = earnReqShortsViews,
                                        onValueChange = { earnReqShortsViews = it },
                                        label = { Text("Required Shorts Views (e.g. 3.0M)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = earnVideoUploads,
                                        onValueChange = { earnVideoUploads = it },
                                        label = { Text("Recent Video Uploads (e.g. 5)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showVideoDialog && videoToEdit != null) {
        val currentVideo = videoToEdit!!
        var editTitle by remember { mutableStateOf(currentVideo.title) }
        var editType by remember { mutableStateOf(currentVideo.type) }
        var editDuration by remember { mutableStateOf(currentVideo.duration) }
        var editPublishedAgo by remember { mutableStateOf(currentVideo.publishedTimeAgo) }
        var editViews by remember { mutableStateOf(currentVideo.views.toString()) }
        var editLikes by remember { mutableStateOf(currentVideo.likes.toString()) }
        var editComments by remember { mutableStateOf(currentVideo.comments.toString()) }
        var editThumbnailUri by remember { mutableStateOf(currentVideo.thumbnailUri) }
        var editThumbnailUrlInput by remember { mutableStateOf("") }
        var editVisibility by remember { mutableStateOf("Public") }
        var editShowOnDashboard by remember { mutableStateOf(true) }

        val videoThumbnailPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) {
                editThumbnailUri = uri.toString()
            }
        }

        AlertDialog(
            onDismissRequest = { showVideoDialog = false },
            containerColor = colors.cardBackground,
            title = {
                Text(
                    text = if (currentVideo.id.startsWith("custom_") && currentVideo.title.isBlank()) "Add New Video Item" else "Edit Video Item",
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = editType == ContentType.VIDEO,
                                onClick = {
                                    editType = ContentType.VIDEO
                                    if (editDuration == "0:30") editDuration = "8:45"
                                },
                                label = { Text("Video") }
                            )
                            FilterChip(
                                selected = editType == ContentType.SHORT,
                                onClick = {
                                    editType = ContentType.SHORT
                                    if (editDuration == "8:45") editDuration = "0:30"
                                },
                                label = { Text("Short") }
                            )
                            FilterChip(
                                selected = editType == ContentType.LIVE,
                                onClick = {
                                    editType = ContentType.LIVE
                                    editDuration = "45:12"
                                },
                                label = { Text("Live") }
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            label = { Text("Video Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = editDuration,
                                onValueChange = { editDuration = it },
                                label = { Text("Duration") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = editPublishedAgo,
                                onValueChange = { editPublishedAgo = it },
                                label = { Text("Published") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = editViews,
                                onValueChange = { editViews = it },
                                label = { Text("Views") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = editLikes,
                                onValueChange = { editLikes = it },
                                label = { Text("Likes") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = editComments,
                                onValueChange = { editComments = it },
                                label = { Text("Comments") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Text("Visibility:", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Public", "Unlisted", "Private").forEach { vis ->
                                FilterChip(
                                    selected = editVisibility == vis,
                                    onClick = { editVisibility = vis },
                                    label = { Text(vis) }
                                )
                            }
                        }
                    }

                    item {
                        Text("Thumbnail Source:", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    videoThumbnailPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.chipBackground),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pick Gallery", color = colors.textPrimary, fontSize = 11.sp)
                            }

                            if (!editThumbnailUri.isNullOrBlank()) {
                                TextButton(
                                    onClick = { editThumbnailUri = null },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("Clear", color = Color(0xFFFF5252), fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = editThumbnailUrlInput,
                                onValueChange = { editThumbnailUrlInput = it },
                                label = { Text("Or Thumbnail URL") },
                                placeholder = { Text("https://...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (editThumbnailUrlInput.isNotBlank()) {
                                        editThumbnailUri = editThumbnailUrlInput.trim()
                                    }
                                },
                                enabled = editThumbnailUrlInput.isNotBlank()
                            ) {
                                Text("Set")
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show on Home Dashboard", fontSize = 12.sp, color = colors.textPrimary)
                            Switch(
                                checked = editShowOnDashboard,
                                onCheckedChange = { editShowOnDashboard = it }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = currentVideo.copy(
                            title = editTitle.trim().ifBlank { "Untitled Video" },
                            type = editType,
                            duration = editDuration.trim().ifBlank { if (editType == ContentType.SHORT) "0:30" else "5:00" },
                            publishedTimeAgo = editPublishedAgo.trim().ifBlank { "Just now" },
                            views = editViews.toLongOrNull() ?: 0L,
                            likes = editLikes.toLongOrNull() ?: 0L,
                            comments = editComments.toLongOrNull() ?: 0L,
                            thumbnailUri = editThumbnailUri
                        )
                        LocalAdminStore.addOrUpdateVideo(updated, showOnDashboard = editShowOnDashboard)
                        showVideoDialog = false
                        videoToEdit = null
                        snackbarMessage = "Video item '${updated.title}' saved!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.navSelected)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showVideoDialog = false
                    videoToEdit = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (videoToDelete != null) {
        val toDel = videoToDelete!!
        AlertDialog(
            onDismissRequest = { videoToDelete = null },
            containerColor = colors.cardBackground,
            title = {
                Text("Delete Video Item?", fontWeight = FontWeight.Bold, color = colors.textPrimary)
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${toDel.title}'? This action will remove it from local demo lists immediately.",
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        LocalAdminStore.deleteVideo(toDel.id, toDel.type)
                        snackbarMessage = "Deleted '${toDel.title}'"
                        videoToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { videoToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            containerColor = colors.cardBackground,
            title = {
                Text("Reset Demo Customizations?", fontWeight = FontWeight.Bold, color = colors.textPrimary)
            },
            text = {
                Text(
                    text = "This will clear all locally customized channel names, metrics, charts, and video items on this device, reverting back to the default demo state.",
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        LocalAdminStore.resetToDefaultDemoData(context)
                        showResetConfirmDialog = false
                        snackbarMessage = "Reset all data back to default demo state!"
                        profileName = StudioRepository.currentProfile.name
                        profileHandle = StudioRepository.currentProfile.handle
                        profileSubs = StudioRepository.currentProfile.subscribersCount
                        statsViews = StudioRepository.dashboardViews
                        statsWatchTime = StudioRepository.dashboardWatchTime
                        statsSubs = StudioRepository.dashboardSubscribers
                        statsRevenue = StudioRepository.dashboardRevenue
                        earnPayoutAmount = StudioRepository.payoutAmount
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text("Reset Now", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

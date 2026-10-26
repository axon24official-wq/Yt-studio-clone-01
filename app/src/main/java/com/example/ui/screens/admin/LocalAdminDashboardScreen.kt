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
    VIDEOS("Videos & Shorts", "🎬"),
    MONETIZATION("Earn", "💰")
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
    var profileName by remember { mutableStateOf(StudioRepository.danielProfile.name) }
    var profileHandle by remember { mutableStateOf(StudioRepository.danielProfile.handle) }
    var profileSubs by remember { mutableStateOf(StudioRepository.danielProfile.subscribersCount) }
    var profileEmail by remember { mutableStateOf(StudioRepository.danielProfile.email) }
    var profileDesc by remember { mutableStateOf(StudioRepository.danielProfile.description) }
    var profileAvatarUri by remember { mutableStateOf(StudioRepository.danielProfile.avatarUri) }
    var profileMonetized by remember { mutableStateOf(StudioRepository.danielProfile.isMonetized) }

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            profileAvatarUri = uri.toString()
            snackbarMessage = "Profile image selected from gallery!"
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
    var generatedViewsDistribution by remember { mutableStateOf<List<Float>>(emptyList()) }
    var isAnalyticsCalculated by remember { mutableStateOf(false) }

    // Initialize or recalculate analytics distribution preview
    fun recalculateDistribution() {
        val total = analyticsTotalViewsInput.toLongOrNull() ?: 5000L
        generatedViewsDistribution = LocalAdminStore.distributeExactSum(total, analyticsDays)
        isAnalyticsCalculated = true
    }

    LaunchedEffect(analyticsDays, analyticsTotalViewsInput) {
        recalculateDistribution()
    }

    // --- Videos and Shorts State ---
    var selectedVideoTab by remember { mutableStateOf(ContentType.VIDEO) }
    var videoToEdit by remember { mutableStateOf<VideoItem?>(null) }
    var showVideoDialog by remember { mutableStateOf(false) }
    var videoToDelete by remember { mutableStateOf<VideoItem?>(null) }

    // --- Monetization State ---
    var earnIsMonetized by remember { mutableStateOf(StudioRepository.isMonetized) }
    var earnWatchHours by remember { mutableStateOf(StudioRepository.monetizationWatchHours) }
    var earnReqWatchHours by remember { mutableStateOf(StudioRepository.monetizationRequiredWatchHours) }
    var earnShortsViews by remember { mutableStateOf(StudioRepository.monetizationShortsViews) }
    var earnReqShortsViews by remember { mutableStateOf(StudioRepository.monetizationRequiredShortsViews) }
    var earnVideoUploads by remember { mutableStateOf(StudioRepository.monetizationVideoUploads) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Local Studio Admin",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Offline demo controls (This Device Only)",
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

                            // Save active section
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
                                    snackbarMessage = "Channel profile saved locally!"
                                }
                                AdminSectionTab.STATISTICS -> {
                                    LocalAdminStore.saveChannelStatistics(
                                        views = statsViews.trim(),
                                        watchTime = statsWatchTime.trim(),
                                        subscribers = statsSubs.trim(),
                                        revenue = statsRevenue.trim(),
                                        period = statsPeriod.trim()
                                    )
                                    snackbarMessage = "Channel statistics updated immediately!"
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
                                    snackbarMessage = "Analytics distributed ($totalV views across $analyticsDays days)!"
                                }
                                AdminSectionTab.VIDEOS -> {
                                    snackbarMessage = "Videos & Shorts saved!"
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
                                    snackbarMessage = "Monetization status updated immediately!"
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
            // Unobtrusive countdown / access status bar
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("admin_countdown_display")
                    ) {
                        Icon(
                            imageVector = if (isUnlimited) Icons.Default.LockOpen else Icons.Default.Timer,
                            contentDescription = "Session Status",
                            tint = if (isUnlimited) Color(0xFF4CAF50) else if (remainingSec < 60) Color(0xFFFF5252) else colors.navSelected,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isUnlimited) "Admin Access: Unlimited" else "Admin Access: $timeString",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (!isUnlimited && remainingSec < 60) Color(0xFFFF5252) else colors.textPrimary
                        )
                    }

                    TextButton(
                        onClick = {
                            LocalAdminStore.lockSession()
                            onSessionExpired()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("admin_session_lock_btn")
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

            // Section Tabs Carousel
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

            // Section Body
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

                                    // Profile Image Picker
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
                                                Text("Choose Logo from Gallery", color = colors.textPrimary, fontSize = 12.sp)
                                            }

                                            if (!profileAvatarUri.isNullOrBlank()) {
                                                TextButton(
                                                    onClick = {
                                                        profileAvatarUri = null
                                                        snackbarMessage = "Reset to default avatar"
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text("Remove Custom Image", color = Color(0xFFFF5252), fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = profileName,
                                        onValueChange = { profileName = it },
                                        label = { Text("Channel Name") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = profileHandle,
                                        onValueChange = { profileHandle = it },
                                        label = { Text("Handle (e.g. @danieldalen)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = profileSubs,
                                        onValueChange = { profileSubs = it },
                                        label = { Text("Subscribers Display (e.g. 48,237)") },
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
                                        Text("Monetized Status Badge", color = colors.textPrimary, fontSize = 14.sp)
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
                                    Text("Dashboard Statistics (2x2 Grid)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textPrimary)
                                    Text("These values appear on the main Dashboard overview tiles.", fontSize = 12.sp, color = colors.textSecondary)

                                    OutlinedTextField(
                                        value = statsViews,
                                        onValueChange = { statsViews = it },
                                        label = { Text("Total Views (e.g. 887.7K)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = statsWatchTime,
                                        onValueChange = { statsWatchTime = it },
                                        label = { Text("Watch Time Hours (e.g. 66.2K)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = statsSubs,
                                        onValueChange = { statsSubs = it },
                                        label = { Text("Subscribers Change (e.g. +39.0K)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = statsRevenue,
                                        onValueChange = { statsRevenue = it },
                                        label = { Text("Estimated Revenue (e.g. $1,247.32)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = statsPeriod,
                                        onValueChange = { statsPeriod = it },
                                        label = { Text("Period Label (e.g. Last 28 days)") },
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
                                    Text("Analytics Distribution & Chart Generator", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textPrimary)
                                    Text(
                                        text = "Enter a total (e.g. 5,000 views). The algorithm distributes the total across the selected days so daily values add up EXACTLY to your total.",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary,
                                        lineHeight = 16.sp
                                    )

                                    Text("Select Date Range:", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(7, 18, 28, 90).forEach { days ->
                                            val selected = analyticsDays == days
                                            FilterChip(
                                                selected = selected,
                                                onClick = { analyticsDays = days },
                                                label = { Text("$days Days", fontSize = 12.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = colors.navSelected,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }

                                    OutlinedTextField(
                                        value = analyticsTotalViewsInput,
                                        onValueChange = { analyticsTotalViewsInput = it },
                                        label = { Text("Total Target Views (Exact Sum)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = analyticsWatchHoursInput,
                                        onValueChange = { analyticsWatchHoursInput = it },
                                        label = { Text("Estimated Watch Time (Hours)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = analyticsRevenueDollarsInput,
                                        onValueChange = { analyticsRevenueDollarsInput = it },
                                        label = { Text("Estimated Revenue ($)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { recalculateDistribution() },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = colors.chipBackground)
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Regenerate Variance", color = colors.textPrimary, fontSize = 12.sp)
                                        }
                                    }

                                    // Exact Sum Verification Banner
                                    val currentSum = generatedViewsDistribution.sum().toLong()
                                    val targetTotal = analyticsTotalViewsInput.toLongOrNull() ?: 0L
                                    val isMatch = currentSum == targetTotal

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isMatch) Color(0x334CAF50) else Color(0x33FF9800)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(if (isMatch) "✓ Exact Sum Match:" else "Sum:", fontWeight = FontWeight.Bold, color = colors.textPrimary, fontSize = 13.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "$currentSum / $targetTotal views across $analyticsDays days",
                                                color = if (isMatch) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }

                                    // Daily Distribution Sparkline / Preview Bar
                                    Text("Daily Breakdown Preview:", fontSize = 12.sp, color = colors.textSecondary)
                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        items(generatedViewsDistribution.size) { index ->
                                            val dayVal = generatedViewsDistribution[index].toLong()
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .background(colors.chipBackground, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                            ) {
                                                Text("D${index + 1}", fontSize = 10.sp, color = colors.textSecondary)
                                                Text("$dayVal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AdminSectionTab.VIDEOS -> {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = selectedVideoTab == ContentType.VIDEO,
                                        onClick = { selectedVideoTab = ContentType.VIDEO },
                                        label = { Text("Videos (${StudioRepository.contentVideos.size})") }
                                    )
                                    FilterChip(
                                        selected = selectedVideoTab == ContentType.SHORT,
                                        onClick = { selectedVideoTab = ContentType.SHORT },
                                        label = { Text("Shorts (${StudioRepository.contentShorts.size})") }
                                    )
                                }

                                Button(
                                    onClick = {
                                        videoToEdit = VideoItem(
                                            id = "custom_${System.currentTimeMillis()}",
                                            title = "",
                                            type = selectedVideoTab,
                                            duration = if (selectedVideoTab == ContentType.SHORT) "0:30" else "8:45",
                                            publishedTimeAgo = "Just now",
                                            views = 1000L,
                                            likes = 80L,
                                            comments = 12L
                                        )
                                        showVideoDialog = true
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.navSelected)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Item", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }

                        val activeList = if (selectedVideoTab == ContentType.VIDEO) {
                            StudioRepository.contentVideos
                        } else {
                            StudioRepository.contentShorts
                        }

                        if (activeList.isEmpty()) {
                            item {
                                Text("No items found. Tap 'Add Item' above to create one.", color = colors.textSecondary, fontSize = 13.sp)
                            }
                        } else {
                            items(activeList) { video ->
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
                                        // Thumbnail preview
                                        Box(
                                            modifier = Modifier
                                                .size(width = 64.dp, height = if (video.type == ContentType.SHORT) 80.dp else 42.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF2C2C2C)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (!video.thumbnailUri.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = video.thumbnailUri,
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Text(
                                                    text = if (video.type == ContentType.SHORT) "🩳" else "▶",
                                                    fontSize = 18.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = video.title.ifBlank { "Untitled" },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = colors.textPrimary,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "${video.views} views · ${video.duration} · ${video.publishedTimeAgo}",
                                                fontSize = 12.sp,
                                                color = colors.textSecondary
                                            )
                                            Text(
                                                text = "${video.likes} likes · ${video.comments} comments",
                                                fontSize = 11.sp,
                                                color = colors.textSecondary
                                            )
                                        }

                                        IconButton(onClick = {
                                            videoToEdit = video
                                            showVideoDialog = true
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                                        }

                                        IconButton(onClick = {
                                            videoToDelete = video
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
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
                                    Text("Monetization & Partner Status Editor", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textPrimary)
                                    Text("Updates the Earn screen and channel partner indicators immediately.", fontSize = 12.sp, color = colors.textSecondary)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Partner Program Monetization", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.textPrimary)
                                            Text(if (earnIsMonetized) "Active (Partner Approved)" else "Inactive (Eligibility Milestones shown)", fontSize = 12.sp, color = colors.textSecondary)
                                        }
                                        Switch(
                                            checked = earnIsMonetized,
                                            onCheckedChange = { earnIsMonetized = it }
                                        )
                                    }

                                    HorizontalDivider(color = colors.divider)

                                    Text("Milestone / Progress Figures:", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)

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
                                        label = { Text("Required Watch Hours (e.g. 3,000 or 4,000)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = earnShortsViews,
                                        onValueChange = { earnShortsViews = it },
                                        label = { Text("Qualified Shorts Views (e.g. 3.2M or 730)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = earnReqShortsViews,
                                        onValueChange = { earnReqShortsViews = it },
                                        label = { Text("Required Shorts Views (e.g. 3.0M or 30 lakh)") },
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

    // --- Add / Edit Video Modal Dialog ---
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
        var editDescription by remember { mutableStateOf(currentVideo.description) }

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
                    text = if (currentVideo.id.startsWith("custom_") && currentVideo.title.isBlank()) "Add New Demo Item" else "Edit Video Item",
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
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = editType == ContentType.VIDEO,
                                onClick = {
                                    editType = ContentType.VIDEO
                                    if (editDuration == "0:30") editDuration = "8:45"
                                },
                                label = { Text("Regular Video") }
                            )
                            FilterChip(
                                selected = editType == ContentType.SHORT,
                                onClick = {
                                    editType = ContentType.SHORT
                                    if (editDuration == "8:45") editDuration = "0:30"
                                },
                                label = { Text("Vertical Short") }
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = editDuration,
                            onValueChange = { editDuration = it },
                            label = { Text("Duration (e.g. 8:45 or 0:30)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = editPublishedAgo,
                            onValueChange = { editPublishedAgo = it },
                            label = { Text("Published Time (e.g. 1 day ago)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editViews,
                                onValueChange = { editViews = it },
                                label = { Text("Views") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = editLikes,
                                onValueChange = { editLikes = it },
                                label = { Text("Likes") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = editComments,
                            onValueChange = { editComments = it },
                            label = { Text("Comments") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = editDescription,
                            onValueChange = { editDescription = it },
                            label = { Text("Description") },
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF2C2C2C)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!editThumbnailUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = editThumbnailUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text("🖼")
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Button(
                                onClick = {
                                    videoThumbnailPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.chipBackground)
                            ) {
                                Text("Choose Thumbnail", color = colors.textPrimary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!checkAccessOrLock()) {
                            showVideoDialog = false
                            return@Button
                        }
                        val newItem = currentVideo.copy(
                            title = editTitle.ifBlank { "Untitled ${editType.name}" },
                            type = editType,
                            duration = editDuration.ifBlank { if (editType == ContentType.SHORT) "0:30" else "5:00" },
                            publishedTimeAgo = editPublishedAgo.ifBlank { "Just now" },
                            views = editViews.toLongOrNull() ?: 0L,
                            likes = editLikes.toLongOrNull() ?: 0L,
                            comments = editComments.toLongOrNull() ?: 0L,
                            thumbnailUri = editThumbnailUri,
                            description = editDescription
                        )
                        LocalAdminStore.addOrUpdateVideo(newItem)
                        showVideoDialog = false
                        snackbarMessage = "Saved '${newItem.title}'!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.navSelected)
                ) {
                    Text("Save Video", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showVideoDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        )
    }

    // --- Delete Confirmation Dialog ---
    if (videoToDelete != null) {
        val target = videoToDelete!!
        AlertDialog(
            onDismissRequest = { videoToDelete = null },
            containerColor = colors.cardBackground,
            title = { Text("Delete '${target.title}'?", fontWeight = FontWeight.Bold, color = colors.textPrimary) },
            text = { Text("Are you sure you want to remove this demo video? This action will update your content lists immediately.", color = colors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        if (!checkAccessOrLock()) {
                            videoToDelete = null
                            return@Button
                        }
                        LocalAdminStore.deleteVideo(target.id, target.type == ContentType.SHORT)
                        videoToDelete = null
                        snackbarMessage = "Deleted '${target.title}'"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { videoToDelete = null }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        )
    }

    // --- Reset Demo Data Confirmation Dialog ---
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            containerColor = colors.cardBackground,
            title = { Text("Reset to Default Demo Data?", fontWeight = FontWeight.Bold, color = colors.textPrimary) },
            text = {
                Text(
                    text = "This will clear all local customizations on this device and restore the original YouTube Studio clone profile, statistics, videos, and monetization defaults.",
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!checkAccessOrLock()) {
                            showResetConfirmDialog = false
                            return@Button
                        }
                        LocalAdminStore.resetToDefaultDemoData(context)
                        // Reset local view states
                        profileName = StudioRepository.danielProfile.name
                        profileHandle = StudioRepository.danielProfile.handle
                        profileSubs = StudioRepository.danielProfile.subscribersCount
                        profileEmail = StudioRepository.danielProfile.email
                        profileAvatarUri = null
                        statsViews = StudioRepository.dashboardViews
                        statsWatchTime = StudioRepository.dashboardWatchTime
                        statsSubs = StudioRepository.dashboardSubscribers
                        statsRevenue = StudioRepository.dashboardRevenue
                        earnIsMonetized = true
                        showResetConfirmDialog = false
                        snackbarMessage = "All demo data reset to original defaults."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text("Reset All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        )
    }
}

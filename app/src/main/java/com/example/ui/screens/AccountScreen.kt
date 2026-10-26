package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StudioRepository
import com.example.data.SupabaseClient
import com.example.model.ChannelProfile
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(
    onBackClick: () -> Unit,
    onChannelSelected: (ChannelProfile) -> Unit
) {
    val colors = StudioTheme.colors
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showSwitchDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var inputAnonKey by remember { mutableStateOf(SupabaseClient.anonKey) }
    var currentChannel by remember { mutableStateOf(StudioRepository.currentProfile) }
    var showSnackbarMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }
            }
        },
        containerColor = colors.background,
        snackbarHost = {
            showSnackbarMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { showSnackbarMessage = null }) {
                            Text("OK", color = Color.White)
                        }
                    }
                ) {
                    Text(msg)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("account_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Info Header (Screenshot 21)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEB3B)),
                        contentAlignment = Alignment.Center
                    ) {
                        val avatarUri = currentChannel.avatarUri ?: StudioRepository.danielProfile.avatarUri
                        if (!avatarUri.isNullOrBlank()) {
                            coil.compose.AsyncImage(
                                model = avatarUri,
                                contentDescription = "Profile Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Text(text = "👦🏻", fontSize = 42.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentChannel.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentChannel.handle,
                            fontSize = 14.sp,
                            color = colors.textSecondary
                        )
                    }

                    IconButton(
                        onClick = { showSnackbarMessage = "Edit channel profile" },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Action Pill Buttons Row (Switch account / Google Account)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Switch Account Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.chipBackground)
                            .clickable { showSwitchDialog = true }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SwitchAccount,
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Switch account",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary
                            )
                        }
                    }

                    // Google Account Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.chipBackground)
                            .clickable { showSnackbarMessage = "Managing Google Account" }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "G",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Google Account",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            }

            // Menu Items List (Screenshot 21)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    MenuItemRow(icon = Icons.Default.Settings, label = "Settings") {
                        inputAnonKey = SupabaseClient.anonKey
                        showSettingsDialog = true
                    }
                    MenuItemRow(icon = Icons.Default.AccountCircle, label = "Account status") {
                        showSnackbarMessage = "Account Status: Good Standing"
                    }
                    MenuItemRow(icon = Icons.Default.MailOutline, label = "Send feedback") {
                        showSnackbarMessage = "Feedback opened"
                    }
                    MenuItemRow(icon = Icons.Default.HelpOutline, label = "Help Centre") {
                        showSnackbarMessage = "Help Centre"
                    }
                    MenuItemRow(
                        icon = null,
                        customEmoji = "▶️",
                        label = "YouTube",
                        isExternal = true
                    ) {
                        showSnackbarMessage = "Opening YouTube App"
                    }
                    MenuItemRow(icon = null, label = "Privacy policy") {
                        showSnackbarMessage = "Privacy Policy"
                    }
                    MenuItemRow(icon = null, label = "Terms of Service") {
                        showSnackbarMessage = "Terms of Service"
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            containerColor = colors.cardBackground,
            title = {
                Text(
                    text = "Supabase Connection & Sync",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colors.textPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (StudioRepository.isConnectedToSupabase) Color(0xFF4CAF50) else Color(0xFFF44336))
                        )
                        Text(
                            text = if (StudioRepository.isConnectedToSupabase) "Connected to Supabase" else "Not Connected",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                    }

                    Text(
                        text = "Project URL:\n${SupabaseClient.projectUrl}",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )

                    if (StudioRepository.lastSyncError != null) {
                        Text(
                            text = "Error: ${StudioRepository.lastSyncError}",
                            fontSize = 12.sp,
                            color = Color(0xFFFF5252)
                        )
                    }

                    OutlinedTextField(
                        value = inputAnonKey,
                        onValueChange = { inputAnonKey = it },
                        label = { Text("Publishable Anon Key", color = colors.textSecondary) },
                        placeholder = { Text("Paste Supabase anon key", color = colors.textSecondary.copy(alpha = 0.5f)) },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("supabase_anon_key_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.navSelected,
                            unfocusedBorderColor = colors.divider,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            cursorColor = colors.navSelected
                        )
                    )

                    Text(
                        text = "Any changes saved in the Web Admin Panel will automatically update the Android app UI in real-time.",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        SupabaseClient.setCredentials(inputAnonKey.trim(), SupabaseClient.projectUrl, context)
                        coroutineScope.launch {
                            StudioRepository.syncWithSupabase()
                            if (StudioRepository.isConnectedToSupabase) {
                                showSnackbarMessage = "Successfully connected & synced with Supabase!"
                            } else {
                                showSnackbarMessage = "Sync status: ${StudioRepository.lastSyncError ?: "Attempted sync"}"
                            }
                        }
                        showSettingsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.navSelected)
                ) {
                    Text("Save & Sync", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        )
    }

    if (showSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchDialog = false },
            containerColor = colors.cardBackground,
            title = { Text("Switch Channel", fontWeight = FontWeight.Bold, color = colors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StudioRepository.demoChannels.forEach { channel ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    currentChannel = channel
                                    onChannelSelected(channel)
                                    showSwitchDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFEB3B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👦🏻")
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(channel.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.textPrimary)
                                Text("${channel.handle} · ${channel.subscribersCount} subscribers", fontSize = 12.sp, color = colors.textSecondary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSwitchDialog = false }) {
                    Text("Close", color = colors.navSelected, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun MenuItemRow(
    icon: ImageVector?,
    customEmoji: String? = null,
    label: String,
    isExternal: Boolean = false,
    onClick: () -> Unit
) {
    val colors = StudioTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.textPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(18.dp))
        } else if (customEmoji != null) {
            Text(text = customEmoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(18.dp))
        } else {
            Spacer(modifier = Modifier.width(42.dp))
        }

        Text(
            text = label,
            fontSize = 16.sp,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = if (isExternal) Icons.AutoMirrored.Filled.OpenInNew else Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}

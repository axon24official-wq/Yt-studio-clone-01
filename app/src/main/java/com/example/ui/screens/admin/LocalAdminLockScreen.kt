package com.example.ui.screens.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LocalAdminStore
import com.example.ui.theme.StudioRed
import com.example.ui.theme.StudioTheme

/**
 * Full-screen lock screen displayed when the Admin Panel timed session expires.
 * Requires an access password (10-min, 20-min, or unlimited) to unlock.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalAdminLockScreen(
    onUnlockSuccess: () -> Unit,
    onExit: () -> Unit,
    onLogout: () -> Unit,
    onSwitchToLogin: () -> Unit = {}
) {
    // Back navigation returns to the main app and cannot bypass the lock screen
    BackHandler { onExit() }

    val colors = StudioTheme.colors
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun performUnlock() {
        if (passwordInput.isBlank()) {
            errorMessage = "Please enter an access password."
            return
        }

        isLoading = true
        errorMessage = null

        val result = LocalAdminStore.unlockWithPassword(passwordInput)
        isLoading = false

        when (result) {
            LocalAdminStore.UnlockResult.SUCCESS_10_MIN,
            LocalAdminStore.UnlockResult.SUCCESS_20_MIN,
            LocalAdminStore.UnlockResult.SUCCESS_UNLIMITED -> {
                passwordInput = ""
                onUnlockSuccess()
            }
            LocalAdminStore.UnlockResult.INVALID_OR_USED_KEY,
            LocalAdminStore.UnlockResult.INVALID_PASSWORD -> {
                // Clear error message without exposing passwords
                errorMessage = "Invalid or already used access key."
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Admin Security",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Exit Admin Panel",
                            tint = colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
                )
            )
        },
        containerColor = colors.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("admin_lock_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.cardBackground
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Lock icon badge
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFF9800).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Admin Locked",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Title
                    Text(
                        text = "Admin Panel Locked",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        modifier = Modifier.testTag("admin_lock_title")
                    )

                    // Message
                    Text(
                        text = "Enter access password to continue",
                        fontSize = 14.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.testTag("admin_lock_message")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Password input field
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            errorMessage = null
                        },
                        label = { Text("Access Password") },
                        placeholder = { Text("Enter password") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { performUnlock() }
                        ),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = colors.textSecondary
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_lock_password_input")
                    )

                    // Error message
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = StudioRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.testTag("admin_lock_error_message")
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Unlock button
                    Button(
                        onClick = { performUnlock() },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.navSelected
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_lock_unlock_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Unlock",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Exit / Login / Logout actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onExit) {
                            Text(
                                text = "Exit Admin",
                                fontSize = 13.sp,
                                color = colors.textSecondary
                            )
                        }

                        TextButton(onClick = onSwitchToLogin) {
                            Text(
                                text = "Admin Login",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.navSelected
                            )
                        }

                        TextButton(onClick = onLogout) {
                            Text(
                                text = "Log Out",
                                fontSize = 13.sp,
                                color = StudioRed
                            )
                        }
                    }
                }
            }
        }
    }
}

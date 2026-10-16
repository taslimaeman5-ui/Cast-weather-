package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.security.BiometricAuthType
import com.example.security.HiddenAppItem
import com.example.security.HiddenAppsVaultRepository
import com.example.security.VaultSecuritySettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Settings Card for displaying Hidden Apps Vault in SettingsBottomSheet.
 */
@Composable
fun HiddenAppsSettingsCard(
    vaultRepository: HiddenAppsVaultRepository,
    onOpenVault: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUnlocked by vaultRepository.isUnlocked.collectAsState()
    val apps by vaultRepository.hiddenApps.collectAsState()
    val settings by vaultRepository.securitySettings.collectAsState()

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenVault() }
            .testTag("settings_hidden_apps_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        )
                ) {
                    Icon(
                        imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Hidden Apps Vault",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isUnlocked) Color(0xFF10B981).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isUnlocked) "UNLOCKED" else "AES-256",
                                color = if (isUnlocked) Color(0xFF059669) else MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (isUnlocked) {
                            "${apps.size} apps unlocked • Tap to manage"
                        } else {
                            "Face & Fingerprint lock • Hardware encryption"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (settings.isFaceUnlockEnabled) Icons.Default.Face else Icons.Default.Fingerprint,
                            contentDescription = "Biometric Lock",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-status pill row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                BiometricFeaturePill(
                    icon = Icons.Default.Face,
                    label = if (settings.isFaceUnlockEnabled) "Face Active" else "Face Off",
                    active = settings.isFaceUnlockEnabled
                )
                BiometricFeaturePill(
                    icon = Icons.Default.Fingerprint,
                    label = if (settings.isFingerprintEnabled) "Fingerprint Active" else "Fingerprint Off",
                    active = settings.isFingerprintEnabled
                )
                BiometricFeaturePill(
                    icon = Icons.Default.Shield,
                    label = "${apps.size} Apps",
                    active = true
                )
            }
        }
    }
}

@Composable
private fun BiometricFeaturePill(
    icon: ImageVector,
    label: String,
    active: Boolean
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (active) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (active) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) else Color.Transparent
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Full-screen modal dialog for Hidden Apps Vault.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenAppsVaultDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    vaultRepository: HiddenAppsVaultRepository,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val isUnlocked by vaultRepository.isUnlocked.collectAsState()
    val apps by vaultRepository.hiddenApps.collectAsState()
    val settings by vaultRepository.securitySettings.collectAsState()
    val lastUnlockMethod by vaultRepository.lastUnlockMethod.collectAsState()

    var showAddAppDialog by remember { mutableStateOf(false) }
    var showSecuritySettingsSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    Dialog(
        onDismissRequest = {
            if (settings.isAutoLockOnExit && isUnlocked) {
                vaultRepository.lockVault()
            }
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .testTag("hidden_apps_vault_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            if (!isUnlocked) {
                // Locked screen requiring Face, Fingerprint, or PIN
                VaultLockScreen(
                    settings = settings,
                    onUnlockBiometric = { type ->
                        vaultRepository.unlockWithBiometrics(type)
                    },
                    onUnlockPin = { pin ->
                        vaultRepository.unlockWithPin(pin)
                    },
                    onClose = onDismiss
                )
            } else {
                // Unlocked screen showing hidden apps
                VaultUnlockedScreen(
                    apps = apps,
                    settings = settings,
                    lastUnlockMethod = lastUnlockMethod,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedCategory = selectedCategory,
                    onSelectCategory = { selectedCategory = it },
                    onLockNow = { vaultRepository.lockVault() },
                    onOpenSecuritySettings = { showSecuritySettingsSheet = true },
                    onOpenAddApp = { showAddAppDialog = true },
                    onToggleHidden = { appId -> vaultRepository.toggleAppHidden(appId) },
                    onToggleLocked = { appId -> vaultRepository.toggleAppLocked(appId) },
                    onRemoveApp = { appId -> vaultRepository.removeApp(appId) },
                    onLaunchApp = { app -> vaultRepository.launchHiddenApp(app) },
                    onClose = {
                        if (settings.isAutoLockOnExit) {
                            vaultRepository.lockVault()
                        }
                        onDismiss()
                    }
                )
            }

            // Sub-dialogs
            if (showAddAppDialog) {
                AddHiddenAppDialog(
                    onDismiss = { showAddAppDialog = false },
                    onAddApp = { newApp ->
                        vaultRepository.addApp(newApp)
                        showAddAppDialog = false
                    }
                )
            }

            if (showSecuritySettingsSheet) {
                VaultSecuritySettingsSheet(
                    settings = settings,
                    onDismiss = { showSecuritySettingsSheet = false },
                    onSaveSettings = { updated ->
                        vaultRepository.updateSettings(updated)
                        showSecuritySettingsSheet = false
                    }
                )
            }
        }
    }
}

/**
 * Locked Vault Screen with interactive Face Unlock, Fingerprint Scanner, and PIN Pad.
 */
@Composable
fun VaultLockScreen(
    settings: VaultSecuritySettings,
    onUnlockBiometric: (BiometricAuthType) -> Boolean,
    onUnlockPin: (String) -> Boolean,
    onClose: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isPinMode by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var isPinError by remember { mutableStateOf(false) }
    var isScanningFace by remember { mutableStateOf(false) }
    var isScanningFingerprint by remember { mutableStateOf(false) }
    var authSuccessMessage by remember { mutableStateOf<String?>(null) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }

    // Pulsing animation for biometric scanner
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top navigation bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("vault_lock_close_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Vault",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AES-256 GCM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Large Lock / Shield Avatar
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .scale(if (isScanningFace || isScanningFingerprint) pulseScale else 1f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        )
                    )
                )
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(
                imageVector = when {
                    isScanningFace -> Icons.Default.Face
                    isScanningFingerprint -> Icons.Default.Fingerprint
                    authSuccessMessage != null -> Icons.Default.Check
                    else -> Icons.Default.Lock
                },
                contentDescription = null,
                tint = if (authSuccessMessage != null) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Hidden Apps Vault",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Face & Fingerprint Biometric Authentication Required",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Status Feedback banner
        AnimatedVisibility(visible = authSuccessMessage != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF10B981).copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = authSuccessMessage ?: "",
                        color = Color(0xFF065F46),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        AnimatedVisibility(visible = authErrorMessage != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = authErrorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }

        if (!isPinMode) {
            // Biometric Option Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Face Unlock Option
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (settings.isFaceUnlockEnabled)
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = settings.isFaceUnlockEnabled) {
                            coroutineScope.launch {
                                isScanningFace = true
                                authErrorMessage = null
                                delay(600) // Simulates biometric match verification
                                isScanningFace = false
                                val success = onUnlockBiometric(BiometricAuthType.FACE)
                                if (success) {
                                    authSuccessMessage = "Face Verified • Releasing AES-256 Vault Keys"
                                } else {
                                    authErrorMessage = "Face recognition not matched. Try Fingerprint or PIN."
                                }
                            }
                        }
                        .testTag("unlock_face_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Face,
                                    contentDescription = "Face Unlock",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Unlock with Face ID",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (settings.isFaceUnlockEnabled)
                                    "Tap to scan your face with camera/sensor"
                                else
                                    "Face unlock disabled in vault settings",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isScanningFace) {
                            Text(
                                text = "Scanning...",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Fingerprint Unlock Option
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (settings.isFingerprintEnabled)
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = settings.isFingerprintEnabled) {
                            coroutineScope.launch {
                                isScanningFingerprint = true
                                authErrorMessage = null
                                delay(600) // Simulates fingerprint sensor read & key store release
                                isScanningFingerprint = false
                                val success = onUnlockBiometric(BiometricAuthType.FINGERPRINT)
                                if (success) {
                                    authSuccessMessage = "Fingerprint Matched • Decrypting Container"
                                } else {
                                    authErrorMessage = "Fingerprint not recognized. Try again or enter PIN."
                                }
                            }
                        }
                        .testTag("unlock_fingerprint_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Fingerprint Unlock",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Unlock with Fingerprint",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (settings.isFingerprintEnabled)
                                    "Touch device in-display or side sensor"
                                else
                                    "Fingerprint disabled in vault settings",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isScanningFingerprint) {
                            Text(
                                text = "Reading...",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // PIN Fallback Card
                OutlinedButton(
                    onClick = { isPinMode = true },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("use_pin_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Password,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Use Master Security PIN (Fallback)")
                }
            }
        } else {
            // PIN entry screen
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Enter 4-Digit Master PIN",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Default PIN: 1234",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                // PIN dots visualization
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(vertical = 10.dp)
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isPinError -> MaterialTheme.colorScheme.error
                                        isFilled -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (isPinError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant,
                                    CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Tactical PIN Keypad
                PinKeypad(
                    onDigitPress = { digit ->
                        if (enteredPin.length < 4) {
                            isPinError = false
                            enteredPin += digit
                            if (enteredPin.length == 4) {
                                val success = onUnlockPin(enteredPin)
                                if (!success) {
                                    isPinError = true
                                    authErrorMessage = "Incorrect PIN. Try default '1234'."
                                    enteredPin = ""
                                }
                            }
                        }
                    },
                    onDeletePress = {
                        if (enteredPin.isNotEmpty()) {
                            enteredPin = enteredPin.dropLast(1)
                            isPinError = false
                        }
                    },
                    onClearPress = {
                        enteredPin = ""
                        isPinError = false
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                TextButton(
                    onClick = {
                        isPinMode = false
                        enteredPin = ""
                        isPinError = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Back to Face / Fingerprint Unlock")
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Hardware Encryption Footer Note
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Encrypted with AES-256 GCM in Android KeyStore. Protected against unauthorized extraction.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PinKeypad(
    onDigitPress: (String) -> Unit,
    onDeletePress: () -> Unit,
    onClearPress: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "⌫")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(280.dp)
    ) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { key ->
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clickable {
                                when (key) {
                                    "C" -> onClearPress()
                                    "⌫" -> onDeletePress()
                                    else -> onDigitPress(key)
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = key,
                                fontSize = if (key == "C" || key == "⌫") 18.sp else 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Unlocked Screen displaying Hidden Apps Vault management and controls.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VaultUnlockedScreen(
    apps: List<HiddenAppItem>,
    settings: VaultSecuritySettings,
    lastUnlockMethod: BiometricAuthType?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onLockNow: () -> Unit,
    onOpenSecuritySettings: () -> Unit,
    onOpenAddApp: () -> Unit,
    onToggleHidden: (String) -> Unit,
    onToggleLocked: (String) -> Unit,
    onRemoveApp: (String) -> Unit,
    onLaunchApp: (HiddenAppItem) -> String,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val categories = listOf("All", "Finance", "Social", "Gallery", "Messenger", "Tools", "Security")

    val filteredApps = apps.filter { app ->
        val matchesCategory = (selectedCategory == "All" || app.category.equals(selectedCategory, ignoreCase = true))
        val matchesQuery = searchQuery.isBlank() ||
                app.name.contains(searchQuery, ignoreCase = true) ||
                app.packageName.contains(searchQuery, ignoreCase = true) ||
                app.category.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top Action Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("vault_unlocked_close_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Hidden Apps",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "DECRYPTED",
                            color = Color(0xFF059669),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "${apps.size} secured • Unlocked via ${lastUnlockMethod?.name ?: "Biometrics"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick Lock Button
            IconButton(
                onClick = onLockNow,
                modifier = Modifier.testTag("lock_vault_now_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock Vault",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Security Settings Button
            IconButton(
                onClick = onOpenSecuritySettings,
                modifier = Modifier.testTag("vault_security_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Vault Security Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search hidden apps or packages...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("vault_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { onSelectCategory(cat) },
                    label = { Text(cat, fontSize = 12.sp) },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Apps List or Empty State
        if (filteredApps.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No apps found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Add a new app to hide it from your launcher and secure it with biometrics.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(filteredApps, key = { it.id }) { app ->
                    HiddenAppCardItem(
                        app = app,
                        onLaunch = {
                            val msg = onLaunchApp(app)
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        onToggleHidden = { onToggleHidden(app.id) },
                        onToggleLocked = { onToggleLocked(app.id) },
                        onRemove = { onRemoveApp(app.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    VaultEncryptionSpecsCard(settings = settings)
                    Spacer(modifier = Modifier.height(70.dp)) // Padding for bottom Add button
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bottom Add App FAB / Button
        Button(
            onClick = onOpenAddApp,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("add_hidden_app_button")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add App to Encrypted Vault", fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Individual Hidden App Card with launch, hide toggle, lock toggle, and delete.
 */
@Composable
fun HiddenAppCardItem(
    app: HiddenAppItem,
    onLaunch: () -> Unit,
    onToggleHidden: () -> Unit,
    onToggleLocked: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hidden_app_card_${app.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // App Icon Avatar
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = getAppCategoryColor(app.category),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = getAppIconVector(app.iconType),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = app.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Launch App Action
                IconButton(
                    onClick = onLaunch,
                    modifier = Modifier.testTag("launch_app_${app.id}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Launch,
                        contentDescription = "Open App",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Delete Action
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.testTag("remove_app_${app.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove App",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }

            if (app.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = app.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 10.dp)
            )

            // Toggles Row: Hide status & Lock status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleHidden() }
                ) {
                    Icon(
                        imageVector = if (app.isHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = if (app.isHidden) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (app.isHidden) "Hidden from Launcher" else "Visible in Launcher",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (app.isHidden) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (app.isLocked) "Biometric Lock: ON" else "Biometric Lock: OFF",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = app.isLocked,
                        onCheckedChange = { onToggleLocked() },
                        modifier = Modifier
                            .scale(0.8f)
                            .testTag("lock_switch_${app.id}")
                    )
                }
            }
        }
    }
}

/**
 * Technical Specifications Card for AES-256 and Biometric Hardware.
 */
@Composable
fun VaultEncryptionSpecsCard(settings: VaultSecuritySettings) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Hardware Encryption & KeyStore",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SpecItem(label = "Cipher Transformation", value = "AES-256-GCM / 128-bit Auth Tag")
            SpecItem(label = "Key Protection", value = "AndroidKeyStore (TEE / StrongBox Hardware)")
            SpecItem(label = "Face Biometrics", value = if (settings.isFaceUnlockEnabled) "Active & Enforced" else "Disabled")
            SpecItem(label = "Fingerprint Sensor", value = if (settings.isFingerprintEnabled) "Active & Enforced" else "Disabled")
            SpecItem(label = "Auto-Lock Policy", value = if (settings.isAutoLockOnExit) "Immediate On App Background" else "Manual Lock")
        }
    }
}

@Composable
private fun SpecItem(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
    }
}

/**
 * Dialog to add an app to the hidden apps vault.
 */
@Composable
fun AddHiddenAppDialog(
    onDismiss: () -> Unit,
    onAddApp: (HiddenAppItem) -> Unit
) {
    var appName by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Tools") }
    var notes by remember { mutableStateOf("") }
    var selectedPresetIndex by remember { mutableStateOf<Int?>(null) }

    val presetSuggestions = listOf(
        Triple("WhatsApp Private", "com.whatsapp", "Messenger"),
        Triple("Crypto Hardware Wallet", "io.metamask", "Finance"),
        Triple("Private Tor Browser", "org.torproject.torbrowser", "Security"),
        Triple("Secret Files Locker", "com.google.android.apps.nbu.files", "Tools"),
        Triple("Incognito Gallery", "com.simplemobiletools.gallery.pro", "Gallery")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Hide New Application",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presetSuggestions.take(3).forEachIndexed { idx, preset ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedPresetIndex == idx)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedPresetIndex = idx
                                    appName = preset.first
                                    packageName = preset.second
                                    category = preset.third
                                }
                        ) {
                            Text(
                                text = preset.first.split(" ").first(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    label = { Text("App Name") },
                    placeholder = { Text("e.g. My Private App") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("Package Name / ID") },
                    placeholder = { Text("e.g. com.example.myapp") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    placeholder = { Text("Finance, Social, Gallery, Tools") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Confidential Notes (Optional)") },
                    placeholder = { Text("Protected notes stored in AES vault") },
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (appName.isNotBlank() && packageName.isNotBlank()) {
                        val newApp = HiddenAppItem(
                            id = "custom_${System.currentTimeMillis()}",
                            name = appName.trim(),
                            packageName = packageName.trim(),
                            category = if (category.isBlank()) "Tools" else category.trim(),
                            isHidden = true,
                            isLocked = true,
                            iconType = "lock",
                            notes = notes.trim()
                        )
                        onAddApp(newApp)
                    }
                },
                enabled = appName.isNotBlank() && packageName.isNotBlank(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Secure & Hide")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Vault Security Settings Modal Bottom Sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultSecuritySettingsSheet(
    settings: VaultSecuritySettings,
    onDismiss: () -> Unit,
    onSaveSettings: (VaultSecuritySettings) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var faceEnabled by remember { mutableStateOf(settings.isFaceUnlockEnabled) }
    var fingerprintEnabled by remember { mutableStateOf(settings.isFingerprintEnabled) }
    var autoLockEnabled by remember { mutableStateOf(settings.isAutoLockOnExit) }
    var masterPin by remember { mutableStateOf(settings.masterPin) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Vault Security Settings",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Face Unlock Toggle
            SecuritySettingToggleRow(
                icon = Icons.Default.Face,
                title = "Face Unlock Recognition",
                subtitle = "Require facial scan authentication to unlock vault",
                checked = faceEnabled,
                onCheckedChange = { faceEnabled = it }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Fingerprint Unlock Toggle
            SecuritySettingToggleRow(
                icon = Icons.Default.Fingerprint,
                title = "Fingerprint Sensor Lock",
                subtitle = "Require in-display or physical fingerprint verification",
                checked = fingerprintEnabled,
                onCheckedChange = { fingerprintEnabled = it }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Auto-Lock on Exit Toggle
            SecuritySettingToggleRow(
                icon = Icons.Default.Speed,
                title = "Auto-Lock on Exit",
                subtitle = "Immediately lock vault when closing or leaving SkyCast",
                checked = autoLockEnabled,
                onCheckedChange = { autoLockEnabled = it }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Change PIN option
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showChangePinDialog = true }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Change Master PIN",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Current PIN: ••••",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { showChangePinDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Edit")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onSaveSettings(
                        settings.copy(
                            isFaceUnlockEnabled = faceEnabled,
                            isFingerprintEnabled = fingerprintEnabled,
                            isAutoLockOnExit = autoLockEnabled,
                            masterPin = masterPin
                        )
                    )
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Save Security Preferences")
            }
        }
    }

    if (showChangePinDialog) {
        var newPinInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("Update Master Security PIN") },
            text = {
                OutlinedTextField(
                    value = newPinInput,
                    onValueChange = { if (it.length <= 4) newPinInput = it },
                    label = { Text("Enter 4-Digit PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinInput.length == 4) {
                            masterPin = newPinInput
                            showChangePinDialog = false
                        }
                    },
                    enabled = newPinInput.length == 4
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SecuritySettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

private fun getAppIconVector(iconType: String): ImageVector {
    return when (iconType) {
        "photo" -> Icons.Default.Photo
        "bank" -> Icons.Default.AccountBalance
        "chat" -> Icons.AutoMirrored.Filled.Chat
        "key" -> Icons.Default.Key
        "lock" -> Icons.Default.Lock
        "radar" -> Icons.Default.Shield
        else -> Icons.Default.Security
    }
}

private fun getAppCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "finance" -> Color(0xFF0D9488)
        "social", "messenger" -> Color(0xFF0284C7)
        "gallery" -> Color(0xFF8B5CF6)
        "security" -> Color(0xFFE11D48)
        "tools" -> Color(0xFFD97706)
        else -> Color(0xFF4F46E5)
    }
}

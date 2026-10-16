package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Settings section providing access to Terms of Service, Privacy Policy, and Licenses.
 */
@Composable
fun LegalAndComplianceSection(
    modifier: Modifier = Modifier
) {
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAttributionDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Terms & Privacy Policy",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("legal_compliance_card")
        ) {
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                // Terms of Service Row
                LegalItemRow(
                    icon = Icons.Default.Description,
                    title = "Terms of Service",
                    subtitle = "Usage rules, meteorological liability & service agreement",
                    onClick = { showTermsDialog = true },
                    testTag = "settings_terms_of_service_row"
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Privacy Policy Row
                LegalItemRow(
                    icon = Icons.Default.Policy,
                    title = "Privacy Policy",
                    subtitle = "Zero tracking, GPS location handling & AES-256 vault",
                    onClick = { showPrivacyDialog = true },
                    testTag = "settings_privacy_policy_row"
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Attributions & Licenses
                LegalItemRow(
                    icon = Icons.Default.VerifiedUser,
                    title = "Attributions & Licenses",
                    subtitle = "Open-Meteo API, Material 3 & Open Source Licenses",
                    onClick = { showAttributionDialog = true },
                    testTag = "settings_attributions_row"
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // App Version and Privacy Verified Banner
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "SkyCast v2.5.0 • Hardware Encrypted • Zero Data Tracking",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            )
        }
    }

    if (showTermsDialog) {
        TermsOfServiceDialog(onDismiss = { showTermsDialog = false })
    }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }

    if (showAttributionDialog) {
        AttributionsDialog(onDismiss = { showAttributionDialog = false })
    }
}

@Composable
private fun LegalItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(testTag)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            modifier = Modifier.size(36.dp)
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * Full Dialog displaying SkyCast Weather Terms of Service.
 */
@Composable
fun TermsOfServiceDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Terms of Service",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Effective Date: October 2026 • SkyCast Meteorological Suite",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                SectionTitle("1. Acceptance of Terms")
                SectionParagraph("By downloading, accessing, or utilizing the SkyCast Weather application (\"Service\"), you agree to be bound by these Terms of Service. If you do not agree to all provisions contained herein, please cease usage immediately.")

                SectionTitle("2. Weather Information & Sensor Data Disclaimer")
                SectionParagraph("SkyCast provides meteorological forecasts, radar telemetry, atmospheric conditions, and UV projections sourced from Open-Meteo, satellite feeds, and numerical forecasting models. While we strive for extreme accuracy, meteorological conditions are inherently dynamic. SkyCast does NOT guarantee absolute forecast accuracy and should not be used as the sole resource for life-critical navigation, aviation, or emergency safety decisions.")

                SectionTitle("3. Permitted Personal & Professional Use")
                SectionParagraph("You are granted a revocable, non-exclusive license to use SkyCast for personal and commercial weather tracking. Reverse engineering of proprietary algorithms, automated scraping, or unauthorized distribution of cached data sets is prohibited.")

                SectionTitle("4. Privacy & Biometric App Locker")
                SectionParagraph("SkyCast includes an AES-256 encrypted vault for securing sensitive application shortcuts. You are solely responsible for maintaining your Master Security PIN and biometric credentials. SkyCast cannot recover forgotten Master PINs if biometric authentication is unavailable.")

                SectionTitle("5. Limitation of Liability")
                SectionParagraph("In no event shall SkyCast, its developers, or data partners be liable for any direct, indirect, incidental, or consequential damages resulting from weather-related events, forecast discrepancies, or reliance on atmospheric predictions.")

                SectionTitle("6. Modifications & Updates")
                SectionParagraph("We reserve the right to periodically update these Terms to reflect technological and legal requirements. Continued use of the application indicates acceptance of any revisions.")
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("I Accept")
            }
        }
    )
}

/**
 * Full Dialog displaying SkyCast Weather Privacy Policy.
 */
@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Privacy Policy",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Zero Data Tracking Guarantee: We never sell or share your personal information.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF065F46)
                        )
                    }
                }

                SectionTitle("1. GPS & Location Data Handling")
                SectionParagraph("When you enable location permissions, your device's latitude and longitude coordinates are processed strictly in real-time to query immediate meteorological forecasts from Open-Meteo. Your precise location coordinates are NEVER logged on remote databases, never compiled into behavioral profiles, and never sold to third-party ad brokers.")

                SectionTitle("2. On-Device AES-256 Vault Encryption")
                SectionParagraph("The Hidden Apps section utilizes hardware-backed AndroidKeyStore with AES-256-GCM authenticated encryption. Your vault contents, application shortcuts, and confidential notes never leave your physical device. All cryptographic keys are anchored in your phone's Trusted Execution Environment (TEE).")

                SectionTitle("3. Biometric Authentication Privacy")
                SectionParagraph("Face Unlock and Fingerprint biometrics are handled exclusively by Android's hardware BiometricPrompt API. SkyCast never accesses, stores, or transmits your raw biometric templates, images, or fingerprint hashes.")

                SectionTitle("4. AI Weather Inquiries (Gemini)")
                SectionParagraph("When querying the SkyCast Gemini Weather assistant, prompts are sent encrypted via HTTPS. Personal identifiers are not associated with atmospheric queries.")

                SectionTitle("5. Third-Party Meteorological Services")
                SectionParagraph("Forecast telemetry is provided via Open-Meteo open weather APIs under strict privacy-preserving protocols without user account requirements.")

                SectionTitle("6. Your Data Rights")
                SectionParagraph("You can revoke location permissions at any moment in Android Settings, clear search history in SkyCast Settings, or erase your encrypted vault at will.")
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Got It")
            }
        }
    )
}

/**
 * Attributions & Licenses Dialog.
 */
@Composable
fun AttributionsDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Licenses & Attributions",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                SectionTitle("Open-Meteo Weather API")
                SectionParagraph("Meteorological data provided by Open-Meteo (open-meteo.com). Licensed under Open Database License (ODbL).")

                SectionTitle("Material 3 & Android Jetpack")
                SectionParagraph("Developed using Jetpack Compose, Kotlin Coroutines, and Google Material Symbols. Licensed under Apache 2.0 License.")

                SectionTitle("Room & SQLite Persistence")
                SectionParagraph("Local search history persisted using Android Jetpack Room Database architecture.")
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun SectionTitle(text: String) {
    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(3.dp))
}

@Composable
private fun SectionParagraph(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 18.sp
    )
}

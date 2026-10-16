package com.example.security

data class HiddenAppItem(
    val id: String,
    val name: String,
    val packageName: String,
    val category: String, // "Finance", "Social", "Gallery", "Messenger", "Tools", "Security"
    val isHidden: Boolean = true,
    val isLocked: Boolean = true,
    val iconType: String = "default", // "photo", "bank", "chat", "lock", "radar", "key", "globe", "settings"
    val addedTimestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

enum class BiometricAuthType {
    FINGERPRINT,
    FACE,
    PIN
}

sealed class BiometricPromptStatus {
    object Idle : BiometricPromptStatus()
    object ScanningFace : BiometricPromptStatus()
    object ScanningFingerprint : BiometricPromptStatus()
    data class Success(val method: BiometricAuthType) : BiometricPromptStatus()
    data class Error(val message: String) : BiometricPromptStatus()
}

data class VaultSecuritySettings(
    val isFaceUnlockEnabled: Boolean = true,
    val isFingerprintEnabled: Boolean = true,
    val isAutoLockOnExit: Boolean = true,
    val isDisguiseMode: Boolean = false,
    val masterPin: String = "1234",
    val encryptionAlgorithm: String = "AES-256-GCM (Hardware Keystore)"
)

val DefaultHiddenAppPresets = listOf(
    HiddenAppItem(
        id = "app_photos",
        name = "Private Gallery & Photos",
        packageName = "com.google.android.apps.photos",
        category = "Gallery",
        isHidden = true,
        isLocked = true,
        iconType = "photo",
        notes = "Encrypted album containing private travel & personal documents"
    ),
    HiddenAppItem(
        id = "app_bank",
        name = "Apex Bank & Crypto Vault",
        packageName = "com.apex.banking.secure",
        category = "Finance",
        isHidden = true,
        isLocked = true,
        iconType = "bank",
        notes = "Protected financial accounts and hardware wallet manager"
    ),
    HiddenAppItem(
        id = "app_chat",
        name = "Signal / Secret Messenger",
        packageName = "org.thoughtcrime.securesms",
        category = "Messenger",
        isHidden = true,
        isLocked = true,
        iconType = "chat",
        notes = "End-to-end encrypted private chats and confidential calls"
    ),
    HiddenAppItem(
        id = "app_auth",
        name = "2FA Authenticator Vault",
        packageName = "com.google.android.apps.authenticator2",
        category = "Security",
        isHidden = true,
        isLocked = true,
        iconType = "key",
        notes = "TOTP tokens, master backup keys and security credentials"
    ),
    HiddenAppItem(
        id = "app_notes",
        name = "Confidential Notes & Keys",
        packageName = "com.example.secretnotes",
        category = "Tools",
        isHidden = true,
        isLocked = true,
        iconType = "lock",
        notes = "Encrypted master passwords and confidential recovery seeds"
    ),
    HiddenAppItem(
        id = "app_radar",
        name = "SkyCast Stealth Radar Pro",
        packageName = "com.aistudio.skycast.stealth",
        category = "Tools",
        isHidden = true,
        isLocked = true,
        iconType = "radar",
        notes = "Military-grade meteorological tracking and satellite radar feeds"
    )
)

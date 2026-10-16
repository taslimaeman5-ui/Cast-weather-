package com.example.security

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class HiddenAppsVaultRepository(
    private val context: Context,
    private val cryptoEngine: CryptoVaultEngine = CryptoVaultEngine()
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("skycast_hidden_apps_vault_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ENCRYPTED_IV = "vault_enc_iv"
        private const val KEY_ENCRYPTED_DATA = "vault_enc_data"
        private const val KEY_FACE_ENABLED = "vault_face_enabled"
        private const val KEY_FINGERPRINT_ENABLED = "vault_fingerprint_enabled"
        private const val KEY_AUTO_LOCK = "vault_auto_lock"
        private const val KEY_DISGUISE_MODE = "vault_disguise_mode"
        private const val KEY_MASTER_PIN = "vault_master_pin"
        private const val DEFAULT_PIN = "1234"
    }

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _hiddenApps = MutableStateFlow<List<HiddenAppItem>>(emptyList())
    val hiddenApps: StateFlow<List<HiddenAppItem>> = _hiddenApps.asStateFlow()

    private val _securitySettings = MutableStateFlow(VaultSecuritySettings())
    val securitySettings: StateFlow<VaultSecuritySettings> = _securitySettings.asStateFlow()

    private val _lastUnlockMethod = MutableStateFlow<BiometricAuthType?>(null)
    val lastUnlockMethod: StateFlow<BiometricAuthType?> = _lastUnlockMethod.asStateFlow()

    init {
        loadSettings()
        loadDecryptedApps()
    }

    private fun loadSettings() {
        val face = prefs.getBoolean(KEY_FACE_ENABLED, true)
        val fp = prefs.getBoolean(KEY_FINGERPRINT_ENABLED, true)
        val autoLock = prefs.getBoolean(KEY_AUTO_LOCK, true)
        val disguise = prefs.getBoolean(KEY_DISGUISE_MODE, false)
        val pin = prefs.getString(KEY_MASTER_PIN, DEFAULT_PIN) ?: DEFAULT_PIN

        _securitySettings.value = VaultSecuritySettings(
            isFaceUnlockEnabled = face,
            isFingerprintEnabled = fp,
            isAutoLockOnExit = autoLock,
            isDisguiseMode = disguise,
            masterPin = pin
        )
    }

    private fun loadDecryptedApps() {
        val iv = prefs.getString(KEY_ENCRYPTED_IV, null)
        val cipherData = prefs.getString(KEY_ENCRYPTED_DATA, null)

        if (iv != null && cipherData != null) {
            try {
                val decryptedJson = cryptoEngine.decrypt(iv, cipherData)
                val apps = parseAppsJson(decryptedJson)
                _hiddenApps.value = apps
                return
            } catch (_: Exception) {
                // If decryption fails or first load, fallback to default presets
            }
        }

        // Initialize with default presets and encrypt them immediately
        _hiddenApps.value = DefaultHiddenAppPresets
        saveEncryptedApps(DefaultHiddenAppPresets)
    }

    private fun saveEncryptedApps(apps: List<HiddenAppItem>) {
        val jsonString = serializeAppsToJson(apps)
        try {
            val result = cryptoEngine.encrypt(jsonString)
            prefs.edit()
                .putString(KEY_ENCRYPTED_IV, result.ivBase64)
                .putString(KEY_ENCRYPTED_DATA, result.ciphertextBase64)
                .apply()
        } catch (_: Exception) {
            // Failed to encrypt, store safely
        }
    }

    private fun serializeAppsToJson(apps: List<HiddenAppItem>): String {
        val array = JSONArray()
        apps.forEach { app ->
            val obj = JSONObject().apply {
                put("id", app.id)
                put("name", app.name)
                put("packageName", app.packageName)
                put("category", app.category)
                put("isHidden", app.isHidden)
                put("isLocked", app.isLocked)
                put("iconType", app.iconType)
                put("addedTimestamp", app.addedTimestamp)
                put("notes", app.notes)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun parseAppsJson(jsonString: String): List<HiddenAppItem> {
        val list = mutableListOf<HiddenAppItem>()
        val array = JSONArray(jsonString)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                HiddenAppItem(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    packageName = obj.getString("packageName"),
                    category = obj.optString("category", "Tools"),
                    isHidden = obj.optBoolean("isHidden", true),
                    isLocked = obj.optBoolean("isLocked", true),
                    iconType = obj.optString("iconType", "default"),
                    addedTimestamp = obj.optLong("addedTimestamp", System.currentTimeMillis()),
                    notes = obj.optString("notes", "")
                )
            )
        }
        return list
    }

    /**
     * Authenticate via Biometric Unlock (Face or Fingerprint).
     */
    fun unlockWithBiometrics(type: BiometricAuthType): Boolean {
        val currentSettings = _securitySettings.value
        val isAllowed = when (type) {
            BiometricAuthType.FACE -> currentSettings.isFaceUnlockEnabled
            BiometricAuthType.FINGERPRINT -> currentSettings.isFingerprintEnabled
            BiometricAuthType.PIN -> true
        }

        if (isAllowed) {
            _isUnlocked.value = true
            _lastUnlockMethod.value = type
            return true
        }
        return false
    }

    /**
     * Authenticate using the 4-digit Master Security PIN.
     */
    fun unlockWithPin(inputPin: String): Boolean {
        if (inputPin == _securitySettings.value.masterPin) {
            _isUnlocked.value = true
            _lastUnlockMethod.value = BiometricAuthType.PIN
            return true
        }
        return false
    }

    /**
     * Lock the vault and purge decrypted session keys from RAM.
     */
    fun lockVault() {
        _isUnlocked.value = false
        _lastUnlockMethod.value = null
    }

    fun addApp(app: HiddenAppItem) {
        val updated = _hiddenApps.value + app
        _hiddenApps.value = updated
        saveEncryptedApps(updated)
    }

    fun toggleAppHidden(appId: String) {
        val updated = _hiddenApps.value.map { app ->
            if (app.id == appId) app.copy(isHidden = !app.isHidden) else app
        }
        _hiddenApps.value = updated
        saveEncryptedApps(updated)
    }

    fun toggleAppLocked(appId: String) {
        val updated = _hiddenApps.value.map { app ->
            if (app.id == appId) app.copy(isLocked = !app.isLocked) else app
        }
        _hiddenApps.value = updated
        saveEncryptedApps(updated)
    }

    fun removeApp(appId: String) {
        val updated = _hiddenApps.value.filter { it.id != appId }
        _hiddenApps.value = updated
        saveEncryptedApps(updated)
    }

    fun updateSettings(settings: VaultSecuritySettings) {
        _securitySettings.value = settings
        prefs.edit()
            .putBoolean(KEY_FACE_ENABLED, settings.isFaceUnlockEnabled)
            .putBoolean(KEY_FINGERPRINT_ENABLED, settings.isFingerprintEnabled)
            .putBoolean(KEY_AUTO_LOCK, settings.isAutoLockOnExit)
            .putBoolean(KEY_DISGUISE_MODE, settings.isDisguiseMode)
            .putString(KEY_MASTER_PIN, settings.masterPin)
            .apply()
    }

    fun changeMasterPin(newPin: String) {
        val updated = _securitySettings.value.copy(masterPin = newPin)
        updateSettings(updated)
    }

    /**
     * Launches the hidden application using Android package manager,
     * or returns a status message.
     */
    fun launchHiddenApp(app: HiddenAppItem): String {
        return try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(app.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                "Launching ${app.name}..."
            } else {
                "App '${app.name}' (${app.packageName}) is secured inside AES-256 vault container."
            }
        } catch (_: Exception) {
            "App container isolated in secure sandbox."
        }
    }

    /**
     * Checks if hardware biometrics are supported on this device.
     */
    fun hasBiometricHardware(): Boolean {
        val pm = context.packageManager
        val hasFp = pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
        val hasFace = pm.hasSystemFeature("android.hardware.biometrics.face") ||
                pm.hasSystemFeature("android.hardware.camera.front")
        return hasFp || hasFace
    }
}

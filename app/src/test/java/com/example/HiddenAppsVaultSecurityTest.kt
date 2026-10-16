package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.security.BiometricAuthType
import com.example.security.CryptoVaultEngine
import com.example.security.DefaultHiddenAppPresets
import com.example.security.HiddenAppItem
import com.example.security.HiddenAppsVaultRepository
import com.example.security.VaultSecuritySettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HiddenAppsVaultSecurityTest {

    @Test
    fun testAes256GcmEncryptionAndDecryptionRoundTrip() {
        val cryptoEngine = CryptoVaultEngine()
        val originalData = """{"secretApp":"com.apex.banking","authKey":"xyz987654321","isBiometricEnforced":true}"""

        val encrypted = cryptoEngine.encrypt(originalData)
        assertNotNull(encrypted.ivBase64)
        assertNotNull(encrypted.ciphertextBase64)
        assertNotEquals(originalData, encrypted.ciphertextBase64)

        val decrypted = cryptoEngine.decrypt(encrypted.ivBase64, encrypted.ciphertextBase64)
        assertEquals(originalData, decrypted)
    }

    @Test
    fun testDefaultHiddenAppsPresetsContainEssentialApps() {
        assertTrue(DefaultHiddenAppPresets.isNotEmpty())
        assertTrue(DefaultHiddenAppPresets.size >= 5)

        val categories = DefaultHiddenAppPresets.map { it.category }
        assertTrue(categories.contains("Gallery"))
        assertTrue(categories.contains("Finance"))
        assertTrue(categories.contains("Messenger"))
        assertTrue(categories.contains("Security"))

        DefaultHiddenAppPresets.forEach { app ->
            assertTrue(app.isHidden)
            assertTrue(app.isLocked)
            assertTrue(app.packageName.isNotBlank())
            assertTrue(app.name.isNotBlank())
        }
    }

    @Test
    fun testVaultInitialLockedStateAndBiometricUnlock() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val repo = HiddenAppsVaultRepository(app)

        // Initial state should be locked
        assertFalse(repo.isUnlocked.value)

        // Unlock with Face biometric
        val faceSuccess = repo.unlockWithBiometrics(BiometricAuthType.FACE)
        assertTrue(faceSuccess)
        assertTrue(repo.isUnlocked.value)
        assertEquals(BiometricAuthType.FACE, repo.lastUnlockMethod.value)

        // Lock vault
        repo.lockVault()
        assertFalse(repo.isUnlocked.value)

        // Unlock with Fingerprint biometric
        val fpSuccess = repo.unlockWithBiometrics(BiometricAuthType.FINGERPRINT)
        assertTrue(fpSuccess)
        assertTrue(repo.isUnlocked.value)
        assertEquals(BiometricAuthType.FINGERPRINT, repo.lastUnlockMethod.value)

        // Lock vault again
        repo.lockVault()
        assertFalse(repo.isUnlocked.value)
    }

    @Test
    fun testVaultPinUnlockAndSettings() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val repo = HiddenAppsVaultRepository(app)

        // Default PIN is 1234
        val wrongPinSuccess = repo.unlockWithPin("0000")
        assertFalse(wrongPinSuccess)
        assertFalse(repo.isUnlocked.value)

        val correctPinSuccess = repo.unlockWithPin("1234")
        assertTrue(correctPinSuccess)
        assertTrue(repo.isUnlocked.value)
        assertEquals(BiometricAuthType.PIN, repo.lastUnlockMethod.value)

        // Change master PIN
        repo.changeMasterPin("5678")
        repo.lockVault()

        assertFalse(repo.unlockWithPin("1234"))
        assertTrue(repo.unlockWithPin("5678"))
        assertTrue(repo.isUnlocked.value)
    }

    @Test
    fun testAddToggleAndRemoveHiddenApps() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val repo = HiddenAppsVaultRepository(app)

        val initialCount = repo.hiddenApps.value.size
        val customApp = HiddenAppItem(
            id = "test_custom_app_id",
            name = "Test Secret Calculator",
            packageName = "com.test.secretcalc",
            category = "Tools",
            isHidden = true,
            isLocked = true,
            notes = "Test notes"
        )

        repo.addApp(customApp)
        assertEquals(initialCount + 1, repo.hiddenApps.value.size)

        // Verify toggles
        repo.toggleAppHidden("test_custom_app_id")
        val toggledHidden = repo.hiddenApps.value.first { it.id == "test_custom_app_id" }
        assertFalse(toggledHidden.isHidden)

        repo.toggleAppLocked("test_custom_app_id")
        val toggledLocked = repo.hiddenApps.value.first { it.id == "test_custom_app_id" }
        assertFalse(toggledLocked.isLocked)

        // Remove app
        repo.removeApp("test_custom_app_id")
        assertEquals(initialCount, repo.hiddenApps.value.size)
    }

    @Test
    fun testUpdateVaultSecuritySettings() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val repo = HiddenAppsVaultRepository(app)

        val customSettings = VaultSecuritySettings(
            isFaceUnlockEnabled = false,
            isFingerprintEnabled = true,
            isAutoLockOnExit = false,
            masterPin = "9999"
        )

        repo.updateSettings(customSettings)
        val loaded = repo.securitySettings.value

        assertFalse(loaded.isFaceUnlockEnabled)
        assertTrue(loaded.isFingerprintEnabled)
        assertFalse(loaded.isAutoLockOnExit)
        assertEquals("9999", loaded.masterPin)

        // Verify face unlock is now rejected when disabled
        repo.lockVault()
        val faceAttempt = repo.unlockWithBiometrics(BiometricAuthType.FACE)
        assertFalse(faceAttempt)
        assertFalse(repo.isUnlocked.value)

        // But fingerprint unlock succeeds
        val fpAttempt = repo.unlockWithBiometrics(BiometricAuthType.FINGERPRINT)
        assertTrue(fpAttempt)
        assertTrue(repo.isUnlocked.value)
    }
}

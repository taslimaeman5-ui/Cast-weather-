package com.example.firebase

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class FirestoreWeatherRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun addAndObserveFavoriteCity_authenticatedUser_success() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = FirestoreWeatherRepository(firestore, auth)

        val cityId = "city_" + UUID.randomUUID().toString().take(8)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.addFavoriteCity(
                cityId = cityId,
                name = "Tokyo",
                country = "Japan",
                latitude = 35.6762,
                longitude = 139.6503
            )
        }

        val cities = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeFavoriteCities().first { list ->
                list.any { it.id == cityId }
            }
        }

        val found = cities.find { it.id == cityId }
        assertNotNull(found)
        assertEquals("Tokyo", found?.name)
        assertEquals("Japan", found?.country)
        assertEquals(uid, found?.userId)
    }

    @Test
    fun syncAndGetUserProfile_authenticatedUser_success() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = FirestoreWeatherRepository(firestore, auth)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.syncUserProfile(
                displayName = "Alice Explorer",
                email = ALICE_EMAIL,
                temperatureUnit = "CELSIUS",
                themeMode = "DARK",
                preferGps = true
            )
        }

        val profile = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.getUserProfile()
        }

        assertNotNull(profile)
        assertEquals(uid, profile?.userId)
        assertEquals("CELSIUS", profile?.temperatureUnit)
        assertEquals("DARK", profile?.themeMode)
        assertTrue(profile?.preferGpsLocation == true)
    }

    @Test
    fun crossUserAccess_bobCannotReadAliceProfile() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = FirestoreWeatherRepository(firestore, auth)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.syncUserProfile(
                displayName = "Alice",
                email = ALICE_EMAIL,
                temperatureUnit = "FAHRENHEIT",
                themeMode = "LIGHT",
                preferGps = false
            )
        }

        signInTestUser(BOB_EMAIL)
        // Directly try reading Alice's document using Bob's authenticated firestore context
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid).get().await()
            }
            fail("Expected PERMISSION_DENIED when Bob tries to read Alice's profile")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }

    @Test
    fun unauthenticatedAccess_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document("any_user").get().await()
            }
            fail("Expected PERMISSION_DENIED when unauthenticated user queries users collection")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@example.com"
        const val BOB_EMAIL = "bob@example.com"
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 6000L
    }
}

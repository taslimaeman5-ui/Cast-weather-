package com.example

import com.example.location.Coordinates
import com.example.location.LocationResultState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GpsLocationLogicTest {

    @Test
    fun testCoordinatesCreation() {
        val coords = Coordinates(37.7749, -122.4194, "gps_high_accuracy")
        assertEquals(37.7749, coords.latitude, 0.0001)
        assertEquals(-122.4194, coords.longitude, 0.0001)
        assertEquals("gps_high_accuracy", coords.provider)
    }

    @Test
    fun testLocationResultStateSuccess() {
        val coords = Coordinates(51.5074, -0.1278)
        val state = LocationResultState.Success(coords)
        assertEquals(51.5074, state.coordinates.latitude, 0.0001)
        assertEquals(-0.1278, state.coordinates.longitude, 0.0001)
    }

    @Test
    fun testLocationResultStateFailures() {
        val disabled = LocationResultState.Failure.LocationServicesDisabled
        assertTrue(disabled.userMessage.contains("turned off") || disabled.userMessage.contains("Location Services"))

        val denied = LocationResultState.Failure.PermissionDenied
        assertTrue(denied.userMessage.contains("denied") || denied.userMessage.contains("permission"))

        val timeout = LocationResultState.Failure.Timeout
        assertTrue(timeout.userMessage.contains("timed out"))
    }
}

package com.example

import com.example.model.WeatherAlert
import com.example.model.WeatherAlertSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherAlertsTest {

    @Test
    fun testAlertSeverityOrderingAndProperties() {
        val alert = WeatherAlert(
            id = "alert_test_1",
            title = "Severe Thunderstorm Warning",
            severity = WeatherAlertSeverity.WARNING,
            eventType = "Thunderstorm",
            headline = "Thunderstorm with flash flooding potential",
            description = "Heavy rain and dangerous cloud-to-ground lightning.",
            instruction = "Seek shelter immediately indoors.",
            effectiveTime = "Active now",
            expiresTime = "Until 6 PM"
        )

        assertEquals("Warning", alert.severity.level)
        assertEquals("Severe Thunderstorm Warning", alert.title)
        assertTrue(alert.description.contains("lightning"))
        assertTrue(alert.instruction.contains("shelter"))
    }
}

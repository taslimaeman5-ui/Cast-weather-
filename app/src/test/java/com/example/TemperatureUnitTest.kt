package com.example

import com.example.model.TemperatureUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class TemperatureUnitTest {

    @Test
    fun testCelsiusToFahrenheitConversion() {
        val unitF = TemperatureUnit.FAHRENHEIT
        val unitC = TemperatureUnit.CELSIUS

        // 0°C is 32°F
        assertEquals(32.0, unitF.convertFromCelsius(0.0), 0.001)
        assertEquals("32°F", unitF.format(0.0))

        // 100°C is 212°F
        assertEquals(212.0, unitF.convertFromCelsius(100.0), 0.001)
        assertEquals("212°F", unitF.format(100.0))

        // 25°C is 77°F
        assertEquals(77.0, unitF.convertFromCelsius(25.0), 0.001)
        assertEquals("77°F", unitF.format(25.0))

        // -40°C is -40°F
        assertEquals(-40.0, unitF.convertFromCelsius(-40.0), 0.001)
        assertEquals("-40°F", unitF.format(-40.0))

        // Celsius formatting
        assertEquals("25°C", unitC.format(25.0))
        assertEquals(25.0, unitC.convertFromCelsius(25.0), 0.001)
    }
}

package com.example

import com.example.ui.components.ExtendedWorldCities
import com.example.ui.components.MapProjectionMode
import com.example.ui.components.MapWeatherLayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorldMapGlobeTest {

    @Test
    fun testWorldCitiesDatasetLoaded() {
        assertTrue(ExtendedWorldCities.isNotEmpty())
        assertTrue(ExtendedWorldCities.size >= 20)

        val tokyo = ExtendedWorldCities.find { it.name == "Tokyo" }
        assertNotNull(tokyo)
        assertEquals("Japan", tokyo?.country)

        val london = ExtendedWorldCities.find { it.name == "London" }
        assertNotNull(london)
        assertEquals("UK", london?.country)
    }

    @Test
    fun testMapProjectionModes() {
        assertEquals(MapProjectionMode.GLOBE_3D, MapProjectionMode.valueOf("GLOBE_3D"))
        assertEquals(MapProjectionMode.FLAT_MAP, MapProjectionMode.valueOf("FLAT_MAP"))
    }

    @Test
    fun testWeatherLayers() {
        val layers = MapWeatherLayer.values()
        assertTrue(layers.contains(MapWeatherLayer.TEMPERATURE))
        assertTrue(layers.contains(MapWeatherLayer.PRECIPITATION))
        assertTrue(layers.contains(MapWeatherLayer.WIND))
        assertTrue(layers.contains(MapWeatherLayer.SATELLITE))
    }

    @Test
    fun testContinentGeometriesLoaded() {
        val geometries = com.example.ui.components.ContinentGeometries
        assertTrue(geometries.isNotEmpty())
        assertTrue("Expected at least 10 continent & island polygons", geometries.size >= 10)

        // Ensure polar polygon is properly classified for realistic glacial ice rendering
        val polarPolygons = geometries.filter { poly -> poly.any { it.first < -60.0 } }
        assertTrue("Expected polar polygons for Antarctica", polarPolygons.isNotEmpty())
    }
}

package com.example.db

import kotlinx.coroutines.flow.Flow

class RecentCityRepository(private val dao: RecentCityDao) {

    val recentCities: Flow<List<RecentCityEntity>> = dao.getLast5SearchedCities()

    suspend fun recordCitySearch(name: String, country: String?, lat: Double, lon: Double) {
        dao.insertCity(
            RecentCityEntity(
                cityName = name,
                country = country,
                latitude = lat,
                longitude = lon,
                searchedAtTimestamp = System.currentTimeMillis()
            )
        )
        dao.pruneOldCities()
    }

    suspend fun removeCity(cityName: String) {
        dao.deleteCity(cityName)
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }
}

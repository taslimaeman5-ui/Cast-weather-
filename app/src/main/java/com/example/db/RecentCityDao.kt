package com.example.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentCityDao {

    @Query("SELECT * FROM recent_cities ORDER BY searchedAtTimestamp DESC LIMIT 5")
    fun getLast5SearchedCities(): Flow<List<RecentCityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: RecentCityEntity)

    @Query("DELETE FROM recent_cities WHERE cityName NOT IN (SELECT cityName FROM recent_cities ORDER BY searchedAtTimestamp DESC LIMIT 5)")
    suspend fun pruneOldCities()

    @Query("DELETE FROM recent_cities WHERE cityName = :cityName")
    suspend fun deleteCity(cityName: String)

    @Query("DELETE FROM recent_cities")
    suspend fun clearAll()
}

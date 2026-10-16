package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_cities")
data class RecentCityEntity(
    @PrimaryKey
    val cityName: String,
    val country: String? = null,
    val latitude: Double,
    val longitude: Double,
    val searchedAtTimestamp: Long = System.currentTimeMillis()
)

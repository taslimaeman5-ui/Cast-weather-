package com.example.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RecentCityEntity::class], version = 1, exportSchema = false)
abstract class SkyCastDatabase : RoomDatabase() {

    abstract fun recentCityDao(): RecentCityDao

    companion object {
        @Volatile
        private var INSTANCE: SkyCastDatabase? = null

        fun getDatabase(context: Context): SkyCastDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SkyCastDatabase::class.java,
                    "skycast_weather.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

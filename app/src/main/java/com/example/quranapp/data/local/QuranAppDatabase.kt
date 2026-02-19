package com.example.quranapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.quranapp.data.local.dao.AyahSearchDao
import com.example.quranapp.data.local.dao.ManualBookmarkDao
import com.example.quranapp.data.local.dao.PrayerStatusDao
import com.example.quranapp.data.local.dao.RecentQuranDao
import com.example.quranapp.data.local.entity.AyahSearchFts
import com.example.quranapp.data.local.entity.ManualBookmarkEntity
import com.example.quranapp.data.local.entity.PrayerStatusEntity
import com.example.quranapp.data.local.entity.RecentQuranEntity

@Database(
    entities = [RecentQuranEntity::class, PrayerStatusEntity::class, AyahSearchFts::class, ManualBookmarkEntity::class],
    version = 3,
    exportSchema = false
)
abstract class QuranAppDatabase : RoomDatabase() {
    abstract fun recentQuranDao(): RecentQuranDao
    abstract fun prayerStatusDao(): PrayerStatusDao
    abstract fun ayahSearchDao(): AyahSearchDao
    abstract fun manualBookmarkDao(): ManualBookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: QuranAppDatabase? = null

        fun getInstance(context: Context): QuranAppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    QuranAppDatabase::class.java,
                    "quran_app_db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}

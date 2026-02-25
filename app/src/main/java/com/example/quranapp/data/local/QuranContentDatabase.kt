package com.example.quranapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.quranapp.data.local.dao.QuranDao
import com.example.quranapp.data.local.dao.SurahDao
import com.example.quranapp.data.local.entity.AyahEntity
import com.example.quranapp.data.local.entity.SurahEntity

@Database(
    entities = [SurahEntity::class, AyahEntity::class],
    version = 1,
    exportSchema = false
)
abstract class QuranContentDatabase : RoomDatabase() {
    abstract fun surahDao(): SurahDao
    abstract fun quranDao(): QuranDao

    companion object {
        @Volatile
        private var INSTANCE: QuranContentDatabase? = null

        fun getInstance(context: Context): QuranContentDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    QuranContentDatabase::class.java,
                    "quran_content_db"
                )
                    .createFromAsset("database/quran.db")
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

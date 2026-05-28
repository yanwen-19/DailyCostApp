package com.example.dailycost.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dailycost.data.dao.ItemDao
import com.example.dailycost.data.dao.ItemTagDao
import com.example.dailycost.data.dao.TagDao
import com.example.dailycost.data.entity.Item
import com.example.dailycost.data.entity.ItemTag
import com.example.dailycost.data.entity.Tag

@Database(
    entities = [Item::class, Tag::class, ItemTag::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun tagDao(): TagDao
    abstract fun itemTagDao(): ItemTagDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "daily_cost_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

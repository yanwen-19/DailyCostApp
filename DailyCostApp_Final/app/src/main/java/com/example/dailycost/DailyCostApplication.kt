package com.example.dailycost

import android.app.Application
import com.example.dailycost.data.database.AppDatabase
import com.example.dailycost.data.repository.ItemRepository

class DailyCostApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val repository: ItemRepository by lazy {
        ItemRepository(
            itemDao = database.itemDao(),
            tagDao = database.tagDao(),
            itemTagDao = database.itemTagDao()
        )
    }
}

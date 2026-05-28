package com.example.dailycost.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val price: Double,
    val purchaseDate: Long,    // 毫秒时间戳
    val createdAt: Long = System.currentTimeMillis()
)

package com.example.dailycost.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/** 物品 + 它关联的所有标签（Room 自动关联查询） */
data class ItemWithTags(
    @Embedded val item: Item,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ItemTag::class,
            parentColumn = "itemId",
            entityColumn = "tagId"
        )
    )
    val tags: List<Tag>
)

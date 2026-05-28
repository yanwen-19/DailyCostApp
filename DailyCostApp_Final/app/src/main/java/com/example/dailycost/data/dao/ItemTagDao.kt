package com.example.dailycost.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dailycost.data.entity.ItemTag

@Dao
interface ItemTagDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(itemTag: ItemTag)

    /** 删除某个物品的所有标签关联 */
    @Query("DELETE FROM item_tags WHERE itemId = :itemId")
    suspend fun deleteByItemId(itemId: Long)

    /** 获取某个物品的所有标签 ID */
    @Query("SELECT tagId FROM item_tags WHERE itemId = :itemId")
    suspend fun getTagIdsForItem(itemId: Long): List<Long>
}

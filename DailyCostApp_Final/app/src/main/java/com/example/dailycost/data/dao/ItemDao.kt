package com.example.dailycost.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.dailycost.data.entity.Item
import com.example.dailycost.data.entity.ItemWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Item): Long

    @Update
    suspend fun update(item: Item)

    @Delete
    suspend fun delete(item: Item)

    /** 获取所有物品（含标签），按购买日期倒序 */
    @Transaction
    @Query("SELECT * FROM items ORDER BY purchaseDate DESC")
    fun getAllItems(): Flow<List<ItemWithTags>>

    /** 一次性获取所有物品（用于导出等一次性操作） */
    @Transaction
    @Query("SELECT * FROM items ORDER BY purchaseDate DESC")
    suspend fun getAllItemsOnce(): List<ItemWithTags>

    /** 按 ID 获取单个物品 */
    @Transaction
    @Query("SELECT * FROM items WHERE id = :id")
    fun getItemById(id: Long): Flow<ItemWithTags?>

    /** 按标签筛选物品 */
    @Transaction
    @Query("""
        SELECT * FROM items
        WHERE id IN (SELECT itemId FROM item_tags WHERE tagId = :tagId)
        ORDER BY purchaseDate DESC
    """)
    fun getItemsByTag(tagId: Long): Flow<List<ItemWithTags>>
}

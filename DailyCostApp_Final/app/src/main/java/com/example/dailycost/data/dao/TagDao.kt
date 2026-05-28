package com.example.dailycost.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dailycost.data.entity.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: Tag): Long

    @Delete
    suspend fun delete(tag: Tag)

    /** 获取所有标签，按名称排序 */
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<Tag>>

    /** 一次性获取所有标签 */
    @Query("SELECT * FROM tags ORDER BY name ASC")
    suspend fun getAllTagsOnce(): List<Tag>

    /** 按名称查找标签（用于避免重复创建） */
    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): Tag?

    /** 按 ID 查找标签 */
    @Query("SELECT * FROM tags WHERE id = :id LIMIT 1")
    suspend fun getTagById(id: Long): Tag?
}

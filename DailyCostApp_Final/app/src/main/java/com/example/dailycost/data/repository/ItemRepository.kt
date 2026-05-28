package com.example.dailycost.data.repository

import com.example.dailycost.data.dao.ItemDao
import com.example.dailycost.data.dao.ItemTagDao
import com.example.dailycost.data.dao.TagDao
import com.example.dailycost.data.entity.Item
import com.example.dailycost.data.entity.ItemTag
import com.example.dailycost.data.entity.ItemWithTags
import com.example.dailycost.data.entity.Tag
import kotlinx.coroutines.flow.Flow

/**
 * 数据仓库 —— 对外提供干净的数据操作接口
 * ViewModel 只和 Repository 打交道，不直接访问 DAO
 */
class ItemRepository(
    private val itemDao: ItemDao,
    private val tagDao: TagDao,
    private val itemTagDao: ItemTagDao
) {
    // ===== 观察（自动刷新） =====

    val allItems: Flow<List<ItemWithTags>> = itemDao.getAllItems()
    val allTags: Flow<List<Tag>> = tagDao.getAllTags()

    fun getItemsByTag(tagId: Long): Flow<List<ItemWithTags>> = itemDao.getItemsByTag(tagId)
    fun getItemById(id: Long): Flow<ItemWithTags?> = itemDao.getItemById(id)

    // ===== 写入 =====

    /** 新增物品（连带标签） */
    suspend fun addItemWithTags(
        name: String,
        price: Double,
        purchaseDate: Long,
        tagNames: List<String>
    ): Long {
        val item = Item(name = name, price = price, purchaseDate = purchaseDate)
        val itemId = itemDao.insert(item)
        linkTags(itemId, tagNames)
        return itemId
    }

    /** 更新物品（连带标签 —— 先删旧关联再建新关联） */
    suspend fun updateItemWithTags(
        itemId: Long,
        name: String,
        price: Double,
        purchaseDate: Long,
        tagNames: List<String>
    ) {
        itemDao.update(Item(id = itemId, name = name, price = price, purchaseDate = purchaseDate))
        itemTagDao.deleteByItemId(itemId)
        linkTags(itemId, tagNames)
    }

    /** 删除物品 */
    suspend fun deleteItem(item: Item) = itemDao.delete(item)

    /** 一次性获取全部数据（用于 CSV 导出） */
    suspend fun getAllItemsOnce(): List<ItemWithTags> = itemDao.getAllItemsOnce()

    /** 一次性获取全部标签 */
    suspend fun getAllTagsOnce(): List<Tag> = tagDao.getAllTagsOnce()

    // ===== 内部辅助 =====

    private suspend fun linkTags(itemId: Long, tagNames: List<String>) {
        for (name in tagNames) {
            var tag = tagDao.getTagByName(name)
            if (tag == null) {
                val newId = tagDao.insert(Tag(name = name))
                tag = Tag(id = newId, name = name)
            }
            itemTagDao.insert(ItemTag(itemId = itemId, tagId = tag.id))
        }
    }
}

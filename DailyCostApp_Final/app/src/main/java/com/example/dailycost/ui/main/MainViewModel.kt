package com.example.dailycost.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailycost.DailyCostApplication
import com.example.dailycost.data.entity.ItemWithTags
import com.example.dailycost.data.entity.Tag
import com.example.dailycost.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortField { PURCHASE_DATE, PRICE, DAILY_COST, NAME }
enum class SortDirection { ASC, DESC }
data class SortOption(val field: SortField, val direction: SortDirection)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as DailyCostApplication).repository
    private val allItems = repository.allItems
    val allTags: StateFlow<List<Tag>> = repository.allTags.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTagId = MutableStateFlow<Long?>(null)
    val selectedTagId: StateFlow<Long?> = _selectedTagId.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption(SortField.PURCHASE_DATE, SortDirection.DESC))
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _filteredItems = MutableStateFlow<List<ItemWithTags>>(emptyList())
    val filteredItems: StateFlow<List<ItemWithTags>> = _filteredItems.asStateFlow()

    data class TagSummary(val totalAmount: Double, val avgDailyCost: Double)
    private val _tagSummary = MutableStateFlow(TagSummary(0.0, 0.0))
    val tagSummary: StateFlow<TagSummary> = _tagSummary.asStateFlow()

    // ★ 新增：资产净值方框数据
    data class AssetSummary(val totalAmount: Double, val itemCount: Int, val totalDailyCost: Double)
    private val _assetSummary = MutableStateFlow(AssetSummary(0.0, 0, 0.0))
    val assetSummary: StateFlow<AssetSummary> = _assetSummary.asStateFlow()

    init {
        viewModelScope.launch {
            combine(allItems, _selectedTagId, _sortOption) { items, tagId, sort ->
                val filtered = if (tagId == null) items else items.filter { it.tags.any { t -> t.id == tagId } }
                val total = filtered.sumOf { it.item.price }
                val avg = if (filtered.isEmpty()) 0.0 else filtered.sumOf { it.item.price / DateUtils.getDaysSince(it.item.purchaseDate) } / filtered.size
                val sorted = when (sort.field) {
                    SortField.PURCHASE_DATE -> if (sort.direction == SortDirection.DESC) filtered.sortedByDescending { it.item.purchaseDate } else filtered.sortedBy { it.item.purchaseDate }
                    SortField.PRICE -> if (sort.direction == SortDirection.DESC) filtered.sortedByDescending { it.item.price } else filtered.sortedBy { it.item.price }
                    SortField.DAILY_COST -> if (sort.direction == SortDirection.DESC) filtered.sortedByDescending { it.item.price / DateUtils.getDaysSince(it.item.purchaseDate) } else filtered.sortedBy { it.item.price / DateUtils.getDaysSince(it.item.purchaseDate) }
                    SortField.NAME -> if (sort.direction == SortDirection.DESC) filtered.sortedByDescending { it.item.name } else filtered.sortedBy { it.item.name }
                }
                Pair(sorted, TagSummary(total, avg))
            }.collectLatest { r -> _filteredItems.value = r.first; _tagSummary.value = r.second }
        }
        // ★ 计算全部物品的资产净值
        viewModelScope.launch {
            allItems.collectLatest { items ->
                _assetSummary.value = AssetSummary(
                    totalAmount = items.sumOf { it.item.price },
                    itemCount = items.size,
                    totalDailyCost = items.sumOf { it.item.price / DateUtils.getDaysSince(it.item.purchaseDate) }
                )
            }
        }
    }

    fun selectTag(tagId: Long) { _selectedTagId.value = if (_selectedTagId.value == tagId) null else tagId }
    fun setSortOption(option: SortOption) { _sortOption.value = option }
    fun deleteItem(itemWithTags: ItemWithTags) { viewModelScope.launch { repository.deleteItem(itemWithTags.item) } }
}

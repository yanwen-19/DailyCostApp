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

    // 原始数据（来自 Room Flow）
    private val allItems = repository.allItems
    val allTags: StateFlow<List<Tag>> = repository.allTags.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // 筛选与排序状态
    private val _selectedTagId = MutableStateFlow<Long?>(null)
    val selectedTagId: StateFlow<Long?> = _selectedTagId.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption(SortField.PURCHASE_DATE, SortDirection.DESC))
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    // 经过筛选 + 排序后的物品列表
    private val _filteredItems = MutableStateFlow<List<ItemWithTags>>(emptyList())
    val filteredItems: StateFlow<List<ItemWithTags>> = _filteredItems.asStateFlow()

    // 标签汇总数据
    data class TagSummary(val totalAmount: Double, val avgDailyCost: Double)
    private val _tagSummary = MutableStateFlow(TagSummary(0.0, 0.0))
    val tagSummary: StateFlow<TagSummary> = _tagSummary.asStateFlow()

    init {
        viewModelScope.launch {
            combine(allItems, _selectedTagId, _sortOption) { items, tagId, sort ->
                computeFilteredSorted(items, tagId, sort)
            }.collectLatest { result ->
                _filteredItems.value = result.sorted
                _tagSummary.value = result.summary
            }
        }
    }

    private data class ComputeResult(
        val sorted: List<ItemWithTags>,
        val summary: TagSummary
    )

    private fun computeFilteredSorted(
        items: List<ItemWithTags>,
        tagId: Long?,
        sort: SortOption
    ): ComputeResult {
        // 1. 筛选
        val filtered = if (tagId == null) {
            items
        } else {
            items.filter { itemWithTags ->
                itemWithTags.tags.any { it.id == tagId }
            }
        }

        // 2. 计算汇总
        val totalAmount = filtered.sumOf { it.item.price }
        val avgDailyCost = if (filtered.isNotEmpty()) {
            filtered.sumOf { itemWithTags ->
                val days = DateUtils.getDaysSince(itemWithTags.item.purchaseDate)
                itemWithTags.item.price / days
            } / filtered.size
        } else 0.0
        val summary = TagSummary(totalAmount, avgDailyCost)

        // 3. 排序
        val sorted = when (sort.field) {
            SortField.PURCHASE_DATE -> {
                if (sort.direction == SortDirection.DESC)
                    filtered.sortedByDescending { it.item.purchaseDate }
                else
                    filtered.sortedBy { it.item.purchaseDate }
            }
            SortField.PRICE -> {
                if (sort.direction == SortDirection.DESC)
                    filtered.sortedByDescending { it.item.price }
                else
                    filtered.sortedBy { it.item.price }
            }
            SortField.DAILY_COST -> {
                if (sort.direction == SortDirection.DESC)
                    filtered.sortedByDescending {
                        val days = DateUtils.getDaysSince(it.item.purchaseDate)
                        it.item.price / days
                    }
                else
                    filtered.sortedBy {
                        val days = DateUtils.getDaysSince(it.item.purchaseDate)
                        it.item.price / days
                    }
            }
            SortField.NAME -> {
                if (sort.direction == SortDirection.DESC)
                    filtered.sortedByDescending { it.item.name }
                else
                    filtered.sortedBy { it.item.name }
            }
        }

        return ComputeResult(sorted, summary)
    }

    fun selectTag(tagId: Long) {
        _selectedTagId.value = if (_selectedTagId.value == tagId) null else tagId
    }

    fun clearTag() {
        _selectedTagId.value = null
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun toggleSortDirection() {
        val current = _sortOption.value
        _sortOption.value = current.copy(
            direction = if (current.direction == SortDirection.ASC) SortDirection.DESC else SortDirection.ASC
        )
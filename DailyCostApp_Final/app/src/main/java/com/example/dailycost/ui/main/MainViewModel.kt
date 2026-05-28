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
    private val repo = (application as DailyCostApplication).repository
    private val allItems = repo.allItems
    val allTags: StateFlow<List<Tag>> = repo.allTags.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _tagId = MutableStateFlow<Long?>(null)
    val selectedTagId: StateFlow<Long?> = _tagId.asStateFlow()
    private val _sort = MutableStateFlow(SortOption(SortField.PURCHASE_DATE, SortDirection.DESC))
    val sortOption: StateFlow<SortOption> = _sort.asStateFlow()
    private val _items = MutableStateFlow<List<ItemWithTags>>(emptyList())
    val filteredItems: StateFlow<List<ItemWithTags>> = _items.asStateFlow()

    data class TagSummary(val totalAmount: Double, val avgDailyCost: Double)
    private val _sum = MutableStateFlow(TagSummary(0.0, 0.0))
    val tagSummary: StateFlow<TagSummary> = _sum.asStateFlow()

    init {
        viewModelScope.launch {
            combine(allItems, _tagId, _sort) { items, tagId, sort ->
                val f = if (tagId == null) items else items.filter { it.tags.any { t -> t.id == tagId } }
                val total = f.sumOf { it.item.price }
                val avg = if (f.isEmpty()) 0.0 else f.sumOf { it.item.price / DateUtils.getDaysSince(it.item.purchaseDate) } / f.size
                val list = when (sort.field) {
                    SortField.PURCHASE_DATE -> if (sort.direction == SortDirection.DESC) f.sortedByDescending { it.item.purchaseDate } else f.sortedBy { it.item.purchaseDate }
                    SortField.PRICE -> if (sort.direction == SortDirection.DESC) f.sortedByDescending { it.item.price } else f.sortedBy { it.item.price }
                    SortField.DAILY_COST -> if (sort.direction == SortDirection.DESC) f.sortedByDescending { it.item.price / DateUtils.getDaysSince(it.item.purchaseDate) } else f.sortedBy { it.item.price / DateUtils.getDaysSince(it.item.purchaseDate) }
                    SortField.NAME -> if (sort.direction == SortDirection.DESC) f.sortedByDescending { it.item.name } else f.sortedBy { it.item.name }
                }
                Pair(list, TagSummary(total, avg))
            }.collectLatest { r -> _items.value = r.first; _sum.value = r.second }
        }
    }

    fun selectTag(id: Long) { _tagId.value = if (_tagId.value == id) null else id }
    fun setSortOption(o: SortOption) { _sort.value = o }
    fun deleteItem(item: ItemWithTags) { viewModelScope.launch { repo.deleteItem(item.item) } }
}

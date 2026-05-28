package com.example.dailycost.ui.addedit

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddEditViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as DailyCostApplication).repository

    // 表单状态
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _price = MutableStateFlow("")
    val price: StateFlow<String> = _price.asStateFlow()

    private val _purchaseDate = MutableStateFlow(DateUtils.formatDate(System.currentTimeMillis()))
    val purchaseDate: StateFlow<String> = _purchaseDate.asStateFlow()

    private val _selectedTagNames = MutableStateFlow<List<String>>(emptyList())
    val selectedTagNames: StateFlow<List<String>> = _selectedTagNames.asStateFlow()

    val allTags: StateFlow<List<Tag>> = repository.allTags.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private var editingItemId: Long? = null

    /** 加载已有物品（编辑模式） */
    fun loadItem(itemId: Long) {
        editingItemId = itemId
        _isEditMode.value = true
        viewModelScope.launch {
            val itemWithTags = repository.getItemById(itemId).first()
            itemWithTags?.let {
                _name.value = it.item.name
                _price.value = String.format("%.2f", it.item.price)
                _purchaseDate.value = DateUtils.formatDate(it.item.purchaseDate)
                _selectedTagNames.value = it.tags.map { tag -> tag.name }
            }
        }
    }

    fun updateName(value: String) { _name.value = value }
    fun updatePrice(value: String) { _price.value = value }
    fun updatePurchaseDate(value: String) { _purchaseDate.value = value }

    fun toggleTag(tagName: String) {
        val current = _selectedTagNames.value.toMutableList()
        if (current.contains(tagName)) {
            current.remove(tagName)
        } else {
            current.add(tagName)
        }
        _selectedTagNames.value = current
    }

    fun addNewTag(tagName: String) {
        if (tagName.isBlank()) return
        val current = _selectedTagNames.value.toMutableList()
        if (!current.contains(tagName)) {
            current.add(tagName)
        }
        _selectedTagNames.value = current
        // 新标签自动保存到数据库
        viewModelScope.launch {
            repository.addItemWithTags("__placeholder__", 0.0, System.currentTimeMillis(), listOf(tagName))
            // 删除这个占位物品
            val items = repository.getAllItemsOnce()
            items.find { it.item.name == "__placeholder__" }?.let {
                repository.deleteItem(it.item)
            }
        }
    }

    /** 保存（新增或更新） */
    fun save(onSuccess: () -> Unit) {
        val nameValue = _name.value.trim()
        val priceText = _price.value.trim()
        val dateValue = _purchaseDate.value.trim()

        if (nameValue.isBlank()) return
        val priceValue = priceText.toDoubleOrNull() ?: return
        if (priceValue <= 0) return
        val dateTimestamp = DateUtils.parseDate(dateValue)

        viewModelScope.launch {
            val itemId = editingItemId
            if (itemId != null) {
                repository.updateItemWithTags(itemId, nameValue, priceValue, dateTimestamp, _selectedTagNames.value)
            } else {
                repository.addItemWithTags(nameValue, priceValue, dateTimestamp, _selectedTagNames.value)
            }
            onSuccess()
        }
    }

    /** 删除当前物品 */
    fun delete(onSuccess: () -> Unit) {
        val itemId = editingItemId ?: return
        viewModelScope.launch {
            val i
package com.example.dailycost.ui.addedit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailycost.DailyCostApplication
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

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _price = MutableStateFlow("")
    val price: StateFlow<String> = _price.asStateFlow()

    private val _purchaseDate = MutableStateFlow(DateUtils.formatDate(System.currentTimeMillis()))
    val purchaseDate: StateFlow<String> = _purchaseDate.asStateFlow()

    private val _selectedTagNames = MutableStateFlow<List<String>>(emptyList())
    val selectedTagNames: StateFlow<List<String>> = _selectedTagNames.asStateFlow()

    val allTags: StateFlow<List<Tag>> = repository.allTags.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private var editingItemId: Long? = null

    fun loadItem(itemId: Long) {
        editingItemId = itemId; _isEditMode.value = true
        viewModelScope.launch {
            repository.getItemById(itemId).first()?.let {
                _name.value = it.item.name
                _price.value = "%.2f".format(it.item.price)
                _purchaseDate.value = DateUtils.formatDate(it.item.purchaseDate)
                _selectedTagNames.value = it.tags.map { t -> t.name }
            }
        }
    }

    fun updateName(v: String) { _name.value = v }
    fun updatePrice(v: String) { _price.value = v }
    fun updatePurchaseDate(v: String) { _purchaseDate.value = v }

    fun toggleTag(name: String) {
        val list = _selectedTagNames.value.toMutableList()
        if (list.contains(name)) list.remove(name) else list.add(name)
        _selectedTagNames.value = list
    }

    fun addNewTag(name: String) {
        if (name.isBlank()) return
        val list = _selectedTagNames.value.toMutableList()
        if (!list.contains(name)) { list.add(name); _selectedTagNames.value = list }
        viewModelScope.launch {
            repository.addItemWithTags("__pl__", 0.0, System.currentTimeMillis(), listOf(name))
            repository.getAllItemsOnce().find { it.item.name == "__pl__" }?.let { repository.deleteItem(it.item) }
        }
    }

    fun save(onSuccess: () -> Unit) {
        val n = _name.value.trim(); val p = _price.value.trim().toDoubleOrNull() ?: return
        if (n.isBlank() || p <= 0) return
        val date = DateUtils.parseDate(_purchaseDate.value.trim())
        viewModelScope.launch {
            val id = editingItemId
            if (id != null) repository.updateItemWithTags(id, n, p, date, _selectedTagNames.value)
            else repository.addItemWithTags(n, p, date, _selectedTagNames.value)
            onSuccess()
        }
    }

    fun delete(onSuccess: () -> Unit) {
        val id = editingItemId ?: return
        viewModelScope.launch {
            repository.getItemById(id).first()?.let { repository.deleteItem(it.item) }
            onSuccess()
        }
    }
}

package com.example.dailycost.ui.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailycost.DailyCostApplication
import com.example.dailycost.util.CsvUtils
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as DailyCostApplication).repository

    fun exportToCsv(uri: Uri) {
        viewModelScope.launch {
            val items = repository.getAllItemsOnce()
            CsvUtils.exportToCsv(getApplication(), items, uri)
        }
    }

    fun importFromCsv(uri: Uri, onComplete: () -> Unit) {
        viewModelScope.launch {
            val rows = CsvUtils.importFromCsv(getApplication(), uri)
            for (row in rows) {
                repository.addItemWithTags(row.name, row.price, row.purchaseDate, row.tags)
            }
            onComplete()
        }
    }
}

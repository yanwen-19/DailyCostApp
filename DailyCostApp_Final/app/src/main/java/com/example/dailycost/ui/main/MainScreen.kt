package com.example.dailycost.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailycost.data.entity.ItemWithTags
import com.example.dailycost.data.entity.Tag
import com.example.dailycost.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onNavigateToAdd: () -> Unit, onNavigateToEdit: (Long) -> Unit, onNavigateToSettings: () -> Unit, viewModel: MainViewModel = viewModel()) {
    val items by viewModel.filteredItems.collectAsState()
    val tags by viewModel.allTags.collectAsState()
    val selectedTagId by viewModel.selectedTagId.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val tagSummary by viewModel.tagSummary.collectAsState()
    val assetSummary by viewModel.assetSummary.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<ItemWithTags?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("日供记账") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onSurface), actions = {
                Box {
                    IconButton(onClick = { showSortMenu = true }) { Icon(Icons.Default.Sort, contentDescription = "排序") }
                    SortDropdownMenu(expanded = showSortMenu, onDismiss = { showSortMenu = false }, current = sortOption, onSelect = { viewModel.setSortOption(it) })
                }
                IconButton(onClick = onNavigateToSettings) { Icon(Icons.Default.Settings, contentDescription = "设置") }
            })
        },
        floatingActionButton = { FloatingActionButton(onClick = onNavigateToAdd) { Icon(Icons.Default.Add, contentDescription = "添加物品") } }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            AssetSummaryCard(summary = assetSummary)
            TagFilterRow(tags = tags, selectedTagId = selectedTagId, onTagClick = { viewModel.selectTag(it) })
            if (selectedTagId != null) { TagSummaryCard(summary = tagSummary) }
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(items, key = { it.item.id }) { item ->
                    ItemCard(itemWithTags = item, onClick = { onNavigateToEdit(item.item.id) }, onLongDelete = { itemToDelete = item })
                }
            }
        }
    }

    itemToDelete?.let { item ->
        AlertDialog(onDismissRequest = { itemToDelete = null }, title = { Text("确认删除") }, text = { Text("确定要删除「${item.item.name}」吗？") },
            confirmButton = { TextButton(onClick = { viewModel.deleteItem(item); itemToDelete = null }) { Text("删除", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { itemToDelete = null }) { Text("取消") } })
    }
}

@Composable
private fun AssetSummaryCard(summary: MainViewModel.AssetSummary) {
    Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF1976D2)).padding(horizontal = 24.dp, vertical = 14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("资产净值", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                Text("¥" + "%.2f".format(summary.totalAmount), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("物品项数", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                Text(summary.itemCount.toString() + " 件", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("总日供", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                Text("¥" + "%.4f".format(summary.totalDailyCost), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SortDropdownMenu(expanded: Boolean, onDismiss: () -> Unit, current: SortOption, onSelect: (SortOption) -> Unit) {
    val labels = mapOf(SortField.PURCHASE_DATE to "购买日期", SortField.PRICE to "价格", SortField.DAILY_COST to "日供", SortField.NAME to "名称")
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        SortField.entries.forEach { field ->
            DropdownMenuItem(text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(labels[field] ?: field.name)
                    if (current.field == field) { Text(if (current.direction == SortDirection.DESC) " ↓" else " ↑", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                }
            }, onClick = {
                if (current.field == field) { onSelect(current.copy(direction = if (current.direction == SortDirection.DESC) SortDirection.ASC else SortDirection.DESC)) }
                else { onSelect(SortOption(field, SortDirection.DESC)) }
                onDismiss()
            })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagFilterRow(tags: List<Tag>, selectedTagId: Long?, onTagClick: (Long) -> Unit) {
    FlowRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = selectedTagId == null, onClick = { if (selectedTagId != null) onTagClick(-1) }, label = { Text("全部") })
        tags.forEach { tag -> FilterChip(selected = selectedTagId == tag.id, onClick = { onTagClick(tag.id) }, label = { Text(tag.name) }) }
    }
}

@Composable
private fun TagSummaryCard(summary: MainViewModel.TagSummary) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("总金额", style = MaterialTheme.typography.bodySmall)
                Text("¥" + "%.2f".format(summary.totalAmount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("平均日供", style = MaterialTheme.typography.bodySmall)
                Text("¥" + "%.4f".format(summary.avgDailyCost), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ItemCard(itemWithTags: ItemWithTags, onClick: () -> Unit, onLongDelete: () -> Unit) {
    val item = itemWithTags.item
    val dc = item.price / DateUtils.getDaysSince(item.purchaseDate)
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp).clickable { onClick() }, shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { itemWithTags.tags.forEach { t -> Text(t.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) } }
                Text(DateUtils.formatDate(item.purchaseDate), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("¥" + "%.2f".format(item.price), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text("日供 ¥" + "%.2f".format(dc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

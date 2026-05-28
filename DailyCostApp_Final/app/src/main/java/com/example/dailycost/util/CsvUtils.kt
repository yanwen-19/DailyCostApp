package com.example.dailycost.util

import android.content.Context
import android.net.Uri
import com.example.dailycost.data.entity.Item
import com.example.dailycost.data.entity.ItemWithTags
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * CSV 格式：
 *   name,price,purchaseDate,tags
 *   矿泉水,2.50,2026-05-01,饮料
 *   充电线,29.00,2026-04-15,数码配件
 */
object CsvUtils {

    fun exportToCsv(context: Context, items: List<ItemWithTags>, uri: Uri) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
                // 写表头
                writer.write("name,price,purchaseDate,tags\n")
                // 写数据行
                for (itemWithTags in items) {
                    val name = escapeCsv(itemWithTags.item.name)
                    val price = itemWithTags.item.price
                    val date = DateUtils.formatDate(itemWithTags.item.purchaseDate)
                    val tags = itemWithTags.tags.joinToString(";") { it.name }
                    writer.write("$name,$price,$date,$tags\n")
                }
            }
        }
    }

    data class CsvRow(
        val name: String,
        val price: Double,
        val purchaseDate: Long,
        val tags: List<String>
    )

    fun importFromCsv(context: Context, uri: Uri): List<CsvRow> {
        val rows = mutableListOf<CsvRow>()
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                // 跳过表头
                reader.readLine()

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val parts = line!!.split(",", limit = 4)
                    if (parts.size >= 3) {
                        val name = parts[0]
                        val price = parts[1].toDoubleOrNull() ?: 0.0
                        val date = DateUtils.parseDate(parts[2])
                        val tags = if (parts.size >= 4) {
                            parts[3].split(";").filter { it.isNotBlank() }
                        } else emptyList()
                        rows.add(CsvRow(name, price, date, tags))
                    }
                }
            }
        }
        return rows
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else value
    }
}

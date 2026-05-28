package com.example.dailycost.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {

    private val displayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val fullFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    /** 将时间戳格式化为 "yyyy-MM-dd" */
    fun formatDate(timestamp: Long): String {
        return displayFormat.format(Date(timestamp))
    }

    /** 将字符串解析为时间戳（毫秒） */
    fun parseDate(dateString: String): Long {
        return displayFormat.parse(dateString)?.time ?: System.currentTimeMillis()
    }

    /** 计算从购买日到今天的天数（至少返回 1） */
    fun getDaysSince(purchaseDateTimestamp: Long): Long {
        val now = System.currentTimeMillis()
        val diffMs = now - purchaseDateTimestamp
        val days = TimeUnit.MILLISECONDS.toDays(diffMs)
        return maxOf(days, 1L)
    }
}

package com.smartdialer.app.data

import android.content.Context
import android.provider.CallLog

object CallLogRepository {

    fun load(context: Context): List<CallLogItem> {
        val grouped = LinkedHashMap<String, CallLogItem>()
        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION
        )
        try {
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                CallLog.Calls.DATE + " DESC"
            )
            cursor?.use { c ->
                val numberIndex = c.getColumnIndex(CallLog.Calls.NUMBER)
                val nameIndex = c.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val typeIndex = c.getColumnIndex(CallLog.Calls.TYPE)
                val dateIndex = c.getColumnIndex(CallLog.Calls.DATE)
                val durationIndex = c.getColumnIndex(CallLog.Calls.DURATION)
                while (c.moveToNext()) {
                    val number = c.getString(numberIndex) ?: continue
                    if (number.isEmpty()) continue
                    val existing = grouped[number]
                    if (existing == null) {
                        grouped[number] = CallLogItem(
                            number = number,
                            name = c.getString(nameIndex),
                            type = c.getInt(typeIndex),
                            date = c.getLong(dateIndex),
                            count = 1,
                            duration = c.getLong(durationIndex)
                        )
                    } else {
                        grouped[number] = existing.copy(count = existing.count + 1)
                    }
                }
            }
        } catch (e: Exception) {
            // permission missing or provider unavailable
        }
        return grouped.values.toList()
    }
}

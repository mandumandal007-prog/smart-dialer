package com.smartdialer.app.data

data class CallLogItem(
    val number: String,
    val name: String?,
    val type: Int,
    val date: Long,
    val count: Int,
    val duration: Long
)

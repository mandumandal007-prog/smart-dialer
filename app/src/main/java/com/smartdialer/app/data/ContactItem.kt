package com.smartdialer.app.data

data class ContactItem(
    val id: Long,
    val name: String,
    val number: String,
    val photoUri: String?,
    val starred: Boolean
)

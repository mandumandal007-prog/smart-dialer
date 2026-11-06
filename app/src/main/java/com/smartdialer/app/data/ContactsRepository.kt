package com.smartdialer.app.data

import android.content.Context
import android.provider.ContactsContract

object ContactsRepository {

    fun load(context: Context): List<ContactItem> {
        val result = ArrayList<ContactItem>()
        val seen = HashSet<String>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
            ContactsContract.CommonDataKinds.Phone.STARRED
        )
        try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
            cursor?.use { c ->
                val idIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)
                val starIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED)
                while (c.moveToNext()) {
                    val number = c.getString(numberIndex) ?: continue
                    val clean = number.replace(" ", "")
                    if (clean.isEmpty() || seen.contains(clean)) continue
                    seen.add(clean)
                    val name = c.getString(nameIndex) ?: ""
                    result.add(
                        ContactItem(
                            id = c.getLong(idIndex),
                            name = name,
                            number = number,
                            photoUri = c.getString(photoIndex),
                            starred = c.getInt(starIndex) == 1
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // permission missing or provider unavailable
        }
        return result
    }
}

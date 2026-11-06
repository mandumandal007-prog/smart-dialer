package com.smartdialer.app.data

import android.content.Context

object SpamRepository {

    private const val PREFS = "smart_dialer_spam"
    private const val KEY_BLOCKED = "blocked_numbers"

    private val spamPrefixes = listOf("140", "1800", "1860")

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun normalize(number: String): String {
        val digits = number.filter { it.isDigit() }
        return if (digits.length > 10) digits.takeLast(10) else digits
    }

    fun blockedNumbers(context: Context): List<String> {
        val stored = prefs(context).getStringSet(KEY_BLOCKED, HashSet<String>())
        val list = ArrayList<String>()
        stored?.forEach { list.add(it) }
        list.sort()
        return list
    }

    fun block(context: Context, number: String) {
        val updated = HashSet(blockedNumbers(context))
        val key = normalize(number)
        if (key.isNotEmpty()) {
            updated.add(key)
            prefs(context).edit().putStringSet(KEY_BLOCKED, updated).apply()
        }
    }

    fun unblock(context: Context, number: String) {
        val updated = HashSet(blockedNumbers(context))
        updated.remove(normalize(number))
        prefs(context).edit().putStringSet(KEY_BLOCKED, updated).apply()
    }

    fun isBlocked(context: Context, number: String): Boolean {
        val key = normalize(number)
        if (key.isEmpty()) return false
        return blockedNumbers(context).contains(key)
    }

    fun isSuspicious(context: Context, number: String): Boolean {
        val key = normalize(number)
        if (key.isEmpty()) return false
        if (isBlocked(context, number)) return true
        return spamPrefixes.any { key.startsWith(it) }
    }
}

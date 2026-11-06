package com.smartdialer.app.dialpad

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smartdialer.app.R
import com.smartdialer.app.data.ContactItem
import com.smartdialer.app.data.ContactsRepository
import com.smartdialer.app.data.SpamRepository
import com.smartdialer.app.ui.ContactAdapter
import com.smartdialer.app.util.CallLauncher
import com.smartdialer.app.util.T9

class DialpadActivity : AppCompatActivity() {

    private var typed = ""
    private lateinit var numberText: TextView
    private lateinit var hintText: TextView
    private lateinit var adapter: ContactAdapter
    private var contacts: List<ContactItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialpad)

        numberText = findViewById(R.id.number_display)
        hintText = findViewById(R.id.caller_hint)
        val results = findViewById<RecyclerView>(R.id.dial_results)

        adapter = ContactAdapter(
            emptyList(),
            { item -> startCall(item.number) },
            { item -> showOptions(item.number, item.name) }
        )
        results.layoutManager = LinearLayoutManager(this)
        results.adapter = adapter

        val keys = listOf(
            R.id.key_1 to "1",
            R.id.key_2 to "2",
            R.id.key_3 to "3",
            R.id.key_4 to "4",
            R.id.key_5 to "5",
            R.id.key_6 to "6",
            R.id.key_7 to "7",
            R.id.key_8 to "8",
            R.id.key_9 to "9",
            R.id.key_star to "*",
            R.id.key_0 to "0",
            R.id.key_hash to "#"
        )
        for (pair in keys) {
            val key = findViewById<View>(pair.first)
            val value = pair.second
            key.setOnClickListener { append(value) }
            key.setOnLongClickListener {
                if (value == "0") {
                    append("+")
                    true
                } else {
                    false
                }
            }
        }

        val delete = findViewById<View>(R.id.btn_delete)
        delete.setOnClickListener { backspace() }
        delete.setOnLongClickListener {
            typed = ""
            refresh()
            true
        }

        findViewById<View>(R.id.btn_close).setOnClickListener { finish() }

        findViewById<View>(R.id.btn_call).setOnClickListener {
            val number = numberText.text.toString()
            if (number.isNotEmpty()) startCall(number)
        }

        contacts = try {
            ContactsRepository.load(this)
        } catch (e: Exception) {
            emptyList()
        }

        val prefill = intent.getStringExtra(EXTRA_NUMBER)
        if (prefill != null && prefill.isNotEmpty()) {
            typed = prefill
        }
        refresh()
    }

    private fun append(value: String) {
        typed = typed + value
        refresh()
    }

    private fun backspace() {
        if (typed.isNotEmpty()) {
            typed = typed.substring(0, typed.length - 1)
        }
        refresh()
    }

    private fun refresh() {
        numberText.text = typed
        if (typed.isEmpty()) {
            hintText.text = ""
            adapter.update(emptyList())
            return
        }

        val typedDigits = typed.filter { it.isDigit() }
        val t9Query = T9.toDigits(typed)
        val matches = ArrayList<ContactItem>()
        for (item in contacts) {
            val nameMatch = T9.toDigits(item.name).contains(t9Query)
            val numberDigits = item.number.filter { it.isDigit() }
            val numberMatch = typedDigits.isNotEmpty() && numberDigits.contains(typedDigits)
            if (nameMatch || numberMatch) {
                matches.add(item)
            }
            if (matches.size >= 30) break
        }
        adapter.update(matches)

        var exactName: String? = null
        for (item in contacts) {
            val numberDigits = item.number.filter { it.isDigit() }
            if (typedDigits.isNotEmpty() && numberDigits.endsWith(typedDigits)) {
                exactName = item.name
                break
            }
        }

        hintText.text = when {
            exactName != null -> exactName
            SpamRepository.isSuspicious(this, typed) -> "Possible spam number"
            else -> "Unknown number"
        }
    }

    private fun startCall(number: String) {
        CallLauncher.dial(this, number)
    }

    private fun showOptions(number: String, name: String?) {
        val options = arrayOf("Call", "WhatsApp", "SMS", "Block number")
        AlertDialog.Builder(this)
            .setTitle(if (name.isNullOrBlank()) number else name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> startCall(number)
                    1 -> CallLauncher.whatsapp(this, number)
                    2 -> CallLauncher.sms(this, number)
                    3 -> SpamRepository.block(this, number)
                }
            }
            .show()
    }

    companion object {
        const val EXTRA_NUMBER = "extra_number"
    }
}

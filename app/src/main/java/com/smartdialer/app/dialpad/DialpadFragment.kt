package com.smartdialer.app.dialpad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smartdialer.app.R
import com.smartdialer.app.data.ContactItem
import com.smartdialer.app.data.ContactsRepository
import com.smartdialer.app.data.SpamRepository
import com.smartdialer.app.ui.ContactAdapter
import com.smartdialer.app.util.CallLauncher
import com.smartdialer.app.util.T9

class DialpadFragment : Fragment() {

    private var typed = ""
    private lateinit var numberText: TextView
    private lateinit var hintText: TextView
    private lateinit var adapter: ContactAdapter
    private var contacts: List<ContactItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dialpad, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        numberText = view.findViewById(R.id.number_display)
        hintText = view.findViewById(R.id.caller_hint)
        val results = view.findViewById<RecyclerView>(R.id.dial_results)

        adapter = ContactAdapter(
            emptyList(),
            { item -> startCall(item.number) },
            { item -> showOptions(item.number, item.name) }
        )
        results.layoutManager = LinearLayoutManager(requireContext())
        results.adapter = adapter

        val keys = listOf(
            R.id.btn_1 to "1",
            R.id.btn_2 to "2",
            R.id.btn_3 to "3",
            R.id.btn_4 to "4",
            R.id.btn_5 to "5",
            R.id.btn_6 to "6",
            R.id.btn_7 to "7",
            R.id.btn_8 to "8",
            R.id.btn_9 to "9",
            R.id.btn_star to "*",
            R.id.btn_0 to "0",
            R.id.btn_hash to "#"
        )
        for (pair in keys) {
            val button = view.findViewById<Button>(pair.first)
            val value = pair.second
            button.setOnClickListener { append(value) }
            button.setOnLongClickListener {
                if (value == "0") {
                    append("+")
                    true
                } else {
                    false
                }
            }
        }

        val delete = view.findViewById<ImageButton>(R.id.btn_delete)
        delete.setOnClickListener { backspace() }
        delete.setOnLongClickListener {
            typed = ""
            refresh()
            true
        }

        view.findViewById<Button>(R.id.btn_call).setOnClickListener {
            val number = numberText.text.toString()
            if (number.isNotEmpty()) startCall(number)
        }

        refresh()
    }

    override fun onResume() {
        super.onResume()
        contacts = try {
            ContactsRepository.load(requireContext())
        } catch (e: Exception) {
            emptyList()
        }
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
        val currentContext = context
        if (typed.isEmpty() || currentContext == null) {
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
            SpamRepository.isSuspicious(currentContext, typed) -> "Possible spam number"
            else -> "Unknown number"
        }
    }

    private fun startCall(number: String) {
        val activity = activity as? AppCompatActivity ?: return
        CallLauncher.dial(activity, number)
    }

    private fun showOptions(number: String, name: String?) {
        val currentContext = context ?: return
        val options = arrayOf("Call", "WhatsApp", "SMS", "Block number")
        AlertDialog.Builder(currentContext)
            .setTitle(if (name.isNullOrBlank()) number else name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> startCall(number)
                    1 -> CallLauncher.whatsapp(currentContext, number)
                    2 -> CallLauncher.sms(currentContext, number)
                    3 -> SpamRepository.block(currentContext, number)
                }
            }
            .show()
    }
}

package com.smartdialer.app.contacts

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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

class ContactsFragment : Fragment() {

    private lateinit var adapter: ContactAdapter
    private lateinit var emptyView: TextView
    private var allContacts: List<ContactItem> = emptyList()
    private var query = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = view.findViewById<RecyclerView>(R.id.contacts_list)
        emptyView = view.findViewById(R.id.contacts_empty)
        adapter = ContactAdapter(
            emptyList(),
            { item -> startCall(item.number) },
            { item -> showOptions(item.number, item.name) }
        )
        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = adapter

        val search = view.findViewById<EditText>(R.id.contact_search)
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                query = s?.toString() ?: ""
                render()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        allContacts = try {
            ContactsRepository.load(requireContext())
        } catch (e: Exception) {
            emptyList()
        }
        render()
    }

    private fun render() {
        val items = if (query.isBlank()) {
            allContacts
        } else {
            val q = query.lowercase()
            allContacts.filter {
                it.name.lowercase().contains(q) || it.number.contains(q)
            }
        }
        adapter.update(items)
        emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
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

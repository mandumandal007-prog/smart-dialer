package com.smartdialer.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smartdialer.app.R
import com.smartdialer.app.data.ContactItem

class ContactsSectionAdapter(
    private val onClick: (ContactItem) -> Unit,
    private val onLongClick: (ContactItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private data class Entry(val isHeader: Boolean, val header: String, val contact: ContactItem?)

    private val entries = ArrayList<Entry>()

    fun submit(contacts: List<ContactItem>) {
        entries.clear()
        var current = ""
        for (item in contacts) {
            val letter = letterFor(item)
            if (letter != current) {
                current = letter
                entries.add(Entry(true, letter, null))
            }
            entries.add(Entry(false, letter, item))
        }
        notifyDataSetChanged()
    }

    private fun letterFor(item: ContactItem): String {
        if (item.name.isBlank()) return "#"
        val first = item.name.trim().take(1).uppercase()
        if (first.isEmpty()) return "#"
        return if (first[0] in 'A'..'Z') first else "#"
    }

    fun letters(): List<String> {
        val result = ArrayList<String>()
        for (entry in entries) {
            if (entry.isHeader) result.add(entry.header)
        }
        return result
    }

    fun positionFor(letter: String): Int {
        for (index in entries.indices) {
            if (entries[index].isHeader && entries[index].header == letter) return index
        }
        return 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (entries[position].isHeader) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 0) {
            HeaderHolder(inflater.inflate(R.layout.item_section_header, parent, false))
        } else {
            RowHolder(inflater.inflate(R.layout.item_contact, parent, false))
        }
    }

    override fun getItemCount(): Int = entries.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val entry = entries[position]
        if (holder is HeaderHolder) {
            holder.header.text = entry.header
        } else if (holder is RowHolder) {
            val contact = entry.contact
            if (contact != null) holder.bind(contact)
        }
    }

    class HeaderHolder(view: View) : RecyclerView.ViewHolder(view) {
        val header: TextView = view.findViewById(R.id.header_text)
    }

    inner class RowHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val avatar: TextView = view.findViewById(R.id.avatar)
        private val name: TextView = view.findViewById(R.id.contact_name)
        private val number: TextView = view.findViewById(R.id.contact_number)
        private val star: ImageView = view.findViewById(R.id.contact_star)

        fun bind(item: ContactItem) {
            val label = if (item.name.isBlank()) item.number else item.name
            name.text = label
            number.text = item.number
            avatar.text = label.take(1).uppercase()
            star.visibility = if (item.starred) View.VISIBLE else View.GONE
            itemView.setOnClickListener { onClick(item) }
            itemView.setOnLongClickListener {
                onLongClick(item)
                true
            }
        }
    }
}

package com.smartdialer.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smartdialer.app.R
import com.smartdialer.app.data.ContactItem

class ContactAdapter(
    private var items: List<ContactItem>,
    private val onClick: (ContactItem) -> Unit,
    private val onLongClick: (ContactItem) -> Unit
) : RecyclerView.Adapter<ContactAdapter.Holder>() {

    fun update(newItems: List<ContactItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
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

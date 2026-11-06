package com.smartdialer.app.ui

import android.provider.CallLog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smartdialer.app.R
import com.smartdialer.app.data.CallLogItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CallLogAdapter(
    private var items: List<CallLogItem>,
    private val onClick: (CallLogItem) -> Unit,
    private val onLongClick: (CallLogItem) -> Unit
) : RecyclerView.Adapter<CallLogAdapter.Holder>() {

    private val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    fun update(newItems: List<CallLogItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calllog, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val avatar: TextView = view.findViewById(R.id.call_avatar)
        private val name: TextView = view.findViewById(R.id.call_name)
        private val info: TextView = view.findViewById(R.id.call_info)

        fun bind(item: CallLogItem) {
            val cachedName = item.name
            val label = if (cachedName.isNullOrBlank()) item.number else cachedName
            name.text = label
            avatar.text = label.take(1).uppercase()
            var line = typeLabel(item.type) + "  -  " + formatter.format(Date(item.date))
            if (item.count > 1) {
                line = line + "  (" + item.count + "x)"
            }
            info.text = line
            itemView.setOnClickListener { onClick(item) }
            itemView.setOnLongClickListener {
                onLongClick(item)
                true
            }
        }

        private fun typeLabel(type: Int): String {
            return when (type) {
                CallLog.Calls.INCOMING_TYPE -> "Incoming"
                CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                CallLog.Calls.MISSED_TYPE -> "Missed"
                CallLog.Calls.REJECTED_TYPE -> "Rejected"
                else -> "Call"
            }
        }
    }
}

package com.smartdialer.app.recents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smartdialer.app.R
import com.smartdialer.app.data.CallLogItem
import com.smartdialer.app.data.CallLogRepository
import com.smartdialer.app.data.SpamRepository
import com.smartdialer.app.ui.CallLogAdapter
import com.smartdialer.app.util.CallLauncher

class RecentsFragment : Fragment() {

    private lateinit var adapter: CallLogAdapter
    private lateinit var emptyView: TextView
    private var allCalls: List<CallLogItem> = emptyList()
    private var filterType = 0
    private val filterButtons = ArrayList<Button>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recents, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = view.findViewById<RecyclerView>(R.id.recents_list)
        emptyView = view.findViewById(R.id.recents_empty)
        adapter = CallLogAdapter(
            emptyList(),
            { item -> startCall(item.number) },
            { item -> showOptions(item.number, item.name) }
        )
        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = adapter

        val all = view.findViewById<Button>(R.id.filter_all)
        val missed = view.findViewById<Button>(R.id.filter_missed)
        val incoming = view.findViewById<Button>(R.id.filter_incoming)
        val outgoing = view.findViewById<Button>(R.id.filter_outgoing)

        filterButtons.add(all)
        filterButtons.add(missed)
        filterButtons.add(incoming)
        filterButtons.add(outgoing)

        all.setOnClickListener { setFilter(0) }
        missed.setOnClickListener { setFilter(3) }
        incoming.setOnClickListener { setFilter(1) }
        outgoing.setOnClickListener { setFilter(2) }

        setFilter(0)
    }

    override fun onResume() {
        super.onResume()
        allCalls = try {
            CallLogRepository.load(requireContext())
        } catch (e: Exception) {
            emptyList()
        }
        render()
    }

    private fun setFilter(type: Int) {
        filterType = type
        val off = ContextCompat.getColor(requireContext(), R.color.text_primary)
        val on = ContextCompat.getColor(requireContext(), R.color.white)
        for (button in filterButtons) {
            button.setBackgroundResource(R.drawable.bg_chip_off)
            button.setTextColor(off)
        }
        val index = when (type) {
            0 -> 0
            3 -> 1
            1 -> 2
            else -> 3
        }
        filterButtons[index].setBackgroundResource(R.drawable.bg_chip_on)
        filterButtons[index].setTextColor(on)
        render()
    }

    private fun render() {
        val items = if (filterType == 0) {
            allCalls
        } else {
            allCalls.filter { it.type == filterType }
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

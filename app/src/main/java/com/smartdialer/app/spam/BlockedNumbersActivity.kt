package com.smartdialer.app.spam

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.smartdialer.app.R
import com.smartdialer.app.data.SpamRepository

class BlockedNumbersActivity : AppCompatActivity() {

    private lateinit var listContainer: LinearLayout
    private lateinit var input: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked)

        listContainer = findViewById(R.id.blocked_list)
        input = findViewById(R.id.blocked_input)

        findViewById<Button>(R.id.blocked_add).setOnClickListener {
            val number = input.text.toString().trim()
            if (number.isNotEmpty()) {
                SpamRepository.block(this, number)
                input.setText("")
                render()
            }
        }

        render()
    }

    private fun render() {
        listContainer.removeAllViews()
        val numbers = SpamRepository.blockedNumbers(this)
        if (numbers.isEmpty()) {
            val empty = TextView(this)
            empty.text = "No blocked numbers yet"
            empty.setTextColor(getColor(R.color.text_secondary))
            empty.setPadding(8, 24, 8, 8)
            listContainer.addView(empty)
            return
        }
        for (number in numbers) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.setPadding(8, 12, 8, 12)

            val label = TextView(this)
            label.text = number
            label.textSize = 16f
            label.setTextColor(getColor(R.color.text_primary))
            label.layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
            row.addView(label)

            val remove = Button(this)
            remove.text = "Remove"
            remove.setOnClickListener {
                SpamRepository.unblock(this, number)
                render()
            }
            row.addView(remove)

            listContainer.addView(row)
        }
    }
}

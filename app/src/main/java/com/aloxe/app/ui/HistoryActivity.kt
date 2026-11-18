package com.aloxe.app.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.aloxe.app.data.MessageStore
import com.aloxe.app.databinding.ActivityHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private val dateFormat = SimpleDateFormat("HH:mm dd/MM", Locale("vi", "VN"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        renderHistory()
    }

    private fun renderHistory() {
        binding.listContainer.removeAllViews()
        val items = MessageStore(this).readAll()
        binding.textEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE

        items.forEach { message ->
            val row = TextView(this).apply {
                text = "${dateFormat.format(Date(message.timestampMillis))} · ${message.groupTitle}\n${message.content}"
                textSize = 14f
                setPadding(0, 16, 0, 16)
            }
            binding.listContainer.addView(row)
        }
    }
}

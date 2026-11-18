package com.aloxe.app.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aloxe.app.R
import com.aloxe.app.data.Prefs
import com.aloxe.app.databinding.ActivityKeywordsBinding

class KeywordsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKeywordsBinding
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeywordsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = Prefs(this)

        binding.editInclude.setText(prefs.getIncludeKeywords().joinToString("\n"))
        binding.editExclude.setText(prefs.getExcludeKeywords().joinToString("\n"))

        binding.btnSave.setOnClickListener {
            val include = binding.editInclude.text.toString().lines().map { it.trim() }
                .filter { it.isNotEmpty() }
            val exclude = binding.editExclude.text.toString().lines().map { it.trim() }
                .filter { it.isNotEmpty() }
            prefs.setIncludeKeywords(include)
            prefs.setExcludeKeywords(exclude)
            Toast.makeText(this, R.string.kw_saved, Toast.LENGTH_SHORT).show()
        }
    }
}

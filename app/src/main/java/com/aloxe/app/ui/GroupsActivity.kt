package com.aloxe.app.ui

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.aloxe.app.data.Prefs
import com.aloxe.app.databinding.ActivityGroupsBinding

class GroupsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupsBinding
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = Prefs(this)
    }

    override fun onResume() {
        super.onResume()
        renderGroups()
    }

    private fun renderGroups() {
        binding.listContainer.removeAllViews()
        val known = prefs.getKnownGroups().sorted()
        val enabled = prefs.getEnabledGroups()

        binding.textEmpty.visibility = if (known.isEmpty()) View.VISIBLE else View.GONE

        known.forEach { groupTitle ->
            val row = SwitchCompat(this).apply {
                text = groupTitle
                textSize = 15f
                setPadding(0, 24, 0, 24)
                isChecked = enabled.contains(groupTitle)
                setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                    val current = prefs.getEnabledGroups().toMutableSet()
                    if (isChecked) current.add(groupTitle) else current.remove(groupTitle)
                    prefs.setEnabledGroups(current)
                }
            }
            binding.listContainer.addView(row)
        }
    }
}

package com.aloxe.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.aloxe.app.R
import com.aloxe.app.databinding.ActivityPermissionsBinding

class PermissionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPermissionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOpenNotifSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
        binding.btnOpenBatterySettings.setOnClickListener {
            openBatterySettings()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStatuses()
    }

    private fun refreshStatuses() {
        val notifGranted = NotificationManagerCompat.getEnabledListenerPackages(this)
            .contains(packageName)
        binding.textNotifStatus.setText(
            if (notifGranted) R.string.perm_notif_status_on else R.string.perm_notif_status_off
        )

        val batteryIgnored = isIgnoringBatteryOptimizations()
        binding.textBatteryStatus.setText(
            if (batteryIgnored) R.string.perm_battery_status_on else R.string.perm_battery_status_off
        )
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val pm = getSystemService(PowerManager::class.java) ?: return true
        return pm.isIgnoringBatteryOptimizations(packageName)
    }

    private fun openBatterySettings() {
        try {
            val intent = Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        } catch (e: Exception) {
            // Một số máy (hoặc bản ROM tùy biến) không hỗ trợ màn hình này trực tiếp,
            // mở trang cài đặt pin chung thay thế.
            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
    }
}

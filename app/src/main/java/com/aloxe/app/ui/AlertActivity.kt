package com.aloxe.app.ui

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aloxe.app.R
import com.aloxe.app.databinding.ActivityAlertBinding

/**
 * Màn hình toàn màn hình, bật được cả khi máy đang khóa, kêu chuông cho tới khi
 * tài xế bấm "Nhận chuyến" hoặc "Bỏ qua".
 */
class AlertActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_GROUP_TITLE = "extra_group_title"
        const val EXTRA_CONTENT = "extra_content"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    private lateinit var binding: ActivityAlertBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupShowOverLockScreen()

        binding = ActivityAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val groupTitle = intent.getStringExtra(EXTRA_GROUP_TITLE).orEmpty()
        val content = intent.getStringExtra(EXTRA_CONTENT).orEmpty()
        binding.textGroupTitle.text = groupTitle
        binding.textContent.text = content

        binding.btnAccept.setOnClickListener {
            stopRinging()
            openZalo()
            finish()
        }
        binding.btnDismiss.setOnClickListener {
            stopRinging()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        startRinging()
    }

    override fun onPause() {
        super.onPause()
        stopRinging()
    }

    private fun setupShowOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun startRinging() {
        if (mediaPlayer != null) return
        try {
            val uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION)
            if (uri != null) {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(this@AlertActivity, uri)
                    isLooping = true
                    prepare()
                    start()
                }
            }
        } catch (e: Exception) {
            // Không có âm thanh mặc định thì vẫn rung, không chặn màn hình.
        }
        startVibrating()
    }

    private fun startVibrating() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = longArrayOf(0, 600, 300, 600, 300)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun stopRinging() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) it.stop()
                it.release()
            } catch (e: Exception) {
                // bỏ qua, chỉ dọn dẹp
            }
        }
        mediaPlayer = null
        vibrator?.cancel()
        vibrator = null
    }

    private fun openZalo() {
        val pm: PackageManager = packageManager
        val launchIntent = pm.getLaunchIntentForPackage("com.zing.zalo")
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            Toast.makeText(this, R.string.alert_zalo_missing, Toast.LENGTH_LONG).show()
        }
    }
}

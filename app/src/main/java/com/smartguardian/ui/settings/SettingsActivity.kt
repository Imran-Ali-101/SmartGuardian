package com.smartguardian.ui.settings

import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.smartguardian.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
        binding.toolbar.setNavigationOnClickListener { finish() }

        loadSavedSettings()
        setupSaveButton()
        setupVolumeSeekBar()
    }

    private fun loadSavedSettings() {
        val prefs = getSharedPreferences("sg_prefs", MODE_PRIVATE)
        binding.etBotToken.setText(prefs.getString("bot_token", ""))
        binding.etFallbackNumber.setText(prefs.getString("fallback_number", ""))
        binding.seekbarVolume.progress = prefs.getInt("siren_volume", 100)
        binding.tvVolumeValue.text = "${prefs.getInt("siren_volume", 100)}%"
    }

    private fun setupVolumeSeekBar() {
        binding.seekbarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvVolumeValue.text = "$progress%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupSaveButton() {
        binding.btnSaveSettings.setOnClickListener {
            val botToken = binding.etBotToken.text.toString().trim()
            val fallbackNumber = binding.etFallbackNumber.text.toString().trim()
            val volume = binding.seekbarVolume.progress

            if (botToken.isEmpty()) {
                binding.etBotToken.error = "Bot Token required"
                return@setOnClickListener
            }

            if (fallbackNumber.isEmpty()) {
                binding.etFallbackNumber.error = "Fallback number required"
                return@setOnClickListener
            }

            getSharedPreferences("sg_prefs", MODE_PRIVATE)
                .edit()
                .putString("bot_token", botToken)
                .putString("fallback_number", fallbackNumber)
                .putInt("siren_volume", volume)
                .apply()

            finish()
        }
    }
}
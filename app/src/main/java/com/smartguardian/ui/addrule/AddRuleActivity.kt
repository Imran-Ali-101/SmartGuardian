package com.smartguardian.ui.addrule

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.smartguardian.data.RuleEntity
import com.smartguardian.databinding.ActivityAddRuleBinding

class AddRuleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRuleBinding
    private var selectedType = RuleType.SIREN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRuleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupTypeSelector()
        setupSaveButton()
    }

    private fun setupTypeSelector() {
        binding.btnSirenType.setOnClickListener {
            selectedType = RuleType.SIREN
            updateTypeUI()
        }

        binding.btnLocationType.setOnClickListener {
            selectedType = RuleType.LOCATION
            updateTypeUI()
        }

        updateTypeUI()
    }

    private fun updateTypeUI() {
        when (selectedType) {
            RuleType.SIREN -> {
                binding.btnSirenType.isSelected = true
                binding.btnLocationType.isSelected = false
                binding.layoutSirenSettings.visibility = View.VISIBLE
                binding.layoutLocationSettings.visibility = View.GONE
                binding.btnSaveRule.text = "+ Save Rule"
                supportActionBar?.title = "Add New Rule"
            }
            RuleType.LOCATION -> {
                binding.btnSirenType.isSelected = false
                binding.btnLocationType.isSelected = true
                binding.layoutSirenSettings.visibility = View.GONE
                binding.layoutLocationSettings.visibility = View.VISIBLE
                binding.btnSaveRule.text = "+ Save Tracking Rule"
                supportActionBar?.title = "Add New Rule (Location)"
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnSaveRule.setOnClickListener {
            val keyword = binding.etSmsKeyword.text.toString().trim()

            if (keyword.isEmpty()) {
                binding.etSmsKeyword.error = "SMS keyword required"
                return@setOnClickListener
            }

            val rule = when (selectedType) {
                RuleType.SIREN -> RuleEntity(
                    type = "SIREN",
                    title = "Anti-Theft Emergency Alarm",
                    label = "Loud Siren, $keyword",
                    status = "Waiting for command",
                    smsKeyword = keyword,
                    isEnabled = true
                )
                RuleType.LOCATION -> RuleEntity(
                    type = "LOCATION",
                    title = "Telegram Tracking Command",
                    label = "SMS Command, $keyword",
                    status = "Waiting for command",
                    smsKeyword = keyword,
                    isEnabled = true
                )
            }

            // TODO: ViewModel দিয়ে Room এ save করব
            finish()
        }
    }

    enum class RuleType { SIREN, LOCATION }
}
package com.smartguardian.ui.onboarding

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.smartguardian.MainActivity
import com.smartguardian.databinding.ActivityOnboardingBinding
import com.smartguardian.utils.PermissionHelper

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private var currentStep = 0

    private val steps = listOf(
        OnboardingStep(
            title = "SMS & Location Access",
            description = "Smart Guardian needs SMS and Location permission to track and respond to commands.",
            buttonText = "Grant Permission"
        ),
        OnboardingStep(
            title = "Do Not Disturb Access",
            description = "Required to automatically switch between Ring and Vibrate mode.",
            buttonText = "Open Sound Settings"
        ),
        OnboardingStep(
            title = "Battery Optimization",
            description = "Disable battery optimization so the app works reliably in background.",
            buttonText = "Disable Optimization"
        ),
        OnboardingStep(
            title = "Auto-Start Permission",
            description = "Allow the app to auto-start on boot (required for some devices).",
            buttonText = "Enable Auto-Start"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showStep(currentStep)

        binding.btnAction.setOnClickListener {
            handleStepAction(currentStep)
        }

        binding.btnNext.setOnClickListener {
            goNextStep()
        }

        binding.btnSkip.setOnClickListener {
            goNextStep()
        }
    }

    private fun showStep(step: Int) {
        val s = steps[step]
        binding.tvTitle.text = s.title
        binding.tvDescription.text = s.description
        binding.btnAction.text = s.buttonText
        binding.tvStepIndicator.text = "${step + 1} / ${steps.size}"

        val isLast = step == steps.size - 1
        binding.btnNext.text = if (isLast) "Get Started" else "Next"
    }

    private fun handleStepAction(step: Int) {
        when (step) {
            0 -> PermissionHelper.requestRuntimePermissions(this)
            1 -> startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
            2 -> {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
            3 -> PermissionHelper.openAutoStartSettings(this)
        }
    }

    private fun goNextStep() {
        if (currentStep < steps.size - 1) {
            currentStep++
            showStep(currentStep)
        } else {
            finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        getSharedPreferences("sg_prefs", MODE_PRIVATE)
            .edit()
            .putBoolean("onboarding_done", true)
            .apply()

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    data class OnboardingStep(
        val title: String,
        val description: String,
        val buttonText: String
    )
}
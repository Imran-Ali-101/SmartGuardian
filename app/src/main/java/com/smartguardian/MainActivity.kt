package com.smartguardian

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.smartguardian.databinding.ActivityMainBinding
import com.smartguardian.ui.onboarding.OnboardingActivity
import com.smartguardian.ui.settings.SettingsActivity
import com.smartguardian.utils.PermissionHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // First launch check — onboarding
        val prefs = getSharedPreferences("sg_prefs", MODE_PRIVATE)
        val onboardingDone = prefs.getBoolean("onboarding_done", false)

        if (!onboardingDone) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        // Permission check on every open
        checkPermissionsOnResume()
    }

    override fun onResume() {
        super.onResume()
        checkPermissionsOnResume()
    }

    private fun checkPermissionsOnResume() {
        val missing = PermissionHelper.getMissingPermissions(this)
        if (missing.isNotEmpty()) {
            PermissionHelper.showMissingPermissionDialog(this, missing)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_about -> {
                // About dialog — পরে যোগ করব
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

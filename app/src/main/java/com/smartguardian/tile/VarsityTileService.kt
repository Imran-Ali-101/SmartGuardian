package com.smartguardian.tile

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.smartguardian.service.RingerService

class VarsityTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()

        val prefs = getSharedPreferences("sg_prefs", MODE_PRIVATE)
        val currentState = prefs.getBoolean("varsity_mode", false)
        val newState = !currentState

        prefs.edit().putBoolean("varsity_mode", newState).apply()

        if (newState) {
            // Varsity ON — RingerService চালু করো
            val intent = Intent(this, RingerService::class.java).apply {
                action = RingerService.ACTION_VARSITY_ON
            }
            startForegroundService(intent)
        } else {
            // Varsity OFF
            val intent = Intent(this, RingerService::class.java).apply {
                action = RingerService.ACTION_VARSITY_OFF
            }
            startService(intent)
        }

        updateTileState()
    }

    private fun updateTileState() {
        val prefs = getSharedPreferences("sg_prefs", MODE_PRIVATE)
        val varsityOn = prefs.getBoolean("varsity_mode", false)

        qsTile?.apply {
            state = if (varsityOn) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            label = "Varsity Mode"
            subtitle = if (varsityOn) "Always Vibrate" else "Off"
            updateTile()
        }
    }

    override fun onTileAdded() {
        super.onTileAdded()
        updateTileState()
    }

    override fun onStopListening() {
        super.onStopListening()
    }
}
package com.smartguardian.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rules")
data class RuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String,         // "SIREN" or "LOCATION"
    val title: String,
    val label: String,
    val status: String,
    val smsKeyword: String,
    val isEnabled: Boolean = true
)
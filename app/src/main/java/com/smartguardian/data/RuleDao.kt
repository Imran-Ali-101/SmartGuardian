package com.smartguardian.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RuleDao {

    @Query("SELECT * FROM rules ORDER BY id DESC")
    fun getAllRules(): LiveData<List<RuleEntity>>

    @Query("SELECT * FROM rules ORDER BY id DESC")
    suspend fun getAllRulesSync(): List<RuleEntity>

    @Query("SELECT * FROM rules WHERE id = :id LIMIT 1")
    suspend fun getRuleById(id: Int): RuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: RuleEntity)

    @Update
    suspend fun updateRule(rule: RuleEntity)

    @Delete
    suspend fun deleteRule(rule: RuleEntity)

    @Query("UPDATE rules SET isEnabled = :enabled WHERE id = :id")
    suspend fun setRuleEnabled(id: Int, enabled: Boolean)
}
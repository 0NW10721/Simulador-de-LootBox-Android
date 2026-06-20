package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chest_history")
data class ChestHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rewardName: String,
    val rewardRarity: String, // "LENDARIO", "EPICO", "COMUM"
    val rewardIcon: String,
    val timestamp: Long = System.currentTimeMillis()
)

package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rewards")
data class Reward(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val rarity: String, // "MITICO", "LENDARIO", "EPICO", "COMUM"
    val icon: String,   // Emoji descriptor (e.g. "⚔️", "💎", "🪙")
    val isCustom: Boolean = false
) {
    fun getRarityLabel(): String {
        return when (rarity) {
            "MITICO" -> "Mítico (0.5%)"
            "LENDARIO" -> "Lendário (3%)"
            "EPICO" -> "Épico (12%)"
            else -> "Comum (84.5%)"
        }
    }
}

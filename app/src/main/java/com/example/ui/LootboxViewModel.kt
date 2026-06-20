package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ChestHistory
import com.example.data.LootboxRepository
import com.example.data.Reward
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class ChestState {
    IDLE, SHAKING, BURSTING, REVEALED
}

data class LootboxStats(
    val totalOpened: Int = 0,
    val mythicCount: Int = 0,
    val legendaryCount: Int = 0,
    val epicCount: Int = 0,
    val commonCount: Int = 0
)

class LootboxViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = LootboxRepository(database.lootboxDao())

    val allRewards: StateFlow<List<Reward>> = repository.allRewards
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allHistory: StateFlow<List<ChestHistory>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _chestState = MutableStateFlow(ChestState.IDLE)
    val chestState: StateFlow<ChestState> = _chestState.asStateFlow()

    private val _wonReward = MutableStateFlow<Reward?>(null)
    val wonReward: StateFlow<Reward?> = _wonReward.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // Bulk chest opening outputs
    private val _tenWonRewards = MutableStateFlow<List<Reward>>(emptyList())
    val tenWonRewards: StateFlow<List<Reward>> = _tenWonRewards.asStateFlow()

    private val _showTenRewardsDialog = MutableStateFlow(false)
    val showTenRewardsDialog: StateFlow<Boolean> = _showTenRewardsDialog.asStateFlow()

    // Dynamic aggregated statistics calculated reactively from our history flow
    val stats: StateFlow<LootboxStats> = repository.allHistory.map { history ->
        val total = history.size
        val mythic = history.count { it.rewardRarity == "MITICO" }
        val legendary = history.count { it.rewardRarity == "LENDARIO" }
        val epic = history.count { it.rewardRarity == "EPICO" }
        val common = history.count { it.rewardRarity == "COMUM" }
        LootboxStats(total, mythic, legendary, epic, common)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LootboxStats()
    )

    private fun getDefaultRewardsList(): List<Reward> {
        return listOf(
            // Mythics (0.5%)
            Reward(name = "Foice Ceifadora de Almas", rarity = "MITICO", icon = "💀"),
            Reward(name = "Orbe Primordial do Caos", rarity = "MITICO", icon = "☄️"),
            Reward(name = "Tridente do Destino Eterno", rarity = "MITICO", icon = "🔱"),
            Reward(name = "Divindade Estelar Ancestral", rarity = "MITICO", icon = "🌌"),

            // Legendaries (3%)
            Reward(name = "Dragão de Ouro Ancestral", rarity = "LENDARIO", icon = "🐲"),
            Reward(name = "Espada Celestial Cósmica", rarity = "LENDARIO", icon = "⚔️"),
            Reward(name = "Cálice de Fogo Sagrado", rarity = "LENDARIO", icon = "🍷"),
            Reward(name = "Asas de Fênix Divina", rarity = "LENDARIO", icon = "🪽"),
            Reward(name = "Coroa Real de Diamantes", rarity = "LENDARIO", icon = "👑"),

            // Epics (12%)
            Reward(name = "Escudo Rúnico do Sol", rarity = "EPICO", icon = "🛡️"),
            Reward(name = "Anel do Eclipse Lunar", rarity = "EPICO", icon = "💍"),
            Reward(name = "Amuleto de Olho Místico", rarity = "EPICO", icon = "🧿"),
            Reward(name = "Poção de Mana Infinita", rarity = "EPICO", icon = "🧪"),
            Reward(name = "Besta Cósmica Veloz", rarity = "EPICO", icon = "🏹"),
            Reward(name = "Cetro Mágico de Jade", rarity = "EPICO", icon = "🪄"),

            // Commons (84.5%)
            Reward(name = "Saco de Moedas de Bronze", rarity = "COMUM", icon = "🪙"),
            Reward(name = "Poção de Vida Menor", rarity = "COMUM", icon = "🧪"),
            Reward(name = "Pergaminho de Teleporte", rarity = "COMUM", icon = "📜"),
            Reward(name = "Minério de Ferro Bruto", rarity = "COMUM", icon = "🪵"),
            Reward(name = "Maçã de Energia Silvestre", rarity = "COMUM", icon = "🍎"),
            Reward(name = "Chave de Ferro Antiga", rarity = "COMUM", icon = "🔑"),
            Reward(name = "Lâmina Curta Enferrujada", rarity = "COMUM", icon = "🔪"),
            Reward(name = "Pão de Trigo Fresco", rarity = "COMUM", icon = "🍞")
        )
    }

    fun openChest() {
        if (_chestState.value != ChestState.IDLE || _isProcessing.value) return

        viewModelScope.launch {
            _isProcessing.value = true
            _chestState.value = ChestState.SHAKING
            _wonReward.value = null
            _tenWonRewards.value = emptyList()
            
            // Suspend for high suspense shaking (1.6 seconds)
            delay(1600)
            
            _chestState.value = ChestState.BURSTING
            // Intense flash visual timing (0.4 seconds)
            delay(400)

            // Roll according to user-provided chance specs:
            // Mítico (0.5%): roll < 0.5
            // Lendário (3%): 0.5 <= roll < 3.5
            // Épico (12%): 3.5 <= roll < 15.5
            // Comum (84.5%): 15.5 <= roll <= 100.0
            val roll = Random.nextDouble(0.0, 100.0)
            val chosenRarity = when {
                roll < 0.5 -> "MITICO"
                roll < 3.5 -> "LENDARIO"
                roll < 15.5 -> "EPICO"
                else -> "COMUM"
            }

            var currentRewards = allRewards.value
            if (currentRewards.isEmpty()) {
                val defaults = getDefaultRewardsList()
                repository.insertRewards(defaults)
                currentRewards = defaults
            }

            var filteredRewards = currentRewards.filter { it.rarity == chosenRarity }

            // Dynamic fallback system using active user list
            if (filteredRewards.isEmpty()) {
                filteredRewards = currentRewards.filter { it.rarity == "COMUM" }
                if (filteredRewards.isEmpty()) {
                    filteredRewards = currentRewards
                }
            }

            if (filteredRewards.isNotEmpty()) {
                val reward = filteredRewards.random()
                _wonReward.value = reward

                // Persist the win record to SQLite Room history
                repository.insertHistory(
                    ChestHistory(
                        rewardName = reward.name,
                        rewardRarity = reward.rarity,
                        rewardIcon = reward.icon
                    )
                )
            }

            _chestState.value = ChestState.REVEALED
            _isProcessing.value = false
        }
    }

    fun openTenChests() {
        if (_chestState.value != ChestState.IDLE || _isProcessing.value) return

        viewModelScope.launch {
            _isProcessing.value = true
            _chestState.value = ChestState.SHAKING
            _wonReward.value = null
            _tenWonRewards.value = emptyList()

            // Faster shaking sequence for 10 openings
            delay(1200)

            _chestState.value = ChestState.BURSTING
            delay(300)

            var currentRewards = allRewards.value
            if (currentRewards.isEmpty()) {
                val defaults = getDefaultRewardsList()
                repository.insertRewards(defaults)
                currentRewards = defaults
            }

            val tempResults = mutableListOf<Reward>()

            repeat(10) {
                val roll = Random.nextDouble(0.0, 100.0)
                val chosenRarity = when {
                    roll < 0.5 -> "MITICO"
                    roll < 3.5 -> "LENDARIO"
                    roll < 15.5 -> "EPICO"
                    else -> "COMUM"
                }

                var filteredRewards = currentRewards.filter { it.rarity == chosenRarity }
                if (filteredRewards.isEmpty()) {
                    filteredRewards = currentRewards.filter { it.rarity == "COMUM" }
                    if (filteredRewards.isEmpty()) {
                        filteredRewards = currentRewards
                    }
                }

                if (filteredRewards.isNotEmpty()) {
                    val reward = filteredRewards.random()
                    tempResults.add(reward)

                    // Persist to history
                    repository.insertHistory(
                        ChestHistory(
                            rewardName = reward.name,
                            rewardRarity = reward.rarity,
                            rewardIcon = reward.icon
                        )
                    )
                }
            }

            _tenWonRewards.value = tempResults
            _showTenRewardsDialog.value = true
            _chestState.value = ChestState.REVEALED
            _isProcessing.value = false
        }
    }

    fun dismissTenRewards() {
        _showTenRewardsDialog.value = false
        _tenWonRewards.value = emptyList()
    }

    fun resetChest() {
        _chestState.value = ChestState.IDLE
        _wonReward.value = null
    }

    fun addCustomReward(name: String, rarity: String, icon: String) {
        if (name.isBlank() || icon.isBlank()) return
        viewModelScope.launch {
            repository.insertReward(
                Reward(
                    name = name.trim(),
                    rarity = rarity,
                    icon = icon.trim(),
                    isCustom = true
                )
            )
        }
    }

    fun deleteReward(reward: Reward) {
        viewModelScope.launch {
            repository.deleteReward(reward)
        }
    }

    fun restoreDefaultRewards() {
        viewModelScope.launch {
            repository.clearAllRewards()
            repository.insertRewards(getDefaultRewardsList())
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun exportBackup(): String {
        return try {
            val rootObj = org.json.JSONObject()

            val rewardsArray = org.json.JSONArray()
            for (reward in allRewards.value) {
                val rewardObj = org.json.JSONObject()
                rewardObj.put("name", reward.name)
                rewardObj.put("rarity", reward.rarity)
                rewardObj.put("icon", reward.icon)
                rewardObj.put("isCustom", reward.isCustom)
                rewardsArray.put(rewardObj)
            }
            rootObj.put("rewards", rewardsArray)

            val historyArray = org.json.JSONArray()
            for (history in allHistory.value) {
                val historyObj = org.json.JSONObject()
                historyObj.put("rewardName", history.rewardName)
                historyObj.put("rewardRarity", history.rewardRarity)
                historyObj.put("rewardIcon", history.rewardIcon)
                historyObj.put("timestamp", history.timestamp)
                historyArray.put(historyObj)
            }
            rootObj.put("history", historyArray)

            rootObj.toString(2)
        } catch (e: Exception) {
            ""
        }
    }

    fun importBackup(jsonString: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val rootObj = org.json.JSONObject(jsonString)

                // Parse rewards
                val rewardsArray = rootObj.optJSONArray("rewards") ?: org.json.JSONArray()
                val parsedRewards = mutableListOf<Reward>()
                for (i in 0 until rewardsArray.length()) {
                    val rewardObj = rewardsArray.getJSONObject(i)
                    parsedRewards.add(
                        Reward(
                            name = rewardObj.getString("name"),
                            rarity = rewardObj.getString("rarity"),
                            icon = rewardObj.getString("icon"),
                            isCustom = rewardObj.optBoolean("isCustom", false)
                        )
                    )
                }

                // Parse history
                val historyArray = rootObj.optJSONArray("history") ?: org.json.JSONArray()
                val parsedHistory = mutableListOf<ChestHistory>()
                for (i in 0 until historyArray.length()) {
                    val historyObj = historyArray.getJSONObject(i)
                    parsedHistory.add(
                        ChestHistory(
                            rewardName = historyObj.getString("rewardName"),
                            rewardRarity = historyObj.getString("rewardRarity"),
                            rewardIcon = historyObj.getString("rewardIcon"),
                            timestamp = historyObj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }

                // Apply changes in DB
                repository.clearAllRewards()
                repository.clearHistory()

                if (parsedRewards.isNotEmpty()) {
                    repository.insertRewards(parsedRewards)
                } else {
                    repository.insertRewards(getDefaultRewardsList())
                }

                if (parsedHistory.isNotEmpty()) {
                    repository.insertHistories(parsedHistory)
                }

                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }
}

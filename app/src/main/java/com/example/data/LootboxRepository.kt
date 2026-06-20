package com.example.data

import kotlinx.coroutines.flow.Flow

class LootboxRepository(private val dao: LootboxDao) {
    val allRewards: Flow<List<Reward>> = dao.getAllRewards()
    val allHistory: Flow<List<ChestHistory>> = dao.getHistory()

    suspend fun insertReward(reward: Reward) {
        dao.insertReward(reward)
    }

    suspend fun insertRewards(rewards: List<Reward>) {
        dao.insertRewards(rewards)
    }

    suspend fun deleteReward(reward: Reward) {
        dao.deleteReward(reward)
    }

    suspend fun clearAllRewards() {
        dao.clearAllRewards()
    }

    suspend fun insertHistory(historyEntry: ChestHistory) {
        dao.insertHistory(historyEntry)
    }

    suspend fun insertHistories(histories: List<ChestHistory>) {
        dao.insertHistories(histories)
    }

    suspend fun clearHistory() {
        dao.clearHistory()
    }
}

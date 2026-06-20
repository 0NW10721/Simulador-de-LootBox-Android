package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LootboxDao {
    // Rewards Management
    @Query("SELECT * FROM rewards ORDER BY CASE rarity WHEN 'MITICO' THEN 1 WHEN 'LENDARIO' THEN 2 WHEN 'EPICO' THEN 3 ELSE 4 END, name ASC")
    fun getAllRewards(): Flow<List<Reward>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReward(reward: Reward)

    @Delete
    suspend fun deleteReward(reward: Reward)

    @Query("SELECT COUNT(*) FROM rewards")
    suspend fun getRewardsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewards(rewards: List<Reward>)

    @Query("DELETE FROM rewards")
    suspend fun clearAllRewards()

    // Chest History Management
    @Query("SELECT * FROM chest_history ORDER BY timestamp DESC")
    fun getHistory(): Flow<List<ChestHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(historyEntry: ChestHistory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistories(histories: List<ChestHistory>)

    @Query("DELETE FROM chest_history")
    suspend fun clearHistory()
}

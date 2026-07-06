package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardsDao {
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStatsFlow(): Flow<UserStats?>

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getUserStatsDirect(): UserStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: UserStats)

    @Query("SELECT * FROM reward_history ORDER BY timestamp DESC")
    fun getAllHistoryFlow(): Flow<List<RewardHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryItem(item: RewardHistory)

    @Query("DELETE FROM reward_history")
    suspend fun clearHistory()
}

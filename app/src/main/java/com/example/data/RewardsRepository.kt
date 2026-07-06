package com.example.data

import kotlinx.coroutines.flow.Flow

class RewardsRepository(private val rewardsDao: RewardsDao) {
    val userStatsFlow: Flow<UserStats?> = rewardsDao.getUserStatsFlow()
    val allHistoryFlow: Flow<List<RewardHistory>> = rewardsDao.getAllHistoryFlow()

    suspend fun getStatsDirect(): UserStats {
        return rewardsDao.getUserStatsDirect() ?: UserStats()
    }

    suspend fun updateStats(stats: UserStats) {
        rewardsDao.insertOrUpdateStats(stats)
    }

    suspend fun addHistory(item: RewardHistory) {
        rewardsDao.insertHistoryItem(item)
    }

    suspend fun clearAllHistory() {
        rewardsDao.clearHistory()
        rewardsDao.insertOrUpdateStats(UserStats())
    }
}

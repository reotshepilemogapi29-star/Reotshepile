package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val currentBalanceMb: Int = 0,
    val totalEarnedMb: Int = 0,
    val totalVideosWatched: Int = 0,
    val videosWatchedInCurrentCycle: Int = 0, // 0 to 10
    val phoneNumber: String = "",
    val selectedCarrier: String = "Vodacom"
)

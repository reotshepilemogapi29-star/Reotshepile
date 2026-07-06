package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reward_history")
data class RewardHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amountMb: Int, // Positive for earnings, negative for activations
    val timestamp: Long = System.currentTimeMillis(),
    val status: String // "Completed", "Synced", "Pending"
)

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val xpReward: Int,
    val isCompleted: Boolean = false,
    val badgeName: String,               // Badge earned upon completion
    val rankRequired: String            // "Beginner", "Grower", "Urban Farmer", "Master Gardener"
)

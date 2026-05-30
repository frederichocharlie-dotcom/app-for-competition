package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plants")
data class PlantEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String,                   // e.g., "Tomat", "Cabai", "Lidah Buaya"
    val location: String,               // "Indoor", "Outdoor", "Balkon"
    val healthScore: Int = 85,          // 0 - 100 Garden Health Score helper
    val soilType: String = "Humus",     // e.g. "Humus", "Liat", "Merah Berpasir"
    val plantedAt: Long = System.currentTimeMillis(),
    val lastWatered: Long = System.currentTimeMillis(),
    val waterIntervalDays: Int = 3,
    val growthStage: String = "Semaian", // "Bibit", "Semaian", "Vegetatif", "Panen"
    val memoryLogsJson: String = "[]",  // AI Plant Memory: JSON list of string logs
    val customNotes: String = ""
)

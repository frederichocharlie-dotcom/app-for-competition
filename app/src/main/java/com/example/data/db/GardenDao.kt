package com.example.data.db

import androidx.room.*
import com.example.data.model.PlantEntity
import com.example.data.model.MissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GardenDao {
    // --- Plant Queries ---
    @Query("SELECT * FROM plants ORDER BY id DESC")
    fun getAllPlants(): Flow<List<PlantEntity>>

    @Query("SELECT * FROM plants WHERE id = :id")
    fun getPlantById(id: Int): Flow<PlantEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: PlantEntity): Long

    @Update
    suspend fun updatePlant(plant: PlantEntity)

    @Delete
    suspend fun deletePlant(plant: PlantEntity)

    // --- Mission Queries ---
    @Query("SELECT * FROM missions ORDER BY id ASC")
    fun getAllMissions(): Flow<List<MissionEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMissions(missions: List<MissionEntity>)

    @Update
    suspend fun updateMission(mission: MissionEntity)

    @Query("DELETE FROM plants")
    suspend fun deleteAllPlants()

    @Query("DELETE FROM missions")
    suspend fun deleteAllMissions()
}

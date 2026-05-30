package com.example.data.repository

import com.example.data.db.GardenDao
import com.example.data.model.PlantEntity
import com.example.data.model.MissionEntity
import kotlinx.coroutines.flow.Flow

class GardenRepository(private val gardenDao: GardenDao) {
    val allPlants: Flow<List<PlantEntity>> = gardenDao.getAllPlants()
    val allMissions: Flow<List<MissionEntity>> = gardenDao.getAllMissions()

    fun getPlantById(id: Int): Flow<PlantEntity?> {
        return gardenDao.getPlantById(id)
    }

    suspend fun insertPlant(plant: PlantEntity): Long {
        return gardenDao.insertPlant(plant)
    }

    suspend fun updatePlant(plant: PlantEntity) {
        gardenDao.updatePlant(plant)
    }

    suspend fun deletePlant(plant: PlantEntity) {
        gardenDao.deletePlant(plant)
    }

    suspend fun insertMissions(missions: List<MissionEntity>) {
        gardenDao.insertMissions(missions)
    }

    suspend fun updateMission(mission: MissionEntity) {
        gardenDao.updateMission(mission)
    }

    suspend fun deleteAllPlants() {
        gardenDao.deleteAllPlants()
    }

    suspend fun deleteAllMissions() {
        gardenDao.deleteAllMissions()
    }
}

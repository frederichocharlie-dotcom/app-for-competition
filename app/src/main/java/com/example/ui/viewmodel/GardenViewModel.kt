package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.MissionEntity
import com.example.data.model.PlantEntity
import com.example.data.repository.GardenRepository
import com.example.data.api.GeminiClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// UI States
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

// Custom Marketplace Item with Seed DNA Passport Integration
data class MarketItem(
    val id: Int,
    val name: String,
    val category: String, // "Benih", "Pupuk", "Peralatan"
    val price: Double,
    val sellerName: String,
    val location: String,
    val imageResName: String, // visual mock
    val origin: String, // Passport: Asal daerah
    val idealCondition: String, // Passport: Kondisi ideal
    val communityHistory: String // Passport: Histori komunitas
)

// Plant SOS Community Forum structure
data class SosThread(
    val id: Int,
    val title: String,
    val plantType: String,
    val author: String,
    val description: String,
    val severity: String, // "🟢 Aman", "🟡 Sedang", "🔴 Kritis"
    val expertAnswer: String = "",
    val votesCount: Int = 0,
    val timestamp: String
)

class GardenViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GardenRepository
    
    // --- AUTHENTICATION & SECURE CLOUD SYNC STATE ---
    data class AuthUser(
        val email: String? = null,
        val phone: String? = null,
        val displayName: String = "Raka Pratama",
        val isGuest: Boolean = false,
        val syncTimestamp: String = "Never"
    )

    data class SimulatedCloudBackup(
        val emailOrPhone: String,
        val displayName: String,
        val savedXp: Int,
        val savedPlants: List<PlantEntity>,
        val savedMissions: List<MissionEntity>
    )

    private val _currentUser = MutableStateFlow<AuthUser?>(null) // null = on boarding / needs login choice
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    // Seeds simulated "remote server" database to enable true cross-device data loading simulation
    private val cloudAccountStorage = mutableMapOf<String, SimulatedCloudBackup>()

    // Core database flows
    val allPlants: StateFlow<List<PlantEntity>>
    val allMissions: StateFlow<List<MissionEntity>>

    // UI Interactive States
    private val _soilScanState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val soilScanState: StateFlow<UiState<String>> = _soilScanState.asStateFlow()

    private val _gardenCoachState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val gardenCoachState: StateFlow<UiState<String>> = _gardenCoachState.asStateFlow()

    private val _emergencyScanState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val emergencyScanState: StateFlow<UiState<String>> = _emergencyScanState.asStateFlow()

    private val _simulatorState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val simulatorState: StateFlow<UiState<String>> = _simulatorState.asStateFlow()

    private val _biodiversityState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val biodiversityState: StateFlow<UiState<String>> = _biodiversityState.asStateFlow()

    private val _reverseMarketplaceState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val reverseMarketplaceState: StateFlow<UiState<String>> = _reverseMarketplaceState.asStateFlow()

    // Location & Weather Simulation State
    private val _userLocation = MutableStateFlow("Malang, Jawa Timur")
    val userLocation: StateFlow<String> = _userLocation.asStateFlow()

    private val _weatherState = MutableStateFlow(
        WeatherInfo(temp = 29, condition = "Cerah Berawan", rainChance = 75, humidity = 78, advice = "Prediksi hujan 75% sore ini. Disarankan untuk menunda penyiraman luar ruangan.")
    )
    val weatherState: StateFlow<WeatherInfo> = _weatherState.asStateFlow()

    // Gamification state
    private val _userXp = MutableStateFlow(120)
    val userXp: StateFlow<Int> = _userXp.asStateFlow()

    // Redeemed vouchers state
    private val _redeemedVouchers = MutableStateFlow<List<String>>(emptyList())
    val redeemedVouchers: StateFlow<List<String>> = _redeemedVouchers.asStateFlow()

    fun redeemVoucher(xpCost: Int, voucherName: String): Boolean {
        if (_userXp.value >= xpCost) {
            _userXp.value -= xpCost
            _redeemedVouchers.value = _redeemedVouchers.value + voucherName
            return true
        }
        return false
    }

    // Marketplace & Reverse Marketplace listing
    private val _marketplaceItems = MutableStateFlow<List<MarketItem>>(emptyList())
    val marketplaceItems: StateFlow<List<MarketItem>> = _marketplaceItems.asStateFlow()

    // Forum state
    private val _sosForumThreads = MutableStateFlow<List<SosThread>>(emptyList())
    val sosForumThreads: StateFlow<List<SosThread>> = _sosForumThreads.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GardenRepository(database.gardenDao())
        
        allPlants = repository.allPlants.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allMissions = repository.allMissions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Prefill simulated cloud map for cross-device synchronization demo
        prefillCloudSimulations()

        // Seed initial missions and assets
        seedMissionsAndMarketplace()
        generateMorningBriefing()
    }

    private fun seedMissionsAndMarketplace() {
        viewModelScope.launch {
            // Seed missions database
            val defaultMissions = listOf(
                MissionEntity(1, "Langkah Pertama", "Tambahkan tanaman pertama Anda ke kebun Palawa.", 100, false, "🌱 Pemula Hijau", "Beginner"),
                MissionEntity(2, "Ahli Tanah Kota", "Lakukan analisis tanah menggunakan media foto kebun.", 150, false, "🧪 Detektif Tanah", "Beginner"),
                MissionEntity(3, "Visioner Kebun", "Jalankan simulasi pertumbuhan tomat di menu Garden Simulator.", 120, false, "🔮 Penjelajah Waktu", "Grower"),
                MissionEntity(4, "Penjaga Keanekaragaman", "Scan tumbuhan untuk memicu Pelindung Keanekaragaman.", 200, false, "⭐ Pelindung Langka", "Urban Farmer"),
                MissionEntity(5, "Pahlawan Ekologi", "Kurangi pemborosan air hingga 100L berdasarkan saran ekologis.", 250, false, "💧 Pendekar Air", "Urban Farmer"),
                MissionEntity(6, "Bibir Hijau Sejati", "Panen pertama dari salah satu tanaman terdaftar Anda.", 500, false, "🏆 Tunas Master", "Master Gardener")
            )
            repository.insertMissions(defaultMissions)

            // Seed default exemplary indoor plants if database is empty
            try {
                val existing = repository.allPlants.first()
                if (existing.isEmpty()) {
                    val initLogs1 = JSONArray().put("[$currentTimestamp] Lidah Buaya ditanam di dalam ruangan.").toString()
                    repository.insertPlant(PlantEntity(
                        id = 0,
                        name = "Lidah Buaya Kamar",
                        type = "Lidah Buaya",
                        location = "Indoor",
                        healthScore = 95,
                        soilType = "Pasir Berpori",
                        plantedAt = System.currentTimeMillis() - 4 * 24 * 3600 * 1000,
                        lastWatered = System.currentTimeMillis() - 2 * 24 * 3600 * 1000,
                        waterIntervalDays = 14,
                        growthStage = "Vegetatif",
                        memoryLogsJson = initLogs1,
                        customNotes = "Sangat baik untuk sirkulasi udara bersih dalam ruangan, hanya memerlukan sedikit air."
                    ))

                    val initLogs2 = JSONArray().put("[$currentTimestamp] Monstera diletakkan di sudut dekat jendela teduh.").toString()
                    repository.insertPlant(PlantEntity(
                        id = 0,
                        name = "Monstera Eksotik",
                        type = "Monstera",
                        location = "Indoor",
                        healthScore = 88,
                        soilType = "Humus Lembab",
                        plantedAt = System.currentTimeMillis() - 12 * 24 * 3600 * 1000,
                        lastWatered = System.currentTimeMillis() - 1 * 24 * 3600 * 1000,
                        waterIntervalDays = 7,
                        growthStage = "Vegetatif",
                        memoryLogsJson = initLogs2,
                        customNotes = "Tanaman hias tropis ikonik, tahan di dalam ruangan teduh dengan penyiraman sedang."
                    ))

                    val initLogs3 = JSONArray().put("[$currentTimestamp] Lidah Mertua diletakkan di sudut ruang kerja AC.").toString()
                    repository.insertPlant(PlantEntity(
                        id = 0,
                        name = "Lidah Mertua Pemurni",
                        type = "Lidah Mertua",
                        location = "Indoor",
                        healthScore = 98,
                        soilType = "Tanah Humus Sukulen",
                        plantedAt = System.currentTimeMillis() - 25 * 24 * 3600 * 1000,
                        lastWatered = System.currentTimeMillis() - 10 * 24 * 3600 * 1000,
                        waterIntervalDays = 10,
                        growthStage = "Vegetatif",
                        memoryLogsJson = initLogs3,
                        customNotes = "Pemurni udara alami yang sangat kuat menyaerp racun, tahan di ruangan AC."
                    ))

                    val initLogs4 = JSONArray().put("[$currentTimestamp] Sirih Gading gantung disiapkan menghias dinding kamar mandi.").toString()
                    repository.insertPlant(PlantEntity(
                        id = 0,
                        name = "Sirih Gading Cantik",
                        type = "Sirih Gading",
                        location = "Indoor",
                        healthScore = 90,
                        soilType = "Tanah Kompos Gembur",
                        plantedAt = System.currentTimeMillis() - 8 * 24 * 3600 * 1000,
                        lastWatered = System.currentTimeMillis() - 1 * 24 * 3600 * 1000,
                        waterIntervalDays = 4,
                        growthStage = "Semaian",
                        memoryLogsJson = initLogs4,
                        customNotes = "Tanaman gantung indoor penyejuk indera mata, pembersih udara polutan formaldehida."
                    ))
                }
            } catch (e: Exception) {
                // Safe fallback
            }

            // Seed mock local marketplace items
            _marketplaceItems.value = listOf(
                MarketItem(
                    1, "Benih Unggul Tomat Ceri", "Benih", 12500.0, "Kebun Raya Jaya", "Kediri, Jatim", "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?auto=format&fit=crop&q=80&w=400",
                    "Kediri, Dataran Rendah", "24°C - 30°C, Tanah Humus berporasi tinggi", "Tingkat keberhasilan panen rata-rata 89% di wilayah urban Jatim."
                ),
                MarketItem(
                    2, "Bibit Unggul Lidah Buaya", "Tumbuhan", 18500.0, "Sentra Tunas Indah", "Bogor, Jabar", "https://images.unsplash.com/photo-1596547609652-9cf5d8d76921?auto=format&fit=crop&q=80&w=400",
                    "Bogor, Lereng Berawan", "18°C - 32°C, Tanah Berpasir Aerasi Bagus", "Telah teraklimatisasi penuh untuk lingkungan pekarangan rumah."
                ),
                MarketItem(
                    3, "Bayam Hidroponik Segar (Hasil Panen)", "Hasil Panen", 9500.0, "Sayur Hidroponik Jaya", "Semarang, Jateng", "https://images.unsplash.com/photo-1576045057995-568f588f82fb?auto=format&fit=crop&q=80&w=400",
                    "Semarang, Atap Rumah", "Siap konsumsi, bebas pestisida sintetis", "Sangat bayam hijau krispi tinggi serat organik, dipetik pagi ini."
                ),
                MarketItem(
                    4, "Pupuk Organik Kascing Premium", "Perlengkapan", 22000.0, "Subur Tani Makmur", "Klaten, Jateng", "https://images.unsplash.com/photo-1595181744155-83e9811b7dfd?auto=format&fit=crop&q=80&w=400",
                    "Klaten, Karst Organik", "Tabur 1 genggam per pot 2 minggu sekali", "Mencerahkan warna daun dalam waktu 10 hari secara alami."
                ),
                MarketItem(
                    5, "Pot Pintar Irigasi Mandiri", "Peralatan", 35000.0, "Dunia Plastik UMKM", "Surabaya, Jatim", "https://images.unsplash.com/photo-1585320806297-9794b3e4eeae?auto=format&fit=crop&q=80&w=400",
                    "Surabaya Industrial", "Tanaman hias & herba indoor", "Mengurangi penguapan air tanah hingga menghemat air 40%."
                ),
                MarketItem(
                    6, "Benih Cabai Rawit Setan KD-4", "Benih", 8000.0, "Agro Mandiri Lestari", "Batu, Malang", "https://images.unsplash.com/photo-1588252303782-cb80119cb665?auto=format&fit=crop&q=80&w=400",
                    "Kota Batu (Dataran Tinggi)", "18°C - 26°C, kelembapan moderat", "Sangat adaptif terhadap cuaca hujan ekstrim."
                )
            )

            // Seed SOS Forum threads
            _sosForumThreads.value = listOf(
                SosThread(
                    1, "Tanaman cabai saya mengeriting, mohon bantuannya expert!", "Cabai Rawit", "Andi Tunas Magelang",
                    "Tingkat serangan agak parah, daun muda menguncup ke atas dan ada semut kecil di balik daun.", "🔴 Kritis",
                    "Itu adalah gejala serangan hama Kutu Kebul (Thrips / Aphids) yang dibantu oleh semit sebagai mediatornya. Segera semprotkan pestisida nabati rebusan daun mimba atau bawang putih + sabun cuci piring ringan 2 kali seminggu.",
                    24, "2 Jam yang lalu"
                ),
                SosThread(
                    2, "Daun Lidah Buaya berwarna coklat lembek di ujungnya", "Lidah Buaya", "Siti Garden Jakarta",
                    "Tanaman diletakkan di dalam kamar ber-AC, disiram sekali sehari setiap pagi.", "🟡 Sedang",
                    "Kelebihan air (Overwatering)! Aloe vera adalah sukulen. AC membatasi penguapan. Kurangi penyiraman jadi 1-2 minggu sekali, pastikan media tanam sangat porus dan pot memiliki lubang drainase.",
                    18, "5 Jam yang lalu"
                )
            )
        }
    }

    // Level calculator
    val userRank: StateFlow<String> = _userXp.map { xp ->
        when {
            xp < 200 -> "🌱 Beginner"
            xp < 500 -> "🌿 Grower"
            xp < 1000 -> "🌳 Urban Farmer"
            else -> "🏆 Master Gardener"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "🌱 Beginner")

    // Dynamic metrics
    val gardenHealthScore: StateFlow<Int> = allPlants.map { list ->
        if (list.isEmpty()) 70
        else (list.sumOf { it.healthScore } / list.size).coerceIn(40, 100)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 70)

    val co2Impact: StateFlow<Double> = allPlants.map { list ->
        // Accumulate mock carbon absorption values (e.g. 15g per plant/day)
        var totalAbsorbed = 0.0
        list.forEach { plant ->
            val daysPlanted = ((System.currentTimeMillis() - plant.plantedAt) / (1000 * 60 * 60 * 24)).coerceAtLeast(1)
            val coeff = when (plant.type.lowercase()) {
                "tomat", "cabai" -> 15.5
                "lidah buaya", "kaktus" -> 8.2
                "kelor", "beringin" -> 45.0
                else -> 12.0
            }
            totalAbsorbed += daysPlanted * coeff
        }
        // Round to 1 decimal place (grams of CO2)
        Math.round(totalAbsorbed * 10.0) / 10.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Set user location and update custom weather forecast
    fun setLocation(newLoc: String) {
        _userLocation.value = newLoc
        viewModelScope.launch {
            // Adjust weather info dynamically based on string inputs
            val lowercase = newLoc.lowercase()
            val temp = if (lowercase.contains("pantai") || lowercase.contains("jakarta") || lowercase.contains("surabaya")) 32 else 26
            val humidity = if (lowercase.contains("bukit") || lowercase.contains("bogor") || lowercase.contains("bandung")) 85 else 65
            _weatherState.value = WeatherInfo(
                temp = temp,
                condition = if (humidity > 80) "Hujan Ringan" else "Cerah Berawan",
                rainChance = if (humidity > 80) 80 else 30,
                humidity = humidity,
                advice = "Membaca iklim lokal di $newLoc. " + (if (humidity > 80) "Peluang hujan tinggi! Tunda menyiram tanaman pot outdoor Anda demi menghemat air tanah." else "Kondisi ideal berkebun. Sinar matahari sedang.")
            )
            // Re-brief coach
            generateMorningBriefing()
        }
    }

    // Add Plant Operation
    fun addNewPlant(name: String, type: String, location: String, soilType: String) {
        viewModelScope.launch {
            val initialLogs = JSONArray().put("[$currentTimestamp] Tanaman ditanam di media $soilType ($location)").toString()
            val newPlant = PlantEntity(
                name = name,
                type = type,
                location = location,
                soilType = soilType,
                memoryLogsJson = initialLogs
            )
            repository.insertPlant(newPlant)
            addXp(50)
            completeMissionByTitle("Langkah Pertama")
        }
    }

    // Water Plant and update internal "AI Plant Memory"
    fun waterPlant(plant: PlantEntity) {
        viewModelScope.launch {
            val logsArray = JSONArray(plant.memoryLogsJson)
            logsArray.put("[$currentTimestamp] Sukses disiram oleh pengguna.")
            
            // Randomly increase health up to 100
            val newHealth = (plant.healthScore + 10).coerceAtLeast(40).coerceAtMost(100)
            
            val updated = plant.copy(
                lastWatered = System.currentTimeMillis(),
                healthScore = newHealth,
                memoryLogsJson = logsArray.toString()
            )
            repository.updatePlant(updated)
            addXp(15)
        }
    }

    // Diagnose or add custom memory logs (such as yellow leaf)
    fun addPlantAnomalyLog(plant: PlantEntity, anomaly: String) {
        viewModelScope.launch {
            val logsArray = JSONArray(plant.memoryLogsJson)
            logsArray.put("[$currentTimestamp] Terdeteksi gejala: $anomaly")
            
            val newHealth = (plant.healthScore - 12).coerceAtLeast(30)
            val updated = plant.copy(
                healthScore = newHealth,
                memoryLogsJson = logsArray.toString()
            )
            repository.updatePlant(updated)
            addXp(10)
        }
    }

    // AI Progress Growth Photo Scan
    fun submitAIGrowthScan(plant: PlantEntity, heightCm: Int, fertility: String, customAiConclusion: String, nextStage: String) {
        viewModelScope.launch {
            val logsArray = JSONArray(plant.memoryLogsJson)
            val formattedLog = "[$currentTimestamp] 📸 AI SCAN: Tinggi ${heightCm}cm | Kesuburan: $fertility | Analisis: $customAiConclusion"
            logsArray.put(formattedLog)
            
            val updated = plant.copy(
                growthStage = nextStage,
                healthScore = (plant.healthScore + 10).coerceAtMost(100),
                memoryLogsJson = logsArray.toString()
            )
            repository.updatePlant(updated)
            addXp(35)
            completeMissionByTitle("Visioner Kebun")
            triggerCloudSync()
        }
    }

    // Complete local task / mission
    fun completeMissionById(mission: MissionEntity) {
        viewModelScope.launch {
            if (!mission.isCompleted) {
                val updated = mission.copy(isCompleted = true)
                repository.updateMission(updated)
                addXp(mission.xpReward)
            }
        }
    }

    private suspend fun completeMissionByTitle(title: String) {
        val missions = allMissions.value
        val target = missions.firstOrNull { it.title == title }
        if (target != null && !target.isCompleted) {
            val updated = target.copy(isCompleted = true)
            repository.updateMission(updated)
            addXp(target.xpReward)
        }
    }

    private fun addXp(amount: Int) {
        _userXp.value += amount
    }

    private val currentTimestamp: String
        get() {
            val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
            return sdf.format(Date())
        }

    // --- AI Operations invoking Gemini API Client ---

    fun analyzeSoil(soilColor: String, location: String) {
        viewModelScope.launch {
            _soilScanState.value = UiState.Loading
            val prompt = """
                Analisis kondisi tanah secara detail berdasar masukan visual/tekstual berikut:
                - Warna Tanah: $soilColor
                - Lokasi Tanam: $location
                
                Instruksi khusus: Identifikasi struktur, perkiraan kesuburan unsur NPK, kelembaban, rekomendasi perlakuan tanah, dan daftar tanaman yang paling cocok tumbuh subur. Berikan tanggapan dalam Bahasa Indonesia yang ramah, profesional, dan informatif.
            """.trimIndent()
            
            val response = GeminiClient.generate(prompt)
            _soilScanState.value = UiState.Success(response)
            completeMissionByTitle("Ahli Tanah Kota")
        }
    }

    fun generateMorningBriefing() {
        viewModelScope.launch {
            _gardenCoachState.value = UiState.Loading
            val prompt = """
                Tulis 3 butir taklimat asisten berkebun harian yang super singkat, seru, dan padat dalam Bahasa Indonesia untuk daerah ${_userLocation.value}.
                Cuaca saat ini: ${_weatherState.value.temp}°C, ${_weatherState.value.condition}, peluang hujan ${_weatherState.value.rainChance}%.
                Berikan rekomendasi praktis tentang siram tanaman, matahari, dan aksi kilat kebun lainnya. Maksimal 1 kalimat pendek per butir! Mulai tiap butir dengan tanda (-) diikuti emoji yang relevan. Jangan bertele-tele.
            """.trimIndent()

            val response = GeminiClient.generate(prompt)
            _gardenCoachState.value = UiState.Success(response)
        }
    }

    fun scanEmergencyDisease(plantName: String, symptomDescription: String) {
        viewModelScope.launch {
            _emergencyScanState.value = UiState.Loading
            val prompt = """
                Bertindaklah sebagai Ahli Patologi Tanaman Senior. Berikan triage medis darurat singkat untuk tanaman:
                - Nama tanaman: $plantName
                - Deskripsi gejala sakit: $symptomDescription
                
                Output WAJIB mengandung:
                1. Penilaian tingkat urgensi dengan ikon warna (🟢 Aman / 🟡 Perlu tindakan / 🔴 Risiko mati)
                2. Skenario terburuk jika tidak dirawat
                3. Prioritas langkah penyelamatan 1, 2, 3, 4 yang mudah dipraktikkan tanpa alat lab.
                Format dalam Bahasa Indonesia yang lugas dan berwibawa.
            """.trimIndent()

            val response = GeminiClient.generate(prompt)
            _emergencyScanState.value = UiState.Success(response)
        }
    }

    fun runGardenSimulator(plantType: String, initialSize: String, shadeLevel: String, seasonMode: String) {
        viewModelScope.launch {
            _simulatorState.value = UiState.Loading
            val prompt = """
                Simulasikan skenario masa depan ("Garden Simulator 3D") untuk tumbuh kembang tanaman:
                - Jenis tumbuhan: $plantType
                - Ukuran awal: $initialSize
                - Terpaan matahari / Naungan: $shadeLevel
                - Mode musim: $seasonMode
                
                Prediksikan tinggi tanaman, kepadatan dedaunan, hasil buah, serta resiko penyakit dalam kurun waktu 1 hingga 2 bulan mendatang dalam Bahasa Indonesia yang interaktif dan dramatis!
            """.trimIndent()

            val response = GeminiClient.generate(prompt)
            _simulatorState.value = UiState.Success(response)
            completeMissionByTitle("Visioner Kebun")
        }
    }

    fun scanBiodiversityGuardian(unidentifiedPlantLabel: String) {
        viewModelScope.launch {
            _biodiversityState.value = UiState.Loading
            val prompt = """
                Periksa spesies ini untuk inisiatif pelestarian keanekaragaman hayati (Biodiversity Guardian):
                - Nama / Label tumbuhan: $unidentifiedPlantLabel
                
                Berikan ulasan interaktif tentang:
                1. Status Kelangkaan & Perlindungan Hukum (Apakah umum atau dilindungi/langka)
                2. Edukasi Konservasi ekosistem
                3. Local Biodiversity Score (dari skala 0 hingga 100) berbasis kegunaan tanaman tersebut dalam menyerap karbon atau memberi makan serangga penyerbuk lokal.
                Format dalam Bahasa Indonesia yang inspiratif dan berpengetahuan luas.
            """.trimIndent()

            val response = GeminiClient.generate(prompt)
            _biodiversityState.value = UiState.Success(response)
            completeMissionByTitle("Penjaga Keanekaragaman")
        }
    }

    fun submitReverseMarketplace(strawItemWanted: String, targetBudget: Double) {
        viewModelScope.launch {
            _reverseMarketplaceState.value = UiState.Loading
            // AI calculates tools, nearest sellers, estimated harvest, costs based on prompt
            val prompt = """
                Saya ingin bercocok tanam: $strawItemWanted
                Anggaran biaya maksimum: Rp $targetBudget
                
                Berikan daftar rincian estimasi komparasi otomatis (Reverse Marketplace Matcher):
                1. Daftar minimal peralatan & benih yang diperlukan
                2. Tiga rekomendasi mitra penjual UMKM terdekat (beserta perkiraan jarak dalam km)
                3. Total biaya riil (apakah sesuai budget?)
                4. Estimasi masa panen & jumlah panen perdana.
                Gunakan Bahasa Indonesia yang rapi, informatif dengan format berpoin.
            """.trimIndent()

            val response = GeminiClient.generate(prompt)
            _reverseMarketplaceState.value = UiState.Success(response)
        }
    }

    // Community SOS Upvote
    fun upvoteSosThread(threadId: Int) {
        val currentList = _sosForumThreads.value.map { thread ->
            if (thread.id == threadId) {
                thread.copy(votesCount = thread.votesCount + 1)
            } else thread
        }
        _sosForumThreads.value = currentList
        addXp(5)
    }

    // Submit a new SOS Thread
    fun createSosThread(title: String, plantType: String, description: String, severity: String) {
        val newThread = SosThread(
            id = _sosForumThreads.value.size + 1,
            title = title,
            plantType = plantType,
            author = if (_currentUser.value?.isGuest == true) "Tamu Botanis" else (_currentUser.value?.displayName ?: "Petani Kota"),
            description = description,
            severity = severity,
            votesCount = 1,
            timestamp = "Baru Saja"
        )
        _sosForumThreads.value = listOf(newThread) + _sosForumThreads.value
        addXp(20)
    }

    // --- INTEGRATED AUTHENTICATION & MULTI-DEVICE CLOUD SYNCING DATABASE ---
    
    fun prefillCloudSimulations() {
        // Prefill raka@gmail.com with outstanding organic progress so users can try switching device profile!
        val rakaPlants = listOf(
            PlantEntity(
                id = 0, name = "Tomat Raka Pratama", type = "Tomat", location = "Balkon", healthScore = 95, 
                soilType = "Humus", plantedAt = System.currentTimeMillis() - 4 * 24 * 3600 * 1000, 
                lastWatered = System.currentTimeMillis(), waterIntervalDays = 3, growthStage = "Vegetatif", 
                memoryLogsJson = "[\\\"[Mulai] Tomat Ceri organik ditanam di balkon dengan tanah humus hara tinggi.\\\"]"
            ),
            PlantEntity(
                id = 0, name = "Lidah Buaya Sehat", type = "Lidah Buaya", location = "Indoor", healthScore = 88, 
                soilType = "Liat", plantedAt = System.currentTimeMillis() - 10 * 24 * 3600 * 1000, 
                lastWatered = System.currentTimeMillis(), waterIntervalDays = 7, growthStage = "Vegetatif", 
                memoryLogsJson = "[\\\"[Mulai] Sukses diletakkan di dekat kaca jendela AC.\\\"]"
            )
        )
        val rakaMissions = listOf(
            MissionEntity(1, "Langkah Pertama", "Tambahkan tanaman pertama Anda ke kebun Palawa.", 100, true, "🌱 Pemula Hijau", "Beginner"),
            MissionEntity(2, "Ahli Tanah Kota", "Lakukan analisis tanah menggunakan media foto kebun.", 150, true, "🧪 Detektif Tanah", "Beginner"),
            MissionEntity(3, "Visioner Kebun", "Jalankan simulasi pertumbuhan tomat di menu Garden Simulator.", 120, false, "🔮 Penjelajah Waktu", "Grower"),
            MissionEntity(4, "Penjaga Keanekaragaman", "Scan tumbuhan untuk memicu Pelindung Keanekaragaman.", 200, false, "⭐ Pelindung Langka", "Urban Farmer"),
            MissionEntity(5, "Pahlawan Ekologi", "Kurangi pemborosan air hingga 100L berdasarkan saran ekologis.", 250, false, "💧 Pendekar Air", "Urban Farmer"),
            MissionEntity(6, "Bibir Hijau Sejati", "Panen pertama dari salah satu tanaman terdaftar Anda.", 500, false, "🏆 Tunas Master", "Master Gardener")
        )
        
        cloudAccountStorage["raka@gmail.com"] = SimulatedCloudBackup(
            emailOrPhone = "raka@gmail.com",
            displayName = "Raka Pratama via Google",
            savedXp = 370,
            savedPlants = rakaPlants,
            savedMissions = rakaMissions
        )
        
        // Simulating second account via Phone Number: "0811223344"
        val phonePlants = listOf(
            PlantEntity(
                id = 0, name = "Cabai Rawit Lestari", type = "Cabai", location = "Balkon", healthScore = 90, 
                soilType = "Merah Berpasir", plantedAt = System.currentTimeMillis() - 15 * 24 * 3600 * 1000, 
                lastWatered = System.currentTimeMillis() - 12 * 3600 * 1000, waterIntervalDays = 3, growthStage = "Panen", 
                memoryLogsJson = "[\\\"[Sistem] Tunas Cabai subur siap panen melimpah pedas.\\\"]"
            )
        )
        cloudAccountStorage["0811223344"] = SimulatedCloudBackup(
            emailOrPhone = "0811223344",
            displayName = "Siti Rahma via SMS OTP",
            savedXp = 220,
            savedPlants = phonePlants,
            savedMissions = rakaMissions.map { it.copy() }
        )
    }

    fun loginOrRegister(email: String?, phone: String?, customName: String) {
        viewModelScope.launch {
            val key = email ?: phone ?: return@launch
            val rawName = if (customName.isNotBlank()) customName else {
                if (email != null) email.substringBefore("@") else "Petani $key"
            }
            
            // Clear current DB to simulate device sync migration
            repository.deleteAllPlants()
            repository.deleteAllMissions()
            
            val backup = cloudAccountStorage[key]
            if (backup != null) {
                // Restore Cloud backup state
                _userXp.value = backup.savedXp
                backup.savedPlants.forEach {
                    repository.insertPlant(it.copy(id = 0))
                }
                repository.insertMissions(backup.savedMissions)
                
                val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                _currentUser.value = AuthUser(
                    email = email,
                    phone = phone,
                    displayName = backup.displayName,
                    isGuest = false,
                    syncTimestamp = "Dimuat (${sdf.format(Date())})"
                )
            } else {
                // First-time Register: Setup clean user profile & auto seed
                _userXp.value = 120
                
                val defaultMissions = listOf(
                    MissionEntity(1, "Langkah Pertama", "Tambahkan tanaman pertama Anda ke kebun Palawa.", 100, false, "🌱 Pemula Hijau", "Beginner"),
                    MissionEntity(2, "Ahli Tanah Kota", "Lakukan analisis tanah menggunakan media foto kebun.", 150, false, "🧪 Detektif Tanah", "Beginner"),
                    MissionEntity(3, "Visioner Kebun", "Jalankan simulasi pertumbuhan tomat di menu Garden Simulator.", 120, false, "🔮 Penjelajah Waktu", "Grower"),
                    MissionEntity(4, "Penjaga Keanekaragaman", "Scan tumbuhan untuk memicu Pelindung Keanekaragaman.", 200, false, "⭐ Pelindung Langka", "Urban Farmer"),
                    MissionEntity(5, "Pahlawan Ekologi", "Kurangi pemborosan air hingga 100L berdasarkan saran ekologis.", 250, false, "💧 Pendekar Air", "Urban Farmer"),
                    MissionEntity(6, "Bibir Hijau Sejati", "Panen pertama dari salah satu tanaman terdaftar Anda.", 500, false, "🏆 Tunas Master", "Master Gardener")
                )
                repository.insertMissions(defaultMissions)
                
                val user = AuthUser(
                    email = email,
                    phone = phone,
                    displayName = rawName,
                    isGuest = false,
                    syncTimestamp = "Tersinkron Baru"
                )
                _currentUser.value = user
                
                // Instantly register with server cache Map
                triggerCloudSync()
            }
            generateMorningBriefing()
        }
    }

    fun logout() {
        viewModelScope.launch {
            // Backup the logging out user's current values to secure server state first!
            val user = _currentUser.value
            if (user != null && !user.isGuest) {
                val key = user.email ?: user.phone
                if (key != null) {
                    cloudAccountStorage[key] = SimulatedCloudBackup(
                        emailOrPhone = key,
                        displayName = user.displayName,
                        savedXp = _userXp.value,
                        savedPlants = allPlants.value,
                        savedMissions = allMissions.value
                    )
                }
            }
            
            // Wipe local persistence tables
            repository.deleteAllPlants()
            repository.deleteAllMissions()
            _userXp.value = 0
            
            // Go back to selection/onboarding
            _currentUser.value = null
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            repository.deleteAllPlants()
            repository.deleteAllMissions()
            _userXp.value = 80 // starting guest level
            
            // Seed 3 gorgeous guest plants to demonstrate location categorization and features (As requested!)
            val guestPlants = listOf(
                PlantEntity(
                    id = 0, name = "Tomat Ceri Balkon", type = "Tomat Ceri", location = "Balkon", healthScore = 92,
                    soilType = "Humus Gembur", plantedAt = System.currentTimeMillis() - 22 * 24 * 3600 * 1000,
                    lastWatered = System.currentTimeMillis() - 4 * 3600 * 1000, waterIntervalDays = 2, growthStage = "Vegetatif",
                    memoryLogsJson = "[\\\"🌱 [Penanaman] Bibit tomat berhasil ditanam di tray semai oleh Palawa.\\\",\\\"🌿 [Perkembangan] Muncul tunas helai daun sejati ketiga.\\\",\\\"🍅 [Subur] Tanaman mulai mengeluarkan kuncup bunga pertama.\\\"]"
                ),
                PlantEntity(
                    id = 0, name = "Lidah Buaya Semarak", type = "Lidah Buaya", location = "Indoor", healthScore = 85,
                    soilType = "Pasir Malang", plantedAt = System.currentTimeMillis() - 45 * 24 * 3600 * 1000,
                    lastWatered = System.currentTimeMillis() - 3 * 24 * 3600 * 1000, waterIntervalDays = 7, growthStage = "Semaian",
                    memoryLogsJson = "[\\\"🌵 [Penanaman] Lidah buaya diletakkan di pot terakota dekat jendela.\\\",\\\"💪 [Bimbingan] Melakukan karantina karena ada indikasi busuk akar.\\\",\\\"✨ [Pulih] Batang mengeras kembali dan tumbuh anakan baru.\\\"]"
                ),
                PlantEntity(
                    id = 0, name = "Kemangi Pekarangan", type = "Kemangi", location = "Outdoor", healthScore = 78,
                    soilType = "Tanah Lempung", plantedAt = System.currentTimeMillis() - 12 * 24 * 3600 * 1000,
                    lastWatered = System.currentTimeMillis() - 24 * 3600 * 1000, waterIntervalDays = 1, growthStage = "Vegetatif",
                    memoryLogsJson = "[\\\"🌿 [Penanaman] Stek batang kemangi diletakkan langsung di tanah luar.\\\",\\\"🌱 [Bimbingan] Mendapat rekomendasi penambahan mulsa daun kelapa oleh asisten Palawa.\\\"]"
                )
            )
            guestPlants.forEach { repository.insertPlant(it) }

            val defaultMissions = listOf(
                MissionEntity(1, "Langkah Pertama", "Tambahkan tanaman pertama Anda ke kebun Palawa.", 100, false, "🌱 Pemula Hijau", "Beginner"),
                MissionEntity(2, "Ahli Tanah Kota", "Lakukan analisis tanah menggunakan media foto kebun.", 150, false, "🧪 Detektif Tanah", "Beginner"),
                MissionEntity(3, "Visioner Kebun", "Jalankan simulasi pertumbuhan tomat di menu Garden Simulator (Terbatas).", 120, false, "🔮 Penjelajah Waktu", "Grower")
            )
            repository.insertMissions(defaultMissions)
            
            _currentUser.value = AuthUser(
                displayName = "Tamu Botanis",
                isGuest = true,
                syncTimestamp = "Cloud Tidak Aktif"
            )
            generateMorningBriefing()
        }
    }

    fun triggerCloudSync() {
        val user = _currentUser.value ?: return
        if (user.isGuest) return
        val key = user.email ?: user.phone ?: return
        
        viewModelScope.launch {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val time = sdf.format(Date())
            
            cloudAccountStorage[key] = SimulatedCloudBackup(
                emailOrPhone = key,
                displayName = user.displayName,
                savedXp = _userXp.value,
                savedPlants = allPlants.value,
                savedMissions = allMissions.value
            )
            
            _currentUser.value = user.copy(syncTimestamp = "Tersinkron ($time)")
        }
    }
}

// Data class and model helper
data class WeatherInfo(
    val temp: Int,
    val condition: String,
    val rainChance: Int,
    val humidity: Int,
    val advice: String
)

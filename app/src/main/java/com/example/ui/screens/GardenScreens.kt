package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MissionEntity
import com.example.data.model.PlantEntity
import com.example.ui.viewmodel.GardenViewModel
import com.example.ui.viewmodel.MarketItem
import com.example.ui.viewmodel.SosThread
import com.example.ui.viewmodel.UiState
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@Composable
fun GardenAppMainContent(viewModel: GardenViewModel) {
    var isSplashActive by remember { mutableStateOf(true) }
    var isOnboarded by remember { mutableStateOf(false) }
    var isWebMode by remember { mutableStateOf(false) } // Dual Mode: Mobile vs Web Viewport
    val currentUser by viewModel.currentUser.collectAsState()

    if (isSplashActive) {
        SplashScreen(onSplashFinished = { isSplashActive = false })
    } else if (!isOnboarded) {
        OnboardingScreen(onGetStarted = { isOnboarded = true })
    } else if (currentUser == null) {
        AuthScreen(
            viewModel = viewModel,
            onSkipAsGuest = { viewModel.continueAsGuest() }
        )
    } else {
        Scaffold(
            topBar = {
                Surface(
                    tonalElevation = 6.dp,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Palawa",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            
                            // Interactive Mode Switcher (Satisfies "Buat versi website juga")
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { isWebMode = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (!isWebMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        contentColor = if (!isWebMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(32.dp).testTag("select_mobile_mode")
                                ) {
                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mobile App", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { isWebMode = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isWebMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        contentColor = if (isWebMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(32.dp).testTag("select_web_mode")
                                ) {
                                    Icon(Icons.Default.Computer, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Website Version", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (isWebMode) {
                    DesktopWebsiteView(viewModel = viewModel)
                } else {
                    MobileAppView(viewModel = viewModel)
                }
            }
        }
    }
}

// ==========================================
// 0. SPLASH SCREEN (GRID BLUEPRINT BACKGROUND)
// ==========================================
@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2200) // Beautiful 2.2s splash duration
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B4D3E)), // Deep blueprint green
        contentAlignment = Alignment.Center
    ) {
        // Blueprint Grid Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacing = 40.dp.toPx()
            val w = size.width
            val h = size.height

            // Horizontal grid lines
            var y = 0f
            while (y <= h) {
                drawLine(
                    color = Color(0xFF2C6B57).copy(alpha = 0.5f),
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(w, y),
                    strokeWidth = 1.dp.toPx()
                )
                y += gridSpacing
            }

            // Vertical grid lines
            var x = 0f
            while (x <= w) {
                drawLine(
                    color = Color(0xFF2C6B57).copy(alpha = 0.5f),
                    start = androidx.compose.ui.geometry.Offset(x, 0f),
                    end = androidx.compose.ui.geometry.Offset(x, h),
                    strokeWidth = 1.dp.toPx()
                )
                x += gridSpacing
            }

            // Central Blueprint Accent Circles
            drawCircle(
                color = Color(0xFF4DB6AC).copy(alpha = 0.2f),
                radius = 180.dp.toPx(),
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 15f), 0f)
                )
            )

            drawCircle(
                color = Color(0xFF4DB6AC).copy(alpha = 0.3f),
                radius = 140.dp.toPx(),
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.6.dp.toPx())
            )

            // Centered crosshair lines
            drawLine(
                color = Color(0xFF4DB6AC).copy(alpha = 0.3f),
                start = androidx.compose.ui.geometry.Offset(center.x - 220.dp.toPx(), center.y),
                end = androidx.compose.ui.geometry.Offset(center.x + 220.dp.toPx(), center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color(0xFF4DB6AC).copy(alpha = 0.3f),
                start = androidx.compose.ui.geometry.Offset(center.x, center.y - 220.dp.toPx()),
                end = androidx.compose.ui.geometry.Offset(center.x, center.y + 220.dp.toPx()),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant Plant Container
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White)
                    .border(3.dp, Color(0xFF66BB6A), RoundedCornerShape(32.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                // Spinning border animation
                val infiniteTransition = rememberInfiniteTransition(label = "spin_logo")
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(4000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rotation"
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color(0xFF1B4D3E),
                        startAngle = rotation,
                        sweepAngle = 280f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                    )
                }

                Text(
                    text = "🌱",
                    fontSize = 48.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Brand Title
            Text(
                text = "ARANYA",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle / Blueprint indicator
            Text(
                text = "BOTANICAL BLUEPRINT • V2.0",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = Color(0xFF81C784).copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Progress bar
            CircularProgressIndicator(
                color = Color(0xFF81C784),
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// ==========================================
// 1. ONBOARDING SCREEN
// ==========================================
@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    val pages = listOf(
        OnboardingPageData(
            title = "Selamat Datang di Palawa",
            description = "Asisten berkebun cerdas berbasis sistem kebun saku Palawa yang dirancang ramah bagi pemula perkotaan maupun pedesaan.",
            icon = Icons.Default.LocalFlorist
        ),
        OnboardingPageData(
            title = "Uji Kondisi Tanah Instan & Cuaca Realistis",
            description = "Analisis kesuburan tanah langsung dari foto kondisi fisik tanah beserta rekomendasi cerdas tanaman terbaik yang cocok di iklim lokasi Anda.",
            icon = Icons.Default.Cloud
        ),
        OnboardingPageData(
            title = "Aplikasi Seluler & Portal Website",
            description = "Gunakan dalam format aplikasi saku mobile yang praktis atau beralih ke layout Widescreen Web Dashboard komprehensif untuk pengawasan kebun yang lebih lapang.",
            icon = Icons.Default.Computer
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = pages[currentPage].icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(100.dp)
                .padding(16.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = pages[currentPage].title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = pages[currentPage].description,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Page Indicators
        Row {
            pages.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == currentPage) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                        .padding(horizontal = 4.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        if (currentPage < pages.lastIndex) {
            Button(
                onClick = { currentPage++ },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp)
                    .testTag("onboarding_next_button")
            ) {
                Text("Lanjut")
            }
        } else {
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp)
                    .testTag("onboarding_finish_button")
            ) {
                Text("Mulai Berkebun! 🌱")
            }
        }
    }
}

data class OnboardingPageData(val title: String, val description: String, val icon: ImageVector)

// ==========================================
// 2. MOBILE APP VIEW (Fully Featured)
// ==========================================
@Composable
fun MobileAppView(viewModel: GardenViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        MobileTabItem("Kebun", Icons.Default.Eco),
        MobileTabItem("Simulator & Cuaca", Icons.Default.Cloud),
        MobileTabItem("Misi", Icons.Default.Star),
        MobileTabItem("Reverse Pasar", Icons.Default.ShoppingCart),
        MobileTabItem("Plant SOS Forum", Icons.Default.People)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant, // #F0F5EB Sage-gray base
                tonalElevation = 8.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        label = { 
                            Text(
                                text = tab.title, 
                                fontSize = 9.sp, 
                                maxLines = 1, 
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ) 
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,      // Vibrant Forest Green (#386B1D)
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer, // Light Green Accent (#D7E8CD)
                            unselectedIconColor = Color(0xFF6B8D7B), // Beautiful distinct brand leaf green, non-gray
                            unselectedTextColor = Color(0xFF6B8D7B)
                        ),
                        modifier = Modifier.testTag("nav_tab_$index")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = selectedTab,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                label = "tab_crossfade"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> MobileGardenDashboard(viewModel)
                    1 -> MobileWeatherAndSimulator(viewModel)
                    2 -> MobileMissionsScreen(viewModel)
                    3 -> MobileReverseMarketplace(viewModel)
                    4 -> MobileSosForum(viewModel)
                }
            }
        }
    }
}

data class MobileTabItem(val title: String, val icon: ImageVector)

// ==========================================
// 2A. TAB 0: MOBILE CANOPY / GARDEN HUB
// ==========================================
@Composable
fun MobileGardenDashboard(viewModel: GardenViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val plants by viewModel.allPlants.collectAsState()
    val healthScore by viewModel.gardenHealthScore.collectAsState()
    val co2Saved by viewModel.co2Impact.collectAsState()
    val rank by viewModel.userRank.collectAsState()
    val xp by viewModel.userXp.collectAsState()
    val coachMessage by viewModel.gardenCoachState.collectAsState()
    
    // Smooth custom value sweep animations for stats
    val animatedHealthFraction by animateFloatAsState(
        targetValue = healthScore / 100f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "garden_health_fraction"
    )
    
    val targetXpFraction = remember(xp) { (xp % 100) / 100f }
    val animatedXpFraction by animateFloatAsState(
        targetValue = if (targetXpFraction > 0f) targetXpFraction else 0.4f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "user_xp_fraction"
    )
    
    var showAddPlantDialog by remember { mutableStateOf(false) }
    var showSoilScanDialog by remember { mutableStateOf(false) }
    var showBiodiversityDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showGuestNoticeDialog by remember { mutableStateOf(false) }
    var activeSubTab by remember { mutableStateOf(0) } // 0: Taman Saya, 1: Semarak Hijau

    // Dialog Confirmation boxes
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Konfirmasi Log Out 👥", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin keluar? Kemajuan lokal Anda akan disimpan dengan aman di server awan sebelum sesi ditutup.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Keluar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Batal") }
            }
        )
    }

    if (showGuestNoticeDialog) {
        AlertDialog(
            onDismissRequest = { showGuestNoticeDialog = false },
            icon = { Icon(Icons.Default.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Daftarkan Akun Sinkronisasi ☁️", fontWeight = FontWeight.Bold) },
            text = { Text("Menanam lebih dari 1 batang memerlukan Akun Gmail atau No. Telepon terdaftar. Hal ini memungkinkan kebun Anda diakses secara mulus dari perangkat mana pun!") },
            confirmButton = {
                Button(
                    onClick = {
                        showGuestNoticeDialog = false
                        viewModel.logout() // Returns to AuthScreen for registration
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Daftar Akun Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGuestNoticeDialog = false }) { Text("Kembali") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header: Dynamic auth card (Fidelity to HTML template)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (currentUser?.isGuest == true) "MENJELAJAH SEBAGAI" else "TUNAS DIGITAL PETANI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Text(
                        text = currentUser?.displayName ?: "Petani Lokal",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    // Cloud Sync Badge & Registration Catalyst
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Surface(
                            color = if (currentUser?.isGuest == true) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (currentUser?.isGuest == true) "☁️ Sesi Tamu Offline" else "☁️ Aktif: ${currentUser?.syncTimestamp}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentUser?.isGuest == true) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        
                        if (currentUser?.isGuest == true) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Daftar Akun",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .clickable { viewModel.logout() }
                                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                // Clickable Avatar Profile (Logout Trigger)
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { showLogoutConfirm = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (currentUser?.isGuest == true) "👤" else "🍀", fontSize = 22.sp)
                }
            }
        }

        // AI Garden Coach Briefing (Fidelity to HTML template styling)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✨", fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Panduan Sobat Lestari",
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(
                            onClick = { viewModel.generateMorningBriefing() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Coach",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    when (coachMessage) {
                        is UiState.Loading -> {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        is UiState.Success -> {
                            val data = (coachMessage as UiState.Success<String>).data
                            val bulletLines = data.split("\n").filter { it.isNotBlank() }
                            
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (bulletLines.isNotEmpty()) {
                                    bulletLines.forEach { line ->
                                        val cleanLine = line.removePrefix("-").removePrefix("•").trim()
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = if (cleanLine.contains("air", true) || cleanLine.contains("siram", true)) "💧" else "🌱",
                                                fontSize = 15.sp,
                                                modifier = Modifier.padding(top = 1.dp)
                                            )
                                            Text(
                                                text = cleanLine,
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = data,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                        else -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("☀️", fontSize = 16.sp)
                                    Text("Hari ini cerah. Ketuk tombol refresh di sudut kanan atas briefing untuk mendapatkan rekomendasi tanaman & cuaca presisi dari bimbingan lestari Anda.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Statistics Grid (Fidelity to HTML template styling)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Garden Health Score Card (#F2F2E7)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(150.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "GARDEN HEALTH",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(60.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = animatedHealthFraction,
                                modifier = Modifier.size(60.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 5.dp,
                                trackColor = Color.White
                            )
                            Text(
                                text = "$healthScore",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (healthScore >= 80) "Healthy & Thriving" else "Needs Attention",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Rank / Gamification Level Card (#E2F3FF)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.1f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(150.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "YOUR LEVEL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "🌿 $rank",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF001E2F)
                            )
                        }
                        
                        Column {
                            // Custom progress bar h-2 bg-white rounded-full with smooth sweep transition
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animatedXpFraction)
                                        .fillMaxHeight()
                                        .background(MaterialTheme.colorScheme.onTertiaryContainer)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            val xpToNext = 100 - (xp % 100)
                            Text(
                                text = "$xpToNext XP to next rank",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions Section (Fidelity to HTML template mockup)
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Large styled CTA: "Scan & Analyze" (📸)
                Button(
                    onClick = { showSoilScanDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("action_soil_scan")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📸", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = "Scan & Analyze",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Identifikasi kondisi tanah & rekomendasi tanaman",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Grid of 2 secondary Quick Action buttons with outline borders (#C1C9BA)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Left action: Biodiversity Guru
                    Card(
                        onClick = { showBiodiversityDialog = true },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFC1C9BA)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(84.dp)
                            .testTag("action_biodiversity")
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("⭐", fontSize = 22.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Kelangkaan Guru", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                     // Right action: Add Plant
                     Card(
                         onClick = { 
                             if (currentUser?.isGuest == true && plants.isNotEmpty()) {
                                 showGuestNoticeDialog = true
                             } else {
                                 showAddPlantDialog = true
                             }
                         },
                         colors = CardDefaults.cardColors(containerColor = Color.White),
                         border = BorderStroke(1.dp, Color(0xFFC1C9BA)),
                         shape = RoundedCornerShape(16.dp),
                         modifier = Modifier
                             .weight(1f)
                             .height(84.dp)
                             .testTag("add_plant_fab")
                     ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("🌱", fontSize = 22.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tanam Baru", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Carbon Impact Tracker (Footer block with Deep Forest Green styling)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111F0E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("🌍", fontSize = 24.sp)
                        Column {
                            Text(
                                text = "IMPACT SCORE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${co2Saved}g CO₂ Absorbed",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFD7E8CD))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "GUARDIAN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF111F0E)
                        )
                    }
                }
            }
        }

        // Section header with Segmented Tab Selection: "Taman Saya" vs "Semarak Hijau"
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab 0: Taman Saya
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeSubTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { activeSubTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Taman Saya",
                        color = if (activeSubTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Tab 1: Semarak Hijau (Thriving Plants Updates)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeSubTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { activeSubTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Semarak Hijau",
                            color = if (activeSubTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (activeSubTab == 1) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "3",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = if (activeSubTab == 1) Color.White else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Contextual rendering based on selected Sub-Tab
        if (activeSubTab == 0) {
            // ============== TAMAN SAYA TAB ==============
            // Garden Summary Card (As requested: "di page my plants buat summary tentang my garden")
            item {
                val totalPlantsCount = plants.size
                val avgHealthScore = if (plants.isNotEmpty()) plants.map { it.healthScore }.average().toInt() else 0
                val domSoil = if (plants.isNotEmpty()) {
                    plants.map { it.soilType }.groupBy { it }.maxByOrNull { it.value.size }?.key ?: "Humus"
                } else "Belum Diketahui"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📊 RINGKASAN KEBUN SAYA", fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, letterSpacing = 0.8.sp)
                            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(6.dp)) {
                                Text("ARANYA DIAGNOSTIC", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Column 1
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Populasi Tunas", fontSize = 10.sp, color = Color.Gray)
                                Text("$totalPlantsCount Batang", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            // Column 2
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Kesehatan Rata-rata", fontSize = 10.sp, color = Color.Gray)
                                Text("$avgHealthScore%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (avgHealthScore >= 80) Color(0xFF1B5E20) else Color(0xFFB71C1C))
                            }
                            // Column 3
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Tipe Tanah Dominan", fontSize = 10.sp, color = Color.Gray)
                                Text(domSoil, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Verbal Description Summary from the application tools
                        Text(
                            text = if (totalPlantsCount == 0) {
                                "Kebun Palawa Anda saat ini masih sunyi. Ketuk tombol 'Tanam Baru' diatas untuk mendaftarkan kecambah pertama Anda dan nikmati pelacakan parameter ekologi cerdas."
                            } else {
                                "Kondisi kebun modular Anda saat ini berkategori ${if (avgHealthScore >= 80) "Sangat Prima & Subur Berkelanjutan" else "Membutuhkan Tindakan Perawatan"}. Didukung dominasi tanah berjenis $domSoil. Berdasarkan bimbingan ekologi Palawa, kelembapan mikro terawat stabil. Pertahankan siklus menyiram agar fotosintesis berlangsung optimal."
                            },
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Section divider text
            item {
                Text(
                    text = "Daftar Tunas Aktif",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // List of personal elements
            if (plants.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.LocalFlorist, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Belum Ada Tanaman", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Ketuk tombol 'Tanam Baru' diatas untuk mendaftarkan kecambah atau pot bunga Anda pertama kali.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(plants) { plant ->
                    PlantCardItem(
                        plant = plant,
                        onWater = { viewModel.waterPlant(plant) },
                        onObserveAnomaly = { anomaly -> viewModel.addPlantAnomalyLog(plant, anomaly) }
                    )
                }
            }
        } else {
            // ============== SEMARAK HIJAU FEED VIEW ==============
            // Predefined high quality community thriving posts feeds
            val communityPosts = listOf(
                ThrivingCommunityPost(
                    id = 1,
                    username = "bayu_rimba",
                    avatarEmoji = "🌵",
                    plantType = "Tomat Ceri",
                    caption = "Panen melimpah pagi ini! Tidak menyangka tomat ceri saya bisa tumbuh lebat begini meskipun ditanam di balkon lantai 4 sempit secara hidro-organik.",
                    imageUrl = "https://images.unsplash.com/photo-1592841208221-a58d8dfb5749?auto=format&fit=crop&q=80&w=400",
                    location = "Jakarta Selatan Balkon",
                    status = "SANGAT SUBUR 🌟",
                    phTest = "6.5 pH (Pristine)",
                    soilCondition = "Campuran Humus & Cocopeat Organik",
                    tips = "Didapat dari Analisis Tanah Cerdas Palawa: Tanah gembur tinggi unsur hara mikro. Rekomendasi penyiraman 3 hari sekali karena kelembapan awat terperangkap."
                ),
                ThrivingCommunityPost(
                    id = 2,
                    username = "kartika_daun",
                    avatarEmoji = "🌸",
                    plantType = "Lidah Buaya",
                    caption = "Lidah buaya raksasa ini awalnya layu menguning parah di sudut teras. Setelah memindai diagnosis dengan fitur Foto & Anomali Palawa, disimpulkan busuk akar akibat overwatering! Langsung saya selamatkan.",
                    imageUrl = "https://images.unsplash.com/photo-1596547609652-9cf5d8d76921?auto=format&fit=crop&q=80&w=400",
                    location = "Bandung Teras Teduh",
                    status = "RECOVERY BERHASIL 🌱",
                    phTest = "7.2 pH (Slightly Alkaline)",
                    soilCondition = "Campuran Pasir Malang, Sekam Bakar, & Liat",
                    tips = "Didapat dari Diagnosis Anomali Palawa: Overwatering terdeteksi. Disarankan meningkatkan aerasi wadah."
                ),
                ThrivingCommunityPost(
                    id = 3,
                    username = "budi_lestari",
                    avatarEmoji = "🌿",
                    plantType = "Cabai Rawit",
                    caption = "Cabai rawit hoki saya terbukti berbunga super lebat dan buah membara matang sempurna! Sensasi menanam bumbu dapur sendiri luar biasa serunya.",
                    imageUrl = "https://images.unsplash.com/photo-1588252303780-5a3b93f6bfb8?auto=format&fit=crop&q=80&w=400",
                    location = "Surabaya Kebun Belakang",
                    status = "SIAP PANEN 🌶️",
                    phTest = "6.2 pH (Optimal)",
                    soilCondition = "Kompos Organik & Tanah Merah Kebun",
                    tips = "Didapat dari Companion Planting Palawa: Tanam selang-seling kemangi di sekitar cabai untuk mengusir koloni semut hitam secara alami dan organik!"
                )
            )

            item {
                Text(
                    text = "Galeri Tumbuh Semarak Hijau",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(communityPosts) { post ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // User info header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(post.avatarEmoji, fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "@${post.username}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = post.location,
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = post.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Thriving plant photo
                        AsyncImage(
                            model = post.imageUrl,
                            contentDescription = "Thriving ${post.plantType}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // User Caption
                        Text(
                            text = post.caption,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Keterangan yang diperoleh dari fitur aplikasi Palawa (As requested)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF4F7F5))
                                .border(1.dp, Color(0xFFC3D6C9), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Eco,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Keterangan Terdiagnosis Palawa:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 0.3.sp
                                    )
                                }
                                
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Row {
                                        Text("🧪 Kesuburan: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                        Text("${post.phTest} • ${post.soilCondition}", fontSize = 11.sp, color = Color.Black)
                                    }
                                    Row {
                                        Text("🌿 Jenis: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                        Text(post.plantType, fontSize = 11.sp, color = Color.Black)
                                    }
                                    Row {
                                        Text("💡 Nutrisi & Solusi: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                        Text(post.tips, fontSize = 11.sp, lineHeight = 14.sp, color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // MODAL DIALOGS FOR CLUSTERING TOOLS
    if (showAddPlantDialog) {
        var plantName by remember { mutableStateOf("") }
        var plantType by remember { mutableStateOf("Tomat Ceri") }
        val plantTypes = listOf("Tomat Ceri", "Cabai Rawit", "Lidah Buaya", "Kaktus", "Kelor", "Kemangi")
        var locSelected by remember { mutableStateOf("Indoor") }
        var soilSelected by remember { mutableStateOf("Humus") }
        
        AlertDialog(
            onDismissRequest = { showAddPlantDialog = false },
            title = { Text("Tanam Baru 🌱", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = plantName,
                        onValueChange = { plantName = it },
                        label = { Text("Nama panggilan tumbuhan") },
                        modifier = Modifier.fillMaxWidth().testTag("add_plant_name_input"),
                        placeholder = { Text("Cth: Tomatku Subur") }
                    )
                    
                    Text("Jenis Tanaman:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(plantTypes) { type ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (plantType == type) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { plantType = type }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(type, color = if (plantType == type) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Lokasi:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            val locs = listOf("Indoor", "Outdoor", "Balkon")
                            locs.forEach { loc ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { locSelected = loc }) {
                                    RadioButton(selected = locSelected == loc, onClick = { locSelected = loc })
                                    Text(loc, fontSize = 12.sp)
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tanah:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            val soils = listOf("Humus", "Liat", "Campuran Pasir")
                            soils.forEach { soil ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { soilSelected = soil }) {
                                    RadioButton(selected = soilSelected == soil, onClick = { soilSelected = soil })
                                    Text(soil, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (plantName.isNotBlank()) {
                            viewModel.addNewPlant(plantName, plantType, locSelected, soilSelected)
                            showAddPlantDialog = false
                        }
                    },
                    modifier = Modifier.testTag("add_plant_confirm_button")
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPlantDialog = false }) { Text("Batal") }
            }
        )
    }

    if (showSoilScanDialog) {
        var soilColor by remember { mutableStateOf("Cokelat Gelap Humus (Subur)") }
        val colorsOfSoil = listOf("Cokelat Gelap Humus (Subur)", "Merah Liat", "Kuning Berpasir", "Abu-abu Padat")
        var locSelected by remember { mutableStateOf("Outdoor Terbuka") }
        val soilResult by viewModel.soilScanState.collectAsState()

        AlertDialog(
            onDismissRequest = { showSoilScanDialog = false },
            title = { Text("Analisis Tanah Cerdas 🧪") },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Simulasikan Foto Tanah: Pilih sampel warna tanah Anda:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(colorsOfSoil) { color ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (soilColor == color) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { soilColor = color }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(color, color = if (soilColor == color) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = locSelected,
                        onValueChange = { locSelected = it },
                        label = { Text("Area Tanam") }
                    )

                    Button(
                        onClick = { viewModel.analyzeSoil(soilColor, locSelected) },
                        modifier = Modifier.fillMaxWidth().testTag("scan_soil_sample_action"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Mulai Analisis Tanah Cerdas")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    when (soilResult) {
                        is UiState.Loading -> Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        is UiState.Success -> {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Hasil Uji Tanaman Cerdas:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text((soilResult as UiState.Success<String>).data, fontSize = 13.sp)
                                }
                            }
                        }
                        else -> Text("Tekan tombol analisis diatas untuk menjalankan simulasi mata kamera detektor.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showSoilScanDialog = false }) { Text("Selesai") }
            }
        )
    }

    if (showBiodiversityDialog) {
        var identifierName by remember { mutableStateOf("Kantong Semar") }
        val bioResult by viewModel.biodiversityState.collectAsState()

        AlertDialog(
            onDismissRequest = { showBiodiversityDialog = false },
            title = { Text("Biodiversity Guardian ⭐") },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Periksa kelangkaan tumbuhan di sekitar Anda.", fontSize = 12.sp)
                    OutlinedTextField(
                        value = identifierName,
                        onValueChange = { identifierName = it },
                        label = { Text("Tulis Spesies Tumbuhan") },
                        modifier = Modifier.fillMaxWidth().testTag("biodiversity_input")
                    )

                    Button(
                        onClick = { viewModel.scanBiodiversityGuardian(identifierName) },
                        modifier = Modifier.fillMaxWidth().testTag("biodiversity_action_btn")
                    ) {
                        Text("Analisis Status Kelangkaan")
                    }

                    when (bioResult) {
                        is UiState.Loading -> Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        is UiState.Success -> {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                                Text(
                                    text = (bioResult as UiState.Success<String>).data,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                        else -> {}
                    }
                }
            },
            confirmButton = { Button(onClick = { showBiodiversityDialog = false }) { Text("Tutup") } }
        )
    }
}

// Custom Design Card for singular Plant items
@Composable
fun PlantCardItem(plant: PlantEntity, onWater: () -> Unit, onObserveAnomaly: (String) -> Unit) {
    var showDiagList by remember { mutableStateOf(false) }
    var expandedLogs by remember { mutableStateOf(false) }

    // Smooth count animations for plant health ratings
    val animatedHealthBadgeScore by animateFloatAsState(
        targetValue = plant.healthScore.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "plant_health_badge"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(plant.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    Text("${plant.type} • ${plant.location} • ${plant.soilType}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (plant.healthScore >= 80) Color(0xFFCFEADE) else Color(0xFFFBE6E1))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Kondisi: ${animatedHealthBadgeScore.toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (plant.healthScore >= 80) Color(0xFF125B49) else Color(0xFF9A3F29)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time Capsule Logs Visual Accordion
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedLogs = !expandedLogs }
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Plant Time Capsule (Histori Medis)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Icon(if (expandedLogs) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
            }

            AnimatedVisibility(
                visible = expandedLogs,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        .padding(8.dp)
                ) {
                    val logs = remember(plant.memoryLogsJson) {
                        try {
                            val arr = org.json.JSONArray(plant.memoryLogsJson)
                            val list = mutableListOf<String>()
                            for (i in 0 until arr.length()) {
                                list.add(arr.getString(i))
                            }
                            list
                        } catch (e: Exception) {
                            listOf("Belum ada rekam medis terdaftar")
                        }
                    }
                    logs.forEach { log ->
                        Text("• $log", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Water operation and Anomaly Log triggering
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { showDiagList = !showDiagList },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Catat Gejala 🩹", fontSize = 12.sp)
                }

                Button(
                    onClick = onWater,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp).testTag("water_plant_btn_${plant.id}")
                ) {
                    Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Disiram", fontSize = 12.sp)
                }
            }

            AnimatedVisibility(
                visible = showDiagList,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Pilih Keadaan Klinis:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val symptoms = listOf("Kekurangan Air", "Daun Menguning", "Batang Melunak", "Bercak Hitam")
                        symptoms.forEach { symptom ->
                            Box(
                                modifier = Modifier
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                    .clickable {
                                        onObserveAnomaly(symptom)
                                        showDiagList = false
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(symptom, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2B. TAB 1: WEATHER FORECAST & SIMULATOR
// ==========================================
@Composable
fun MobileWeatherAndSimulator(viewModel: GardenViewModel) {
    val weather by viewModel.weatherState.collectAsState()
    val rawLoc by viewModel.userLocation.collectAsState()
    val listSimulatorResult by viewModel.simulatorState.collectAsState()
    val plants by viewModel.allPlants.collectAsState()
    
    var showAddressEditor by remember { mutableStateOf(false) }
    var locationInput by remember { mutableStateOf(rawLoc) }

    // Navigation and sub-screen state
    var selectedProgressPlant by remember { mutableStateOf<PlantEntity?>(null) }

    // Simulator input parameters
    var plantSelected by remember { mutableStateOf("Tomat Ceri") }
    var shadeLevel by remember { mutableStateOf("Teduh Sebagian (4 Jam Matahari)") }
    var initialSize by remember { mutableStateOf("Ukuran Kecil (Kecambah)") }
    var seasonChoice by remember { mutableStateOf("Kemarau Panas") }

    if (selectedProgressPlant != null) {
        val currentPlant = plants.firstOrNull { it.id == selectedProgressPlant!!.id } ?: selectedProgressPlant!!
        PlantProgressDetailScreen(
            plant = currentPlant,
            viewModel = viewModel,
            onBack = { selectedProgressPlant = null }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Location Header with GPS Editor
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("📍 LOKASI UTAMA DAERAH:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(rawLoc, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        }
                        IconButton(onClick = { showAddressEditor = !showAddressEditor }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Lokasi")
                        }
                    }

                    if (showAddressEditor) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = locationInput,
                                onValueChange = { locationInput = it },
                                label = { Text("Atur Daerah") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.setLocation(locationInput)
                                    showAddressEditor = false
                                },
                                modifier = Modifier.align(Alignment.CenterVertically)
                            ) {
                                Text("Set")
                            }
                        }
                    }
                }
            }

            // Title: Kategorisasi Lokasi & Dampak Lingkungan
            Text("Koleksi Lokasi Tanaman (My Plants) 🌱", fontSize = 18.sp, fontWeight = FontWeight.Black)
            
            // Selector Tabs for Indoor, Outdoor, Balkon
            var selectedLocCategory by remember { mutableStateOf("Indoor") }
            val categories = listOf("Indoor", "Outdoor", "Balkon")
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedLocCategory == cat
                    Button(
                        onClick = { selectedLocCategory = cat },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            text = when (cat) {
                                "Indoor" -> "🏡 Indoor"
                                "Outdoor" -> "☀️ Outdoor"
                                else -> "🪴 Balkon"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Environmental Impact info card for Selected Category
            val metInfo = when (selectedLocCategory) {
                "Indoor" -> LocationMetInfo(
                    bgColor = Color(0xFFE3F2FD),
                    textColor = Color(0xFF1E88E5),
                    tempRange = "20°C - 25°C",
                    light = "Terlindung (Shadow)",
                    humidity = "Sedang (50-60%)",
                    advice = "Tanaman terlindung dari paparan panas terik langsung. Penguapan air tanah lambat, mohon awasi kelebihan penyiraman (overwatering) demi menghindari kebusukan akar."
                )
                "Outdoor" -> LocationMetInfo(
                    bgColor = Color(0xFFFFF3E0),
                    textColor = Color(0xFFFB8C00),
                    tempRange = "${weather.temp}°C - ${weather.temp + 4}°C",
                    light = "Matahari Penuh (Direct)",
                    humidity = "Tinggi (Alami)",
                    advice = "Fotosintesis maksimal terpicu di lapangan terbuka, sirkulasi udara optimal. Namun penguapan tanah terjadi amat pesat, silakan sirami tanaman ini setiap pagi secara konsisten."
                )
                else -> LocationMetInfo(
                    bgColor = Color(0xFFE8F5E9),
                    textColor = Color(0xFF43A047),
                    tempRange = "${weather.temp - 1}°C - ${weather.temp + 2}°C",
                    light = "Terang Sebagian (4-6 jam)",
                    humidity = "Cukup Tinggi (60-70%)",
                    advice = "Suhu ternaungi & sirkulasi udara sepoi-sepoi ideal untuk herba sayuran bumbu dapur dan tanaman pot hias gantung. Lindungi dari sirkulasi badai hujan ekstrem."
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = metInfo.bgColor),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, metInfo.textColor.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📊 METEOROLOGI & ESTIMASI PERTUMBUHAN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = metInfo.textColor,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Suhu Rata-rata:", fontSize = 9.sp, color = Color.Gray)
                            Text(metInfo.tempRange, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sinar Matahari:", fontSize = 9.sp, color = Color.Gray)
                            Text(metInfo.light, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Kelembaban:", fontSize = 9.sp, color = Color.Gray)
                            Text(metInfo.humidity, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = metInfo.textColor.copy(alpha = 0.15f))
                    
                    Text(
                        text = "💡 Faktor Tumbuh: ${metInfo.advice}",
                        fontSize = 11.sp,
                        color = Color.DarkGray,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Plant listing under this category
            val filteredPlants = plants.filter { it.location.equals(selectedLocCategory, ignoreCase = true) }
            
            Text(
                text = "Daftar Tanaman di $selectedLocCategory (${filteredPlants.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (filteredPlants.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🌱", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Belum ada tanaman di lokasi $selectedLocCategory.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Gunakan tombol 'Tanam Baru' di tab utama 'Kebun' untuk menempatkan tanaman.",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                filteredPlants.forEach { plant ->
                    LocationPlantCardItem(
                        plant = plant,
                        onOpenProgress = { selectedProgressPlant = plant }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

        // Garden Simulator Section
        Text("Garden Simulator (Eksperimen Bebas) 🎮", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Uji tumbuh kembang secara digital tanpa merusak bibit asli.", fontSize = 12.sp, color = Color.Gray)
                
                // Plant choice
                Text("Pilih Tanaman Simulator:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val plantsAva = listOf("Tomat Ceri", "Cabai Rawit", "Lidah Buaya", "Kemangi")
                    items(plantsAva) { p ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (plantSelected == p) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { plantSelected = p }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(p, color = if (plantSelected == p) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
                        }
                    }
                }

                // Shade slider simulator choice
                Text("Intensitas Sinar Sesuai Tempat:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                val shades = listOf("Teduh Sebagian (4 Jam Matahari)", "Matahari Penuh (8 Jam)", "Dalam Ruangan Gelap (<2 Jam)")
                shades.forEach { s ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { shadeLevel = s }) {
                        RadioButton(selected = shadeLevel == s, onClick = { shadeLevel = s })
                        Text(s, fontSize = 12.sp)
                    }
                }

                // Season parameters selector
                Text("Model Pengkondisian Cuaca:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                val seasons = listOf("Kemarau Panas", "Hujan Kelembapan Ekstrim")
                seasons.forEach { s ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { seasonChoice = s }) {
                        RadioButton(selected = seasonChoice == s, onClick = { seasonChoice = s })
                        Text(s, fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = { viewModel.runGardenSimulator(plantSelected, initialSize, shadeLevel, seasonChoice) },
                    modifier = Modifier.fillMaxWidth().testTag("simulate_growth_action")
                ) {
                    Text("Jalankan Simulasi Tumbuh Kembang")
                }

                // Results Layout
                when (listSimulatorResult) {
                    is UiState.Loading -> Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    is UiState.Success -> {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Prediksi Panen & Pertumbuhan (Simulated):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text((listSimulatorResult as UiState.Success<String>).data, fontSize = 12.sp, lineHeight = 16.sp)
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
    }
}

@Composable
fun WeatherMetricItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, fontSize = 10.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

// ==========================================
// 2C. TAB 2: SMART GARDEN MISSION
// ==========================================
@Composable
fun MobileMissionsScreen(viewModel: GardenViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val missions by viewModel.allMissions.collectAsState()
    val rank by viewModel.userRank.collectAsState()
    val xp by viewModel.userXp.collectAsState()
    val redeemedList by viewModel.redeemedVouchers.collectAsState()

    var subTabSelected by remember { mutableStateOf(0) }
    val subTabs = listOf("Misi Aktif 🎯", "Redeem Voucher 🎁", "Panduan Level 🎓")

    // Claim Success Dialog state
    var showRedeemDialog by remember { mutableStateOf(false) }
    var redeemedVoucherTitle by remember { mutableStateOf("") }
    var redeemedVoucherPromoCode by remember { mutableStateOf("") }

    // Dynamic level milestone calculation
    val currentXpMin = when {
        xp < 200 -> 0
        xp < 500 -> 200
        xp < 1000 -> 500
        else -> 1000
    }
    val currentXpMax = when {
        xp < 200 -> 200
        xp < 500 -> 500
        xp < 1000 -> 1000
        else -> 1000
    }
    val nextLevelLabel = when {
        xp < 200 -> "Level 2: 🌿 Grower (Butuh 200 XP)"
        xp < 500 -> "Level 3: 🌳 Urban Farmer (Butuh 500 XP)"
        xp < 1000 -> "Level 4: 🏆 Master Gardener (Butuh 1000 XP)"
        else -> "Level Maksimum Terlampaui!"
    }
    val progressFraction = if (currentXpMax == currentXpMin) 1f else {
        ((xp - currentXpMin).toFloat() / (currentXpMax - currentXpMin).toFloat()).coerceIn(0f, 1f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core Profile Level Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text("Palawa Gamifikasi Kebun 🏆", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                Text("Selesaikan misi berkebun perkotaan Anda dan tingkatkan Level berkebun Anda!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                
                if (currentUser?.isGuest == true) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔒", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Peringkat Terbatas Offline", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                Text("Registrasikan No. HP / Gmail untuk mengaktifkan sinkronisasi silang-perangkat & klaim permanen!", fontSize = 10.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Level Anda Saat Ini:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(rank, fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                }
                
                // Dynamic Progress bar indicators
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = progressFraction,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("$xp XP Terkumpul", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(nextLevelLabel, fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

        // Subcategory Capsule filter-tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            subTabs.forEachIndexed { index, title ->
                val active = subTabSelected == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { subTabSelected = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        color = if (active) Color.White else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Display Subtab contents
        when (subTabSelected) {
            0 -> { // TAB 0: Active Missions List
                Text("Misi Berkelanjutan Aktif:", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(missions) { mission ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (mission.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) 
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mission.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (mission.isCompleted) Color.Gray else MaterialTheme.colorScheme.primary
                                    )
                                    Text(mission.description, fontSize = 11.sp, color = Color.Gray, lineHeight = 15.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                            Text("+${mission.xpReward} XP", fontSize = 9.sp, modifier = Modifier.padding(2.dp))
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Lencana: ${mission.badgeName}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                                
                                Button(
                                    onClick = { viewModel.completeMissionById(mission) },
                                    enabled = !mission.isCompleted,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp).testTag("claim_mission_${mission.id}")
                                ) {
                                    Text(if (mission.isCompleted) "Selesai ✅" else "Klaim", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            1 -> { // TAB 1: XP Voucher Redemption Shop
                Text("Tukar XP dengan Voucher Menarik! 🎁", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                Text("Kumpulkan XP dari menyiram & scan klorofil. Tukarkan dengan voucher fisik non-tunai di bawah untuk menanam pohon & tanaman asli!", fontSize = 12.sp, color = Color.Gray)

                val availableVouchers = remember {
                    listOf(
                        Triple("Paket Premium Benih Tomat Ceri", 150, "CERI-FREE-PALAWA"),
                        Triple("Kelas Online Hidroponik Balkon", 200, "CLASS-HYDRO-PALAWA"),
                        Triple("Gratis 1 Kg Pupuk Kascing Murni", 250, "PUPUK-KASCING-FREE"),
                        Triple("Diskon 50% Pot Terakota Estetik", 300, "POT-TERAKOTA-50"),
                        Triple("Sesi Konsultasi Ahli Palawa 1on1", 100, "SOS-EXPERT-PALAWA")
                    )
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(availableVouchers) { item ->
                        val (title, cost, code) = item
                        val isEligible = xp >= cost
                        val redeemCount = redeemedList.count { it == title }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.5.dp, if (isEligible) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.LightGray.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🎟️", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                            Text("Sarat biaya: $cost XP", fontSize = 11.sp, color = if (isEligible) MaterialTheme.colorScheme.primary else Color.Red, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    
                                    if (redeemCount > 0) {
                                        Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                            Text("Dimiliki: $redeemCount x", fontSize = 9.sp, modifier = Modifier.padding(2.dp))
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (title.contains("Benih")) "Klaim benih tomat ceri rimbun gratis di depot agro terdekat."
                                    else if (title.contains("Kelas")) "Akses penuh bimbingan video zoom interaktif eksklusif bersama pakar botani."
                                    else if (title.contains("Pupuk")) "Pupuk padat tinggi nitrogen merangsang fotosintesis dahan teras."
                                    else if (title.contains("Pot")) "Pot custom tanah liat berlubang aerasi mematikan peluang busuk jangkar root rot."
                                    else "Sesi privat 20 menit live chat interaktif mendiagnosis seluruh penyakit tanaman kebun pekarangan Anda.",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray,
                                    lineHeight = 15.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Button(
                                    onClick = {
                                        val success = viewModel.redeemVoucher(cost, title)
                                        if (success) {
                                            redeemedVoucherTitle = title
                                            redeemedVoucherPromoCode = "$code-${(1000..9999).random()}"
                                            showRedeemDialog = true
                                        }
                                    },
                                    enabled = isEligible,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isEligible) MaterialTheme.colorScheme.primary else Color.LightGray
                                    ),
                                    modifier = Modifier.fillMaxWidth().height(36.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(if (isEligible) "Tukarkan Sekarang" else "XP Belum Mencukupi", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
            }
            2 -> { // TAB 2: Levels Hierarchy & Benefits Information
                Text("Struktur Pembagian Level & Hak Istimewa 🎓", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                Text("Naikkan total XP Anda dengan merawat pohon secara rutin untuk naik tangga level berikut:", fontSize = 11.sp, color = Color.Gray)

                val levelsList = remember {
                    listOf(
                        Quadruple(
                            "Level 1: 🌱 Beginner",
                            "Syarat: 0 - 199 XP",
                            "Division: Fase Perkenalan & Adaptasi Saku Perkembangan",
                            listOf(
                                "💧 Akses gratis Forum Komunitas SOS untuk diskusi penyakit tanaman harian.",
                                "📖 Panduan saku penanaman dasar Palawa."
                            )
                        ),
                        Quadruple(
                            "Level 2: 🌿 Grower",
                            "Syarat: 200 - 499 XP",
                            "Division: Fase Perawatan & Budidaya Kebun Aktif",
                            listOf(
                                "🔮 Akses penuh fitur Simulator Pertumbuhan Cuaca presisi tinggi.",
                                "🎟️ Diskon Voucher 5% belanja benih berlisensi resmi di Toko UMKM Tunas.",
                                "⚡ Hak mengklaim misi interaktif harian kategori hobiis."
                            )
                        ),
                        Quadruple(
                            "Level 3: 🌳 Urban Farmer",
                            "Syarat: 500 - 999 XP",
                            "Division: Fase Profesional Swadaya Pangan Mandiri",
                            listOf(
                                "🤖 Akses penuh diagnosis visual AI klorofil presisi tinggi.",
                                "🎟️ Diskon Voucher 15% di Toko UMKM Tunas.",
                                "⭐ Lencana profil terverifikasi asisten hijau 'Pahlawan Hijau' pekarangan.",
                                "💦 Rekomendasi taklimat siram harian otomatis presisi evapotranspirasi."
                            )
                        ),
                        Quadruple(
                            "Level 4: 🏆 Master Gardener",
                            "Syarat: 1000+ XP",
                            "Division: Tingkat Kehormatan Tertinggi Ekologi Mandiri",
                            listOf(
                                "🌿 Sesi video call konsultasi privat 1-on-1 gratis bulanan dengan asisten hijau botanical.",
                                "📦 Gratis biaya kirim voucher benih terakota ke rumah skala nasional.",
                                "👑 Lencana kehormatan emas 'Duta Legenda Botanical' di komunitas.",
                                "🛡️ Mandat moderasi penuh forum SOS komunitas lokal."
                            )
                        )
                    )
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(levelsList) { levelInfo ->
                        val (title, cond, div, benefits) = levelInfo
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                    Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                        Text(cond, fontSize = 9.sp, modifier = Modifier.padding(2.dp))
                                    }
                                }
                                Text(div, fontSize = 10.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Manfaat / Benefits Keanggotaan:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                benefits.forEach { benefit ->
                                    Text("• $benefit", fontSize = 10.sp, color = Color.DarkGray, lineHeight = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Success Redeem Promo dialog
    if (showRedeemDialog) {
        AlertDialog(
            onDismissRequest = { showRedeemDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎉", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Redeem Voucher Berhasil!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Selamat! Anda berhasil menukarkan XP Anda dengan voucher:", fontSize = 12.sp)
                    Text(redeemedVoucherTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    
                    Text("Kode Voucher Promo Anda:", fontSize = 11.sp, color = Color.Gray)
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = redeemedVoucherPromoCode,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(12.dp),
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Text(
                        text = "*Tunjukkan kode unik ini ke depo logistik tani mitra Palawa terdekat atau masukkan saat memesan di marketplace untuk diskon. Selamat menumbuhkan kebaikan hijau!",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        lineHeight = 14.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showRedeemDialog = false }) {
                    Text("Selesai & Salin Kode", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// Small helper tuple for Level info structure
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// ==========================================
// 2D. TAB 3: LOCAL MARKETPLACE & REVERSE MARKETPLACE
// ==========================================
@Composable
fun MobileReverseMarketplace(viewModel: GardenViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val items by viewModel.marketplaceItems.collectAsState()
    val reverseResult by viewModel.reverseMarketplaceState.collectAsState()

    var showReverseSearch by remember { mutableStateOf(false) }
    var seedQueryWanted by remember { mutableStateOf("Buah Stroberi") }
    var targetBudget by remember { mutableStateOf("100000") }

    // Search and category filtration states
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterCategory by remember { mutableStateOf("") } // "" means all categories
    var showFilters by remember { mutableStateOf(false) }

    val categoriesList = listOf("Benih", "Tumbuhan", "Hasil Panen", "Perlengkapan", "Peralatan")

    // Filtered items computed state
    val filteredMarketItems = remember(items, searchQuery, selectedFilterCategory) {
        items.filter { item ->
            val matchesQuery = searchQuery.isEmpty() || 
                item.name.contains(searchQuery, ignoreCase = true) || 
                item.sellerName.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedFilterCategory.isEmpty() || 
                item.category.equals(selectedFilterCategory, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Reverse Marketplace Launcher Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Reverse Marketplace Hunter 🔄", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                Text("Bingung apa saja yang perlu dibeli untuk menanam tanaman impian? Tulis impian Anda, biar sistem mencarikan paket perkakas hemat & penjual daerah terdekat!", fontSize = 12.sp)
                
                Spacer(modifier = Modifier.height(12.dp))

                if (currentUser?.isGuest == true) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🔒 Fitur Terkunci Khusus Anggota", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Untuk merumuskan pencari mitra otomatis 'Reverse Marketplace Matcher', silakan hubungkan nomor ponsel atau Akun Google Anda.",
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 15.sp,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.logout() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Daftar / Masuk Akun", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = { showReverseSearch = !showReverseSearch },
                        modifier = Modifier.fillMaxWidth().testTag("launch_reverse_market")
                    ) {
                        Text(if (showReverseSearch) "Tutup Formulir" else "Hubungkan Rekomendasi Komparasi")
                    }

                    if (showReverseSearch) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = seedQueryWanted,
                            onValueChange = { seedQueryWanted = it },
                            label = { Text("Saya ingin menanam:") },
                            placeholder = { Text("Cth: Stroberi, Selada hidropnik") },
                            modifier = Modifier.fillMaxWidth().testTag("reverse_market_query")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = targetBudget,
                            onValueChange = { targetBudget = it },
                            label = { Text("Target anggaran kas maksimum (Rupiah)") },
                            modifier = Modifier.fillMaxWidth().testTag("reverse_market_budget")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.submitReverseMarketplace(seedQueryWanted, targetBudget.toDoubleOrNull() ?: 50000.0) },
                            modifier = Modifier.fillMaxWidth().testTag("reverse_market_trigger"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Temukan Kebutuhan Mulai Menanam")
                        }
                    }
                }

                // AI Match Results
                if (reverseResult is UiState.Loading) {
                    Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else if (reverseResult is UiState.Success) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Rekomendasi Paket Hemat UMKM Lokal:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text((reverseResult as UiState.Success<String>).data, fontSize = 11.sp, lineHeight = 16.sp)
                        }
                    }
                }
            }
        }

        // Standard Store items list header
        Text("Toko Marketplace Lokal (UMKM Tunas):", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

        // Modern Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it 
                showFilters = true 
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("marketplace_search_bar"),
            placeholder = { Text("Cari benih, tumbuhan, hasil panen...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari", tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (searchQuery.isNotEmpty() || selectedFilterCategory.isNotEmpty()) {
                        IconButton(onClick = { 
                            searchQuery = "" 
                            selectedFilterCategory = ""
                            showFilters = false
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Bersihkan", tint = Color.Gray)
                        }
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.LightGray
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Interactive Categories Expandable Filter Options
        AnimatedVisibility(visible = showFilters || searchQuery.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Saring berdasarkan kategori:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        val isAll = selectedFilterCategory.isEmpty()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isAll) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedFilterCategory = "" }
                                .padding(vertical = 6.dp, horizontal = 12.dp)
                        ) {
                            Text("Semua", fontSize = 11.sp, color = if (isAll) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        }
                    }
                    items(categoriesList) { cat ->
                        val isSel = selectedFilterCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedFilterCategory = cat }
                                .padding(vertical = 6.dp, horizontal = 12.dp)
                        ) {
                            Text(cat, fontSize = 11.sp, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Marketplace items list (Standard Card View)
        if (filteredMarketItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Tidak ada item yang cocok dengan pencarian Anda.", color = Color.Gray, fontSize = 12.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .height(420.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredMarketItems) { shopItem ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        var showPassport by remember { mutableStateOf(false) }

                        Column {
                            // Render AI-generated product images via Coil AsyncImage
                            AsyncImage(
                                model = shopItem.imageResName, // Holds beautiful Unsplash URL
                                contentDescription = shopItem.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(shopItem.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                            Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                                Text(shopItem.category, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Penjual: ${shopItem.sellerName}", fontSize = 10.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                    Text(
                                        text = "Rp ${shopItem.price.toInt()}",
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 15.sp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Lokasi: ${shopItem.location}", fontSize = 10.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { showPassport = !showPassport },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Seed DNA Passport 📋", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { /* buy action placeholder */ },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("Beli Langsung", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (showPassport) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("🧬 PASPOR DNA BENIH ASLI", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                            Text("• Asal Daerah: ${shopItem.origin}", fontSize = 10.sp, color = Color.DarkGray)
                                            Text("• Kondisi Tumbuh: ${shopItem.idealCondition}", fontSize = 10.sp, color = Color.DarkGray)
                                            Text("• Histori Komunitas: ${shopItem.communityHistory}", fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2E. TAB 4: PLANT SOS COMMUNITY FORUM
// ==========================================
@Composable
fun MobileSosForum(viewModel: GardenViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val threads by viewModel.sosForumThreads.collectAsState()
    
    var showCreateForm by remember { mutableStateOf(false) }
    var threadTitle by remember { mutableStateOf("") }
    var threadDesc by remember { mutableStateOf("") }
    var threadPlantType by remember { mutableStateOf("Tomat") }
    var threadUrgency by remember { mutableStateOf("🟡 Sedang") }

    var showEmergencyScan by remember { mutableStateOf(false) }
    var emergencyPlantName by remember { mutableStateOf("Tomat") }
    var emergencySymptomText by remember { mutableStateOf("Bercak kuning melingkar di daun terbawah") }
    val emerResult by viewModel.emergencyScanState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Emergency Triage triggers
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFECEF)),
            border = BorderStroke(1.dp, Color(0xFFE0C0C4))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC00000))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Emergency Plant Mode 🚨", fontWeight = FontWeight.ExtraBold, color = Color(0xFFC00000), fontSize = 16.sp)
                }
                Text("Tanaman sakit parah? Cari tingkat keparahan & tindakan prioritas instan dengan AI Patologi Tanaman.", fontSize = 12.sp, color = Color.DarkGray)
                
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { showEmergencyScan = !showEmergencyScan },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC00000)),
                    modifier = Modifier.fillMaxWidth().testTag("emergency_trigger_btn")
                ) {
                    Text(if (showEmergencyScan) "Sembunyikan Emergency Scanner" else "Jalankan Emergency Scan Cerdas")
                }

                if (showEmergencyScan) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = emergencyPlantName,
                        onValueChange = { emergencyPlantName = it },
                        label = { Text("Tulis Nama Tumbuhan") },
                        modifier = Modifier.fillMaxWidth().testTag("emergency_name_input")
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = emergencySymptomText,
                        onValueChange = { emergencySymptomText = it },
                        label = { Text("Uraikan gejala fisik penyakit") },
                        modifier = Modifier.fillMaxWidth().testTag("emergency_symptom_input")
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { viewModel.scanEmergencyDisease(emergencyPlantName, emergencySymptomText) },
                        modifier = Modifier.fillMaxWidth().testTag("emergency_action_trigger"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Dapatkan Langkah Pertolongan Pertama")
                    }

                    when (emerResult) {
                        is UiState.Loading -> Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        is UiState.Success -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                Text(
                                    text = (emerResult as UiState.Success<String>).data,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(12.dp),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
        }

        // Forum Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Plant SOS Community Forum 💬", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Button(
                onClick = { showCreateForm = !showCreateForm },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier.height(30.dp).testTag("write_sos_btn")
            ) {
                Text(if (showCreateForm) "Batal" else "Tanya Forum", fontSize = 11.sp)
            }
        }

        if (showCreateForm) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                if (currentUser?.isGuest == true) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔒 Penulisan SOS Terkunci", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Untuk melaporkan kegawatdaruratan tanaman ke forum publik, Anda harus log in menggunakan Google atau No. Telepon terlebih dahulu guna mencegah penyalahgunaan forum.",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.logout() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Daftar Sekarang", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Tanyakan kasus kepada komunitas & expert:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        
                        OutlinedTextField(
                            value = threadTitle,
                            onValueChange = { threadTitle = it },
                            label = { Text("Judul keluhan") },
                            modifier = Modifier.fillMaxWidth().testTag("sos_title_input")
                        )
                        OutlinedTextField(
                            value = threadPlantType,
                            onValueChange = { threadPlantType = it },
                            label = { Text("Jenis Tanaman") },
                            modifier = Modifier.fillMaxWidth().testTag("sos_plant_input")
                        )
                        OutlinedTextField(
                            value = threadDesc,
                            onValueChange = { threadDesc = it },
                            label = { Text("Deskripsi keluhan detail") },
                            modifier = Modifier.fillMaxWidth().testTag("sos_desc_input")
                        )

                        Button(
                            onClick = {
                                if (threadTitle.isNotBlank()) {
                                    viewModel.createSosThread(threadTitle, threadPlantType, threadDesc, threadUrgency)
                                    showCreateForm = false
                                    threadTitle = ""
                                    threadDesc = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("sos_submit_action")
                        ) {
                            Text("Kirim ke Plant SOS Community")
                        }
                    }
                }
            }
        }

        // SOS Threads Lazy List
        LazyColumn(
            modifier = Modifier
                .height(400.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(threads) { thread ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(thread.severity, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(thread.timestamp, fontSize = 10.sp, color = Color.Gray)
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(thread.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        Text("Tumbuhan: ${thread.plantType} • Ditanya oleh: ${thread.author}", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(thread.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

                        if (thread.expertAnswer.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Jawaban Spesialis Ahli Tanaman & AI 🎓", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(thread.expertAnswer, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Apakah solusi ini membantu?", fontSize = 10.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                        .clickable { viewModel.upvoteSosThread(thread.id) }
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("👍 ${thread.votesCount}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. DESKTOP WEBSITE VERSION VIEWPORT MOCK
// ==========================================
@Composable
fun DesktopWebsiteView(viewModel: GardenViewModel) {
    var webTabSelected by remember { mutableStateOf(0) }
    
    val allPlants by viewModel.allPlants.collectAsState()
    val co2Val by viewModel.co2Impact.collectAsState()
    val healthByVal by viewModel.gardenHealthScore.collectAsState()
    val userRank by viewModel.userRank.collectAsState()

    // Web simulation inputs
    var webPlant by remember { mutableStateOf("Tomat Ceri") }
    var webMoist by remember { mutableStateOf("Matahari Penuh (8 Jam)") }
    var webSeason by remember { mutableStateOf("Kemarau Panas") }
    val simResult by viewModel.simulatorState.collectAsState()

    // Reverse Marketplace Web inputs
    var inputQueryStr by remember { mutableStateOf("Stroberi Segar Hortikultura") }
    var inputBudget by remember { mutableStateOf("150000") }
    val reverseResult by viewModel.reverseMarketplaceState.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F3F0)) // Light gray desktop container background
    ) {
        // Desktop Left Navigation Sidebar (Responsive M3 Rails styled)
        Column(
            modifier = Modifier
                .width(220.dp)
                .fillMaxHeight()
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("🎛️ PORTAL WEB", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
            
            WebSidebarItem(label = "Dashboard Kebun", icon = Icons.Default.Eco, isSelected = webTabSelected == 0, onClick = { webTabSelected = 0 })
            WebSidebarItem(label = "Simulator Lab", icon = Icons.Default.Cloud, isSelected = webTabSelected == 1, onClick = { webTabSelected = 1 })
            WebSidebarItem(label = "Reverse Pasar Lestari", icon = Icons.Default.ShoppingCart, isSelected = webTabSelected == 2, onClick = { webTabSelected = 2 })
            WebSidebarItem(label = "Informasi & Panduan", icon = Icons.Default.Info, isSelected = webTabSelected == 3, onClick = { webTabSelected = 3 })

            Spacer(modifier = Modifier.weight(1.0f))

            // User Info Badge
            Divider()
            Text("Akun Pengguna:", fontSize = 11.sp, color = Color.Gray)
            Text(userRank, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            Text("Tunas ID: frederick@gmail", fontSize = 10.sp, color = Color.LightGray)
        }

        // Desktop Right Content Web Console
        Box(
            modifier = Modifier
                .weight(1.0f)
                .fillMaxHeight()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (webTabSelected) {
                0 -> { // WEB DASHBOARD
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Daftar Pengawasan Kebun Kota Terpusat", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        Text("Sistem Palawa memantau parameter iklim mikro untuk perkotaan makro secara real-time.", fontSize = 13.sp, color = Color.Gray)

                        // Top metrics side-by-side row
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            WebLargeMetricCard(
                                title = "Garden Health Score",
                                value = "$healthByVal%",
                                desc = "Rata-rata kesehatan vegetatif menyeluruh.",
                                modifier = Modifier.weight(1f)
                            )
                            WebLargeMetricCard(
                                title = "Estimasi Absorpsi CO₂",
                                value = "${co2Val}g",
                                desc = "Komitmen Anda mengurangi emisi karbon.",
                                modifier = Modifier.weight(1f)
                            )
                            WebLargeMetricCard(
                                title = "Jumlah Tanaman Aktif",
                                value = "${allPlants.size} Pot",
                                desc = "Tersambung di Cloud Lokal.",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Wide layout showing active plants and general instructions
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Daftar Tanaman Terkoordinasi:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (allPlants.isEmpty()) {
                                    Text("Belum ada tanaman yang tersambung. Anda dapat menambahkannya melalui Tampilan Mobile App.", fontSize = 13.sp, color = Color.Gray)
                                } else {
                                    allPlants.forEach { p ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp)
                                                .background(Color(0xFFF7F9F6))
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(p.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("Spesies: ${p.type} | Media: ${p.soilType} | Peletakan: ${p.location}", fontSize = 11.sp, color = Color.Gray)
                                            }
                                            Text("Kondisi Pot: ${p.healthScore}%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> { // WEB SIMULATOR LAB
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Web Simulator Lab 3D", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        Text("Uji tumbuh kembang secara analitis di panel desktop dengan representasi sirkulasi udara makro.", fontSize = 13.sp, color = Color.Gray)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Parameter Simulasi", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    
                                    OutlinedTextField(
                                        value = webPlant,
                                        onValueChange = { webPlant = it },
                                        label = { Text("Jenis Tumbuhan") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = webMoist,
                                        onValueChange = { webMoist = it },
                                        label = { Text("Intensitas Sinar") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = webSeason,
                                        onValueChange = { webSeason = it },
                                        label = { Text("Faktor Musim") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Button(
                                        onClick = { viewModel.runGardenSimulator(webPlant, "Kecambah", webMoist, webSeason) },
                                        modifier = Modifier.fillMaxWidth().testTag("web_simulate_btn")
                                    ) {
                                        Text("Jalankan Skenario Simulator Web")
                                    }
                                }
                            }

                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Grafik Prediksi Lanjutan AI:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    when (simResult) {
                                        is UiState.Loading -> CircularProgressIndicator()
                                        is UiState.Success -> Text((simResult as UiState.Success<String>).data, fontSize = 13.sp, lineHeight = 18.sp)
                                        else -> Text("Siap menerima simulasi.", fontSize = 13.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> { // REVERSE MARKETPLACE
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Reverse Marketplace Web Console", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        Text("Konsol pencarian suplai regional untuk UMKM terintegrasi.", fontSize = 13.sp, color = Color.Gray)

                        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    OutlinedTextField(
                                        value = inputQueryStr,
                                        onValueChange = { inputQueryStr = it },
                                        label = { Text("Nama Tanaman Impian") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = inputBudget,
                                        onValueChange = { inputBudget = it },
                                        label = { Text("Anggaran Maksimal (IDR)") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Button(
                                    onClick = { viewModel.submitReverseMarketplace(inputQueryStr, inputBudget.toDoubleOrNull() ?: 100000.0) },
                                    modifier = Modifier.fillMaxWidth().testTag("web_reverse_btn")
                                ) {
                                    Text("Hitung Kebutuhan Mulai Menanam Terdekat")
                                }

                                when (reverseResult) {
                                    is UiState.Loading -> CircularProgressIndicator()
                                    is UiState.Success -> {
                                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))) {
                                            Text(
                                                text = (reverseResult as UiState.Success<String>).data,
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                }
                3 -> { // DOCUMENTATION / USAGE INSTRUCTIONS
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Edukasi Konservasi & Panduan Web", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Panduan Palawa Berkelanjutan:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("1. Jaga Tanaman Lokal: Sejajarkan kebun dengan ekologi setempat. Gunakan pupuk organik lokal.", fontSize = 13.sp)
                                Text("2. Hemat Air: Ikuti ramalan cuaca. Tunda menyiram jika awan mendung mencapai keyakinan tinggi (75% hujan).", fontSize = 13.sp)
                                Text("3. Companion Planting: Tanam tomat dengan basil untuk perlindungan lalat buah alami. Hindari mencampur ketimun dengan kentang.", fontSize = 13.sp)
                                Text("4. Lencana & XP: Menyiram tanaman dan memecahkan teka-teki tanah via scanner AI menaikkan kasta urban farming Anda dari Beginner hingga Master Gardener.", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WebSidebarItem(label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray, fontSize = 13.sp)
    }
}

@Composable
fun WebLargeMetricCard(title: String, value: String, desc: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

// ==========================================
// 5. AUTHENTICATION & SINKRONISASI SEED PAGE
// ==========================================
@Composable
fun AuthScreen(
    viewModel: GardenViewModel,
    onSkipAsGuest: () -> Unit
) {
    var emailInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var displayNameInput by remember { mutableStateOf("") }
    var isPhoneTab by remember { mutableStateOf(false) } // Dual Tab: Email vs Phone registration
    
    var showOtpField by remember { mutableStateOf(false) }
    var oTPInput by remember { mutableStateOf("") }
    var isConnecting by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF3F7F5), // LightBg Morn Mist
                        Color(0xFFE4EDE7)  // Pale Green Cream
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Identity Brand Logo Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFFCFEADE)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFCFEADE)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌱", fontSize = 32.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Palawa",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF125B49) // Authentic Green Primary
                )
                Text(
                    text = "Akses Akun & Sinkronisasi Lintas Peranti",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF608C75),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Tab Selector (Gmail vs Phone)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE8ECE9))
                .padding(4.dp)
        ) {
            Button(
                onClick = { isPhoneTab = false },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isPhoneTab) Color.White else Color.Transparent,
                    contentColor = if (!isPhoneTab) Color(0xFF125B49) else Color.Gray
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = if (!isPhoneTab) 2.dp else 0.dp)
            ) {
                Text("Akun Gmail / Google", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { isPhoneTab = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPhoneTab) Color.White else Color.Transparent,
                    contentColor = if (isPhoneTab) Color(0xFF125B49) else Color.Gray
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isPhoneTab) 2.dp else 0.dp)
            ) {
                Text("Nomor Telepon", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Input Fields Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE8ECE9)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Profile Display Name (Optional)
                Text("Username (Opsional)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF608C75))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = displayNameInput,
                    onValueChange = { displayNameInput = it },
                    placeholder = { Text("cth: raka_pratama", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF125B49),
                        unfocusedBorderColor = Color(0xFFE8ECE9)
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (!isPhoneTab) {
                    // Gmail Registry
                    Text("Alamat Email Google / Gmail", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF608C75))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        placeholder = { Text("cth: raka@gmail.com", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("email_input"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF125B49),
                            unfocusedBorderColor = Color(0xFFE8ECE9)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    // Realistic Device Sync Hint
                    Text(
                        text = "💡 Petunjuk Simulasi: Ketik \"raka@gmail.com\" untuk mensimulasikan pemulihan data kebun Raka yang tersimpan di server awan ke perangkat ini.",
                        fontSize = 11.sp,
                        color = Color(0xFFE07A5F),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    // Phone Registry
                    Text("Nomor Telepon", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF608C75))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        placeholder = { Text("cth: 0811223344", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("phone_input"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF125B49),
                            unfocusedBorderColor = Color(0xFFE8ECE9)
                        )
                    )
                    
                    if (showOtpField) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Masukkan Kode Verifikasi SMS OTP (4 Digit)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE07A5F))
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = oTPInput,
                            onValueChange = { oTPInput = it },
                            placeholder = { Text("cth: 1234", fontSize = 13.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("otp_input"),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF125B49),
                                unfocusedBorderColor = Color(0xFFE07A5F)
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    // Realistic Device Sync Hint
                    Text(
                        text = "💡 Petunjuk Simulasi: Gunakan nomor \"0811223344\" untuk memulihkan kebun sekunder Siti Rahma berupa Cabai Rawit.",
                        fontSize = 11.sp,
                        color = Color(0xFFE07A5F),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Connection trigger with smooth loader
                if (isConnecting) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF125B49))
                    }
                } else {
                    Button(
                        onClick = {
                            if (!isPhoneTab) {
                                if (emailInput.isNotBlank() && emailInput.contains("@")) {
                                    isConnecting = true
                                    coroutineScope.launch {
                                        kotlinx.coroutines.delay(1200) // Beautiful realistic network latency delay
                                        viewModel.loginOrRegister(email = emailInput, phone = null, customName = displayNameInput)
                                        isConnecting = false
                                    }
                                }
                            } else {
                                if (!showOtpField) {
                                    if (phoneInput.isNotBlank() && phoneInput.length >= 8) {
                                        isConnecting = true
                                        coroutineScope.launch {
                                            kotlinx.coroutines.delay(800)
                                            showOtpField = true
                                            isConnecting = false
                                        }
                                    }
                                } else {
                                    if (oTPInput.length >= 4) {
                                        isConnecting = true
                                        coroutineScope.launch {
                                            kotlinx.coroutines.delay(1200)
                                            viewModel.loginOrRegister(email = null, phone = phoneInput, customName = displayNameInput)
                                            isConnecting = false
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("auth_submit_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF125B49)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isPhoneTab && !showOtpField) "Kirim SMS OTP Verifikasi" else "Daftar / Masuk & Sinkronisasi",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Guest mode entry link!
        Text(
            text = "Atau eksplorasi terlebih dahulu tanpa akun",
            fontSize = 12.sp,
            color = Color(0xFF608C75),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        OutlinedButton(
            onClick = {
                isConnecting = true
                coroutineScope.launch {
                    kotlinx.coroutines.delay(650)
                    onSkipAsGuest()
                    isConnecting = false
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f).height(46.dp).testTag("guest_mode_btn"),
            border = BorderStroke(1.dp, Color(0xFF125B49)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF125B49)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Masuk Sebagai Tamu (Terbatas)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ====================================================================
// SUPPORTING DATA CLASSES & SCREENS FOR MY PLANTS PROGRESS & AI SCANNER
// ====================================================================

data class ThrivingCommunityPost(
    val id: Int,
    val username: String,
    val avatarEmoji: String,
    val plantType: String,
    val caption: String,
    val imageUrl: String,
    val location: String,
    val status: String,
    val phTest: String,
    val soilCondition: String,
    val tips: String
)

private data class LocationMetInfo(
    val bgColor: Color,
    val textColor: Color,
    val tempRange: String,
    val light: String,
    val humidity: String,
    val advice: String
)

private data class ScanConclusion(
    val height: Int,
    val fertility: String,
    val stage: String,
    val desc: String
)

fun getPlantRealImageUrl(plantType: String): String {
    val typeLower = plantType.trim().lowercase()
    return when {
        typeLower.contains("lidah buaya") || typeLower.contains("aloe") -> 
            "https://images.unsplash.com/photo-1596547609652-9cf5d8d76921?auto=format&fit=crop&q=80&w=400"
        typeLower.contains("lidah mertua") || typeLower.contains("sansevieria") || typeLower.contains("snake plant") -> 
            "https://images.unsplash.com/photo-1597055181300-e3633a207518?auto=format&fit=crop&q=80&w=400"
        typeLower.contains("monstera") -> 
            "https://images.unsplash.com/photo-1614594975525-e45190c55d0b?auto=format&fit=crop&q=80&w=400"
        typeLower.contains("sirih gading") || typeLower.contains("pothos") || typeLower.contains("gading") -> 
            "https://images.unsplash.com/photo-1596436889106-be35e843f974?auto=format&fit=crop&q=80&w=400"
        typeLower.contains("tomat") -> 
            "https://images.unsplash.com/photo-1592841208221-a58d8dfb5749?auto=format&fit=crop&q=80&w=400"
        typeLower.contains("cabai") || typeLower.contains("cabe") || typeLower.contains("chili") -> 
            "https://images.unsplash.com/photo-1588252303780-5a3b93f6bfb8?auto=format&fit=crop&q=80&w=400"
        else -> 
            "https://images.unsplash.com/photo-1463936575829-25148e1db1b8?auto=format&fit=crop&q=80&w=400"
    }
}

@Composable
fun LocationPlantCardItem(
    plant: PlantEntity,
    onOpenProgress: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenProgress() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE2EBE5))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Real Plant Image Thumbnail loaded via Coil
                AsyncImage(
                    model = getPlantRealImageUrl(plant.type),
                    contentDescription = "Gambar asli ${plant.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE2EBE5))
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = plant.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF112E1A)
                            )
                            Text(
                                text = "${plant.type} • Media: ${plant.soilType}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        
                        // Health Score pill
                        Surface(
                            color = when {
                                plant.healthScore >= 80 -> Color(0xFFE8F5E9)
                                plant.healthScore >= 60 -> Color(0xFFFFF3E0)
                                else -> Color(0xFFFFEBEE)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Sehat: ${plant.healthScore}%",
                                color = when {
                                    plant.healthScore >= 80 -> Color(0xFF2E7D32)
                                    plant.healthScore >= 60 -> Color(0xFFEF6C00)
                                    else -> Color(0xFFC62828)
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFFF1F5F2))
            Spacer(modifier = Modifier.height(10.dp))
            
            // Description & Growth stage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fase Tumbuh: ${plant.growthStage}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF558B2F)
                    )
                    
                    // Simple description
                    Text(
                        text = if (plant.customNotes.isNotBlank()) plant.customNotes else "Penyiraman ideal berkala setiap ${plant.waterIntervalDays} hari sekali.",
                        fontSize = 10.sp,
                        color = Color.DarkGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Highly visual button with arrow pointing to progress
                Button(
                    onClick = onOpenProgress,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF125B49)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Progres & Pindai", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

data class ScanResultDetails(
    val heightCm: Int,
    val fertility: String,
    val chlorophyll: String = "Sangat Baik (94%)",
    val statusText: String,
    val actionAdvice: String,
    val growerPraise: String
)

@Composable
fun PlantProgressDetailScreen(
    plant: PlantEntity,
    viewModel: GardenViewModel,
    onBack: () -> Unit
) {
    var showPhotoCaptureDialog by remember { mutableStateOf(false) }
    var selectedMockPhotoId by remember { mutableStateOf(1) } // 1: Tunas, 2: Daun rimbun, 3: Bunga, 4: Buah
    var isScanningAI by remember { mutableStateOf(false) }
    var scanProgressText by remember { mutableStateOf("") }
    var scanProgressValue by remember { mutableStateOf(0f) }
    var showScanResultSuccess by remember { mutableStateOf(false) }
    var latestScanResultSummary by remember { mutableStateOf<String?>(null) }
    var activeScanResult by remember { mutableStateOf<ScanResultDetails?>(null) }
    
    val coroutineScope = rememberCoroutineScope()
    val currentTimestamp = remember {
        val sdf = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault())
        sdf.format(java.util.Date())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back Button & Name header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = plant.name,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Progres Tumbuh • Lokasi ${plant.location}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        // Live Photo Viewfinder Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val (emoji, bgGradient) = when (plant.growthStage) {
                    "Semaian" -> Pair("🌱", Brush.verticalGradient(listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))))
                    "Vegetatif" -> Pair("🌿", Brush.verticalGradient(listOf(Color(0xFFC8E6C9), Color(0xFF81C784))))
                    "Panen" -> Pair("🍅", Brush.verticalGradient(listOf(Color(0xFFFFF3E0), Color(0xFFFFB74D))))
                    else -> Pair("🪴", Brush.verticalGradient(listOf(Color(0xFFE0F2F1), Color(0xFF80CBC4))))
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(emoji, fontSize = 68.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Foto Pantauan Kebun Saat Ini (${plant.growthStage})",
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Active Scan radar pulse overlay if scanning
                if (isScanningAI) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                progress = scanProgressValue,
                                color = Color.Green,
                                strokeWidth = 4.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = scanProgressText,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick action: Ambil Foto Progres Baru
        Button(
            onClick = { showPhotoCaptureDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_capture_progress"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF125B49)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Foto Progres Baru & Pindai Tumbuh", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        // Dynamic State Metrics
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Fase Tumbuh", fontSize = 9.sp, color = Color.Gray)
                    Text(plant.growthStage, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Nutrisi Tanah", fontSize = 9.sp, color = Color.Gray)
                    Text(plant.soilType, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Interval Air", fontSize = 9.sp, color = Color.Gray)
                    Text("${plant.waterIntervalDays} Hari", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Scan Success Details Card
        if (showScanResultSuccess && activeScanResult != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🤖", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hasil Analisis Tumbuh Cerdik AI",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { showScanResultSuccess = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                    
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    
                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("PROGRES TINGGI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("📏 ${activeScanResult!!.heightCm} cm", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Column {
                            Text("KESUBURAN TANAH", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("🧪 ${activeScanResult!!.fertility}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Column {
                            Text("INDEKS KLOROFIL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("🍃 ${activeScanResult!!.chlorophyll}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    
                    // Status
                    Column {
                        Text("SIMPULAN PALAWA:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(activeScanResult!!.statusText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    
                    // Advice / Action suggestions
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color(0xFFEF6C00).copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                            Text("💡", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("SARAN TINDAKAN AHLI:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD84315))
                                Text(activeScanResult!!.actionAdvice, fontSize = 11.sp, color = Color.DarkGray, lineHeight = 15.sp)
                            }
                        }
                    }
                    
                    // Praise Appreciation
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        border = BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                            Text("💖", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("APRESIASI PALAWA UNTUKMU:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                Text(
                                    text = activeScanResult!!.growerPraise,
                                    fontSize = 11.sp,
                                    color = Color(0xFF1B5E20),
                                    fontWeight = FontWeight.Medium,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Timeline Header
        Text(
            text = "Timeline Progres & Riwayat Tumbuh Tunas 📆",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Parse memoryLogsJson and render chronological timeline
        val logs = remember(plant.memoryLogsJson) {
            try {
                val arr = org.json.JSONArray(plant.memoryLogsJson)
                val list = mutableListOf<String>()
                for (i in 0 until arr.length()) {
                    list.add(arr.getString(i))
                }
                list.reversed()
            } catch (e: Exception) {
                listOf("[$currentTimestamp] Tanaman diletakkan di area ${plant.location} pertama kali.")
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            logs.forEach { log ->
                val isAiScan = log.contains("AI SCAN", ignoreCase = true)
                val isWatering = log.contains("disiram", ignoreCase = true)
                val isAnomaly = log.contains("Terdeteksi gejala", ignoreCase = true)
                
                val itemColor = when {
                    isAiScan -> Color(0xFF1B5E20)
                    isWatering -> Color(0xFF0D47A1)
                    isAnomaly -> Color(0xFFB71C1C)
                    else -> Color(0xFF37474F)
                }
                
                val itemBg = when {
                    isAiScan -> Color(0xFFE8F5E9)
                    isWatering -> Color(0xFFE3F2FD)
                    isAnomaly -> Color(0xFFFFEBEE)
                    else -> Color(0xFFECEFF1)
                }
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = itemBg),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, itemColor.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                isAiScan -> "🤖"
                                isWatering -> "💧"
                                isAnomaly -> "⚠️"
                                else -> "📝"
                            },
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = log,
                                fontSize = 11.sp,
                                color = itemColor,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Photo Capture and AI Selection Dialog
    if (showPhotoCaptureDialog) {
        AlertDialog(
            onDismissRequest = { if (!isScanningAI) showPhotoCaptureDialog = false },
            title = { Text("Jepret Kamera AI Tunas 📸", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Simulasikan memfoto tanaman bumbu/kebun Anda di dunia nyata. Pilih kondisi hasil foto Anda saat ini:",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                    
                    val dummyPhotos = listOf(
                        Pair(1, "🌱 Kecambah Muda (Tinggi: ~9cm, Fertilitas Tinggi)"),
                        Pair(2, "🌿 Vegetatif Daun Rimbun (Tinggi: ~23cm, Daun Lebar)"),
                        Pair(3, "🌸 Fase Kuncup Bunga (Tinggi: ~38cm, Siap Bakal Buah)"),
                        Pair(4, "🍅 Buah Matang Siap Panen (Tinggi: ~52cm, Maksimal)")
                    )
                    
                    dummyPhotos.forEach { (id, label) ->
                        val isSelected = selectedMockPhotoId == id
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isScanningAI) { selectedMockPhotoId = id }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(10.dp),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isScanningAI = true
                            scanProgressText = "Menyelaraskan matriks tangkapan kamera..."
                            scanProgressValue = 0.25f
                            kotlinx.coroutines.delay(400)
                            
                            scanProgressText = "Menghitung luas piksel daun klorofil..."
                            scanProgressValue = 0.55f
                            kotlinx.coroutines.delay(400)
                            
                            scanProgressText = "Mengukur estimasi tinggi batang..."
                            scanProgressValue = 0.85f
                            kotlinx.coroutines.delay(400)
                            
                            scanProgressText = "Menyimpulkan indeks kesuburan AI..."
                            scanProgressValue = 1.0f
                            kotlinx.coroutines.delay(300)
                            
                            val conclusion = when (selectedMockPhotoId) {
                                1 -> ScanConclusion(9, "Sangat Subur (94%)", "Semaian", "Kecambah baru berkembang dengan tingkat klorofilitas sehat prima.")
                                2 -> ScanConclusion(23, "Sehat Optimal (89%)", "Vegetatif", "Sistem daun rimbun dengan diameter batang kokoh tercukupi silika dan nitrogen.")
                                3 -> ScanConclusion(38, "Subur Produktif (92%)", "Panen", "Fase generatif bersemi dengan kuncup bunga yang rimbun bebas patogen karat.")
                                else -> ScanConclusion(52, "Subur & Siap Panen (96%)", "Panen", "Kematangan buah optimal rimbun dan subur melimpah gizi.")
                            }
                            
                            val details = when (selectedMockPhotoId) {
                                1 -> ScanResultDetails(
                                    heightCm = 9,
                                    fertility = "Sangat Subur (94%)",
                                    chlorophyll = "Cukup Tinggi (88%)",
                                    statusText = "Kecambah baru berkembang dengan tingkat klorofilitas sehat prima.",
                                    actionAdvice = "Jaga kelembapan permukaan tanah agar perakaran pertama stabil. Taruh di tempat teduh bersinar sepoi-sepoi.",
                                    growerPraise = "Luar biasa! Dedikasi Anda menyemai benih dari dasar melahirkan kecambah kokoh berkilau. Terus sayangi tunas kecil ini!"
                                )
                                2 -> ScanResultDetails(
                                    heightCm = 23,
                                    fertility = "Sehat Optimal (89%)",
                                    chlorophyll = "Sangat Baik (92%)",
                                    statusText = "Sistem fungsional daun rimbun dengan diameter batang mantap perkasa.",
                                    actionAdvice = "Mulai berikan pupuk kompos tipis dan pangkas helai daun paling bawah yang terlihat kutilang.",
                                    growerPraise = "Hebat sekali! Rimbun hijau mempesona ini adalah bukti nyata sentuhan lembut tangan dingin Anda. Pertahankan konsistensi siram pagi!"
                                )
                                3 -> ScanResultDetails(
                                    heightCm = 38,
                                    fertility = "Subur Produktif (92%)",
                                    chlorophyll = "Rasio Sempurna (95%)",
                                    statusText = "Fase generatif bersemi dengan kuncup bunga yang lebat aman dari serangan karat.",
                                    actionAdvice = "Tingkatkan jemuran matahari penuh di bawah pukul 11 pagi. Kurangi air agar pembungaan terstimulasi intensif.",
                                    growerPraise = "Selamat! Tanaman Anda telah bersemi kuncup indah. Pembungaan matang ini merupakan hasil manis dari kesabaran memelihara Anda."
                                )
                                else -> ScanResultDetails(
                                    heightCm = 52,
                                    fertility = "Subur & Siap Panen (96%)",
                                    chlorophyll = "Istimewa (98%)",
                                    statusText = "Kematangan buah optimal rimbun dan subur melimpah gizi siap petik.",
                                    actionAdvice = "Siapkan wadah panen! Ambil gunting steril lalu potong tangkai buah secara rapi untuk merangsang produksi putaran cabang baru.",
                                    growerPraise = "Mahakarya botanical sejati! Panen ranum nan berkilau ini bernilai seni tinggi. Dedikasi dan jerih payah Anda merawat harian sungguh menginspirasi!"
                                )
                            }
                            activeScanResult = details
                            
                            viewModel.submitAIGrowthScan(
                                plant = plant,
                                heightCm = conclusion.height,
                                fertility = conclusion.fertility,
                                customAiConclusion = conclusion.desc,
                                nextStage = conclusion.stage
                            )
                            
                            latestScanResultSummary = "Tinggi: ${conclusion.height}cm, Kesuburan: ${conclusion.fertility}, Status: ${conclusion.desc}"
                            isScanningAI = false
                            showPhotoCaptureDialog = false
                            showScanResultSuccess = true
                        }
                    },
                    enabled = !isScanningAI,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF125B49)),
                    modifier = Modifier.testTag("btn_confirm_scan")
                ) {
                    Text(if (isScanningAI) "Memindai..." else "Bidik & Analisis ⚡")
                }
            },
            dismissButton = {
                if (!isScanningAI) {
                    TextButton(onClick = { showPhotoCaptureDialog = false }) {
                        Text("Batal")
                    }
                }
            }
        )
    }
}


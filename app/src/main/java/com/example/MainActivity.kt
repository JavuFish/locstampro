package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.screens.ExportScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MapScreen
import com.example.ui.screens.NewStampScreen
import com.example.ui.screens.SettingScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TimelineScreen
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryCyan
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LocStampViewModel

enum class MainTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    GALLERY("Gallery", Icons.Default.Collections),
    NEW_STAMP("New Stamp", Icons.Default.CameraAlt),
    PETA_LOKASI("Peta Lokasi", Icons.Default.Map),
    SETTING("Setting", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: LocStampViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val appTheme by viewModel.appTheme.collectAsState()
            MyApplicationTheme(themeMode = appTheme) {
                LocStampApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LocStampApp(viewModel: LocStampViewModel) {
    val context = LocalContext.current
    var showSplash by remember { mutableStateOf(true) }
    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var showExportOverlay by remember { mutableStateOf(false) }

    // Request Camera and Location runtime permissions on launch
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.refreshLocation()
        }
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    if (showSplash) {
        SplashScreen(
            onSplashFinished = { showSplash = false }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (!showExportOverlay) {
                    LocStampBottomNavigation(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showExportOverlay) {
                    ExportScreen(
                        viewModel = viewModel,
                        onNavigateBack = { showExportOverlay = false },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "TabTransition"
                    ) { targetTab ->
                        when (targetTab) {
                            MainTab.HOME -> HomeScreen(
                                viewModel = viewModel,
                                onNavigateToNewStamp = { currentTab = MainTab.NEW_STAMP },
                                onNavigateToGallery = { currentTab = MainTab.GALLERY },
                                onNavigateToMap = { currentTab = MainTab.PETA_LOKASI },
                                onNavigateToSetting = { currentTab = MainTab.SETTING },
                                onNavigateToExport = { currentTab = MainTab.SETTING }
                            )
                            MainTab.GALLERY -> TimelineScreen(
                                viewModel = viewModel,
                                onNavigateToNewStamp = { currentTab = MainTab.NEW_STAMP },
                                onNavigateToMap = { photo ->
                                    viewModel.selectMapPhoto(photo)
                                    currentTab = MainTab.PETA_LOKASI
                                },
                                onNavigateToExport = { currentTab = MainTab.SETTING }
                            )
                            MainTab.NEW_STAMP -> NewStampScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentTab = MainTab.HOME },
                                onSaved = {
                                    currentTab = MainTab.GALLERY
                                }
                            )
                            MainTab.PETA_LOKASI -> MapScreen(
                                viewModel = viewModel,
                                onNavigateToTimeline = { currentTab = MainTab.GALLERY },
                                onNavigateToExport = { currentTab = MainTab.SETTING }
                            )
                            MainTab.SETTING -> SettingScreen(
                                viewModel = viewModel,
                                onNavigateToExport = {
                                    showExportOverlay = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun LocStampBottomNavigation(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(vertical = 4.dp)
        ) {
            NavigationBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
                modifier = Modifier.height(64.dp)
            ) {
                MainTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    val activeColor = when (tab) {
                        MainTab.HOME -> PrimaryCyan
                        MainTab.GALLERY -> AccentViolet
                        MainTab.NEW_STAMP -> AccentEmerald
                        MainTab.PETA_LOKASI -> AccentPink
                        MainTab.SETTING -> AccentAmber
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { onTabSelected(tab) },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 9.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                letterSpacing = (-0.3).sp,
                                softWrap = false,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Visible
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeColor,
                            selectedTextColor = activeColor,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = activeColor.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    }
}

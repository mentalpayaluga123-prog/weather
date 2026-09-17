package com.example.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.alerts.AlertsScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.forecast.ForecastScreen
import com.example.ui.globe.GlobeScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.AlertWarning
import com.example.ui.theme.AtmosphericBlue
import com.example.ui.theme.DeepOceanBlue
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NightSky
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.WeatherViewModel

data class NavItem(
    val screen: AppScreen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val navItems = listOf(
    NavItem(AppScreen.DASHBOARD, "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    NavItem(AppScreen.MAP_3D, "3D Map", Icons.Filled.Public, Icons.Outlined.Public),
    NavItem(AppScreen.FORECAST, "Forecast", Icons.Filled.CalendarToday, Icons.Outlined.CalendarToday),
    NavItem(AppScreen.ALERTS, "Alerts", Icons.Filled.Notifications, Icons.Outlined.Notifications),
    NavItem(AppScreen.SETTINGS, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
)

@Composable
fun WeatherAppShell(
    viewModel: WeatherViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val unreadAlertsCount by viewModel.unreadAlertsCount.collectAsState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace)
    ) {
        val isWideScreen = maxWidth > 600.dp

        if (isWideScreen) {
            // Wide Screen / Tablet Layout: Side Navigation Rail + Main Content Pane
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = NightSky,
                    contentColor = Color.White,
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(vertical = 18.dp)
                                .windowInsetsPadding(WindowInsets.statusBars)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Brush.linearGradient(listOf(ElectricCyan, DeepOceanBlue)),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "WeatherSphere",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    },
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    navItems.forEach { item ->
                        val isSelected = currentScreen == item.screen
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { viewModel.navigateTo(item.screen) },
                            icon = {
                                if (item.screen == AppScreen.ALERTS && unreadAlertsCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(containerColor = AlertWarning, contentColor = DeepSpace) {
                                                Text(unreadAlertsCount.toString(), fontSize = 10.sp)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                }
                            },
                            label = { Text(item.label, fontSize = 11.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = DeepSpace,
                                selectedTextColor = ElectricCyan,
                                indicatorColor = ElectricCyan,
                                unselectedIconColor = AtmosphericBlue,
                                unselectedTextColor = AtmosphericBlue.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.testTag("nav_rail_${item.screen.name.lowercase()}")
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    ScreenContent(currentScreen = currentScreen, viewModel = viewModel)
                }
            }
        } else {
            // Mobile Compact Layout: Scaffold with Bottom Navigation Bar
            Scaffold(
                containerColor = DeepSpace,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    Surface(
                        color = NightSky.copy(alpha = 0.95f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp,
                            windowInsets = WindowInsets(0, 0, 0, 0),
                            modifier = Modifier.height(64.dp)
                        ) {
                            navItems.forEach { item ->
                                val isSelected = currentScreen == item.screen
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { viewModel.navigateTo(item.screen) },
                                    icon = {
                                        if (item.screen == AppScreen.ALERTS && unreadAlertsCount > 0) {
                                            BadgedBox(
                                                badge = {
                                                    Badge(containerColor = AlertWarning, contentColor = DeepSpace) {
                                                        Text(unreadAlertsCount.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                                    contentDescription = item.label,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = item.label,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = item.label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = DeepSpace,
                                        selectedTextColor = ElectricCyan,
                                        indicatorColor = ElectricCyan,
                                        unselectedIconColor = AtmosphericBlue.copy(alpha = 0.7f),
                                        unselectedTextColor = AtmosphericBlue.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier.testTag("nav_bottom_${item.screen.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    ScreenContent(currentScreen = currentScreen, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun ScreenContent(
    currentScreen: AppScreen,
    viewModel: WeatherViewModel
) {
    Crossfade(
        targetState = currentScreen,
        animationSpec = tween(280),
        label = "screen_crossfade"
    ) { screen ->
        when (screen) {
            AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
            AppScreen.MAP_3D -> GlobeScreen(viewModel = viewModel)
            AppScreen.FORECAST -> ForecastScreen(viewModel = viewModel)
            AppScreen.ALERTS -> AlertsScreen(viewModel = viewModel)
            AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
        }
    }
}

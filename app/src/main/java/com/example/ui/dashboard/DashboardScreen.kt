package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CurrentWeather
import com.example.model.DailyForecast
import com.example.model.HourlyForecast
import com.example.model.WeatherAlert
import com.example.model.WeatherCondition
import com.example.ui.components.SearchBarView
import com.example.ui.components.WeatherConditionGraphic
import com.example.ui.components.WeatherConditionSmallIcon
import com.example.ui.globe.Globe3DView
import com.example.ui.theme.AlertSevere
import com.example.ui.theme.AlertWarning
import com.example.ui.theme.AtmosphericBlue
import com.example.ui.theme.DeepOceanBlue
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HeatRed
import com.example.ui.theme.NeonAzure
import com.example.ui.theme.NightSky
import com.example.ui.theme.RainCyan
import com.example.ui.theme.StormViolet
import com.example.ui.theme.SunGold
import com.example.ui.theme.SunOrange
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.WeatherViewModel

@Composable
fun DashboardScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentWeather by viewModel.currentWeather.collectAsState()
    val hourlyForecast by viewModel.hourlyForecast.collectAsState()
    val dailyForecast by viewModel.dailyForecast.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val unreadAlertsCount by viewModel.unreadAlertsCount.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val markers by viewModel.globeMarkers.collectAsState()
    val activeLayer by viewModel.activeLayer.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpace)
            .verticalScroll(scrollState)
            .padding(bottom = 96.dp) // Space for bottom navigation
            .testTag("dashboard_screen")
    ) {
        // 1. Top Bar: App Logo, Name "WeatherSphere", Notification Bell
        TopHeader(
            unreadAlertCount = unreadAlertsCount,
            onNotificationClick = { viewModel.navigateTo(AppScreen.ALERTS) },
            onRefresh = { viewModel.refreshWeather() }
        )

        // 2. Search & Location Bar
        SearchBarView(
            searchQuery = searchQuery,
            onQueryChange = { viewModel.onSearchQueryChanged(it) },
            isSearching = isSearching,
            onSearchActiveChange = { viewModel.setSearching(it) },
            searchResults = searchResults,
            onLocationSelected = { loc -> viewModel.selectLocation(loc) },
            onUseMyLocation = { viewModel.useMyLocation(context) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // 3. Active Alert Banner (if any)
        if (alerts.isNotEmpty()) {
            val primaryAlert = alerts.first()
            AlertBanner(
                alert = primaryAlert,
                onClick = { viewModel.navigateTo(AppScreen.ALERTS) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // 4. Large Current-Weather Hero Card (Main Focus)
        CurrentWeatherHeroCard(
            weather = currentWeather,
            tempUnit = settings.tempUnit,
            onToggleUnit = { viewModel.toggleTempUnit() },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // 5. 24-Hour Hourly Forecast Section
        HourlyForecastSection(
            forecasts = hourlyForecast,
            tempUnit = settings.tempUnit,
            modifier = Modifier.padding(top = 16.dp)
        )

        // 6. Prominent "Explore 3D Weather Map" Button & Mini Preview
        GlobePreviewCard(
            markers = markers,
            activeLayer = activeLayer,
            tempUnit = settings.tempUnit,
            onExploreClick = { viewModel.navigateTo(AppScreen.MAP_3D) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )

        // 7. Atmospheric Telemetry / Weather Statistics Grid
        WeatherStatisticsGrid(
            weather = currentWeather,
            windUnit = settings.windUnit,
            pressureUnit = settings.pressureUnit,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // 8. 7-Day Forecast Section Preview
        SevenDayForecastPreview(
            dailyForecast = dailyForecast,
            tempUnit = settings.tempUnit,
            onViewAllForecast = { viewModel.navigateTo(AppScreen.FORECAST) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun TopHeader(
    unreadAlertCount: Int,
    onNotificationClick: () -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Logo & Brand Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(ElectricCyan, DeepOceanBlue)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = "WeatherSphere",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Global Atmospheric Telemetry",
                    style = MaterialTheme.typography.labelSmall,
                    color = AtmosphericBlue.copy(alpha = 0.7f),
                    letterSpacing = 0.8.sp
                )
            }
        }

        // Actions: Refresh & Notification Bell with Unread Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onRefresh,
                modifier = Modifier.minimumInteractiveComponentSize().testTag("refresh_weather_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = AtmosphericBlue
                )
            }

            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier.minimumInteractiveComponentSize().testTag("notification_bell_button")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadAlertCount > 0) {
                            Badge(
                                containerColor = AlertWarning,
                                contentColor = DeepSpace
                            ) {
                                Text(
                                    text = unreadAlertCount.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Weather Alerts",
                        tint = if (unreadAlertCount > 0) SunGold else AtmosphericBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertBanner(
    alert: WeatherAlert,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bannerBg = when (alert.severity) {
        com.example.model.AlertSeverity.SEVERE -> AlertSevere.copy(alpha = 0.2f)
        com.example.model.AlertSeverity.WARNING -> AlertWarning.copy(alpha = 0.2f)
        else -> NeonAzure.copy(alpha = 0.15f)
    }
    val borderColor = when (alert.severity) {
        com.example.model.AlertSeverity.SEVERE -> AlertSevere
        com.example.model.AlertSeverity.WARNING -> AlertWarning
        else -> NeonAzure
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("dashboard_alert_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bannerBg),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(borderColor.copy(alpha = 0.6f))
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = borderColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = alert.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = AtmosphericBlue,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = AtmosphericBlue,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun CurrentWeatherHeroCard(
    weather: CurrentWeather,
    tempUnit: com.example.model.TempUnit,
    onToggleUnit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardGradient = Brush.linearGradient(
        colors = when (weather.condition) {
            WeatherCondition.SUNNY -> listOf(Color(0xFF1E3C72), Color(0xFF2A5298))
            WeatherCondition.THUNDERSTORM -> listOf(Color(0xFF2C194D), Color(0xFF16222F))
            WeatherCondition.RAINY, WeatherCondition.HEAVY_RAIN -> listOf(Color(0xFF132F4C), Color(0xFF0A1929))
            else -> listOf(Color(0xFF132F4C), Color(0xFF1A365D))
        }
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("current_weather_hero_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorderDark)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardGradient)
                .padding(22.dp)
        ) {
            Column {
                // Header: Location name, Country & Updated Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = weather.location.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (weather.location.state.isNotEmpty())
                                "${weather.location.state}, ${weather.location.country}"
                            else weather.location.country,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AtmosphericBlue
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NightSky.copy(alpha = 0.6f),
                        modifier = Modifier.clickable(onClick = onToggleUnit)
                    ) {
                        Text(
                            text = "Unit: ${tempUnit.symbol}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Center Telemetry: Temperature & Dynamic Weather Graphic
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${tempUnit.convert(weather.tempC)}${tempUnit.symbol}",
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Feels like ${tempUnit.convert(weather.feelsLikeC)}${tempUnit.symbol}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AtmosphericBlue
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = weather.condition.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = ElectricCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    WeatherConditionGraphic(
                        condition = weather.condition,
                        size = 110.dp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Mini stats row inside hero card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(NightSky.copy(alpha = 0.5f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetricMiniItem(
                        icon = Icons.Default.WaterDrop,
                        label = "Humidity",
                        value = "${weather.humidityPercent}%",
                        tint = NeonAzure
                    )
                    MetricMiniItem(
                        icon = Icons.Default.Air,
                        label = "Wind",
                        value = "${weather.windSpeedKmh} km/h",
                        tint = ElectricCyan
                    )
                    MetricMiniItem(
                        icon = Icons.Default.WbSunny,
                        label = "UV Index",
                        value = "${weather.uvIndex} ${if (weather.uvIndex >= 8) "High" else "Mod"}",
                        tint = SunGold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Last updated: ${weather.lastUpdated}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AtmosphericBlue.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun MetricMiniItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = AtmosphericBlue.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HourlyForecastSection(
    forecasts: List<HourlyForecast>,
    tempUnit: com.example.model.TempUnit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "24-Hour Forecast",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Hourly Intervals",
                style = MaterialTheme.typography.labelSmall,
                color = AtmosphericBlue
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            forecasts.forEach { item ->
                HourlyForecastCard(item = item, tempUnit = tempUnit)
            }
        }
    }
}

@Composable
private fun HourlyForecastCard(
    item: HourlyForecast,
    tempUnit: com.example.model.TempUnit
) {
    val isCurrent = item.isNow
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) DeepOceanBlue.copy(alpha = 0.85f) else SurfaceCardDark.copy(alpha = 0.75f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isCurrent) ElectricCyan else SurfaceBorderDark
            )
        ),
        modifier = Modifier
            .width(82.dp)
            .testTag("hourly_card_${item.timeLabel.replace(" ", "_")}")
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.timeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = if (isCurrent) Color.White else AtmosphericBlue,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(8.dp))
            WeatherConditionSmallIcon(
                condition = item.condition,
                tint = if (isCurrent) SunGold else ElectricCyan,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${tempUnit.convert(item.tempC)}${tempUnit.symbol}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${item.rainProbability}%",
                style = MaterialTheme.typography.labelSmall,
                color = RainCyan,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun GlobePreviewCard(
    markers: List<com.example.model.GlobeMarker>,
    activeLayer: com.example.model.WeatherLayerType,
    tempUnit: com.example.model.TempUnit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("globe_preview_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = NightSky),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorderDark)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "3D Weather Globe",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Interactive Planetary Atmosphere",
                        style = MaterialTheme.typography.bodySmall,
                        color = AtmosphericBlue
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ElectricCyan.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = activeLayer.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Embedded interactive 3D Globe preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(DeepSpace)
            ) {
                Globe3DView(
                    markers = markers,
                    activeLayer = activeLayer,
                    tempUnit = tempUnit,
                    autoRotate = true,
                    autoRotateSpeed = 0.25f,
                    onMarkerClick = { onExploreClick() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Prominent "Explore 3D Weather Map" Button
            Button(
                onClick = onExploreClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("explore_3d_map_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = DeepSpace
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Explore 3D Weather Map",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun WeatherStatisticsGrid(
    weather: CurrentWeather,
    windUnit: com.example.model.WindUnit,
    pressureUnit: com.example.model.PressureUnit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Atmospheric Telemetry",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Row 1: Humidity & Wind
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TelemetryCard(
                title = "Humidity",
                value = "${weather.humidityPercent}%",
                subtitle = "Dew point ~${weather.tempC - 4}°C",
                icon = Icons.Default.WaterDrop,
                iconTint = NeonAzure,
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                title = "Wind Flow",
                value = windUnit.convert(weather.windSpeedKmh),
                subtitle = "Heading: ${weather.windDirection}",
                icon = Icons.Default.Air,
                iconTint = ElectricCyan,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: UV Index & Visibility
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val uvDesc = when {
                weather.uvIndex >= 8 -> "Very High"
                weather.uvIndex >= 6 -> "High"
                weather.uvIndex >= 3 -> "Moderate"
                else -> "Low Risk"
            }
            TelemetryCard(
                title = "UV Radiation",
                value = "Level ${weather.uvIndex}",
                subtitle = uvDesc,
                icon = Icons.Default.WbSunny,
                iconTint = SunGold,
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                title = "Visibility",
                value = "${weather.visibilityKm} km",
                subtitle = "Clear Horizon",
                icon = Icons.Default.Visibility,
                iconTint = AtmosphericBlue,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 3: Barometric Pressure & Sunrise/Sunset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TelemetryCard(
                title = "Pressure",
                value = pressureUnit.convert(weather.pressureHpa),
                subtitle = "Barometric Equilibrium",
                icon = Icons.Default.Compress,
                iconTint = ElectricCyan,
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                title = "Solar Ephemeris",
                value = "${weather.sunrise} ↑",
                subtitle = "Sunset: ${weather.sunset} ↓",
                icon = Icons.Default.WbTwilight,
                iconTint = SunOrange,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TelemetryCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardDark.copy(alpha = 0.8f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorderDark)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = AtmosphericBlue
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = AtmosphericBlue.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun SevenDayForecastPreview(
    dailyForecast: List<DailyForecast>,
    tempUnit: com.example.model.TempUnit,
    onViewAllForecast: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("seven_day_forecast_preview"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardDark.copy(alpha = 0.8f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorderDark)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "7-Day Outlook",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View Detailed →",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricCyan,
                    modifier = Modifier.clickable(onClick = onViewAllForecast)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Show top 4 days preview
            dailyForecast.take(4).forEach { day ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.width(90.dp)) {
                        Text(
                            text = day.dayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = day.dateFormatted,
                            style = MaterialTheme.typography.labelSmall,
                            color = AtmosphericBlue.copy(alpha = 0.7f)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WeatherConditionSmallIcon(
                            condition = day.condition,
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "${day.rainProbability}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = RainCyan
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${tempUnit.convert(day.minTempC)}°",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AtmosphericBlue
                        )
                        // Bar graph representation
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(NightSky)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(NeonAzure, SunGold)
                                        )
                                    )
                            )
                        }
                        Text(
                            text = "${tempUnit.convert(day.maxTempC)}°",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

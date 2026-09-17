package com.example.ui.globe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.model.GlobeMarker
import com.example.model.WeatherLayerType
import com.example.ui.components.SearchBarView
import com.example.ui.components.WeatherConditionGraphic
import com.example.ui.components.WeatherConditionSmallIcon
import com.example.ui.theme.AtmosphericBlue
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonAzure
import com.example.ui.theme.NightSky
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.viewmodel.WeatherViewModel

@Composable
fun GlobeScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val markers by viewModel.globeMarkers.collectAsState()
    val activeLayer by viewModel.activeLayer.collectAsState()
    val selectedMarker by viewModel.selectedMarker.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    var autoRotate by remember { mutableStateOf(settings.autoRotateGlobe) }

    Box(modifier = modifier.fillMaxSize()) {
        // Full screen 3D Globe Canvas
        Globe3DView(
            markers = markers,
            activeLayer = activeLayer,
            tempUnit = settings.tempUnit,
            autoRotate = autoRotate,
            autoRotateSpeed = settings.autoRotateSpeed,
            selectedMarker = selectedMarker,
            onMarkerClick = { marker ->
                viewModel.selectGlobeMarker(marker)
            },
            modifier = Modifier.fillMaxSize().testTag("interactive_3d_globe")
        )

        // Top Floating Search Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopCenter)
        ) {
            SearchBarView(
                searchQuery = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                isSearching = isSearching,
                onSearchActiveChange = { viewModel.setSearching(it) },
                searchResults = searchResults,
                onLocationSelected = { loc ->
                    viewModel.selectLocation(loc)
                },
                onUseMyLocation = {
                    viewModel.useMyLocation(context)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Floating Weather Layer Selector Horizontal Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                WeatherLayerType.values().forEach { layer ->
                    val isSelected = activeLayer == layer
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setActiveLayer(layer) },
                        label = {
                            Text(
                                text = layer.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            val icon = when (layer) {
                                WeatherLayerType.TEMPERATURE -> Icons.Default.Thermostat
                                WeatherLayerType.RAIN -> Icons.Default.Grain
                                WeatherLayerType.WIND -> Icons.Default.Air
                                WeatherLayerType.CLOUDS -> Icons.Default.Cloud
                                WeatherLayerType.STORM -> Icons.Default.Thunderstorm
                                WeatherLayerType.SATELLITE -> Icons.Default.Public
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = layer.label,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) DeepSpace else ElectricCyan
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = NightSky.copy(alpha = 0.8f),
                            labelColor = Color.White,
                            selectedContainerColor = ElectricCyan,
                            selectedLabelColor = DeepSpace
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = SurfaceBorderDark,
                            selectedBorderColor = ElectricCyan,
                            enabled = true,
                            selected = isSelected
                        ),
                        modifier = Modifier.testTag("layer_chip_${layer.name.lowercase()}")
                    )
                }
            }
        }

        // Floating Right Control Stack (Zoom In, Zoom Out, Auto-Rotate, Reset View)
        Column(
            modifier = Modifier
                .padding(end = 16.dp)
                .align(Alignment.CenterEnd),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = NightSky.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
                modifier = Modifier.size(44.dp)
            ) {
                IconButton(
                    onClick = { autoRotate = !autoRotate },
                    modifier = Modifier.testTag("auto_rotate_button").minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.RotateRight,
                        contentDescription = "Toggle Auto-Rotation",
                        tint = if (autoRotate) ElectricCyan else AtmosphericBlue.copy(alpha = 0.6f)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = NightSky.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
                modifier = Modifier.size(44.dp)
            ) {
                IconButton(
                    onClick = { viewModel.selectGlobeMarker(null) },
                    modifier = Modifier.testTag("reset_view_button").minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Selection",
                        tint = AtmosphericBlue
                    )
                }
            }
        }

        // Location Information Popup Card (When a location is selected on the 3D Globe)
        AnimatedVisibility(
            visible = selectedMarker != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter)
        ) {
            selectedMarker?.let { marker ->
                LocationPopupCard(
                    marker = marker,
                    tempUnit = settings.tempUnit,
                    onDismiss = { viewModel.selectGlobeMarker(null) },
                    onSetPrimaryLocation = {
                        viewModel.applyMarkerAsPrimaryLocation(marker)
                    }
                )
            }
        }
    }
}

@Composable
private fun LocationPopupCard(
    marker: GlobeMarker,
    tempUnit: com.example.model.TempUnit,
    onDismiss: () -> Unit,
    onSetPrimaryLocation: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("globe_location_popup"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceCardDark.copy(alpha = 0.95f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorderDark)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Location name, Country & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = marker.location.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = marker.location.country,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AtmosphericBlue
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NightSky)
                        .minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Popup",
                        tint = AtmosphericBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Telemetry Row: Temp, Condition, Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WeatherConditionGraphic(
                        condition = marker.condition,
                        size = 52.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${tempUnit.convert(marker.tempC)}${tempUnit.symbol}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = marker.condition.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ElectricCyan
                        )
                    }
                }

                // Mini weather metrics
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = NeonAzure,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "68% Humidity",
                            style = MaterialTheme.typography.labelMedium,
                            color = AtmosphericBlue
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Air,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "18 km/h Wind",
                            style = MaterialTheme.typography.labelMedium,
                            color = AtmosphericBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button: Set as Primary Location
            Button(
                onClick = {
                    onSetPrimaryLocation()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("set_primary_location_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = DeepSpace
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "View on Dashboard & Forecast",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PressureUnit
import com.example.model.TempUnit
import com.example.model.WindUnit
import com.example.ui.theme.AtmosphericBlue
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NightSky
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.viewmodel.WeatherViewModel

@Composable
fun SettingsScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpace)
            .padding(horizontal = 16.dp)
            .padding(bottom = 96.dp)
            .testTag("settings_screen")
    ) {
        item {
            Row(
                modifier = Modifier.padding(top = 16.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "App Settings",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Telemetry & Interface Preferences",
                        style = MaterialTheme.typography.bodySmall,
                        color = AtmosphericBlue.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Units of Measurement
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardDark.copy(alpha = 0.85f)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorderDark)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Units of Measurement",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Temperature Unit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Temperature Unit",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = settings.tempUnit == TempUnit.CELSIUS,
                                onClick = { viewModel.updateSettings(settings.copy(tempUnit = TempUnit.CELSIUS)) },
                                label = { Text("°C") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricCyan,
                                    selectedLabelColor = DeepSpace,
                                    containerColor = NightSky,
                                    labelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = settings.tempUnit == TempUnit.FAHRENHEIT,
                                onClick = { viewModel.updateSettings(settings.copy(tempUnit = TempUnit.FAHRENHEIT)) },
                                label = { Text("°F") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricCyan,
                                    selectedLabelColor = DeepSpace,
                                    containerColor = NightSky,
                                    labelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Wind Unit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Air,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Wind Speed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(WindUnit.KMH, WindUnit.MPH, WindUnit.MS).forEach { unit ->
                                FilterChip(
                                    selected = settings.windUnit == unit,
                                    onClick = { viewModel.updateSettings(settings.copy(windUnit = unit)) },
                                    label = { Text(unit.symbol, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ElectricCyan,
                                        selectedLabelColor = DeepSpace,
                                        containerColor = NightSky,
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pressure Unit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Compress,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pressure",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(PressureUnit.HPA, PressureUnit.INHG).forEach { unit ->
                                FilterChip(
                                    selected = settings.pressureUnit == unit,
                                    onClick = { viewModel.updateSettings(settings.copy(pressureUnit = unit)) },
                                    label = { Text(unit.symbol, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ElectricCyan,
                                        selectedLabelColor = DeepSpace,
                                        containerColor = NightSky,
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3D Globe Preferences
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardDark.copy(alpha = 0.85f)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorderDark)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3D Weather Globe Options",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Auto-Rotate Globe", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                Text("Continuous planetary orbit rotation", color = AtmosphericBlue.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                            }
                        }

                        Switch(
                            checked = settings.autoRotateGlobe,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(autoRotateGlobe = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DeepSpace,
                                checkedTrackColor = ElectricCyan
                            )
                        )
                    }

                    if (settings.autoRotateGlobe) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Rotation Velocity: ${(settings.autoRotateSpeed * 10).toInt() / 10f}x",
                            style = MaterialTheme.typography.bodySmall,
                            color = AtmosphericBlue
                        )
                        Slider(
                            value = settings.autoRotateSpeed,
                            onValueChange = { viewModel.updateSettings(settings.copy(autoRotateSpeed = it)) },
                            valueRange = 0.1f..1.2f,
                            colors = SliderDefaults.colors(
                                thumbColor = ElectricCyan,
                                activeTrackColor = ElectricCyan,
                                inactiveTrackColor = NightSky
                            )
                        )
                    }
                }
            }
        }

        // Notification Preferences
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardDark.copy(alpha = 0.85f)),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Severe Weather Alerts", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                Text("Receive urgent meteorological bulletins", color = AtmosphericBlue.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                            }
                        }

                        Switch(
                            checked = settings.notificationsEnabled,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(notificationsEnabled = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DeepSpace,
                                checkedTrackColor = ElectricCyan
                            )
                        )
                    }
                }
            }
        }

        // System & Version Info
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardDark.copy(alpha = 0.85f)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorderDark)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "About WeatherSphere",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Version: 1.0.0 (Release Build)",
                        style = MaterialTheme.typography.bodySmall,
                        color = AtmosphericBlue
                    )
                    Text(
                        text = "Engine: Spherical 3D Atmospheric Projection Canvas",
                        style = MaterialTheme.typography.bodySmall,
                        color = AtmosphericBlue
                    )
                    Text(
                        text = "Telemetry: Doppler Radar, Synoptic Weather Forecasting",
                        style = MaterialTheme.typography.bodySmall,
                        color = AtmosphericBlue.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

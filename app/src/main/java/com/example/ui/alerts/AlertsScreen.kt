package com.example.ui.alerts

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AlertSeverity
import com.example.model.WeatherAlert
import com.example.ui.theme.AlertNormal
import com.example.ui.theme.AlertSevere
import com.example.ui.theme.AlertWarning
import com.example.ui.theme.AtmosphericBlue
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonAzure
import com.example.ui.theme.NightSky
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.viewmodel.WeatherViewModel

@Composable
fun AlertsScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val alerts by viewModel.alerts.collectAsState()
    val selectedFilter by viewModel.selectedAlertFilter.collectAsState()
    val unreadCount by viewModel.unreadAlertsCount.collectAsState()

    val filteredAlerts = if (selectedFilter == null) alerts else alerts.filter { it.severity == selectedFilter }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpace)
            .padding(horizontal = 16.dp)
            .padding(bottom = 96.dp)
            .testTag("alerts_screen")
    ) {
        // Top Header
        item {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AlertWarning.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = AlertWarning,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Weather Alerts",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Early Hazard Warning System",
                                style = MaterialTheme.typography.bodySmall,
                                color = AtmosphericBlue.copy(alpha = 0.7f)
                            )
                        }
                    }

                    if (unreadCount > 0) {
                        OutlinedButton(
                            onClick = { viewModel.markAlertsAsRead() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark Read", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Severity Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { viewModel.setAlertFilter(null) },
                    label = { Text("All (${alerts.size})") },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = NightSky,
                        labelColor = Color.White,
                        selectedContainerColor = ElectricCyan,
                        selectedLabelColor = DeepSpace
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = SurfaceBorderDark,
                        selectedBorderColor = ElectricCyan,
                        enabled = true,
                        selected = selectedFilter == null
                    )
                )

                FilterChip(
                    selected = selectedFilter == AlertSeverity.SEVERE,
                    onClick = { viewModel.setAlertFilter(if (selectedFilter == AlertSeverity.SEVERE) null else AlertSeverity.SEVERE) },
                    label = { Text("Severe") },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = NightSky,
                        labelColor = AlertSevere,
                        selectedContainerColor = AlertSevere,
                        selectedLabelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = SurfaceBorderDark,
                        selectedBorderColor = AlertSevere,
                        enabled = true,
                        selected = selectedFilter == AlertSeverity.SEVERE
                    )
                )

                FilterChip(
                    selected = selectedFilter == AlertSeverity.WARNING,
                    onClick = { viewModel.setAlertFilter(if (selectedFilter == AlertSeverity.WARNING) null else AlertSeverity.WARNING) },
                    label = { Text("Warning") },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = NightSky,
                        labelColor = AlertWarning,
                        selectedContainerColor = AlertWarning,
                        selectedLabelColor = DeepSpace
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = SurfaceBorderDark,
                        selectedBorderColor = AlertWarning,
                        enabled = true,
                        selected = selectedFilter == AlertSeverity.WARNING
                    )
                )

                FilterChip(
                    selected = selectedFilter == AlertSeverity.NORMAL,
                    onClick = { viewModel.setAlertFilter(if (selectedFilter == AlertSeverity.NORMAL) null else AlertSeverity.NORMAL) },
                    label = { Text("Advisory") },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = NightSky,
                        labelColor = AlertNormal,
                        selectedContainerColor = AlertNormal,
                        selectedLabelColor = DeepSpace
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = SurfaceBorderDark,
                        selectedBorderColor = AlertNormal,
                        enabled = true,
                        selected = selectedFilter == AlertSeverity.NORMAL
                    )
                )
            }
        }

        // Alert Items
        items(filteredAlerts) { alert ->
            WeatherAlertCard(alert = alert, modifier = Modifier.padding(bottom = 12.dp))
        }

        // Safety Tips Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardDark.copy(alpha = 0.85f)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorderDark)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Meteorological Preparedness Guidelines",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    SafetyTipRow("Severe Storms", "Unplug high-voltage devices, remain indoors away from windows.")
                    SafetyTipRow("Flash Flooding", "Never drive or walk across water-covered roads or subways.")
                    SafetyTipRow("High Gale Winds", "Fasten outdoor structures, watch for falling tree branches.")
                    SafetyTipRow("Extreme UV", "Apply broad-spectrum sunscreen every 2 hours and hydrate.")
                }
            }
        }
    }
}

@Composable
private fun WeatherAlertCard(
    alert: WeatherAlert,
    modifier: Modifier = Modifier
) {
    val (cardBorder, badgeBg, badgeText) = when (alert.severity) {
        AlertSeverity.SEVERE -> Triple(AlertSevere, AlertSevere.copy(alpha = 0.25f), AlertSevere)
        AlertSeverity.WARNING -> Triple(AlertWarning, AlertWarning.copy(alpha = 0.25f), AlertWarning)
        AlertSeverity.NORMAL -> Triple(AlertNormal, AlertNormal.copy(alpha = 0.25f), AlertNormal)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("alert_card_${alert.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceCardDark.copy(alpha = 0.9f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(cardBorder.copy(alpha = 0.6f))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = alert.severity.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeText,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        letterSpacing = 0.6.sp
                    )
                }

                Text(
                    text = "Expires: ${alert.expiresTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AtmosphericBlue.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = alert.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = alert.description,
                style = MaterialTheme.typography.bodyMedium,
                color = AtmosphericBlue,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Precautionary Recommendation box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = NightSky.copy(alpha = 0.8f),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = alert.precautionaryTip,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SafetyTipRow(title: String, advice: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = NeonAzure,
            modifier = Modifier.size(16.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = advice,
                style = MaterialTheme.typography.bodySmall,
                color = AtmosphericBlue.copy(alpha = 0.75f),
                fontSize = 11.sp
            )
        }
    }
}

package com.example.ui.forecast

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyForecast
import com.example.model.HourlyForecast
import com.example.ui.components.WeatherConditionGraphic
import com.example.ui.components.WeatherConditionSmallIcon
import com.example.ui.theme.AtmosphericBlue
import com.example.ui.theme.DeepOceanBlue
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonAzure
import com.example.ui.theme.NightSky
import com.example.ui.theme.RainCyan
import com.example.ui.theme.SunGold
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.viewmodel.WeatherViewModel

@Composable
fun ForecastScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val dailyForecast by viewModel.dailyForecast.collectAsState()
    val hourlyForecast by viewModel.hourlyForecast.collectAsState()
    val settings by viewModel.settings.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpace)
            .padding(bottom = 96.dp)
            .testTag("forecast_screen")
    ) {
        // Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = selectedLocation.fullDisplayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Meteorological Multi-Day Forecast",
                    style = MaterialTheme.typography.bodySmall,
                    color = AtmosphericBlue.copy(alpha = 0.7f)
                )
            }
        }

        // 24-Hour Horizon Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Next 24 Hours",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    hourlyForecast.forEach { item ->
                        DetailedHourlyCard(item = item, tempUnit = settings.tempUnit)
                    }
                }
            }
        }

        // 7-Day Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "7-Day Extended Forecast",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 7 Daily Forecast Cards
        items(dailyForecast) { day ->
            DayForecastDetailCard(
                day = day,
                tempUnit = settings.tempUnit,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun DetailedHourlyCard(
    item: HourlyForecast,
    tempUnit: com.example.model.TempUnit
) {
    val isCurrent = item.isNow
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) DeepOceanBlue.copy(alpha = 0.85f) else SurfaceCardDark.copy(alpha = 0.8f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isCurrent) ElectricCyan else SurfaceBorderDark
            )
        ),
        modifier = Modifier.width(90.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.timeLabel,
                style = MaterialTheme.typography.labelMedium,
                color = if (isCurrent) Color.White else AtmosphericBlue,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(10.dp))
            WeatherConditionGraphic(
                condition = item.condition,
                size = 36.dp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "${tempUnit.convert(item.tempC)}${tempUnit.symbol}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Grain,
                    contentDescription = null,
                    tint = RainCyan,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${item.rainProbability}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = RainCyan,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun DayForecastDetailCard(
    day: DailyForecast,
    tempUnit: com.example.model.TempUnit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("forecast_day_${day.dayName.lowercase()}"),
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
                Column {
                    Text(
                        text = day.dayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = day.dateFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = AtmosphericBlue.copy(alpha = 0.7f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WeatherConditionGraphic(
                        condition = day.condition,
                        size = 38.dp
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = day.condition.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ElectricCyan,
                            fontWeight = FontWeight.Medium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Grain,
                                contentDescription = null,
                                tint = RainCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${day.rainProbability}% Rain",
                                style = MaterialTheme.typography.labelSmall,
                                color = RainCyan
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Temperature Range Graphic Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Low: ${tempUnit.convert(day.minTempC)}${tempUnit.symbol}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AtmosphericBlue
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp)
                        .height(6.dp)
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
                    text = "High: ${tempUnit.convert(day.maxTempC)}${tempUnit.symbol}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = day.summary,
                style = MaterialTheme.typography.bodySmall,
                color = AtmosphericBlue.copy(alpha = 0.8f),
                fontSize = 12.sp
            )
        }
    }
}

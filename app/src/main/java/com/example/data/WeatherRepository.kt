package com.example.data

import com.example.model.AlertSeverity
import com.example.model.CurrentWeather
import com.example.model.DailyForecast
import com.example.model.GlobeMarker
import com.example.model.HourlyForecast
import com.example.model.LocationInfo
import com.example.model.WeatherAlert
import com.example.model.WeatherCondition
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class WeatherRepository {

    val defaultLocations: List<LocationInfo> = listOf(
        LocationInfo("sf", "San Francisco", "CA", "United States", "94102", 37.7749, -122.4194, -7),
        LocationInfo("nyc", "New York", "NY", "United States", "10001", 40.7128, -74.0060, -4),
        LocationInfo("lon", "London", "", "United Kingdom", "EC1A", 51.5074, -0.1278, 1),
        LocationInfo("par", "Paris", "", "France", "75001", 48.8566, 2.3522, 2),
        LocationInfo("tok", "Tokyo", "", "Japan", "100-0001", 35.6762, 139.6503, 9),
        LocationInfo("syd", "Sydney", "NSW", "Australia", "2000", -33.8688, 151.2093, 10),
        LocationInfo("dxb", "Dubai", "", "United Arab Emirates", "00000", 25.2048, 55.2708, 4),
        LocationInfo("sin", "Singapore", "", "Singapore", "018989", 1.3521, 103.8198, 8),
        LocationInfo("ber", "Berlin", "", "Germany", "10115", 52.5200, 13.4050, 2),
        LocationInfo("cai", "Cairo", "", "Egypt", "11511", 30.0444, 31.2357, 3),
        LocationInfo("rio", "Rio de Janeiro", "", "Brazil", "20000", -22.9068, -43.1729, -3),
        LocationInfo("mum", "Mumbai", "MH", "India", "400001", 19.0760, 72.8777, 5),
        LocationInfo("tor", "Toronto", "ON", "Canada", "M5H", 43.6532, -79.3832, -4),
        LocationInfo("rey", "Reykjavik", "", "Iceland", "101", 64.1466, -21.9426, 0),
        LocationInfo("cpt", "Cape Town", "", "South Africa", "8001", -33.9249, 18.4241, 2),
        LocationInfo("sel", "Seoul", "", "South Korea", "03000", 37.5665, 126.9780, 9),
        LocationInfo("zrh", "Zurich", "", "Switzerland", "8001", 47.3769, 8.5417, 2),
        LocationInfo("hkg", "Hong Kong", "", "China", "999077", 22.3193, 114.1694, 8),
        LocationInfo("bue", "Buenos Aires", "", "Argentina", "C1000", -34.6037, -58.3816, -3),
        LocationInfo("hnl", "Honolulu", "HI", "United States", "96815", 21.3069, -157.8583, -10)
    )

    fun searchLocations(query: String): List<LocationInfo> {
        val trimmed = query.trim().lowercase(Locale.ROOT)
        if (trimmed.isEmpty()) return defaultLocations.take(6)
        return defaultLocations.filter { loc ->
            loc.name.lowercase(Locale.ROOT).contains(trimmed) ||
            loc.state.lowercase(Locale.ROOT).contains(trimmed) ||
            loc.country.lowercase(Locale.ROOT).contains(trimmed) ||
            loc.zipCode.lowercase(Locale.ROOT).startsWith(trimmed)
        }
    }

    fun findNearestLocation(lat: Double, lon: Double): LocationInfo {
        return defaultLocations.minByOrNull { loc ->
            val dLat = loc.latitude - lat
            val dLon = loc.longitude - lon
            dLat * dLat + dLon * dLon
        } ?: defaultLocations.first()
    }

    fun getCurrentWeather(location: LocationInfo): CurrentWeather {
        val seed = (abs(location.latitude) * 100 + abs(location.longitude) * 10).toInt()
        val isNorthern = location.latitude > 0
        
        // Temperature based on latitude and baseline seasonal curve
        val baseTemp = if (abs(location.latitude) > 60) 4
        else if (abs(location.latitude) > 45) 16
        else if (abs(location.latitude) > 25) 23
        else 30

        val tempVariance = (seed % 9) - 4
        val tempC = (baseTemp + tempVariance).coerceIn(-15, 45)
        val feelsLikeC = tempC + if (tempC > 25) 3 else -2

        val condition = when ((seed % 10)) {
            0, 1 -> WeatherCondition.SUNNY
            2, 3 -> WeatherCondition.PARTLY_CLOUDY
            4 -> WeatherCondition.CLOUDY
            5 -> WeatherCondition.RAINY
            6 -> WeatherCondition.HEAVY_RAIN
            7 -> WeatherCondition.THUNDERSTORM
            8 -> if (tempC <= 2) WeatherCondition.SNOW else WeatherCondition.MIST
            else -> WeatherCondition.WINDY
        }

        val humidity = 45 + (seed % 45)
        val windSpeed = 8 + (seed % 32)
        val windDeg = ((seed * 37) % 360).toFloat()
        val windDir = getDirectionName(windDeg)
        val visibility = if (condition == WeatherCondition.FOG || condition == WeatherCondition.MIST) 3.5 else 10.0
        val uv = if (condition == WeatherCondition.SUNNY) (seed % 6) + 4 else (seed % 4) + 1
        val pressure = 1008 + (seed % 18)

        return CurrentWeather(
            location = location,
            tempC = tempC,
            condition = condition,
            feelsLikeC = feelsLikeC,
            humidityPercent = humidity,
            windSpeedKmh = windSpeed,
            windDirection = windDir,
            windDirectionDegrees = windDeg,
            visibilityKm = visibility,
            uvIndex = uv,
            pressureHpa = pressure,
            sunrise = "06:14 AM",
            sunset = "07:48 PM",
            lastUpdated = "Just now"
        )
    }

    fun getHourlyForecast(location: LocationInfo, currentTempC: Int, currentCondition: WeatherCondition): List<HourlyForecast> {
        val hours = mutableListOf<HourlyForecast>()
        val hoursOfDay = listOf(
            "Now", "1 AM", "2 AM", "3 AM", "4 AM", "5 AM", "6 AM", "7 AM", "8 AM", "9 AM",
            "10 AM", "11 AM", "12 PM", "1 PM", "2 PM", "3 PM", "4 PM", "5 PM", "6 PM", "7 PM",
            "8 PM", "9 PM", "10 PM", "11 PM"
        )

        hours.add(HourlyForecast("Now", currentTempC, currentCondition, 15, isNow = true))

        for (i in 1 until 24) {
            val hourLabel = "${(i % 12).let { if (it == 0) 12 else it }} ${if (i < 12) "AM" else "PM"}"
            // Slight diurnal temperature fluctuation curve
            val tempOffset = (sin((i.toDouble() - 6.0) / 24.0 * Math.PI * 2) * 5).roundToInt()
            val temp = currentTempC + tempOffset
            val rainProb = ((sin(i.toDouble() * 0.8) + 1.0) * 35.0).toInt().coerceIn(0, 95)
            val cond = if (rainProb > 65) WeatherCondition.RAINY
                       else if (rainProb > 40) WeatherCondition.CLOUDY
                       else if (i in 6..19) WeatherCondition.PARTLY_CLOUDY
                       else WeatherCondition.CLEAR_NIGHT

            hours.add(HourlyForecast(hourLabel, temp, cond, rainProb, isNow = false))
        }

        return hours
    }

    fun get7DayForecast(location: LocationInfo, currentTempC: Int): List<DailyForecast> {
        val days = listOf("Today", "Tomorrow", "Thursday", "Friday", "Saturday", "Sunday", "Monday")
        val dates = listOf("Sep 17", "Sep 18", "Sep 19", "Sep 20", "Sep 21", "Sep 22", "Sep 23")
        val conditions = listOf(
            WeatherCondition.PARTLY_CLOUDY,
            WeatherCondition.SUNNY,
            WeatherCondition.THUNDERSTORM,
            WeatherCondition.RAINY,
            WeatherCondition.CLOUDY,
            WeatherCondition.SUNNY,
            WeatherCondition.PARTLY_CLOUDY
        )

        return days.indices.map { i ->
            val maxT = currentTempC + (i % 3) + 2
            val minT = currentTempC - (i % 2) - 4
            val rainP = when (conditions[i]) {
                WeatherCondition.THUNDERSTORM -> 85
                WeatherCondition.RAINY -> 70
                WeatherCondition.CLOUDY -> 35
                WeatherCondition.PARTLY_CLOUDY -> 20
                else -> 5
            }
            val summary = when (conditions[i]) {
                WeatherCondition.SUNNY -> "Bright & clear skies throughout the day"
                WeatherCondition.THUNDERSTORM -> "Scattered electrical storms in afternoon"
                WeatherCondition.RAINY -> "Persistent rainfall with gentle breezes"
                WeatherCondition.CLOUDY -> "Overcast clouds with moderate humidity"
                else -> "Mild conditions with pleasant solar intervals"
            }
            DailyForecast(
                dayName = days[i],
                dateFormatted = dates[i],
                condition = conditions[i],
                maxTempC = maxT,
                minTempC = minT,
                rainProbability = rainP,
                summary = summary
            )
        }
    }

    fun getAlerts(location: LocationInfo, condition: WeatherCondition): List<WeatherAlert> {
        val alerts = mutableListOf<WeatherAlert>()
        
        when (condition) {
            WeatherCondition.THUNDERSTORM -> {
                alerts.add(
                    WeatherAlert(
                        id = "alert_storm_${location.id}",
                        title = "Severe Thunderstorm Warning",
                        description = "Atmospheric Doppler radar indicates severe convective cells with frequent cloud-to-ground lightning and potential wind gusts up to 85 km/h.",
                        severity = AlertSeverity.SEVERE,
                        issuedTime = "Today, 14:15",
                        expiresTime = "Today, 20:00",
                        precautionaryTip = "Seek sturdy indoor shelter immediately. Avoid electrical appliances and stay clear of ungrounded metal structures."
                    )
                )
            }
            WeatherCondition.HEAVY_RAIN, WeatherCondition.RAINY -> {
                alerts.add(
                    WeatherAlert(
                        id = "alert_rain_${location.id}",
                        title = "Heavy Rain & Flash Flood Watch",
                        description = "Precipitation accumulation expected between 40-70mm over the next 18 hours. Low-lying drainage channels may experience rapid localized flooding.",
                        severity = AlertSeverity.WARNING,
                        issuedTime = "Today, 08:00",
                        expiresTime = "Tomorrow, 06:00",
                        precautionaryTip = "Exercise caution while driving. Do not attempt to cross flooded roadways or waterlogged pedestrian underpasses."
                    )
                )
            }
            WeatherCondition.WINDY -> {
                alerts.add(
                    WeatherAlert(
                        id = "alert_wind_${location.id}",
                        title = "High Wind Advisory",
                        description = "Sustained gale winds of 45-60 km/h with peak gusts reaching 80 km/h due to steep regional barometric pressure gradient.",
                        severity = AlertSeverity.WARNING,
                        issuedTime = "Today, 10:30",
                        expiresTime = "Tonight, 23:00",
                        precautionaryTip = "Secure loose outdoor furniture, tarpaulins, and lightweight items. High-profile vehicles should use caution on bridges."
                    )
                )
            }
            WeatherCondition.SUNNY -> {
                alerts.add(
                    WeatherAlert(
                        id = "alert_uv_${location.id}",
                        title = "High UV Index Warning (Level 9+)",
                        description = "Very high solar ultraviolet radiation levels during midday peak window (11:00 AM - 03:30 PM). Unprotected skin can burn rapidly in under 15 minutes.",
                        severity = AlertSeverity.NORMAL,
                        issuedTime = "Today, 09:00",
                        expiresTime = "Today, 17:00",
                        precautionaryTip = "Apply SPF 50+ broad-spectrum sunscreen liberally, wear UV400 protective sunglasses, and seek shade during solar zenith."
                    )
                )
            }
            else -> {
                alerts.add(
                    WeatherAlert(
                        id = "alert_general_${location.id}",
                        title = "Atmospheric Air Quality & Thermal Advisory",
                        description = "Nominal barometric stability with moderate maritime humidity. Ideal window for outdoor activities and recreational travel.",
                        severity = AlertSeverity.NORMAL,
                        issuedTime = "Today, 06:00",
                        expiresTime = "Tomorrow, 00:00",
                        precautionaryTip = "Stay hydrated and monitor standard seasonal forecast updates."
                    )
                )
            }
        }

        // Add a global forecast alert
        alerts.add(
            WeatherAlert(
                id = "alert_extended_${location.id}",
                title = "Upcoming Cold Front Convergence",
                description = "Synoptic analysis predicts an approaching baroclinic trough moving southeastward across the territorial sector within 48-72 hours.",
                severity = AlertSeverity.NORMAL,
                issuedTime = "Yesterday, 18:00",
                expiresTime = "In 3 days",
                precautionaryTip = "Expect temperature drops of 5-8°C with gusty boundary layer winds."
            )
        )

        return alerts
    }

    fun getAllGlobeMarkers(): List<GlobeMarker> {
        return defaultLocations.map { loc ->
            val weather = getCurrentWeather(loc)
            GlobeMarker(loc, weather.tempC, weather.condition)
        }
    }

    private fun getDirectionName(deg: Float): String {
        val normalized = ((deg % 360) + 360) % 360
        return when {
            normalized in 22.5f..67.5f -> "NE"
            normalized in 67.5f..112.5f -> "E"
            normalized in 112.5f..157.5f -> "SE"
            normalized in 157.5f..202.5f -> "S"
            normalized in 202.5f..247.5f -> "SW"
            normalized in 247.5f..292.5f -> "W"
            normalized in 292.5f..337.5f -> "NW"
            else -> "N"
        }
    }
}

package com.example.model

enum class WeatherCondition(val displayName: String) {
    SUNNY("Sunny"),
    CLEAR_NIGHT("Clear Night"),
    PARTLY_CLOUDY("Partly Cloudy"),
    CLOUDY("Overcast"),
    RAINY("Rain"),
    HEAVY_RAIN("Heavy Rain"),
    THUNDERSTORM("Thunderstorm"),
    SNOW("Snow"),
    MIST("Mist"),
    FOG("Dense Fog"),
    WINDY("High Winds")
}

data class LocationInfo(
    val id: String,
    val name: String,
    val state: String = "",
    val country: String,
    val zipCode: String = "",
    val latitude: Double,
    val longitude: Double,
    val timezoneOffsetHours: Int = 0
) {
    val fullDisplayName: String
        get() = if (state.isNotBlank()) "$name, $state, $country" else "$name, $country"
}

data class CurrentWeather(
    val location: LocationInfo,
    val tempC: Int,
    val condition: WeatherCondition,
    val feelsLikeC: Int,
    val humidityPercent: Int,
    val windSpeedKmh: Int,
    val windDirection: String, // e.g. "NE 45°", "SSW"
    val windDirectionDegrees: Float,
    val visibilityKm: Double,
    val uvIndex: Int,
    val pressureHpa: Int,
    val sunrise: String,
    val sunset: String,
    val lastUpdated: String
)

data class HourlyForecast(
    val timeLabel: String,
    val tempC: Int,
    val condition: WeatherCondition,
    val rainProbability: Int,
    val isNow: Boolean = false
)

data class DailyForecast(
    val dayName: String,
    val dateFormatted: String,
    val condition: WeatherCondition,
    val maxTempC: Int,
    val minTempC: Int,
    val rainProbability: Int,
    val summary: String
)

enum class AlertSeverity(val label: String) {
    NORMAL("Advisory"),
    WARNING("Watch / Warning"),
    SEVERE("Severe Emergency")
}

data class WeatherAlert(
    val id: String,
    val title: String,
    val description: String,
    val severity: AlertSeverity,
    val issuedTime: String,
    val expiresTime: String,
    val precautionaryTip: String,
    var isRead: Boolean = false
)

data class GlobeMarker(
    val location: LocationInfo,
    val tempC: Int,
    val condition: WeatherCondition
)

enum class WeatherLayerType(val label: String, val iconDesc: String) {
    TEMPERATURE("Temperature", "Isotherm heat spectrum"),
    RAIN("Rain Radar", "Real-time precipitation radar"),
    WIND("Wind Flow", "Atmospheric jet streamlines"),
    CLOUDS("Cloud Cover", "Satellite infrared cloud formations"),
    STORM("Storm Cells", "Convective lightning activity"),
    SATELLITE("Satellite Earth", "Day-night solar terminator view")
}

enum class TempUnit(val symbol: String) {
    CELSIUS("°C"),
    FAHRENHEIT("°F");

    fun convert(tempC: Int): Int = when (this) {
        CELSIUS -> tempC
        FAHRENHEIT -> ((tempC * 9.0 / 5.0) + 32).toInt()
    }
}

enum class WindUnit(val symbol: String) {
    KMH("km/h"),
    MPH("mph"),
    MS("m/s");

    fun convert(speedKmh: Int): String = when (this) {
        KMH -> "$speedKmh km/h"
        MPH -> "${(speedKmh * 0.621371).toInt()} mph"
        MS -> "${(speedKmh / 3.6).toInt()} m/s"
    }
}

enum class PressureUnit(val symbol: String) {
    HPA("hPa"),
    INHG("inHg");

    fun convert(pressureHpa: Int): String = when (this) {
        HPA -> "$pressureHpa hPa"
        INHG -> String.format("%.2f inHg", pressureHpa * 0.02953)
    }
}

data class AppSettings(
    val tempUnit: TempUnit = TempUnit.CELSIUS,
    val windUnit: WindUnit = WindUnit.KMH,
    val pressureUnit: PressureUnit = PressureUnit.HPA,
    val autoRotateGlobe: Boolean = true,
    val autoRotateSpeed: Float = 0.4f,
    val darkTheme: Boolean = true,
    val notificationsEnabled: Boolean = true
)

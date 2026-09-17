package com.example.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.WeatherRepository
import com.example.model.AlertSeverity
import com.example.model.AppSettings
import com.example.model.CurrentWeather
import com.example.model.DailyForecast
import com.example.model.GlobeMarker
import com.example.model.HourlyForecast
import com.example.model.LocationInfo
import com.example.model.TempUnit
import com.example.model.WeatherAlert
import com.example.model.WeatherLayerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppScreen(val title: String) {
    DASHBOARD("Dashboard"),
    MAP_3D("3D Weather Map"),
    FORECAST("Forecast"),
    ALERTS("Alerts"),
    SETTINGS("Settings")
}

class WeatherViewModel(
    private val repository: WeatherRepository = WeatherRepository()
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedLocation = MutableStateFlow(repository.defaultLocations.first())
    val selectedLocation: StateFlow<LocationInfo> = _selectedLocation.asStateFlow()

    private val _currentWeather = MutableStateFlow(repository.getCurrentWeather(_selectedLocation.value))
    val currentWeather: StateFlow<CurrentWeather> = _currentWeather.asStateFlow()

    private val _hourlyForecast = MutableStateFlow(
        repository.getHourlyForecast(_selectedLocation.value, _currentWeather.value.tempC, _currentWeather.value.condition)
    )
    val hourlyForecast: StateFlow<List<HourlyForecast>> = _hourlyForecast.asStateFlow()

    private val _dailyForecast = MutableStateFlow(
        repository.get7DayForecast(_selectedLocation.value, _currentWeather.value.tempC)
    )
    val dailyForecast: StateFlow<List<DailyForecast>> = _dailyForecast.asStateFlow()

    private val _alerts = MutableStateFlow(
        repository.getAlerts(_selectedLocation.value, _currentWeather.value.condition)
    )
    val alerts: StateFlow<List<WeatherAlert>> = _alerts.asStateFlow()

    private val _unreadAlertsCount = MutableStateFlow(2)
    val unreadAlertsCount: StateFlow<Int> = _unreadAlertsCount.asStateFlow()

    private val _globeMarkers = MutableStateFlow(repository.getAllGlobeMarkers())
    val globeMarkers: StateFlow<List<GlobeMarker>> = _globeMarkers.asStateFlow()

    private val _activeLayer = MutableStateFlow(WeatherLayerType.TEMPERATURE)
    val activeLayer: StateFlow<WeatherLayerType> = _activeLayer.asStateFlow()

    private val _selectedMarker = MutableStateFlow<GlobeMarker?>(null)
    val selectedMarker: StateFlow<GlobeMarker?> = _selectedMarker.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow(repository.defaultLocations.take(5))
    val searchResults: StateFlow<List<LocationInfo>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _selectedAlertFilter = MutableStateFlow<AlertSeverity?>(null)
    val selectedAlertFilter: StateFlow<AlertSeverity?> = _selectedAlertFilter.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectLocation(location: LocationInfo) {
        _selectedLocation.value = location
        _isSearching.value = false
        _searchQuery.value = ""
        refreshWeatherDataForLocation(location)
    }

    private fun refreshWeatherDataForLocation(location: LocationInfo) {
        val weather = repository.getCurrentWeather(location)
        _currentWeather.value = weather
        _hourlyForecast.value = repository.getHourlyForecast(location, weather.tempC, weather.condition)
        _dailyForecast.value = repository.get7DayForecast(location, weather.tempC)
        val alertList = repository.getAlerts(location, weather.condition)
        _alerts.value = alertList
        _unreadAlertsCount.value = alertList.count { !it.isRead }
        _selectedMarker.value = _globeMarkers.value.find { it.location.id == location.id }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _searchResults.value = repository.searchLocations(query)
    }

    fun setSearching(isSearching: Boolean) {
        _isSearching.value = isSearching
        if (isSearching && _searchQuery.value.isEmpty()) {
            _searchResults.value = repository.defaultLocations.take(5)
        }
    }

    fun setActiveLayer(layer: WeatherLayerType) {
        _activeLayer.value = layer
    }

    fun selectGlobeMarker(marker: GlobeMarker?) {
        _selectedMarker.value = marker
    }

    fun applyMarkerAsPrimaryLocation(marker: GlobeMarker) {
        selectLocation(marker.location)
    }

    fun toggleTempUnit() {
        _settings.update {
            it.copy(tempUnit = if (it.tempUnit == TempUnit.CELSIUS) TempUnit.FAHRENHEIT else TempUnit.CELSIUS)
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
    }

    fun markAlertsAsRead() {
        _alerts.update { list ->
            list.map { it.copy(isRead = true) }
        }
        _unreadAlertsCount.value = 0
    }

    fun setAlertFilter(severity: AlertSeverity?) {
        _selectedAlertFilter.value = severity
    }

    fun refreshWeather() {
        refreshWeatherDataForLocation(_selectedLocation.value)
    }

    @SuppressLint("MissingPermission")
    fun useMyLocation(context: Context) {
        viewModelScope.launch {
            try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val gpsLoc: Location? = try {
                    lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                } catch (e: Exception) {
                    null
                }
                val netLoc: Location? = try {
                    lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                } catch (e: Exception) {
                    null
                }

                val bestLoc = gpsLoc ?: netLoc
                if (bestLoc != null) {
                    val nearest = repository.findNearestLocation(bestLoc.latitude, bestLoc.longitude)
                    selectLocation(nearest)
                } else {
                    // Fallback to first location
                    selectLocation(repository.defaultLocations.first())
                }
            } catch (e: Exception) {
                // Graceful fallback
                selectLocation(repository.defaultLocations.first())
            }
        }
    }
}

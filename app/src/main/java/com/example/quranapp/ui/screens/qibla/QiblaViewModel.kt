package com.example.quranapp.ui.screens.qibla

import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quranapp.data.sensor.QiblaSensorManager
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class QiblaUiState(
    val qiblaBearing: Double = 0.0,
    val currentHeading: Float = 0f,
    val hasLocationPermission: Boolean = false
)

class QiblaViewModel(application: Application) : AndroidViewModel(application) {
    
    private val sensorManager = QiblaSensorManager(application)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private val _uiState = MutableStateFlow(QiblaUiState())
    val uiState: StateFlow<QiblaUiState> = _uiState.asStateFlow()

    init {
        // Start listening to compass
        viewModelScope.launch {
            sensorManager.getCompassOrientation().collect { azimuth ->
                _uiState.value = _uiState.value.copy(currentHeading = azimuth)
            }
        }
        checkPermissionAndGetLocation()
    }

    fun checkPermissionAndGetLocation() {
        val hasPermission = ActivityCompat.checkSelfPermission(
            getApplication(),
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            getApplication(),
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        _uiState.value = _uiState.value.copy(hasLocationPermission = hasPermission)

        if (hasPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val bearing = QiblaUtils.calculateQiblaDirection(location.latitude, location.longitude)
                    _uiState.value = _uiState.value.copy(qiblaBearing = bearing)
                }
            }
        }
    }
}


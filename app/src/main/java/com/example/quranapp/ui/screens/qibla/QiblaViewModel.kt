package com.example.quranapp.ui.screens.qibla

import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QiblaViewModel(application: Application) : AndroidViewModel(application) {
    
    private val sensorManager = QiblaSensorManager(application)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    // Qibla Direction (Bearing from North)
    private val _qiblaBearing = MutableStateFlow(0.0)
    val qiblaBearing: StateFlow<Double> = _qiblaBearing.asStateFlow()

    // Current Compass Heading (Azimuth)
    private val _currentHeading = MutableStateFlow(0f)
    val currentHeading: StateFlow<Float> = _currentHeading.asStateFlow()

    // Location Permission Status
    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    init {
        // Start listening to compass
        viewModelScope.launch {
            sensorManager.getCompassOrientation().collect { azimuth ->
                _currentHeading.value = azimuth
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

        _hasLocationPermission.value = hasPermission

        if (hasPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val bearing = QiblaUtils.calculateQiblaDirection(location.latitude, location.longitude)
                    _qiblaBearing.value = bearing
                }
            }
        }
    }
}

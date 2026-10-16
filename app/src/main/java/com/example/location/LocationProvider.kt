package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

data class Coordinates(
    val latitude: Double,
    val longitude: Double,
    val provider: String = "gps"
)

sealed class LocationResultState {
    data class Success(val coordinates: Coordinates) : LocationResultState()
    sealed class Failure(val userMessage: String) : LocationResultState() {
        object PermissionDenied : Failure(
            "Location permission was denied. Please grant permission in Android settings to get weather for your current position."
        )
        object LocationServicesDisabled : Failure(
            "Location Services (GPS) are turned off. Please enable Location in your device settings."
        )
        object Timeout : Failure(
            "Location request timed out. Please check your GPS signal and try again."
        )
        data class Unavailable(val detail: String = "Unable to determine current GPS coordinates.") : Failure(detail)
    }
}

class LocationProvider(private val context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    /**
     * Checks if device location services (GPS or Network) are physically turned on.
     */
    fun isLocationServiceEnabled(): Boolean {
        val lm = locationManager ?: return false
        return try {
            LocationManagerCompat.isLocationEnabled(lm)
        } catch (_: Exception) {
            val gps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val net = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            gps || net
        }
    }

    /**
     * Resilient high-accuracy GPS location retriever matching the requirements:
     * - Uses enableHighAccuracy (Priority.PRIORITY_HIGH_ACCURACY)
     * - Configures reasonable timeout (5000ms)
     * - Checks LocationManager provider states
     * - Falls back to lastLocation & network providers when satellites are indoor
     */
    @SuppressLint("MissingPermission")
    suspend fun getAccurateCurrentCoordinates(): LocationResultState = withContext(Dispatchers.IO) {
        // 1. Check if device location services are enabled
        if (!isLocationServiceEnabled()) {
            return@withContext LocationResultState.Failure.LocationServicesDisabled
        }

        // 2. High Accuracy Current Location with CancellationTokenSource (Timeout: 5.5s)
        try {
            val highAccuracyLocation = withTimeoutOrNull(5500) {
                suspendCancellableCoroutine<Location?> { continuation ->
                    val cts = CancellationTokenSource()
                    fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                        .addOnSuccessListener { loc ->
                            if (continuation.isActive) continuation.resume(loc)
                        }
                        .addOnFailureListener {
                            if (continuation.isActive) continuation.resume(null)
                        }
                        .addOnCanceledListener {
                            if (continuation.isActive) continuation.resume(null)
                        }

                    continuation.invokeOnCancellation {
                        cts.cancel()
                    }
                }
            }

            if (highAccuracyLocation != null && isValidLocation(highAccuracyLocation)) {
                return@withContext LocationResultState.Success(
                    Coordinates(highAccuracyLocation.latitude, highAccuracyLocation.longitude, "gps_high_accuracy")
                )
            }
        } catch (_: Exception) { }

        // 3. Balanced Power Accuracy Current Location (e.g. WiFi / Cell tower fallback)
        try {
            val balancedLocation = withTimeoutOrNull(3500) {
                suspendCancellableCoroutine<Location?> { continuation ->
                    val cts = CancellationTokenSource()
                    fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                        .addOnSuccessListener { loc ->
                            if (continuation.isActive) continuation.resume(loc)
                        }
                        .addOnFailureListener {
                            if (continuation.isActive) continuation.resume(null)
                        }
                    continuation.invokeOnCancellation {
                        cts.cancel()
                    }
                }
            }

            if (balancedLocation != null && isValidLocation(balancedLocation)) {
                return@withContext LocationResultState.Success(
                    Coordinates(balancedLocation.latitude, balancedLocation.longitude, "gps_balanced")
                )
            }
        } catch (_: Exception) { }

        // 4. Try FusedLocationProviderClient last known location (cached within reasonable maximumAge)
        try {
            val lastLoc: Location? = suspendCancellableCoroutine { continuation ->
                fusedClient.lastLocation
                    .addOnSuccessListener { loc ->
                        if (continuation.isActive) continuation.resume(loc)
                    }
                    .addOnFailureListener {
                        if (continuation.isActive) continuation.resume(null)
                    }
            }

            if (lastLoc != null && isValidLocation(lastLoc)) {
                return@withContext LocationResultState.Success(
                    Coordinates(lastLoc.latitude, lastLoc.longitude, "gps_last_known")
                )
            }
        } catch (_: Exception) { }

        // 5. Try Android LocationManager providers (GPS, Network, Passive)
        try {
            if (locationManager != null) {
                val providers = listOf(
                    LocationManager.GPS_PROVIDER,
                    LocationManager.NETWORK_PROVIDER,
                    LocationManager.PASSIVE_PROVIDER
                )

                for (provider in providers) {
                    if (locationManager.isProviderEnabled(provider)) {
                        val loc = locationManager.getLastKnownLocation(provider)
                        if (loc != null && isValidLocation(loc)) {
                            return@withContext LocationResultState.Success(
                                Coordinates(loc.latitude, loc.longitude, provider)
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) { }

        // 6. Actively request a single fresh location update (3.5s timeout)
        try {
            val freshLoc = withTimeoutOrNull(3500) {
                suspendCancellableCoroutine<Location?> { continuation ->
                    val locationRequest = LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        1000
                    ).setMaxUpdates(1).build()

                    val callback = object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            fusedClient.removeLocationUpdates(this)
                            if (continuation.isActive) {
                                continuation.resume(result.lastLocation)
                            }
                        }
                    }

                    fusedClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())

                    continuation.invokeOnCancellation {
                        fusedClient.removeLocationUpdates(callback)
                    }
                }
            }

            if (freshLoc != null && isValidLocation(freshLoc)) {
                return@withContext LocationResultState.Success(
                    Coordinates(freshLoc.latitude, freshLoc.longitude, "gps_active_update")
                )
            }
        } catch (_: Exception) { }

        return@withContext LocationResultState.Failure.Unavailable(
            "GPS location is currently unavailable. Please verify GPS is enabled and try again."
        )
    }

    private fun isValidLocation(location: Location): Boolean {
        return location.latitude != 0.0 || location.longitude != 0.0
    }
}

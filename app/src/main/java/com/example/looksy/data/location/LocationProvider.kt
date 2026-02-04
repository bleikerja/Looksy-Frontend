package com.example.looksy.data.location

import android.content.Context
import android.Manifest
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Provides location permission checks and current location.
 * Full location retrieval requires FusedLocationProviderClient (Android framework).
 */
class LocationProvider(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    suspend fun getCurrentLocation(): Result<Location> {
        if (!hasLocationPermission()) {
            return Result.failure(SecurityException("Location permission not granted"))
        }
        return suspendCancellableCoroutine { cont ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    cont.resume(
                        if (location != null) Result.success(location)
                        else Result.failure(NoSuchElementException("Location unavailable"))
                    )
                }
                .addOnFailureListener { e ->
                    cont.resume(Result.failure(e))
                }
        }
    }
}

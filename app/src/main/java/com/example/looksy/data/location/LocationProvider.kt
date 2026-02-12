package com.example.looksy.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Simple data class to hold latitude and longitude
 */
data class Location(
    val latitude: Double,
    val longitude: Double
)

/**
 * Handles location retrieval using Google Play Services Location API
 *
 * Usage:
 * ```
 * val locationProvider = LocationProvider(context)
 * if (locationProvider.hasLocationPermission()) {
 *     val result = locationProvider.getCurrentLocation()
 *     result.onSuccess { location ->
 *         // Use location.latitude and location.longitude
 *     }
 * }
 * ```
 */
class LocationProvider(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Checks if the app has location permission
     * @return true if ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION is granted
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Gets the current device location
     * @return Result<Location> - Success with Location or Failure with exception
     *
     * Possible failures:
     * - SecurityException: Location permission not granted
     * - Exception: Location is null (GPS disabled, no last known location)
     * - Exception: Any other location service error
     */
    suspend fun getCurrentLocation(): Result<Location> = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission()) {
            continuation.resume(
                Result.failure(SecurityException("Location permission not granted"))
            )
            return@suspendCancellableCoroutine
        }

        val cancellationTokenSource = CancellationTokenSource()

        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    continuation.resume(
                        Result.success(
                            Location(
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        )
                    )
                } else {
                    continuation.resume(
                        Result.failure(Exception("Location is null - GPS might be disabled"))
                    )
                }
            }.addOnFailureListener { exception ->
                continuation.resume(Result.failure(exception))
            }
        } catch (e: SecurityException) {
            continuation.resume(Result.failure(e))
        }

        continuation.invokeOnCancellation {
            cancellationTokenSource.cancel()
        }
    }
}
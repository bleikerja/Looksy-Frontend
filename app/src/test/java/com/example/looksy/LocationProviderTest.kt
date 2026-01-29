package com.example.looksy

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.looksy.data.location.LocationProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for LocationProvider
 * 
 * Tests the LocationProvider's ability to:
 * - Check location permissions correctly
 * - Handle granted and denied permission states
 * - Handle permission denied scenario in getCurrentLocation()
 * 
 * Note: Full getCurrentLocation() testing with real location data requires 
 * instrumented tests because it depends on Android framework classes (FusedLocationProviderClient)
 */
class LocationProviderTest {

    private lateinit var context: Context
    private lateinit var locationProvider: LocationProvider

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        mockkStatic(ContextCompat::class)
        locationProvider = LocationProvider(context)
    }

    @Test
    fun `hasLocationPermission() returns true when COARSE_LOCATION granted`() {
        // Given
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        } returns PackageManager.PERMISSION_GRANTED
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        } returns PackageManager.PERMISSION_DENIED

        // When
        val result = locationProvider.hasLocationPermission()

        // Then
        assertTrue("Should return true when COARSE_LOCATION is granted", result)
    }

    @Test
    fun `hasLocationPermission() returns false when both permissions denied`() {
        // Given
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        } returns PackageManager.PERMISSION_DENIED
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        } returns PackageManager.PERMISSION_DENIED

        // When
        val result = locationProvider.hasLocationPermission()

        // Then
        assertFalse("Should return false when both permissions are denied", result)
    }

    @Test
    fun `getCurrentLocation() returns SecurityException when permission not granted`() = runTest {
        // Given - Both permissions denied
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        } returns PackageManager.PERMISSION_DENIED
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        } returns PackageManager.PERMISSION_DENIED

        // When
        val result = locationProvider.getCurrentLocation()

        // Then
        assertTrue("Result should be failure", result.isFailure)
        assertTrue(
            "Exception should be SecurityException but was ${result.exceptionOrNull()?.javaClass?.simpleName}",
            result.exceptionOrNull() is SecurityException
        )
        assertTrue(
            "Exception message should mention permission",
            result.exceptionOrNull()?.message?.contains("Location permission not granted") == true
        )
    }
}

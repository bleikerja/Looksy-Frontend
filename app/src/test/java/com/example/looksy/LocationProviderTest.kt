package com.example.looksy

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.looksy.data.location.LocationProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for LocationProvider
 * 
 * Tests the LocationProvider's ability to:
 * - Check location permissions correctly
 * - Handle granted and denied permission states
 * 
 * Note: Testing getCurrentLocation() requires instrumented tests
 * because it depends on real Android framework classes (FusedLocationProviderClient)
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
}

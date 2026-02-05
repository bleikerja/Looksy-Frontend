package com.example.looksy

import android.Manifest
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.example.looksy.data.location.LocationProvider
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertTrue

/**
 * Instrumented tests for LocationProvider.
 * Tests real Android location services with granted permissions.
 * 
 * Note: Tests are device-dependent and may pass/fail based on:
 * - GPS availability and status
 * - Location provider availability  
 * - Device airplane mode
 */
@RunWith(AndroidJUnit4::class)
class LocationProviderInstrumentedTest {

   @get:Rule
   val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
       Manifest.permission.ACCESS_COARSE_LOCATION,
       Manifest.permission.ACCESS_FINE_LOCATION
   )

   private lateinit var context: Context
   private lateinit var locationProvider: LocationProvider

   @Before
   fun setup() {
       context = InstrumentationRegistry.getInstrumentation().targetContext
       locationProvider = LocationProvider(context)
   }

    @Test
    fun locationProvider_hasPermission_whenGranted() {
        // Given: Permissions granted by GrantPermissionRule
        
        // When: Check permission status
        val hasPermission = locationProvider.hasLocationPermission()

        // Then: Should return true
        assertTrue(hasPermission)
    }

    @Test
    fun locationProvider_returnsValidLocation_orHandlesError() = runTest {
        // Given: Permission granted and location provider ready
        
        // When: Request current location with timeout
        val result = try {
            withTimeout(10_000) {
                locationProvider.getCurrentLocation()
            }
        } catch (e: Exception) {
            Result.failure<com.example.looksy.data.location.Location>(e)
        }

        // Then: Either succeeds with valid coordinates or fails gracefully
        if (result.isSuccess) {
            val location = result.getOrNull()!!
            assertTrue(location.latitude in -90.0..90.0)
            assertTrue(location.longitude in -180.0..180.0)
        } else {
            val exception = result.exceptionOrNull()!!
            assertTrue(
                exception.message?.contains("Location is null") == true ||
                exception.message?.contains("GPS") == true ||
                exception is java.util.concurrent.TimeoutException
            )
        }
    }

    @Test
    fun locationProvider_verifies_permissionCheckWorks() = runTest {
        // Given: Test environment with granted permissions
        
        // When: Check permission status
        val hasPermission = locationProvider.hasLocationPermission()

        // Then: Permission check returns true
        assertTrue(hasPermission)
        
        // Note: SecurityException path tested in unit tests (LocationProviderTest)
    }

    @Test
    fun locationProvider_handlesNullLocation_gracefully() = runTest {
        // Given: Permission granted
        assertTrue(locationProvider.hasLocationPermission())

        // When: Request location (may be null if GPS disabled)
        val result = try {
            withTimeout(10_000) {
                locationProvider.getCurrentLocation()
            }
        } catch (e: Exception) {
            Result.failure<com.example.looksy.data.location.Location>(e)
        }

        // Then: Handles both success and null gracefully
        assertTrue(
            result.isSuccess || 
            result.exceptionOrNull()?.message?.contains("Location is null") == true ||
            result.exceptionOrNull() is java.util.concurrent.TimeoutException
        )
    }

    @Test
    fun locationProvider_completesWithinTimeout() = runTest {
        // Given: Permission granted and 10 second timeout
        val timeoutMs = 10_000L
        
        // When: Request location and measure duration
        val startTime = System.currentTimeMillis()
        try {
            withTimeout(timeoutMs) {
                locationProvider.getCurrentLocation()
            }
        } catch (e: Exception) {
            // Expected - may timeout or fail on some devices
        }
        val duration = System.currentTimeMillis() - startTime

        // Then: Completes within timeout
        assertTrue(duration < timeoutMs)
    }
}

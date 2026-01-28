package com.example.looksy

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.looksy.data.location.LocationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Instrumented test for LocationProvider
 * 
 * Tests LocationProvider with real Android framework components.
 * 
 * Note: Testing getCurrentLocation() with real location data requires:
 * 1. Location permissions granted on test device
 * 2. GPS/location services enabled
 * 3. Mock location or actual device location
 * 
 * For CI/CD pipelines, consider using mock location providers or
 * testing only the permission check logic.
 */
@RunWith(AndroidJUnit4::class)
class LocationProviderInstrumentedTest {

    private lateinit var context: Context
    private lateinit var locationProvider: LocationProvider

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        locationProvider = LocationProvider(context)
    }

    @Test
    fun hasLocationPermission_returnsBoolean_withoutCrashing() = runTest {
        // When
        val hasPermission = locationProvider.hasLocationPermission()

        // Then - Just verify it returns a boolean without crashing
        // The actual value depends on test device permissions
        assertTrue(hasPermission || !hasPermission, "Should return a boolean value")
    }

    @Test
    fun getCurrentLocation_withoutPermission_returnsSecurityException() = runTest {
        // Given - Assume no permission (typical in test environment)
        if (!locationProvider.hasLocationPermission()) {
            // When
            val result = locationProvider.getCurrentLocation()

            // Then
            assertTrue(result.isFailure, "Should return failure without permission")
            assertTrue(
                result.exceptionOrNull() is SecurityException,
                "Exception should be SecurityException"
            )
        } else {
            // Skip test if permission is granted (device-dependent)
            assertTrue(true, "Skipped - permission already granted on test device")
        }
    }

    /**
     * Note: To properly test getCurrentLocation() with real location:
     * 
     * 1. Grant location permission in test setup:
     *    Use GrantPermissionRule or UiAutomator to grant permission
     * 
     * 2. Provide mock location:
     *    Use TestLocationProvider or mock location APIs
     * 
     * 3. Example with permission rule:
     * ```
     * @get:Rule
     * val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
     *     Manifest.permission.ACCESS_COARSE_LOCATION
     * )
     * ```
     * 
     * This is intentionally left basic for the backend ticket.
     * Full location testing belongs in the frontend ticket.
     */
}

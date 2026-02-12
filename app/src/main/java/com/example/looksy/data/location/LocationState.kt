package com.example.looksy.data.location

/**
 * Represents the state of location permission
 */
enum class PermissionState {
    NOT_ASKED,           // Permission has never been requested
    DENIED,              // User denied permission
    GRANTED_ONCE,        // Granted for "nur dieses Mal" (one time)
    GRANTED_WHILE_IN_USE // Granted for "während der Nutzung der App" (while using app)
}

/**
 * Represents whether location services are enabled on the device
 */
enum class LocationServicesState {
    UNKNOWN,  // Haven't checked yet
    ON,       // GPS/Location services are enabled
    OFF       // GPS/Location services are disabled
}

/**
 * Represents the input mode for getting location
 */
enum class LocationInputMode {
    GPS,         // Using device GPS
    MANUAL_CITY  // User entering city name manually
}

package com.sallie.weathera.data.local

import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Result of a GPS location request.
 */
sealed interface GpsResult {
    data class Success(
        val latitude: Double,
        val longitude: Double,
        val cityName: String,
        val country: String,
    ) : GpsResult

    data class Error(val reason: GpsErrorReason) : GpsResult
}

enum class GpsErrorReason {
    PERMISSION_DENIED,
    LOCATION_DISABLED,
    TIMEOUT,
    GEOCODING_FAILED,
    UNKNOWN,
}

/**
 * Retrieves the device's current location using the system LocationManager.
 * Uses a one-shot request (no continuous tracking) to conserve battery.
 * Reverse-geocodes coordinates to get a human-readable city/country name.
 *
 * Must be called with COARSE or FINE location permission already granted.
 * Returns [GpsResult] — never throws.
 */
suspend fun getCurrentLocation(context: Context): GpsResult {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    // Check if any location provider is enabled
    val fineEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    val coarseEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    if (!fineEnabled && !coarseEnabled) {
        return GpsResult.Error(GpsErrorReason.LOCATION_DISABLED)
    }

    // Prefer GPS (fine), fall back to Network (coarse)
    val provider = when {
        fineEnabled -> LocationManager.GPS_PROVIDER
        else -> LocationManager.NETWORK_PROVIDER
    }

    // Try last known location first (instant, no battery cost)
    val lastKnown = try {
        @Suppress("MissingPermission")
        locationManager.getLastKnownLocation(provider)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    } catch (_: SecurityException) {
        return GpsResult.Error(GpsErrorReason.PERMISSION_DENIED)
    }

    val location = if (lastKnown != null && isLocationFresh(lastKnown)) {
        lastKnown
    } else {
        // Request a fresh location with 15-second timeout
        val fresh = withTimeoutOrNull(15_000L) {
            suspendCancellableCoroutine { continuation ->
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        // API 30+ one-shot API
                        @Suppress("MissingPermission")
                        locationManager.getCurrentLocation(
                            provider,
                            null,
                            context.mainExecutor,
                        ) { loc ->
                            continuation.resume(loc)
                        }
                    } else {
                        // API 24-29: use requestSingleUpdate with a listener
                        val listener = object : android.location.LocationListener {
                            override fun onLocationChanged(loc: android.location.Location) {
                                if (continuation.isActive) continuation.resume(loc)
                            }
                            @Deprecated("Deprecated in Java")
                            override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) = Unit
                            override fun onProviderEnabled(provider: String) = Unit
                            override fun onProviderDisabled(provider: String) {
                                if (continuation.isActive) continuation.resume(null)
                            }
                        }
                        @Suppress("MissingPermission", "DEPRECATION")
                        locationManager.requestSingleUpdate(provider, listener, null)
                        continuation.invokeOnCancellation {
                            locationManager.removeUpdates(listener)
                        }
                    }
                } catch (_: SecurityException) {
                    continuation.resume(null)
                } catch (_: Exception) {
                    continuation.resume(null)
                }
            }
        }
        fresh ?: lastKnown // fall back to stale last-known if fresh request failed/timed out
    }

    if (location == null) {
        return GpsResult.Error(GpsErrorReason.TIMEOUT)
    }

    // Reverse geocode to get city + country
    return reverseGeocode(context, location.latitude, location.longitude)
}

/**
 * Returns true if the location fix is less than 10 minutes old.
 */
private fun isLocationFresh(location: android.location.Location): Boolean {
    val ageMs = System.currentTimeMillis() - location.time
    return ageMs < 10 * 60 * 1000L // 10 minutes
}

/**
 * Reverse geocodes lat/lon to a city and country string.
 * Falls back to coordinate-based name if geocoder is unavailable.
 */
private suspend fun reverseGeocode(context: Context, lat: Double, lon: Double): GpsResult {
    if (!Geocoder.isPresent()) {
        // No geocoder available — use coordinate as name fallback
        val latStr = String.format("%.4f", lat)
        val lonStr = String.format("%.4f", lon)
        return GpsResult.Success(
            latitude = lat,
            longitude = lon,
            cityName = "$latStr, $lonStr",
            country = "",
        )
    }

    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API 33+ asynchronous geocoder
            suspendCancellableCoroutine { continuation ->
                Geocoder(context).getFromLocation(lat, lon, 1) { addresses ->
                    val address = addresses.firstOrNull()
                    if (address != null) {
                        val city = address.locality
                            ?: address.subAdminArea
                            ?: address.adminArea
                            ?: "${String.format("%.4f", lat)}, ${String.format("%.4f", lon)}"
                        val country = address.countryName ?: ""
                        continuation.resume(GpsResult.Success(lat, lon, city, country))
                    } else {
                        val latStr = String.format("%.4f", lat)
                        val lonStr = String.format("%.4f", lon)
                        continuation.resume(GpsResult.Success(lat, lon, "$latStr, $lonStr", ""))
                    }
                }
            }
        } else {
            // Legacy synchronous geocoder
            @Suppress("DEPRECATION")
            val addresses = Geocoder(context).getFromLocation(lat, lon, 1)
            val address = addresses?.firstOrNull()
            if (address != null) {
                val city = address.locality
                    ?: address.subAdminArea
                    ?: address.adminArea
                    ?: "${String.format("%.4f", lat)}, ${String.format("%.4f", lon)}"
                val country = address.countryName ?: ""
                GpsResult.Success(lat, lon, city, country)
            } else {
                val latStr = String.format("%.4f", lat)
                val lonStr = String.format("%.4f", lon)
                GpsResult.Success(lat, lon, "$latStr, $lonStr", "")
            }
        }
    } catch (_: Exception) {
        val latStr = String.format("%.4f", lat)
        val lonStr = String.format("%.4f", lon)
        GpsResult.Success(lat, lon, "$latStr, $lonStr", "")
    }
}

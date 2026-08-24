package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

data class LocStampLocationData(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val accuracy: Float,
    val address: String,
    val timestamp: String,
    val city: String,
    val country: String
)

object LocationHelper {

    fun getCurrentFormattedTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): LocStampLocationData {
        return withContext(Dispatchers.IO) {
            val fusedLocationClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context)

            var location: Location? = null
            try {
                location = suspendCancellableCoroutine { continuation ->
                    val cts = CancellationTokenSource()
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cts.token
                    ).addOnSuccessListener { loc ->
                        continuation.resume(loc)
                    }.addOnFailureListener {
                        continuation.resume(null)
                    }
                    continuation.invokeOnCancellation {
                        cts.cancel()
                    }
                }
            } catch (e: Exception) {
                location = null
            }

            if (location == null) {
                try {
                    location = suspendCancellableCoroutine { continuation ->
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { loc -> continuation.resume(loc) }
                            .addOnFailureListener { continuation.resume(null) }
                    }
                } catch (e: Exception) {
                    location = null
                }
            }

            // Fallback default coordinates (e.g. Monas, Jakarta Pusat) if GPS offline/emulator
            val lat = location?.latitude ?: -6.175392
            val lng = location?.longitude ?: 106.827153
            val alt = location?.altitude ?: 28.5
            val acc = location?.accuracy ?: 4.2f

            val addressInfo = getAddressFromCoordinates(context, lat, lng)
            val time = getCurrentFormattedTimestamp()

            LocStampLocationData(
                latitude = lat,
                longitude = lng,
                altitude = alt,
                accuracy = acc,
                address = addressInfo.first,
                city = addressInfo.second,
                country = addressInfo.third,
                timestamp = time
            )
        }
    }

    private fun getAddressFromCoordinates(
        context: Context,
        latitude: Double,
        longitude: Double
    ): Triple<String, String, String> {
        return try {
            val geocoder = Geocoder(context, Locale("id", "ID"))
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val street = address.thoroughfare ?: address.featureName ?: "Kawasan Terpantau"
                val subLoc = address.subLocality ?: address.locality ?: "Jakarta Pusat"
                val city = address.subAdminArea ?: address.adminArea ?: "DKI Jakarta"
                val country = address.countryName ?: "Indonesia"
                val fullAddress = address.getAddressLine(0) ?: "$street, $subLoc, $city"
                Triple(fullAddress, city, country)
            } else {
                Triple("Jl. Medan Merdeka Barat, Gambir, Jakarta Pusat", "Jakarta Pusat", "Indonesia")
            }
        } catch (e: Exception) {
            Triple("Kawasan Dokumentasi Lapangan, Jakarta Pusat", "Jakarta Pusat", "Indonesia")
        }
    }
}

package org.com.campus.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.gms.location.LocationServices

private var applicationContext: Context? = null

fun initMapUtility(context: Context) {
    applicationContext = context.applicationContext
}

actual fun openExternalMap(latitude: Double, longitude: Double, label: String) {
    val context = applicationContext ?: return
    val uri = Uri.parse("geo:0,0?q=$latitude,$longitude($label)")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
        val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(webIntent)
    }
}

actual fun openGoogleMapsNavigation(
    originLat: Double,
    originLng: Double,
    destLat: Double,
    destLng: Double
) {
    val context = applicationContext ?: return
    val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$originLat,$originLng&destination=$destLat,$destLng&travelmode=driving")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        val webIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(webIntent)
    }
}

@SuppressLint("MissingPermission")
actual fun getCurrentUserLocation(onLocationReceived: (Double, Double) -> Unit) {
    val context = applicationContext
    if (context == null) {
        onLocationReceived(-6.7824, 39.2083)
        return
    }
    try {
        val client = LocationServices.getFusedLocationProviderClient(context)
        client.lastLocation.addOnSuccessListener { loc ->
            if (loc != null && isValidCoordinate(loc.latitude, loc.longitude)) {
                onLocationReceived(loc.latitude, loc.longitude)
            } else {
                onLocationReceived(-6.7824, 39.2083)
            }
        }.addOnFailureListener {
            onLocationReceived(-6.7824, 39.2083)
        }
    } catch (e: Exception) {
        onLocationReceived(-6.7824, 39.2083)
    }
}

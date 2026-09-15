package org.com.campus.navigation

import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.json.*
import org.com.core.network.apiClient

class GoogleRoutingService(private val apiKey: String) : RoutingService {
    override suspend fun calculateRoute(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double
    ): Route {
        val url = "https://maps.googleapis.com/maps/api/directions/json"
        val response: JsonObject = apiClient.get(url) {
            parameter("origin", "$originLat,$originLng")
            parameter("destination", "$destLat,$destLng")
            parameter("mode", "walking")
            parameter("key", apiKey)
        }.body()

        val status = response["status"]?.jsonPrimitive?.content
        if (status != "OK") {
            throw Exception("Routing failed: $status")
        }

        val route = response["routes"]?.jsonArray?.get(0)?.jsonObject
        val leg = route?.get("legs")?.jsonArray?.get(0)?.jsonObject
        
        val distance = leg?.get("distance")?.jsonObject?.get("value")?.jsonPrimitive?.double ?: 0.0
        val duration = leg?.get("duration")?.jsonObject?.get("value")?.jsonPrimitive?.double ?: 0.0
        
        val points = mutableListOf<RoutePoint>()
        val encodedPolyline = route?.get("overview_polyline")?.jsonObject?.get("points")?.jsonPrimitive?.content
        if (encodedPolyline != null) {
            points.addAll(decodePolyline(encodedPolyline))
        }

        return Route(
            points = points,
            distanceMeters = distance,
            durationSeconds = duration,
            origin = RoutePoint(originLat, originLng),
            destination = RoutePoint(destLat, destLng)
        )
    }

    private fun decodePolyline(encoded: String): List<RoutePoint> {
        val poly = mutableListOf<RoutePoint>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            poly.add(RoutePoint(lat.toDouble() / 1E5, lng.toDouble() / 1E5))
        }
        return poly
    }
}

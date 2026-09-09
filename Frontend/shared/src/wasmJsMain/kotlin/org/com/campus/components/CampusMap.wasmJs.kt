package org.com.campus.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.browser.document
import org.com.campus.data.CampusLocation
import org.com.campus.data.University
import org.w3c.dom.HTMLElement

@Composable
actual fun CampusMap(
    modifier: Modifier,
    university: University,
    locations: List<CampusLocation>,
    onLocationSelected: (CampusLocation) -> Unit,
    onBack: () -> Unit
) {
    // Placeholder in Compose tree
    Box(modifier = modifier)

    LaunchedEffect(university, locations) {
        println("[CAMPNAV MAP] Initializing full-screen Web map for: " + university.shortName)
        
        val mapDiv = document.getElementById("campus-map") as? HTMLElement
        if (mapDiv != null) {
            // 1. Ensure the map container is visible and full screen
            mapDiv.style.display = "block"
            
            // 2. Initialize map and add native Back button
            startWebMapLifecycle(
                mapDiv, 
                university.latitude ?: 0.0, 
                university.longitude ?: 0.0, 
                university.defaultZoom ?: 15f,
                onBack
            )
            
            // 3. Sync markers
            clearWebMarkers()
            locations.forEach { location ->
                addWebMarker(location.latitude, location.longitude, location.name, location.id) { id ->
                    val selected = locations.find { it.id == id }
                    if (selected != null) onLocationSelected(selected)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            println("[CAMPNAV MAP] Disposing map component")
            val mapDiv = document.getElementById("campus-map") as? HTMLElement
            if (mapDiv != null) {
                mapDiv.style.display = "none"
                // Clean up any dynamic buttons we added
                val backBtn = document.getElementById("native-back-btn")
                backBtn?.remove()
            }
        }
    }
}

private fun startWebMapLifecycle(element: HTMLElement, lat: Double, lng: Double, zoom: Float, onBack: () -> Unit): Unit = js("""{
    const init = async () => {
        // Wait for dimensions
        while (element.clientWidth === 0 || element.clientHeight === 0) {
            console.log("[CAMPNAV MAP] Waiting for dimensions...");
            await new Promise(resolve => setTimeout(resolve, 50));
        }

        // Wait for API
        while (!window.google || !window.google.maps || !window.google.maps.importLibrary) {
            await new Promise(resolve => setTimeout(resolve, 100));
        }

        const { Map } = await google.maps.importLibrary("maps");
        const { AdvancedMarkerElement } = await google.maps.importLibrary("marker");
        
        const mapOptions = {
            center: { lat: lat, lng: lng },
            zoom: zoom,
            mapId: "DEMO_MAP_ID",
            disableDefaultUI: false,
            mapTypeControl: false,
            streetViewControl: false,
            fullscreenControl: false,
            styles: [
                { "featureType": "poi", "elementType": "geometry", "stylers": [{ "color": "#000000" }, { "lightness": 21 }] }
            ]
        };

        if (!window.campusMap) {
            console.log("[CAMPNAV MAP] Creating new instance");
            window.campusMap = new Map(element, mapOptions);
            
            // Add Native Back Button (Not a Compose overlay)
            const backBtn = document.createElement("button");
            backBtn.id = "native-back-btn";
            backBtn.innerText = "← BACK";
            backBtn.style.cssText = "position:absolute; top:20px; left:20px; z-index:1000; padding:12px 24px; background:#0F0F23; color:#6C63FF; border:2px solid #6C63FF; cursor:pointer; border-radius:12px; font-weight:bold; box-shadow: 0 4px 12px rgba(0,0,0,0.5);";
            backBtn.onclick = () => {
                onBack();
            };
            element.appendChild(backBtn);
        } else {
            console.log("[CAMPNAV MAP] Updating existing instance");
            window.campusMap.setCenter({ lat: lat, lng: lng });
            window.campusMap.setZoom(zoom);
        }
        
        window.AdvancedMarkerElement = AdvancedMarkerElement;
        window.mapInitialized = true;
    };
    init();
}""")

private fun clearWebMarkers(): Unit = js("""{
    if (window.mapMarkers) {
        window.mapMarkers.forEach(m => m.map = null);
    }
    window.mapMarkers = [];
}""")

@Suppress("UNUSED_PARAMETER")
private fun addWebMarker(lat: Double, lng: Double, title: String, id: Long, onSelected: (Long) -> Unit): Unit = js("""{
    (async () => {
        while (!window.mapInitialized || !window.AdvancedMarkerElement) {
            await new Promise(resolve => setTimeout(resolve, 50));
        }

        const marker = new window.AdvancedMarkerElement({
            map: window.campusMap,
            position: { lat: lat, lng: lng },
            title: title
        });
        
        marker.addListener("click", () => {
            onSelected(id);
        });
        
        if (!window.mapMarkers) window.mapMarkers = [];
        window.mapMarkers.push(marker);
    })();
}""")

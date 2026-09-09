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
    // Act as a placeholder in the Compose tree
    Box(modifier = modifier)

    LaunchedEffect(university, locations) {
        println("[CAMPNAV MAP] Initializing full-screen Web map for: " + university.shortName)
        
        val mapDiv = document.getElementById("campus-map") as? HTMLElement
        if (mapDiv != null) {
            // 1. Ensure the map container is visible and full screen
            mapDiv.style.display = "block"
            
            // 2. Initialize the map exactly once or update existing
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
                addWebMarker(location.latitude, location.longitude, location.name, location.id, onLocationSelected = { id ->
                    val selected = locations.find { it.id == id }
                    if (selected != null) onLocationSelected(selected)
                })
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            println("[CAMPNAV MAP] Disposing map component")
            val mapDiv = document.getElementById("campus-map") as? HTMLElement
            if (mapDiv != null) {
                mapDiv.style.display = "none"
                
                // Clean up native UI elements
                document.getElementById("native-back-btn")?.remove()
                document.getElementById("native-style-container")?.remove()
            }
        }
    }
}

private fun startWebMapLifecycle(element: HTMLElement, lat: Double, lng: Double, zoom: Float, onBack: () -> Unit): Unit = js("""{
    const init = async () => {
        // Wait for dimensions to be non-zero
        while (element.clientWidth === 0 || element.clientHeight === 0) {
            await new Promise(resolve => setTimeout(resolve, 50));
        }

        // Wait for Google Maps API to be loaded
        while (!window.google || !window.google.maps || !window.google.maps.importLibrary) {
            await new Promise(resolve => setTimeout(resolve, 100));
        }

        const { Map } = await google.maps.importLibrary("maps");
        const { AdvancedMarkerElement } = await google.maps.importLibrary("marker");
        
        const minimalStyle = [
            { "featureType": "poi", "elementType": "labels", "stylers": [{ "visibility": "off" }] },
            { "featureType": "transit", "elementType": "labels.icon", "stylers": [{ "visibility": "off" }] },
            { "featureType": "road", "elementType": "labels.icon", "stylers": [{ "visibility": "off" }] },
            { "featureType": "water", "stylers": [{ "color": "#0F0F23" }] }
        ];

        const standardStyle = []; 
        
        const detailedStyle = [
            { "featureType": "poi", "stylers": [{ "visibility": "on" }] }
        ];

        const mapOptions = {
            center: { lat: lat, lng: lng },
            zoom: zoom,
            mapId: "DEMO_MAP_ID",
            disableDefaultUI: false,
            mapTypeControl: false,
            streetViewControl: false,
            fullscreenControl: true,
            styles: minimalStyle
        };

        if (!window.campusMap) {
            console.log("[CAMPNAV MAP] Creating new Google Map instance");
            window.campusMap = new Map(element, mapOptions);
            
            // Add Native Back Button
            const backBtn = document.createElement("button");
            backBtn.id = "native-back-btn";
            backBtn.innerText = "← BACK";
            backBtn.style.cssText = "position:absolute; top:20px; left:20px; z-index:1000; padding:12px 24px; background:#0F0F23; color:#6C63FF; border:2px solid #6C63FF; cursor:pointer; border-radius:12px; font-weight:bold; box-shadow: 0 4px 12px rgba(0,0,0,0.5);";
            backBtn.onclick = () => { onBack(); };
            element.appendChild(backBtn);

            // Add Style Selector
            const styleContainer = document.createElement("div");
            styleContainer.id = "native-style-container";
            styleContainer.style.cssText = "position:absolute; bottom:20px; right:20px; z-index:1000; display:flex; gap:8px; background:rgba(15, 15, 35, 0.8); padding:8px; border-radius:12px;";
            
            const createStyleBtn = (label, style) => {
                const btn = document.createElement("button");
                btn.innerText = label;
                btn.style.cssText = "padding:8px 16px; background:#1A1A3E; color:white; border:none; cursor:pointer; border-radius:8px; font-size:12px;";
                btn.onclick = () => { window.campusMap.setOptions({ styles: style }); };
                return btn;
            };

            styleContainer.appendChild(createStyleBtn("MINIMAL", minimalStyle));
            styleContainer.appendChild(createStyleBtn("STANDARD", standardStyle));
            styleContainer.appendChild(createStyleBtn("DETAILED", detailedStyle));
            element.appendChild(styleContainer);

        } else {
            console.log("[CAMPNAV MAP] Reusing existing Google Map instance");
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
private fun addWebMarker(lat: Double, lng: Double, title: String, id: Long, onLocationSelected: (Long) -> Unit): Unit = js("""{
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
            onLocationSelected(id);
        });
        
        if (!window.mapMarkers) window.mapMarkers = [];
        window.mapMarkers.push(marker);
    })();
}""")

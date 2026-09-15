package org.com.campus.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.browser.document
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.com.campus.data.CampusLocation
import org.com.campus.data.University
import org.com.campus.navigation.NavigationState
import org.com.campus.navigation.RoutePoint
import org.w3c.dom.HTMLElement

@Composable
actual fun CampusMap(
    modifier: Modifier,
    university: University,
    locations: List<CampusLocation>,
    onLocationSelected: (CampusLocation) -> Unit,
    onBack: () -> Unit,
    initialSelectedLocation: CampusLocation?,
    navigationState: NavigationState,
    onStartNavigation: (CampusLocation, CampusLocation?) -> Unit,
    onEndNavigation: () -> Unit,
    onLocationUpdate: (RoutePoint) -> Unit
) {
    // Placeholder in Compose tree
    Box(modifier = modifier)

    LaunchedEffect(university, locations) {
        val mapDiv = document.getElementById("campus-map") as? HTMLElement
        if (mapDiv != null) {
            mapDiv.style.display = "block"
            onEndNavInternal = onEndNavigation
            
            val startLat = initialSelectedLocation?.latitude ?: university.latitude ?: 0.0
            val startLng = initialSelectedLocation?.longitude ?: university.longitude ?: 0.0
            val startZoom = if (initialSelectedLocation != null) 18f else university.defaultZoom ?: 15f
            
            startWebMapLifecycle(
                mapDiv, 
                startLat, 
                startLng, 
                startZoom,
                onBack,
                { lat, lng -> onLocationUpdate(RoutePoint(lat, lng)) },
                { destId, originId ->
                    val dest = locations.find { it.id == destId }
                    val origin = originId?.let { id -> locations.find { id.toLong() == it.id } }
                    if (dest != null) onStartNavigation(dest, origin)
                },
                { onEndNavInternal() }
            )
            
            syncWebMarkers(locations, onLocationSelected)
        }
    }

    // React to navigation state changes
    LaunchedEffect(navigationState) {
        val stateJson = Json.encodeToString(navigationState)
        updateWebNavigationState(stateJson)
        
        // Auto-request location if selecting origin and we don't have it
        if (navigationState.status == org.com.campus.navigation.NavigationStatus.SELECTING_ORIGIN && navigationState.userLocation == null) {
            js("""
                if (navigator.geolocation) {
                    navigator.geolocation.getCurrentPosition((pos) => {
                        if (window.onLocationUpdate) {
                            window.onLocationUpdate(pos.coords.latitude, pos.coords.longitude);
                        }
                    }, (err) => {
                        console.warn("Auto-location failed:", err.message);
                    }, { enableHighAccuracy: true, timeout: 5000, maximumAge: 0 });
                }
            """)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val mapDiv = document.getElementById("campus-map") as? HTMLElement
            if (mapDiv != null) {
                mapDiv.style.display = "none"
                document.getElementById("native-back-btn")?.remove()
                document.getElementById("native-nav-panel")?.remove()
                document.getElementById("native-style-container")?.remove()
            }
        }
    }
}

// Global end nav callback to be called from JS
private var onEndNavInternal: () -> Unit = {}

private fun syncWebMarkers(locations: List<CampusLocation>, onLocationSelected: (CampusLocation) -> Unit) {
    clearWebMarkers()
    locations.forEach { location ->
        addWebMarker(location.latitude, location.longitude, location.name, location.id, onLocationSelected = { id ->
            val selected = locations.find { it.id == id }
            if (selected != null) onLocationSelected(selected)
        })
    }
}

private fun startWebMapLifecycle(
    element: HTMLElement, 
    lat: Double, 
    lng: Double, 
    zoom: Float, 
    onBack: () -> Unit,
    onLocationUpdate: (Double, Double) -> Unit,
    onStartNav: (Long, String?) -> Unit,
    onEndNav: () -> Unit
): Unit = js("""{
    const init = async () => {
        while (element.clientWidth === 0 || element.clientHeight === 0) {
            await new Promise(resolve => setTimeout(resolve, 50));
        }

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

        const mapOptions = {
            center: { lat: lat, lng: lng },
            zoom: zoom,
            mapId: "DEMO_MAP_ID",
            disableDefaultUI: false,
            mapTypeControl: false,
            streetViewControl: false,
            fullscreenControl: false,
            styles: minimalStyle
        };

        if (!window.campusMap) {
            window.campusMap = new Map(element, mapOptions);
            
            // Back Button
            const backBtn = document.createElement("button");
            backBtn.id = "native-back-btn";
            backBtn.innerText = "← BACK";
            backBtn.style.cssText = "position:absolute; top:20px; left:20px; z-index:1000; padding:12px 24px; background:#0F0F23; color:#6C63FF; border:2px solid #6C63FF; cursor:pointer; border-radius:12px; font-weight:bold; box-shadow: 0 4px 12px rgba(0,0,0,0.5);";
            backBtn.onclick = () => { onBack(); };
            element.appendChild(backBtn);

            // Nav Panel (Hidden by default)
            const navPanel = document.createElement("div");
            navPanel.id = "native-nav-panel";
            navPanel.style.cssText = "position:absolute; bottom:80px; left:20px; right:20px; z-index:1000; background:#0F0F23; color:white; padding:16px; border-radius:16px; border:1px solid #6C63FF; display:none; flex-direction:column; gap:12px; font-family: sans-serif;";
            element.appendChild(navPanel);
            
            window.onStartNav = onStartNav;
            window.onEndNav = onEndNav;
            window.onLocationUpdate = onLocationUpdate;

        } else {
            window.campusMap.setCenter({ lat: lat, lng: lng });
            window.campusMap.setZoom(zoom);
        }
        
        window.AdvancedMarkerElement = AdvancedMarkerElement;
        window.mapInitialized = true;
    };
    init();
}""")

private fun updateWebNavigationState(stateJson: String): Unit = js("""{
    (async () => {
        while (!window.mapInitialized) await new Promise(r => setTimeout(r, 100));
        
        const state = JSON.parse(stateJson);
        const panel = document.getElementById("native-nav-panel");
        if (!panel) return;

        if (state.status === "IDLE") {
            if (window.directionsRenderer) window.directionsRenderer.setMap(null);
            if (window.userMarker) window.userMarker.map = null;
            
            if (state.destination) {
                panel.style.display = "flex";
                panel.innerHTML = "";
                
                const title = document.createElement("div");
                title.innerText = state.destination.name;
                title.style.cssText = "font-weight:bold; font-size: 18px; color: white;";
                panel.appendChild(title);

                const navBtn = document.createElement("button");
                navBtn.innerText = "Navigate to destination";
                navBtn.style.cssText = "padding:12px; background:#6C63FF; color:white; border:none; border-radius:8px; cursor:pointer; font-weight: bold; margin-top: 8px;";
                navBtn.onclick = () => { window.onStartNav(state.destination.id, null); };
                panel.appendChild(navBtn);
            } else {
                panel.style.display = "none";
            }
            return;
        }

        panel.style.display = "flex";
        panel.innerHTML = ""; // Clear

        if (state.status === "SELECTING_ORIGIN") {
            const title = document.createElement("div");
            title.innerText = "Navigate to " + state.destination.name;
            title.style.cssText = "font-weight:bold; font-size: 18px; color: #6C63FF;";
            panel.appendChild(title);

            const btnContainer = document.createElement("div");
            btnContainer.style.cssText = "display: flex; flex-direction: column; gap: 8px;";
            panel.appendChild(btnContainer);

            const currentLocBtn = document.createElement("button");
            currentLocBtn.innerText = "Use My Current Location";
            currentLocBtn.style.cssText = "padding:12px; background:#6C63FF; color:white; border:none; border-radius:8px; cursor:pointer; font-weight: bold;";
            currentLocBtn.onclick = () => {
                navigator.geolocation.getCurrentPosition((pos) => {
                    window.onLocationUpdate(pos.coords.latitude, pos.coords.longitude);
                    window.onStartNav(state.destination.id, null);
                }, (err) => {
                    alert("Location access denied or unavailable.");
                });
            };
            btnContainer.appendChild(currentLocBtn);

            const closeBtn = document.createElement("button");
            closeBtn.innerText = "Cancel";
            closeBtn.style.cssText = "padding:8px; background:transparent; color:gray; border:none; cursor:pointer;";
            closeBtn.onclick = () => { window.onEndNav(); };
            btnContainer.appendChild(closeBtn);
        }

        if (state.status === "ACTIVE" || state.status === "CALCULATING") {
            const title = document.createElement("div");
            title.innerText = state.destination.name;
            title.style.cssText = "font-weight:bold; font-size: 18px;";
            panel.appendChild(title);

            if (state.status === "CALCULATING") {
                const loader = document.createElement("div");
                loader.innerText = "Calculating route...";
                panel.appendChild(loader);
            } else {
                const info = document.createElement("div");
                info.style.cssText = "color: #6C63FF; font-weight: bold; font-size: 20px;";
                
                const { DirectionsService, DirectionsRenderer } = await google.maps.importLibrary("routes");
                if (!window.directionsService) window.directionsService = new DirectionsService();
                if (!window.directionsRenderer) window.directionsRenderer = new DirectionsRenderer({ 
                    map: window.campusMap,
                    suppressMarkers: false,
                    polylineOptions: {
                        strokeColor: "#6C63FF",
                        strokeWeight: 6
                    }
                });
                
                const origin = state.originLatLng ? { lat: state.originLatLng.latitude, lng: state.originLatLng.longitude } : { lat: state.origin.latitude, lng: state.origin.longitude };
                const destination = { lat: state.destination.latitude, lng: state.destination.longitude };

                window.directionsService.route({
                    origin: origin,
                    destination: destination,
                    travelMode: google.maps.TravelMode.WALKING
                }, (result, status) => {
                    if (status === "OK") {
                        window.directionsRenderer.setDirections(result);
                        const route = result.routes[0].legs[0];
                        info.innerText = route.distance.text + " • " + route.duration.text;
                    } else {
                        info.innerText = "Route not found";
                    }
                });
                panel.appendChild(info);
            }

            const endBtn = document.createElement("button");
            endBtn.innerText = "End Navigation";
            endBtn.style.cssText = "padding:12px; background:#FF4B4B; color:white; border:none; border-radius:8px; cursor:pointer; font-weight: bold; margin-top: 8px;";
            endBtn.onclick = () => { window.onEndNav(); };
            panel.appendChild(endBtn);
        }
    })();
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

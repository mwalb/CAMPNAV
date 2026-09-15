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

    LaunchedEffect(university, locations, navigationState.destination) {
        val mapDiv = document.getElementById("campus-map") as? HTMLElement
        if (mapDiv != null) {
            mapDiv.style.display = "block"
            onEndNavInternal = onEndNavigation
            
            val selectedDest = navigationState.destination
            val startLat = selectedDest?.latitude ?: initialSelectedLocation?.latitude ?: university.latitude ?: 0.0
            val startLng = selectedDest?.longitude ?: initialSelectedLocation?.longitude ?: university.longitude ?: 0.0
            val startZoom = if (selectedDest != null || initialSelectedLocation != null) 18f else university.defaultZoom ?: 15f
            
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
            
            syncWebMarkers(locations, selectedDest?.id, onLocationSelected)
        }
    }

    // React to navigation state changes
    LaunchedEffect(navigationState, locations) {
        val stateJson = Json.encodeToString(navigationState)
        val locationsJson = Json.encodeToString(locations)
        
        // Sync locations to JS
        syncWebLocations(locationsJson)
        
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
                document.getElementById("native-map-overlay")?.remove()
                document.getElementById("native-style-container")?.remove()
            }
        }
    }
}

// Global end nav callback to be called from JS
private var onEndNavInternal: () -> Unit = {}

private fun syncWebMarkers(locations: List<CampusLocation>, selectedId: Long?, onLocationSelected: (CampusLocation) -> Unit) {
    clearWebMarkers()
    // Show only the selected marker
    val selected = locations.find { it.id == selectedId }
    if (selected != null) {
        addWebMarker(
            selected.latitude, 
            selected.longitude, 
            selected.name, 
            selected.id, 
            true, 
            onLocationSelected = { id ->
                val loc = locations.find { it.id == id }
                if (loc != null) onLocationSelected(loc)
            }
        )
    }
}

private fun syncWebLocations(locationsJson: String): Unit = js("""{
    window.campusLocationsData = JSON.parse(locationsJson);
}""")

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
            { "featureType": "poi", "stylers": [{ "visibility": "off" }] },
            { "featureType": "transit", "stylers": [{ "visibility": "off" }] },
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
            
            // Overlay for dimming
            const overlay = document.createElement("div");
            overlay.id = "native-map-overlay";
            overlay.style.cssText = "position:absolute; top:0; left:0; right:0; bottom:0; z-index:999; background:rgba(0,0,0,0.7); display:none; pointer-events:auto; backdrop-filter: blur(2px);";
            element.appendChild(overlay);

            // Nav Panel / Popup
            const navPanel = document.createElement("div");
            navPanel.id = "native-nav-panel";
            navPanel.style.cssText = "position:absolute; z-index:1000; display:none; flex-direction:column; font-family: sans-serif;";
            element.appendChild(navPanel);
            
            window.onStartNav = onStartNav;
            window.onEndNav = onEndNav;
            window.onLocationUpdate = onLocationUpdate;

            window.showOriginSelectionPopup = (destId) => {
                const locations = window.campusLocationsData || [];
                const dest = locations.find(l => l.id == destId);
                if (!dest) return;

                overlay.style.display = "block";
                navPanel.style.display = "flex";
                navPanel.style.cssText = "position:absolute; top:50%; left:50%; transform:translate(-50%, -50%); z-index:1000; background:#0F0F23; color:white; padding:24px; border-radius:24px; border:2px solid #6C63FF; display:flex; flex-direction:column; gap:16px; font-family: sans-serif; width: 85%; max-width: 400px; box-shadow: 0 8px 32px rgba(0,0,0,0.8);";
                
                navPanel.innerHTML = "";
                const title = document.createElement("div");
                title.innerText = "Navigate to " + dest.name;
                title.style.cssText = "font-weight:bold; font-size: 20px; color: white; text-align: center;";
                navPanel.appendChild(title);
                
                const subtitle = document.createElement("div");
                subtitle.innerText = "Choose your starting point";
                subtitle.style.cssText = "font-size: 14px; color: #AAA; text-align: center; margin-top: -8px;";
                navPanel.appendChild(subtitle);

                let selectedOrigin = null;
                const selectionContainer = document.createElement("div");
                selectionContainer.style.cssText = "display: flex; flex-direction: column; gap: 12px;";
                navPanel.appendChild(selectionContainer);

                const currentLocBtn = document.createElement("button");
                currentLocBtn.innerText = "📍 Use My Current Location";
                currentLocBtn.style.cssText = "padding: 16px; background:#1A1A35; color:white; border:1px solid #6C63FF; border-radius:12px; cursor:pointer; font-weight: bold; font-size: 16px; text-align: left;";
                currentLocBtn.onclick = () => {
                    selectedOrigin = "GPS";
                    resetSelectionStyles();
                    currentLocBtn.style.background = "#6C63FF";
                    startBtn.disabled = false;
                    startBtn.style.opacity = "1";
                };
                selectionContainer.appendChild(currentLocBtn);

                const listContainer = document.createElement("div");
                listContainer.style.cssText = "max-height: 150px; overflow-y: auto; display: flex; flex-direction: column; gap: 8px;";
                selectionContainer.appendChild(listContainer);

                const items = [];
                locations.filter(l => l.id != destId).forEach(loc => {
                    const item = document.createElement("button");
                    item.innerText = loc.name;
                    item.style.cssText = "padding:12px; background:#1A1A35; color:white; border:1px solid #333; border-radius:10px; cursor:pointer; text-align: left; font-size: 14px;";
                    item.onclick = () => {
                        selectedOrigin = loc.id.toString();
                        resetSelectionStyles();
                        item.style.borderColor = "#6C63FF";
                        item.style.background = "#252545";
                        startBtn.disabled = false;
                        startBtn.style.opacity = "1";
                    };
                    items.push(item);
                    listContainer.appendChild(item);
                });

                const resetSelectionStyles = () => {
                    currentLocBtn.style.background = "#1A1A35";
                    items.forEach(i => { i.style.borderColor = "#333"; i.style.background = "#1A1A35"; });
                };

                const startBtn = document.createElement("button");
                startBtn.innerText = "NAVIGATE";
                startBtn.disabled = true;
                startBtn.style.cssText = "padding:18px; background:#6C63FF; color:white; border:none; border-radius:14px; cursor:pointer; font-weight: bold; font-size: 18px; margin-top: 8px; opacity: 0.5; transition: opacity 0.2s;";
                startBtn.onclick = () => {
                    overlay.style.display = "none";
                    navPanel.style.display = "none";
                    if (selectedOrigin === "GPS") {
                        navigator.geolocation.getCurrentPosition((pos) => {
                            window.onLocationUpdate(pos.coords.latitude, pos.coords.longitude);
                            window.onStartNav(destId, null);
                        }, (err) => {
                            alert("Location access denied. Please enable location permissions.");
                        });
                    } else {
                        window.onStartNav(destId, selectedOrigin);
                    }
                };
                navPanel.appendChild(startBtn);

                const closeBtn = document.createElement("button");
                closeBtn.innerText = "Cancel";
                closeBtn.style.cssText = "padding:12px; background:transparent; color:#888; border:none; cursor:pointer; font-weight: bold;";
                closeBtn.onclick = () => { 
                    overlay.style.display = "none";
                    navPanel.style.display = "none";
                };
                navPanel.appendChild(closeBtn);
            };

            // Back Button
            const backBtn = document.createElement("button");
            backBtn.id = "native-back-btn";
            backBtn.innerText = "← BACK";
            backBtn.style.cssText = "position:absolute; top:20px; left:20px; z-index:1000; padding:12px 24px; background:#0F0F23; color:#6C63FF; border:2px solid #6C63FF; cursor:pointer; border-radius:12px; font-weight:bold; box-shadow: 0 4px 12px rgba(0,0,0,0.5);";
            backBtn.onclick = () => { onBack(); };
            element.appendChild(backBtn);

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
        const overlay = document.getElementById("native-map-overlay");
        if (!panel || !overlay) return;

        if (state.status === "IDLE") {
            if (window.directionsRenderer) window.directionsRenderer.setMap(null);
            if (window.userMarker) window.userMarker.map = null;
            overlay.style.display = "none";
            panel.style.display = "none";
            return;
        }

        if (state.status === "SELECTING_ORIGIN") {
            if (window.showOriginSelectionPopup) {
                window.showOriginSelectionPopup(state.destination.id);
            }
        }

        if (state.status === "ACTIVE" || state.status === "CALCULATING") {
            overlay.style.display = "none";
            panel.style.display = "none";

            const { DirectionsService, DirectionsRenderer } = await google.maps.importLibrary("routes");
            if (!window.directionsService) window.directionsService = new DirectionsService();
            if (!window.directionsRenderer) window.directionsRenderer = new DirectionsRenderer({ 
                map: window.campusMap,
                suppressMarkers: false,
                polylineOptions: { strokeColor: "#6C63FF", strokeWeight: 6 }
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
                }
            });
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
private fun addWebMarker(lat: Double, lng: Double, title: String, id: Long, isSelected: Boolean, onLocationSelected: (Long) -> Unit): Unit = js("""{
    (async () => {
        while (!window.mapInitialized || !window.AdvancedMarkerElement) {
            await new Promise(resolve => setTimeout(resolve, 50));
        }

        const marker = new window.AdvancedMarkerElement({
            map: window.campusMap,
            position: { lat: lat, lng: lng },
            title: title
        });
        
        const infoWindow = new google.maps.InfoWindow({
            content: `
                <div style="color:black; padding:12px; font-family: sans-serif; min-width: 180px;">
                    <div style="font-weight:bold; margin-bottom:12px; font-size:16px; color:#0F0F23;">${'$'}{title}</div>
                    <button id="nav-btn-${'$'}{id}" style="width:100%; padding:12px; background:#6C63FF; color:white; border:none; border-radius:10px; cursor:pointer; font-weight:bold; font-size:14px; box-shadow: 0 2px 4px rgba(0,0,0,0.2);">
                        NAVIGATE
                    </button>
                </div>
            `
        });

        const setupNavBtn = () => {
            const btn = document.getElementById(`nav-btn-${'$'}{id}`);
            if (btn) {
                btn.onclick = (e) => {
                    e.stopPropagation();
                    if (window.showOriginSelectionPopup) {
                        window.showOriginSelectionPopup(id);
                    }
                    infoWindow.close();
                };
            }
        };

        marker.addListener("click", () => {
            infoWindow.open(window.campusMap, marker);
            google.maps.event.addListenerOnce(infoWindow, 'domready', setupNavBtn);
            onLocationSelected(id);
        });
        
        if (isSelected) {
            infoWindow.open(window.campusMap, marker);
            google.maps.event.addListenerOnce(infoWindow, 'domready', setupNavBtn);
        }

        if (!window.mapMarkers) window.mapMarkers = [];
        window.mapMarkers.push(marker);
    })();
}""")

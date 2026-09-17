@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
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
                { onEndNavInternal() }
            )
            
            syncWebMarkers(locations, selectedDest?.id, onLocationSelected)
        }
    }

    LaunchedEffect(navigationState, locations) {
        val locationsJson = Json.encodeToString(locations)
        syncWebLocations(locationsJson)
        
        if (navigationState.status == org.com.campus.navigation.NavigationStatus.SELECTING_ORIGIN && navigationState.destination != null) {
            triggerOriginSelectionJS(navigationState.destination.id.toString())
        }
        
        startTrackingUserLocation()
    }

    DisposableEffect(Unit) {
        onDispose {
            val mapDiv = document.getElementById("campus-map") as? HTMLElement
            if (mapDiv != null) {
                mapDiv.style.display = "none"
                document.getElementById("native-back-btn")?.remove()
                document.getElementById("native-nav-panel")?.remove()
                document.getElementById("native-map-overlay")?.remove()
                document.getElementById("native-info-card")?.remove()
                stopTrackingUserLocation()
            }
        }
    }
}

private var onEndNavInternal: () -> Unit = {}

private fun syncWebMarkers(locations: List<CampusLocation>, selectedId: Long?, onLocationSelected: (CampusLocation) -> Unit) {
    clearWebMarkers()
    val selected = locations.find { it.id == selectedId }
    if (selected != null) {
        addWebMarker(
            selected.latitude, 
            selected.longitude, 
            selected.name, 
            selected.id.toString(), 
            true, 
            onLocationSelected = { id: String ->
                val loc = locations.find { it.id.toString() == id }
                if (loc != null) onLocationSelected(loc)
            }
        )
    }
}

@JsFun("(destId) => { if (window.showOriginSelectionPopup) window.showOriginSelectionPopup(destId); }")
private external fun triggerOriginSelectionJS(destId: String)

@JsFun("(locationsJson) => { window.campusLocationsData = JSON.parse(locationsJson); }")
private external fun syncWebLocations(locationsJson: String)

@JsFun("""() => {
    if (navigator.geolocation && !window.watchId) {
        const options = { enableHighAccuracy: true, timeout: 30000, maximumAge: 3000 };
        window.watchId = navigator.geolocation.watchPosition((pos) => {
            const lat = Number(pos.coords.latitude);
            const lng = Number(pos.coords.longitude);
            if (window.onLocationUpdate) window.onLocationUpdate(lat, lng);
            if (window.userMarker && window.campusMap) {
                window.userMarker.position = { lat: lat, lng: lng };
                if (!window.userMarker.map) window.userMarker.map = window.campusMap;
            }
        }, (err) => {
            console.warn("Location tracking failed:", err.code, err.message);
        }, options);
    }
}""")
private external fun startTrackingUserLocation()

@JsFun("""() => {
    if (window.watchId) {
        navigator.geolocation.clearWatch(window.watchId);
        window.watchId = null;
    }
    if (window.navWatchId) {
        navigator.geolocation.clearWatch(window.navWatchId);
        window.navWatchId = null;
    }
}""")
private external fun stopTrackingUserLocation()

@JsFun("""(element, lat, lng, zoom, onBack, onLocationUpdate, onEndNav) => {
    const init = async () => {
        while (element.clientWidth === 0 || element.clientHeight === 0) {
            await new Promise(resolve => setTimeout(resolve, 50));
        }

        while (!window.google || !window.google.maps || !window.google.maps.importLibrary) {
            await new Promise(resolve => setTimeout(resolve, 100));
        }

        const { Map, LatLngBounds } = await google.maps.importLibrary("maps");
        const { AdvancedMarkerElement, PinElement } = await google.maps.importLibrary("marker");
        const { Route } = await google.maps.importLibrary("routes");
        const { encoding } = await google.maps.importLibrary("geometry");

        const isValidCoordinate = (lat, lng) => {
            const l = Number(lat);
            const g = Number(lng);
            return !isNaN(l) && !isNaN(g) && l >= -90 && l <= 90 && g >= -180 && g <= 180;
        };
        
        const mapOptions = {
            center: { lat: Number(lat), lng: Number(lng) },
            zoom: Number(zoom),
            mapId: "DEMO_MAP_ID",
            disableDefaultUI: false,
            mapTypeControl: false,
            streetViewControl: false,
            fullscreenControl: false
        };

        if (!window.campusMap) {
            window.campusMap = new Map(element, mapOptions);
            
            const overlay = document.createElement("div");
            overlay.id = "native-map-overlay";
            overlay.style.cssText = "position:absolute; top:0; left:0; right:0; bottom:0; z-index:999; background:rgba(0,0,0,0.7); display:none; pointer-events:auto; backdrop-filter: blur(2px);";
            element.appendChild(overlay);

            const navPanel = document.createElement("div");
            navPanel.id = "native-nav-panel";
            navPanel.style.cssText = "position:absolute; z-index:1000; display:none; flex-direction:column; font-family: sans-serif;";
            element.appendChild(navPanel);

            const infoCard = document.createElement("div");
            infoCard.id = "native-info-card";
            infoCard.style.cssText = "position:absolute; bottom:30px; left:20px; right:20px; z-index:1000; background:#0F0F23; color:white; padding:20px; border-radius:20px; border:1px solid #6C63FF; display:none; flex-direction:column; gap:8px; font-family: sans-serif; box-shadow: 0 4px 20px rgba(0,0,0,0.5);";
            element.appendChild(infoCard);
            
            window.onLocationUpdate = onLocationUpdate;
            window.onEndNav = onEndNav;

            window.showOriginSelectionPopup = (destId) => {
                const locations = window.campusLocationsData || [];
                const dest = locations.find(l => String(l.id) === String(destId));
                if (!dest) return;

                overlay.style.display = "block";
                navPanel.style.display = "flex";
                navPanel.style.cssText = "position:absolute; top:50%; left:50%; transform:translate(-50%, -50%); z-index:1000; background:#0F0F23; color:white; padding:24px; border-radius:24px; border:2px solid #6C63FF; display:flex; flex-direction:column; gap:16px; font-family: sans-serif; width: 85%; max-width: 400px; box-shadow: 0 8px 32px rgba(0,0,0,0.8);";
                
                navPanel.innerHTML = "";
                const title = document.createElement("div");
                title.innerText = "Set Starting Point";
                title.style.cssText = "font-weight:bold; font-size: 20px; color: white; text-align: center;";
                navPanel.appendChild(title);
                
                const subtitle = document.createElement("div");
                subtitle.innerText = "To navigate to " + dest.name;
                subtitle.style.cssText = "font-size: 14px; color: #AAA; text-align: center; margin-top: -8px;";
                navPanel.appendChild(subtitle);

                let selectedOriginCoord = null;
                const selectionContainer = document.createElement("div");
                selectionContainer.style.cssText = "display: flex; flex-direction: column; gap: 12px;";
                navPanel.appendChild(selectionContainer);

                const currentLocBtn = document.createElement("button");
                currentLocBtn.innerText = "📍 Use My Current Location";
                currentLocBtn.style.cssText = "padding: 16px; background:#1A1A35; color:white; border:1px solid #6C63FF; border-radius:12px; cursor:pointer; font-weight: bold; font-size: 16px; text-align: left;";
                
                const selectOnMapBtn = document.createElement("button");
                selectOnMapBtn.innerText = "🗺️ Select Point on Map";
                selectOnMapBtn.style.cssText = "padding: 16px; background:#1A1A35; color:white; border:1px solid #6C63FF; border-radius:12px; cursor:pointer; font-weight: bold; font-size: 16px; text-align: left;";

                currentLocBtn.onclick = async () => {
                    const usePosition = (lat, lng) => {
                        selectedOriginCoord = { lat: Number(lat), lng: Number(lng) };
                        resetSelectionStyles();
                        currentLocBtn.innerText = "✅ Current Location Set";
                        currentLocBtn.style.background = "#6C63FF";
                        currentLocBtn.disabled = false;
                        startBtn.disabled = false;
                        startBtn.style.opacity = "1";
                    };

                    const markerPos = window.userMarker ? window.userMarker.position : null;
                    if (markerPos) {
                        const lat = typeof markerPos.lat === 'function' ? markerPos.lat() : markerPos.lat;
                        const lng = typeof markerPos.lng === 'function' ? markerPos.lng() : markerPos.lng;
                        if (lat !== undefined && lng !== undefined && !isNaN(Number(lat)) && !isNaN(Number(lng))) {
                            usePosition(lat, lng);
                            return;
                        }
                    }

                    currentLocBtn.innerText = "⌛ Locating...";
                    currentLocBtn.disabled = true;
                    
                    navigator.geolocation.getCurrentPosition((pos) => {
                        usePosition(pos.coords.latitude, pos.coords.longitude);
                    }, (err) => {
                        console.error("Location tracking failed:", err.code, err.message);
                        currentLocBtn.innerText = "❌ GPS Failed - Retry?";
                        currentLocBtn.disabled = false;
                        alert("Unable to determine your current location. Please allow location access and try again.");
                    }, { enableHighAccuracy: true, timeout: 30000, maximumAge: 30000 });
                };
                selectionContainer.appendChild(currentLocBtn);

                selectOnMapBtn.onclick = () => {
                    overlay.style.display = "none";
                    navPanel.style.display = "none";
                    window.isSelectingOnMap = true;
                    window.activeDest = dest;
                    alert("Tap on the map to set start point.");
                };
                selectionContainer.appendChild(selectOnMapBtn);

                const listContainer = document.createElement("div");
                listContainer.style.cssText = "max-height: 150px; overflow-y: auto; display: flex; flex-direction: column; gap: 8px;";
                selectionContainer.appendChild(listContainer);

                const items = [];
                locations.filter(l => String(l.id) !== String(destId)).forEach(loc => {
                    const item = document.createElement("button");
                    item.innerText = loc.name;
                    item.style.cssText = "padding:12px; background:#1A1A35; color:white; border:1px solid #333; border-radius:10px; cursor:pointer; text-align: left; font-size: 14px;";
                    item.onclick = () => {
                        selectedOriginCoord = { lat: Number(loc.latitude), lng: Number(loc.longitude) };
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
                    selectOnMapBtn.style.background = "#1A1A35";
                    items.forEach(i => { i.style.borderColor = "#333"; i.style.background = "#1A1A35"; });
                };

                const startBtn = document.createElement("button");
                startBtn.innerText = "START NAVIGATION";
                startBtn.style.cssText = "padding:18px; background:#6C63FF; color:white; border:none; border-radius:14px; cursor:pointer; font-weight: bold; font-size: 18px; margin-top: 8px;";
                startBtn.onclick = async () => {
                    const runWithOrigin = (lat, lng) => {
                        overlay.style.display = "none";
                        navPanel.style.display = "none";
                        window.runRoutingJS({ lat, lng }, dest);
                    };

                    if (selectedOriginCoord) {
                        runWithOrigin(selectedOriginCoord.lat, selectedOriginCoord.lng);
                    } else {
                        startBtn.innerText = "⌛ Locating...";
                        startBtn.disabled = true;
                        navigator.geolocation.getCurrentPosition((pos) => {
                            runWithOrigin(pos.coords.latitude, pos.coords.longitude);
                        }, (err) => {
                            console.error("GPS error:", err.code, err.message);
                            alert("Location permission is required for navigation. Please enable location access and try again.");
                            startBtn.innerText = "START NAVIGATION";
                            startBtn.disabled = false;
                        }, { enableHighAccuracy: true, timeout: 30000, maximumAge: 0 });
                    }
                };
                navPanel.appendChild(startBtn);

                const closeBtn = document.createElement("button");
                closeBtn.innerText = "Cancel";
                closeBtn.style.cssText = "padding:12px; background:transparent; color:#888; border:none; cursor:pointer; font-weight: bold;";
                closeBtn.onclick = () => { overlay.style.display = "none"; navPanel.style.display = "none"; };
                navPanel.appendChild(closeBtn);
            };

            window.runRoutingJS = async (origin, destinationObj) => {
                console.log("=== RAW ROUTING INPUT ===", {
                    origin,
                    destinationObj,
                    originType: typeof origin,
                    destinationObjType: typeof destinationObj
                });

                if (!origin || !destinationObj) {
                    alert("Unable to start navigation: Missing origin or destination.");
                    return;
                }

                // Explicitly normalize origin coordinates to numbers
                const originLat = Number(origin?.lat);
                const originLng = Number(origin?.lng);
                
                // Use entrance coordinates if available (check for null), otherwise use official marker coordinates
                const destinationLat = Number(destinationObj.entranceLatitude != null ? destinationObj.entranceLatitude : destinationObj.latitude);
                const destinationLng = Number(destinationObj.entranceLongitude != null ? destinationObj.entranceLongitude : destinationObj.longitude);

                if (!Number.isFinite(originLat) || !Number.isFinite(originLng) || 
                    !Number.isFinite(destinationLat) || !Number.isFinite(destinationLng)) {
                    console.error("INVALID ORIGIN OR DESTINATION:", { 
                        origin, 
                        destinationObj, 
                        originLat, 
                        originLng,
                        destinationLat,
                        destinationLng
                    });
                    alert("Invalid coordinates detected. Please ensure location services are enabled.");
                    return;
                }

                const calculateDistance = (l1, n1, l2, n2) => {
                    const R = 6371e3;
                    const φ1 = l1 * Math.PI/180;
                    const φ2 = l2 * Math.PI/180;
                    const Δφ = (l2-l1) * Math.PI/180;
                    const Δλ = (n2-n1) * Math.PI/180;
                    const a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
                              Math.cos(φ1) * Math.cos(φ2) *
                              Math.sin(Δλ/2) * Math.sin(Δλ/2);
                    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
                    return R * c;
                };

                const startRouting = async (startLat, startLng, fitToMap = false) => {
                    const oLat = Number(startLat);
                    const oLng = Number(startLng);
                    const dLat = Number(destinationLat);
                    const dLng = Number(destinationLng);

                    if (!Number.isFinite(oLat) || !Number.isFinite(oLng) || !Number.isFinite(dLat) || !Number.isFinite(dLng)) {
                        console.error("Invalid routing coordinates:", {
                            oLat, oLng, dLat, dLng,
                            oLatType: typeof oLat, oLngType: typeof oLng,
                            dLatType: typeof dLat, dLngType: typeof dLng
                        });
                        throw new Error("Invalid origin or destination coordinates");
                    }

                    const origin = { lat: oLat, lng: oLng };
                    const destination = { lat: dLat, lng: dLng };

                    console.log("=== ROUTING DEBUG ===");
                    console.log("origin:", origin);
                    console.log("destination:", destination);
                    console.log("origin.lat:", origin.lat, typeof origin.lat);
                    console.log("origin.lng:", origin.lng, typeof origin.lng);
                    console.log("destination.lat:", destination.lat, typeof destination.lat);
                    console.log("destination.lng:", destination.lng, typeof destination.lng);
                    console.log("travelMode: DRIVING");

                    const req = {
                        origin: origin,
                        destination: destination,
                        travelMode: 'DRIVING',
                        polylineQuality: 'HIGH_QUALITY',
                        fields: ['path', 'distanceMeters', 'durationMillis', 'viewport']
                    };

                    console.log("request:", req);

                    try {
                        const { Route } = await google.maps.importLibrary("routes");
                        const response = await Route.computeRoutes(req);
                        
                        if (!response.routes || response.routes.length === 0) {
                            alert("No driving route found to this destination.");
                            return;
                        }
                        
                        const route = response.routes[0];
                        if (window.routePolylines) {
                            window.routePolylines.forEach(p => p.setMap(null));
                        }
                        
                        window.routePolylines = route.createPolylines({
                            polylineOptions: {
                                strokeColor: "#6C63FF",
                                strokeOpacity: 1.0,
                                strokeWeight: 6
                            }
                        });
                        
                        window.routePolylines.forEach(polyline => {
                            polyline.setMap(window.campusMap);
                        });

                        if (fitToMap && route.viewport && window.campusMap) {
                            window.campusMap.fitBounds(route.viewport, { top: 50, right: 50, bottom: 250, left: 50 });
                        }

                        infoCard.style.display = "flex";
                        infoCard.style.cssText = "position:absolute; bottom:0; left:0; right:0; z-index:1000; background:#0F0F23; color:white; padding:24px; border-radius:24px 24px 0 0; border-top:1px solid #6C63FF; display:flex; flex-direction:column; gap:12px; font-family: sans-serif; box-shadow: 0 -4px 20px rgba(0,0,0,0.5); max-height: 300px;";
                        infoCard.innerHTML = "";
                        
                        const headDiv = document.createElement("div");
                        headDiv.style.cssText = "display:flex; justify-content:space-between; align-items:center;";
                        
                        const titleGroup = document.createElement("div");
                        const nameDiv = document.createElement("div");
                        nameDiv.innerText = destinationObj.name;
                        nameDiv.style.cssText = "font-weight:bold; font-size: 20px;";
                        titleGroup.appendChild(nameDiv);
                        
                        const modeDiv = document.createElement("div");
                        modeDiv.innerText = "Driving Mode";
                        modeDiv.style.cssText = "color: #AAA; font-size: 14px;";
                        titleGroup.appendChild(modeDiv);
                        headDiv.appendChild(titleGroup);
                        
                        const statsDiv = document.createElement("div");
                        const durationMin = Math.ceil(Number(route.durationMillis) / 60000);
                        const distanceM = Number(route.distanceMeters);
                        const distText = distanceM >= 1000 ? (distanceM/1000).toFixed(1) + " km" : distanceM + " m";
                        statsDiv.innerText = distText + " • " + durationMin + " min";
                        statsDiv.style.cssText = "color: #6C63FF; font-weight: bold; font-size: 18px; text-align: right;";
                        headDiv.appendChild(statsDiv);
                        
                        infoCard.appendChild(headDiv);
                        
                        const endBtn = document.createElement("button");
                        endBtn.innerText = "END NAVIGATION";
                        endBtn.style.cssText = "margin-top: 12px; padding: 18px; background: #FF4B4B; color: white; border: none; border-radius: 16px; font-weight: bold; cursor: pointer; font-size: 16px;";
                        endBtn.onclick = () => {
                            if (window.navWatchId) {
                                navigator.geolocation.clearWatch(window.navWatchId);
                                window.navWatchId = null;
                            }
                            if (window.routePolylines) {
                                window.routePolylines.forEach(p => p.setMap(null));
                                window.routePolylines = null;
                            }
                            infoCard.style.display = "none";
                            window.onEndNav();
                        };
                        infoCard.appendChild(endBtn);

                        // Start continuous tracking during navigation
                        if (!window.navWatchId) {
                            let lastRecalcPos = { lat: oLat, lng: oLng };
                            window.navWatchId = navigator.geolocation.watchPosition((pos) => {
                                const curLat = Number(pos.coords.latitude);
                                const curLng = Number(pos.coords.longitude);

                                console.log("ACTUAL GPS UPDATE:", {
                                    latitude: curLat,
                                    longitude: curLng,
                                    accuracy: pos.coords.accuracy
                                });
                                
                                if (window.userMarker) {
                                    window.userMarker.position = { lat: curLat, lng: curLng };
                                    if (!window.userMarker.map) window.userMarker.map = window.campusMap;
                                }

                                // Arrival detection: 20m threshold
                                const distToDest = calculateDistance(curLat, curLng, dLat, dLng);
                                if (distToDest < 20) {
                                    console.log("Arrived at destination!");
                                    if (window.navWatchId && window.navWatchId !== "ARRIVED") {
                                        navigator.geolocation.clearWatch(window.navWatchId);
                                        window.navWatchId = "ARRIVED"; // Special state to stop recalculation
                                    }
                                    return;
                                }

                                const moved = calculateDistance(curLat, curLng, lastRecalcPos.lat, lastRecalcPos.lng);
                                if (moved > 25 && window.navWatchId !== "ARRIVED") { 
                                    lastRecalcPos = { lat: curLat, lng: curLng };
                                    startRouting(curLat, curLng, false); // Don't fit bounds on update
                                }

                                // Follow user
                                if (window.campusMap) {
                                    window.campusMap.panTo({ lat: curLat, lng: curLng });
                                }

                            }, (err) => console.warn("Nav tracking error:", err.code, err.message), 
                            { enableHighAccuracy: true, timeout: 30000, maximumAge: 3000 });
                        }
                    } catch (e) {
                        console.error("Routes API Error:", e);
                        alert("Routing failed: " + e.message);
                    }
                };

                await startRouting(originLat, originLng, true);
            };

            const backBtn = document.createElement("button");
            backBtn.id = "native-back-btn";
            backBtn.innerText = "← BACK";
            backBtn.style.cssText = "position:absolute; top:20px; left:20px; z-index:1000; padding:12px 24px; background:#0F0F23; color:#6C63FF; border:2px solid #6C63FF; cursor:pointer; border-radius:12px; font-weight:bold; box-shadow: 0 4px 12px rgba(0,0,0,0.5);";
            backBtn.onclick = () => { onBack(); };
            element.appendChild(backBtn);

            const pin = new PinElement({ background: "#4285F4", borderColor: "white", glyphColor: "white", scale: 0.8 });
            window.userMarker = new AdvancedMarkerElement({ map: null, content: pin, title: "My Location" });

            window.campusMap.addListener("click", (e) => {
                if (window.isSelectingOnMap) {
                    window.isSelectingOnMap = false;
                    window.runRoutingJS({ lat: e.latLng.lat(), lng: e.latLng.lng() }, window.activeDest);
                }
            });

        } else {
            window.campusMap.setCenter({ lat: Number(lat), lng: Number(lng) });
            window.campusMap.setZoom(Number(zoom));
        }
        
        window.AdvancedMarkerElement = AdvancedMarkerElement;
        window.mapInitialized = true;
    };
    init();
}""")
private external fun startWebMapLifecycleInternal(
    element: HTMLElement, 
    lat: Double, 
    lng: Double, 
    zoom: Float, 
    onBack: () -> Unit,
    onLocationUpdate: (Double, Double) -> Unit,
    onEndNav: () -> Unit
)

private fun startWebMapLifecycle(
    element: HTMLElement, 
    lat: Double, 
    lng: Double, 
    zoom: Float, 
    onBack: () -> Unit,
    onLocationUpdate: (Double, Double) -> Unit,
    onEndNav: () -> Unit
) = startWebMapLifecycleInternal(element, lat, lng, zoom, onBack, onLocationUpdate, onEndNav)

@JsFun("""() => { if (window.mapMarkers) { window.mapMarkers.forEach(m => m.map = null); } window.mapMarkers = []; }""")
private external fun clearWebMarkers()

@JsFun("""(lat, lng, title, id, isSelected, onLocationSelected) => {
    (async () => {
        while (!window.mapInitialized || !window.AdvancedMarkerElement) {
            await new Promise(resolve => setTimeout(resolve, 50));
        }

        const marker = new window.AdvancedMarkerElement({
            map: window.campusMap,
            position: { lat: Number(lat), lng: Number(lng) },
            title: title
        });
        
        const infoWindow = new google.maps.InfoWindow({
            content: '<div style="color:black; padding:12px; font-family: sans-serif; min-width: 180px;"><div style="font-weight:bold; margin-bottom:12px; font-size:16px; color:#0F0F23;">' + title + '</div><button id="nav-btn-wasm-' + id + '" style="width:100%; padding:12px; background:#6C63FF; color:white; border:none; border-radius:10px; cursor:pointer; font-weight:bold; font-size:14px; box-shadow: 0 2px 4px rgba(0,0,0,0.2);">NAVIGATE</button></div>'
        });

        const setupNavBtn = () => {
            const btn = document.getElementById('nav-btn-wasm-' + id);
            if (btn) {
                btn.onclick = (e) => {
                    e.stopPropagation();
                    if (window.showOriginSelectionPopup) window.showOriginSelectionPopup(String(id));
                    infoWindow.close();
                };
            }
        };

        marker.addEventListener("gmp-click", () => {
            infoWindow.open(window.campusMap, marker);
            google.maps.event.addListenerOnce(infoWindow, 'domready', setupNavBtn);
            onLocationSelected(String(id));
        });
        
        if (isSelected) {
            infoWindow.open(window.campusMap, marker);
            google.maps.event.addListenerOnce(infoWindow, 'domready', setupNavBtn);
        }

        if (!window.mapMarkers) window.mapMarkers = [];
        window.mapMarkers.push(marker);
    })();
}""")
private external fun addWebMarkerInternal(lat: Double, lng: Double, title: String, id: String, isSelected: Boolean, onLocationSelected: (String) -> Unit)

private fun addWebMarker(lat: Double, lng: Double, title: String, id: String, isSelected: Boolean, onLocationSelected: (String) -> Unit) = 
    addWebMarkerInternal(lat, lng, title, id, isSelected, onLocationSelected)


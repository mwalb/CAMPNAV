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

        const { Map, LatLngBounds, Polyline } = await google.maps.importLibrary("maps");
        const { AdvancedMarkerElement, PinElement } = await google.maps.importLibrary("marker");
        const { Route } = await google.maps.importLibrary("routes");
        const { encoding, spherical } = await google.maps.importLibrary("geometry");

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
            fullscreenControl: false,
            tilt: 0,
            heading: 0
        };

        if (!window.campusMap) {
            window.campusMap = new Map(element, mapOptions);
            
            // Internal navigation state
            window.navState = {
                active: false,
                mode: "DRIVING",
                steps: [],
                currentStepIndex: 0,
                isFollowing: true,
                voiceEnabled: true,
                lastHeading: 0,
                destination: null,
                drivingPath: [],
                walkingPath: [],
                recalculateCooldown: 0,
                lastAnnouncedStep: -1,
                lastAnnouncedDist: -1
            };

            window.navPolylines = [];

            const overlay = document.createElement("div");
            overlay.id = "native-map-overlay";
            overlay.style.cssText = "position:absolute; top:0; left:0; right:0; bottom:0; z-index:999; background:rgba(0,0,0,0.7); display:none; pointer-events:auto; backdrop-filter: blur(2px);";
            element.appendChild(overlay);

            // 1. TOP NAVIGATION BANNER (Includes "Then" upcoming maneuver)
            const topBanner = document.createElement("div");
            topBanner.id = "nav-top-banner";
            topBanner.style.cssText = "position:absolute; top:0; left:0; right:0; z-index:1100; background:#004D40; color:white; padding:16px; display:none; flex-direction:column; font-family: sans-serif; box-shadow: 0 4px 12px rgba(0,0,0,0.3);";
            element.appendChild(topBanner);

            // 3. BOTTOM TRIP BAR
            const bottomBar = document.createElement("div");
            bottomBar.id = "nav-bottom-bar";
            bottomBar.style.cssText = "position:absolute; bottom:0; left:0; right:0; z-index:1100; background:white; color:#3C4043; padding:16px; display:none; flex-direction:row; align-items:center; justify-content:space-between; font-family: sans-serif; box-shadow: 0 -4px 12px rgba(0,0,0,0.1); border-radius: 20px 20px 0 0;";
            element.appendChild(bottomBar);

            // 4. FLOATING CONTROLS
            const fabContainer = document.createElement("div");
            fabContainer.id = "nav-fab-container";
            fabContainer.style.cssText = "position:absolute; right:16px; bottom:120px; z-index:1100; display:none; flex-direction:column; gap:12px;";
            element.appendChild(fabContainer);

            const createFab = (icon, id, onClick) => {
                const btn = document.createElement("button");
                btn.id = id;
                btn.innerHTML = icon;
                btn.style.cssText = "width:56px; height:56px; border-radius:28px; background:white; border:none; box-shadow: 0 4px 12px rgba(0,0,0,0.2); cursor:pointer; display:flex; align-items:center; justify-content:center; font-size: 24px; color:#3C4043;";
                btn.onclick = onClick;
                fabContainer.appendChild(btn);
                return btn;
            };

            const recentreBtn = document.createElement("button");
            recentreBtn.id = "nav-recentre-btn";
            recentreBtn.innerHTML = '<span style="margin-right:8px;">🎯</span> Re-centre';
            recentreBtn.style.cssText = "position:absolute; bottom:120px; left:50%; transform:translateX(-50%); z-index:1100; padding:12px 24px; background:white; color:#005C53; border:none; border-radius:28px; box-shadow: 0 4px 12px rgba(0,0,0,0.2); cursor:pointer; font-weight:bold; display:none; font-size:16px; align-items:center;";
            recentreBtn.onclick = () => {
                window.navState.isFollowing = true;
                recentreBtn.style.display = "none";
                const markerPos = window.userMarker ? window.userMarker.position : null;
                if (markerPos && window.campusMap) {
                    window.campusMap.panTo(markerPos);
                    window.campusMap.setZoom(19);
                    window.campusMap.setTilt(45);
                    const heading = window.navState.lastHeading;
                    if (heading !== undefined) window.campusMap.setHeading(heading);
                }
            };
            element.appendChild(recentreBtn);

            const compassFab = createFab("🧭", "nav-compass-fab", () => {
                if (window.campusMap) {
                    const currentHeading = window.campusMap.getHeading();
                    if (currentHeading !== 0) {
                        window.campusMap.setHeading(0);
                    } else {
                        window.campusMap.setHeading(window.navState.lastHeading || 0);
                    }
                }
            });

            const searchFab = createFab("🔍", "nav-search-fab", () => {
                // Future: Open search along route
                alert("Search along route - Coming soon");
            });

            const voiceFab = createFab("🔊", "nav-voice-fab", () => {
                window.navState.voiceEnabled = !window.navState.voiceEnabled;
                voiceFab.innerHTML = window.navState.voiceEnabled ? "🔊" : "🔇";
            });

            const navPanel = document.createElement("div");
            navPanel.id = "native-nav-panel";
            navPanel.style.cssText = "position:absolute; z-index:1200; display:none; flex-direction:column; font-family: sans-serif;";
            element.appendChild(navPanel);
            
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
                if (!origin || !destinationObj) {
                    alert("Unable to start navigation: Missing origin or destination.");
                    return;
                }

                const originLat = Number(origin?.lat);
                const originLng = Number(origin?.lng);
                const destinationLat = Number(destinationObj.entranceLatitude != null ? destinationObj.entranceLatitude : destinationObj.latitude);
                const destinationLng = Number(destinationObj.entranceLongitude != null ? destinationObj.entranceLongitude : destinationObj.longitude);

                const speak = (text) => {
                    if (window.navState.voiceEnabled && window.speechSynthesis) {
                        const utterance = new SpeechSynthesisUtterance(text);
                        window.speechSynthesis.cancel();
                        window.speechSynthesis.speak(utterance);
                    }
                };

                const getManeuverIcon = (maneuver) => {
                    const m = (maneuver || "").toLowerCase().replace(/_/g, "-");
                    if (m.includes("turn-left")) return "←";
                    if (m.includes("turn-right")) return "→";
                    if (m.includes("turn-slight-left")) return "↖";
                    if (m.includes("turn-slight-right")) return "↗";
                    if (m.includes("turn-sharp-left")) return "↙";
                    if (m.includes("turn-sharp-right")) return "↘";
                    if (m.includes("u-turn")) return "U";
                    if (m.includes("straight")) return "↑";
                    if (m.includes("merge")) return "↑";
                    if (m.includes("ramp")) return "↗";
                    if (m.includes("fork")) return "Y";
                    if (m.includes("roundabout")) return "O";
                    return "↑";
                };

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

                const updateNavUI = (stepIndex) => {
                    const step = window.navState.steps[stepIndex];
                    const nextStep = window.navState.steps[stepIndex + 1];
                    
                    topBanner.style.display = "flex";
                    let bannerHTML = '<div style="display:flex; align-items:center; gap:16px;">' +
                        '<div style="font-size:48px; font-weight:bold; min-width:60px; text-align:center;">' + (step?.isWalking ? "🚶" : getManeuverIcon(step?.maneuver)) + '</div>' +
                        '<div style="flex:1;">' +
                            '<div style="font-size:22px; font-weight:bold;">' + (step?.instructions || "Head to destination") + '</div>' +
                            '<div id="nav-step-dist" style="font-size:18px; opacity:0.9; margin-top:4px;"></div>' +
                        '</div>' +
                        '<div style="width:44px; height:44px; background:rgba(255,255,255,0.2); border-radius:22px; display:flex; align-items:center; justify-content:center; cursor:pointer; font-size:20px;">✨</div>' +
                    '</div>';

                    if (nextStep) {
                        bannerHTML += '<div style="margin-top:12px; padding-top:12px; border-top:1px solid rgba(255,255,255,0.1); font-size:15px; opacity:0.9; display:flex; align-items:center; gap:8px;">' +
                            '<span>Then</span> <span style="font-weight:bold;">' + (nextStep.isWalking ? "🚶" : getManeuverIcon(nextStep.maneuver)) + '</span> ' + nextStep.instructions +
                            '</div>';
                    }
                    
                    topBanner.innerHTML = bannerHTML;

                    bottomBar.style.display = "flex";
                    
                    let remDist = 0;
                    let remDur = 0;
                    for (let i = stepIndex; i < window.navState.steps.length; i++) {
                        remDist += (Number(window.navState.steps[i].distanceMeters) || 0);
                        remDur += (Number(window.navState.steps[i].durationMillis || window.navState.steps[i].staticDurationMillis) || 0);
                    }
                    
                    const durationMin = Math.ceil(remDur / 60000);
                    const distText = remDist >= 1000 ? (remDist/1000).toFixed(1) + " km" : Math.round(remDist) + " m";
                    const arrivalTime = new Date(Date.now() + remDur);
                    const eta = arrivalTime.getHours().toString().padStart(2, '0') + ":" + arrivalTime.getMinutes().toString().padStart(2, '0');

                    bottomBar.innerHTML = '<div id="nav-exit-btn" style="font-size:28px; cursor:pointer; padding:12px; color:#5F6368;">✕</div>' +
                        '<div style="flex:1; display:flex; flex-direction:column; align-items:center;">' +
                            '<div style="display:flex; align-items:center; gap:8px;">' +
                                '<span style="color:#D96B00; font-weight:bold; font-size:24px;">' + durationMin + ' min</span>' +
                                '<span style="color:#1E8E3E; font-size:20px;">🌿</span>' +
                            '</div>' +
                            '<div style="color:#5F6368; font-size:16px; font-weight:500;">' + distText + ' • ' + eta + '</div>' +
                        '</div>' +
                        '<div id="nav-overview-btn" style="font-size:28px; cursor:pointer; padding:12px; color:#5F6368;">▢</div>';

                    document.getElementById("nav-exit-btn").onclick = () => stopNavigation();
                    document.getElementById("nav-overview-btn").onclick = () => {
                        const bounds = new google.maps.LatLngBounds();
                        window.navState.drivingPath.forEach(p => bounds.extend(p));
                        window.navState.walkingPath.forEach(p => bounds.extend(p));
                        window.campusMap.fitBounds(bounds, { top: 150, right: 50, bottom: 200, left: 50 });
                        window.navState.isFollowing = false;
                        recentreBtn.style.display = "flex";
                    };
                };

                const stopNavigation = () => {
                    if (window.navWatchId) {
                        navigator.geolocation.clearWatch(window.navWatchId);
                        window.navWatchId = null;
                    }
                    if (window.navPolylines) {
                        window.navPolylines.forEach(p => p.setMap(null));
                        window.navPolylines = [];
                    }
                    window.navState.active = false;
                    topBanner.style.display = "none";
                    bottomBar.style.display = "none";
                    fabContainer.style.display = "none";
                    recentreBtn.style.display = "none";
                    if (window.campusMap) {
                        window.campusMap.setTilt(0);
                        window.campusMap.setHeading(0);
                    }
                    window.onEndNav();
                };

                const startRouting = async (startLat, startLng, fitToMap = false) => {
                    const drivingReq = {
                        origin: { lat: Number(startLat), lng: Number(startLng) },
                        destination: { lat: Number(destinationLat), lng: Number(destinationLng) },
                        travelMode: 'DRIVING',
                        polylineQuality: 'HIGH_QUALITY',
                        fields: ['path', 'distanceMeters', 'durationMillis', 'viewport', 'legs']
                    };

                    try {
                        const { Route } = await google.maps.importLibrary("routes");
                        const response = await Route.computeRoutes(drivingReq);
                        if (!response.routes || response.routes.length === 0) {
                            alert("No driving route found.");
                            return;
                        }
                        
                        const drivingRoute = response.routes[0];
                        const route = drivingRoute;
                        const routeEnd = drivingRoute.path[drivingRoute.path.length - 1];
                        const endLat = typeof routeEnd.lat === "function" ? routeEnd.lat() : (routeEnd.lat || routeEnd.latitude);
                        const endLng = typeof routeEnd.lng === "function" ? routeEnd.lng() : (routeEnd.lng || routeEnd.longitude);
                        const drivingEndpoint = { lat: Number(endLat), lng: Number(endLng) };
                        const destination = { lat: Number(destinationObj.latitude), lng: Number(destinationObj.longitude) };
                        const distToEnd = calculateDistance(drivingEndpoint.lat, drivingEndpoint.lng, destination.lat, destination.lng);
                        const walkingRequired = distToEnd > 20;

                        console.log("[CAMPNAV] Driving route received", route);
                        console.log("[CAMPNAV] Driving endpoint", drivingEndpoint);
                        console.log("[CAMPNAV] Exact destination", destination);
                        console.log("[CAMPNAV] Walking required", walkingRequired);

                        window.navState.active = true;
                        window.navState.mode = "DRIVING";
                        window.navState.drivingPath = drivingRoute.path;
                        window.navState.walkingPath = [];
                        
                        let allSteps = drivingRoute.legs?.flatMap(leg => leg.steps ?? []) ?? [];

                        if (window.navPolylines) window.navPolylines.forEach(p => p.setMap(null));
                        window.navPolylines = [];

                        const drivingPolys = drivingRoute.createPolylines({
                            polylineOptions: {
                                strokeColor: "#3800D8",
                                strokeOpacity: 0.95,
                                strokeWeight: 12,
                                zIndex: 11
                            }
                        });
                        drivingPolys.forEach(p => {
                            p.setMap(window.campusMap);
                            window.navPolylines.push(p);
                        });

                        const actualDest = { lat: Number(destinationObj.latitude), lng: Number(destinationObj.longitude) };

                        if (walkingRequired) {
                            const walkingReq = {
                                origin: { lat: drivingEndpoint.lat, lng: drivingEndpoint.lng },
                                destination: { lat: actualDest.lat, lng: actualDest.lng },
                                travelMode: 'WALK',
                                polylineQuality: 'HIGH_QUALITY',
                                fields: ['path', 'distanceMeters', 'durationMillis', 'legs']
                            };
                            try {
                                const walkResponse = await Route.computeRoutes(walkingReq);
                                if (walkResponse.routes && walkResponse.routes.length > 0) {
                                    const walkRoute = walkResponse.routes[0];
                                    const walkingRoute = walkRoute;
                                    console.log("[CAMPNAV] Walking route received", walkingRoute);

                                    window.navState.walkingPath = walkRoute.path;
                                    
                                    const walkPolys = walkRoute.createPolylines({
                                        polylineOptions: {
                                            strokeColor: "#5F6368",
                                            strokeOpacity: 0,
                                            icons: [{
                                                icon: { path: google.maps.SymbolPath.CIRCLE, fillOpacity: 1, scale: 4, strokeColor: "#5F6368", strokeWeight: 0 },
                                                offset: '0px', repeat: '15px'
                                            }],
                                            zIndex: 12
                                        }
                                    });
                                    walkPolys.forEach(p => {
                                        p.setMap(window.campusMap);
                                        window.navPolylines.push(p);
                                    });
                                    
                                    const walkSteps = walkRoute.legs?.flatMap(leg => leg.steps ?? []) ?? [];
                                    if (walkSteps.length > 0) {
                                        walkSteps.forEach(s => s.isWalking = true);
                                        allSteps.push({
                                            instructions: "Park and continue on foot",
                                            maneuver: "straight",
                                            endLocation: walkSteps[0].startLocation,
                                            distanceMeters: distToEnd,
                                            isWalking: true
                                        });
                                        allSteps = allSteps.concat(walkSteps);
                                    }
                                }
                            } catch (e) { console.warn("Walking route failed", e); }
                        }

                        window.navState.steps = allSteps;
                        window.navState.currentStepIndex = 0;

                        const routeStart = drivingRoute.path[0];
                        const startPtLat = typeof routeStart.lat === "function" ? routeStart.lat() : (routeStart.lat || routeStart.latitude);
                        const startPtLng = typeof routeStart.lng === "function" ? routeStart.lng() : (routeStart.lng || routeStart.longitude);
                        const distFromStart = calculateDistance(Number(startLat), Number(startLng), Number(startPtLat), Number(startPtLng));
                        if (distFromStart > 15) {
                            const originReq = {
                                origin: { lat: Number(startLat), lng: Number(startLng) },
                                destination: { lat: Number(startPtLat), lng: Number(startPtLng) },
                                travelMode: 'WALK',
                                polylineQuality: 'HIGH_QUALITY',
                                fields: ['path']
                            };
                            try {
                                const originResponse = await Route.computeRoutes(originReq);
                                if (originResponse.routes && originResponse.routes.length > 0) {
                                    const originRoute = originResponse.routes[0];
                                    const originPolys = originRoute.createPolylines({
                                        polylineOptions: {
                                            strokeColor: "#5F6368",
                                            strokeOpacity: 0,
                                            icons: [{
                                                icon: { path: google.maps.SymbolPath.CIRCLE, fillOpacity: 1, scale: 4, strokeColor: "#5F6368", strokeWeight: 0 },
                                                offset: '0px', repeat: '15px'
                                            }],
                                            zIndex: 12
                                        }
                                    });
                                    originPolys.forEach(p => {
                                        p.setMap(window.campusMap);
                                        window.navPolylines.push(p);
                                    });
                                }
                            } catch (err) {
                                console.warn("Origin walking route failed", err);
                            }
                        }

                        if (fitToMap && window.campusMap) {
                            window.campusMap.setTilt(45);
                            window.campusMap.panTo({ lat: Number(startLat), lng: Number(startLng) });
                            window.campusMap.setZoom(19);
                        }

                        fabContainer.style.display = "flex";
                        updateNavUI(0);
                        speak(allSteps[0]?.instructions || "Head to destination");

                        if (!window.navWatchId) {
                            window.navWatchId = navigator.geolocation.watchPosition((pos) => {
                                if (!window.navState.active) return;
                                const curLat = Number(pos.coords.latitude);
                                const curLng = Number(pos.coords.longitude);
                                const heading = pos.coords.heading;
                                if (heading !== null) window.navState.lastHeading = heading;

                                if (window.userMarker) {
                                    window.userMarker.position = { lat: curLat, lng: curLng };
                                    if (!window.userMarker.map) window.userMarker.map = window.campusMap;
                                }

                                if (window.navState.isFollowing && window.campusMap) {
                                    window.campusMap.panTo({ lat: curLat, lng: curLng });
                                    if (heading !== null) window.campusMap.setHeading(heading);
                                }

                                const distToDest = calculateDistance(curLat, curLng, actualDest.lat, actualDest.lng);
                                if (distToDest < 15) {
                                    speak("You have arrived at your destination.");
                                    stopNavigation();
                                    return;
                                }

                                const currentStep = window.navState.steps[window.navState.currentStepIndex];
                                if (currentStep) {
                                    window.navState.mode = currentStep.isWalking ? "WALKING" : "DRIVING";
                                    const nextStepLoc = currentStep.endLocation;
                                    const distToManeuver = calculateDistance(curLat, curLng, nextStepLoc.lat, nextStepLoc.lng);
                                    const distEl = document.getElementById("nav-step-dist");
                                    if (distEl) distEl.innerText = distToManeuver >= 1000 ? (distToManeuver/1000).toFixed(1) + " km" : Math.round(distToManeuver) + " m";

                                    if (distToManeuver < 20) {
                                        window.navState.currentStepIndex++;
                                        if (window.navState.currentStepIndex < window.navState.steps.length) {
                                            updateNavUI(window.navState.currentStepIndex);
                                            speak(window.navState.steps[window.navState.currentStepIndex].instructions);
                                        }
                                    } else if (distToManeuver < 100 && window.navState.lastAnnouncedDist !== 100 && window.navState.lastAnnouncedStep !== window.navState.currentStepIndex) {
                                        speak("In 100 meters, " + currentStep.instructions);
                                        window.navState.lastAnnouncedDist = 100;
                                        window.navState.lastAnnouncedStep = window.navState.currentStepIndex;
                                    }
                                }

                                if (window.navState.recalculateCooldown > 0) {
                                    window.navState.recalculateCooldown--;
                                } else {
                                    const activePath = window.navState.mode === "WALKING" ? window.navState.walkingPath : window.navState.drivingPath;
                                    let minOffRouteDist = 100000;
                                    for (let i=0; i < activePath.length; i+=5) {
                                        const pt = activePath[i];
                                        const ptLat = typeof pt.lat === "function" ? pt.lat() : (pt.lat || pt.latitude);
                                        const ptLng = typeof pt.lng === "function" ? pt.lng() : (pt.lng || pt.longitude);
                                        const d = calculateDistance(curLat, curLng, Number(ptLat), Number(ptLng));
                                        if (d < minOffRouteDist) minOffRouteDist = d;
                                    }
                                    if (minOffRouteDist > 60) {
                                        window.navState.recalculateCooldown = 15;
                                        startRouting(curLat, curLng, false);
                                    }
                                }
                            }, (err) => console.warn(err), { enableHighAccuracy: true, timeout: 30000, maximumAge: 3000 });
                        }
                    } catch (e) { console.error("Routes error", e); }
                };

                await startRouting(originLat, originLng, true);
            };

            const backBtn = document.createElement("button");
            backBtn.id = "native-back-btn";
            backBtn.innerText = "← BACK";
            backBtn.style.cssText = "position:absolute; top:20px; left:20px; z-index:1000; padding:12px 24px; background:#0F0F23; color:#6C63FF; border:2px solid #6C63FF; cursor:pointer; border-radius:12px; font-weight:bold; box-shadow: 0 4px 12px rgba(0,0,0,0.5); transition: opacity 0.2s;";
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
            window.campusMap.addListener("dragstart", () => {
                if (window.navState.active) {
                    window.navState.isFollowing = false;
                    recentreBtn.style.display = "flex";
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


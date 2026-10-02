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
import org.com.campus.navigation.NavigationStatus
import org.com.campus.navigation.RoutePoint
import org.w3c.dom.HTMLElement

@Composable
actual fun CampusMap(
    modifier: Modifier,
    university: University,
    locations: List<CampusLocation>,
    allLocations: List<CampusLocation>,
    onLocationSelected: (CampusLocation) -> Unit,
    onBack: () -> Unit,
    initialSelectedLocation: CampusLocation?,
    navigationState: NavigationState,
    onStartNavigation: (CampusLocation, CampusLocation?) -> Unit,
    onEndNavigation: () -> Unit,
    onLocationUpdate: (RoutePoint) -> Unit,
    onMapClick: (RoutePoint) -> Unit,
    onStatusChange: (NavigationStatus) -> Unit
) {
    Box(modifier = modifier)

    LaunchedEffect(university, locations, allLocations, navigationState.destination, navigationState.selectedStartPoint) {
        val mapDiv = document.getElementById("campus-map") as? HTMLElement
        if (mapDiv != null) {
            mapDiv.style.display = "block"
            onEndNavInternal = onEndNavigation
            onStatusChangeInternal = onStatusChange
            
            val selectedDest = navigationState.destination ?: initialSelectedLocation
            val startLat = selectedDest?.latitude ?: university.latitude ?: 0.0
            val startLng = selectedDest?.longitude ?: university.longitude ?: 0.0
            val startZoom = if (selectedDest != null) 18f else university.defaultZoom ?: 15f
            
            startWebMapLifecycle(
                mapDiv, 
                startLat, 
                startLng, 
                startZoom,
                onBack,
                { lat, lng -> onLocationUpdate(RoutePoint(lat, lng)) },
                { onEndNavInternal() },
                { lat, lng -> onMapClick(RoutePoint(lat, lng)) },
                { status -> onStatusChangeInternal(NavigationStatus.valueOf(status)) }
            )
            
            syncWebMarkers(locations, selectedDest?.id, navigationState.selectedStartPoint, onLocationSelected)
        }
    }

    LaunchedEffect(navigationState, allLocations) {
        val locationsJson = Json.encodeToString(allLocations)
        syncWebLocations(locationsJson)

        navigationState.destination?.let { dest ->
            val destJson = Json.encodeToString(dest)
            syncSelectedDestinationJS(destJson)
        }
        
        when (navigationState.status) {
            NavigationStatus.SHOWING_NAV_CHOICE -> {
                navigationState.destination?.let { dest ->
                    println("[CAMPNAV][NAV] navigate clicked: ${dest.name}")
                    println("[CAMPNAV][NAV] destination id: ${dest.id}")
                    println("[CAMPNAV][NAV] destination latitude: ${dest.latitude}")
                    println("[CAMPNAV][NAV] destination longitude: ${dest.longitude}")
                    triggerNavChoiceJS(dest.id.toString(), dest.name)
                }
            }
            NavigationStatus.SELECTING_START_POINT -> {
                println("[CAMPNAV][NAV] starting-point dialog opened")
                triggerStartPointSelectionJS()
            }
            NavigationStatus.SELECTING_ORIGIN -> {
                navigationState.destination?.let { triggerOriginSelectionJS(it.id.toString()) }
            }
            else -> {
                hideNavOverlaysJS()
            }
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
                document.getElementById("select-point-overlay")?.remove()
                stopTrackingUserLocation()
            }
            println("[CAMPNAV][NAV] navigation state reset")
            resetWebMapState()
        }
    }
}

private var onEndNavInternal: () -> Unit = {}
private var onStatusChangeInternal: (NavigationStatus) -> Unit = {}

private fun syncWebMarkers(locations: List<CampusLocation>, selectedId: Long?, startPoint: RoutePoint?, onLocationSelected: (CampusLocation) -> Unit) {
    clearWebMarkers()
    locations.forEach { location ->
        val isSelected = (location.id == selectedId)
        addWebMarker(
            location.latitude,
            location.longitude,
            location.name,
            location.id.toString(),
            isSelected,
            onLocationSelected = { id ->
                val loc = locations.find { it.id.toString() == id }
                if (loc != null) onLocationSelected(loc)
            }
        )
    }
    
    if (startPoint != null) {
        addWebMarker(
            startPoint.latitude,
            startPoint.longitude,
            "Start Point",
            "selected_start",
            false,
            onLocationSelected = {}
        )
    }
}

@JsFun("(destId, destName) => { if (window.showNavChoicePopup) window.showNavChoicePopup(destId, destName); }")
private external fun triggerNavChoiceJS(destId: String, destName: String)

@JsFun("() => { if (window.showStartPointSelectionOverlay) window.showStartPointSelectionOverlay(); }")
private external fun triggerStartPointSelectionJS()

@JsFun("() => { if (window.hideNavOverlays) window.hideNavOverlays(); }")
private external fun hideNavOverlaysJS()

@JsFun("(destId) => { if (window.showOriginSelectionPopup) window.showOriginSelectionPopup(destId); }")
private external fun triggerOriginSelectionJS(destId: String)

@JsFun("(locationsJson) => { window.campusLocationsData = JSON.parse(locationsJson); }")
private external fun syncWebLocations(locationsJson: String)

@JsFun("(destJson) => { try { window.lastSelectedDest = JSON.parse(destJson); } catch(e){} }")
private external fun syncSelectedDestinationJS(destJson: String)

@JsFun("""() => { if (window.mapMarkers) { window.mapMarkers.forEach(m => m.map = null); } window.mapMarkers = []; window.campusMap = null; window.mapInitialized = false; }""")
private external fun resetWebMapState()

@JsFun("""() => {
    if (navigator.geolocation && !window.watchId) {
        const options = { enableHighAccuracy: true, timeout: 15000, maximumAge: 3000 };
        window.watchId = navigator.geolocation.watchPosition((pos) => {
            const lat = Number(pos.coords.latitude);
            const lng = Number(pos.coords.longitude);
            if (window.onLocationUpdate) window.onLocationUpdate(lat, lng);
            if (window.userMarker && window.campusMap) {
                window.userMarker.position = { lat: lat, lng: lng };
                if (!window.userMarker.map) window.userMarker.map = window.campusMap;
            }
        }, (err) => {
            console.warn("[CAMPNAV] Location tracking warning:", err.message);
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

@JsFun("""(element, lat, lng, zoom, onBack, onLocationUpdate, onEndNav, onMapClick, onStatusChange) => {
    const init = () => {
        if (element.clientWidth === 0 || element.clientHeight === 0) {
            setTimeout(init, 50);
            return;
        }

        if (!window.google || !window.google.maps || !window.google.maps.importLibrary) {
            setTimeout(init, 100);
            return;
        }

        Promise.all([
            google.maps.importLibrary("maps"),
            google.maps.importLibrary("marker")
        ]).then(([mapsLib, markerLib]) => {
            const { Map, LatLngBounds, Polyline } = mapsLib;
            const { AdvancedMarkerElement } = markerLib;
            
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
                window.navState = { active: false };
            } else {
                window.campusMap.setCenter({ lat: Number(lat), lng: Number(lng) });
                window.campusMap.setZoom(Number(zoom));
            }

            window.onLocationUpdate = onLocationUpdate;
            window.onEndNav = onEndNav;
            window.onStatusChange = onStatusChange;
            window.onMapClick = onMapClick;

            window.openGoogleMapsNavigation = (oLat, oLng, dLat, dLng) => {
                console.log("[CAMPNAV][NAV] maps URL generated");
                console.log("[CAMPNAV][NAV] maps launch: origin=" + oLat + "," + oLng + " dest=" + dLat + "," + dLng);
                const url = 'https://www.google.com/maps/dir/?api=1&origin=' + oLat + ',' + oLng + '&destination=' + dLat + ',' + dLng + '&travelmode=driving';
                window.open(url, '_blank');
            };

            window.getPositionWithFallback = (onSuccess, onError) => {
                if (!navigator.geolocation) {
                    onError("Geolocation not supported");
                    return;
                }
                navigator.geolocation.getCurrentPosition(
                    (pos) => onSuccess(pos),
                    (err1) => {
                        console.warn("[CAMPNAV] High accuracy location timeout/fail, trying standard accuracy...", err1.message);
                        navigator.geolocation.getCurrentPosition(
                            (pos2) => onSuccess(pos2),
                            (err2) => {
                                console.warn("[CAMPNAV] Standard location fail:", err2.message);
                                onError(err2.message || "Location access failed");
                            },
                            { enableHighAccuracy: false, timeout: 10000, maximumAge: 300000 }
                        );
                    },
                    { enableHighAccuracy: true, timeout: 5000, maximumAge: 30000 }
                );
            };

            window.hideNavOverlays = () => {
                const overlay = document.getElementById("native-map-overlay");
                if (overlay) overlay.style.display = "none";
                const navPanel = document.getElementById("native-nav-panel");
                if (navPanel) navPanel.style.display = "none";
                const selOverlay = document.getElementById("select-point-overlay");
                if (selOverlay) selOverlay.style.display = "none";
            };

            window.showNavChoicePopup = (destId, destName) => {
                const sessionId = Math.random().toString(36).substring(2, 9);
                window.currentNavSessionId = sessionId;
                console.log("[CAMPNAV][NAV] starting-point dialog opened");
                console.log("[CAMPNAV][NAV] destination id: " + destId);

                const locations = window.campusLocationsData || [];
                let dest = locations.find(l => String(l.id) === String(destId));
                if (!dest && window.lastSelectedDest && String(window.lastSelectedDest.id) === String(destId)) {
                    dest = window.lastSelectedDest;
                }
                if (!dest && window.lastSelectedDest) {
                    dest = window.lastSelectedDest;
                }
                if (!dest) {
                    console.warn("[CAMPNAV] Destination missing for ID:", destId);
                    return;
                }
                window.lastSelectedDest = dest;

                const mapDiv = document.getElementById("campus-map");
                if (!mapDiv) return;

                let overlay = document.getElementById("native-map-overlay");
                if (!overlay) {
                    overlay = document.createElement("div");
                    overlay.id = "native-map-overlay";
                    mapDiv.appendChild(overlay);
                }
                overlay.style.cssText = "position:absolute; top:0; left:0; right:0; bottom:0; z-index:999; background:rgba(0,0,0,0.7); display:block; pointer-events:auto; backdrop-filter: blur(2px);";

                let navPanel = document.getElementById("native-nav-panel");
                if (!navPanel) {
                    navPanel = document.createElement("div");
                    navPanel.id = "native-nav-panel";
                    mapDiv.appendChild(navPanel);
                }

                navPanel.style.display = "flex";
                navPanel.style.cssText = "position:absolute; top:50%; left:50%; transform:translate(-50%, -50%); z-index:1200; background:#0F0F23; color:white; padding:32px; border-radius:24px; border:2px solid #6C63FF; display:flex; flex-direction:column; gap:16px; font-family: sans-serif; width: 85%; max-width: 400px; box-shadow: 0 8px 32px rgba(0,0,0,0.8);";

                navPanel.innerHTML = 
                    '<div style="text-align: center;">' +
                    '<div style="font-size: 14px; color: #AAA;">Navigate to</div>' +
                    '<div style="font-weight:bold; font-size: 24px; color: white; margin-top: 4px;">' + destName + '</div>' +
                    '<div style="font-size: 16px; color: white; margin-top: 16px; margin-bottom: 24px;">How would you like to start?</div>' +
                    '</div>';

                const btnStyle = "padding: 16px; background:#1A1A35; color:white; border:1px solid #6C63FF; border-radius:12px; cursor:pointer; font-weight: bold; font-size: 16px; text-align: left; display: flex; align-items: center; gap: 12px;";
                
                const currentLocBtn = document.createElement("button");
                currentLocBtn.innerHTML = "Use my current location";
                currentLocBtn.style.cssText = btnStyle;
                currentLocBtn.onclick = () => {
                    console.log("[CAMPNAV][NAV] origin selection: current location");
                    const navWindow = window.open('about:blank', '_blank');
                    window.getPositionWithFallback((pos) => {
                        const oLat = pos.coords.latitude;
                        const oLng = pos.coords.longitude;
                        console.log("[CAMPNAV][NAV] origin latitude: " + oLat);
                        console.log("[CAMPNAV][NAV] origin longitude: " + oLng);
                        console.log("[CAMPNAV][NAV] destination consumed: " + dest.name + " (" + dest.latitude + ", " + dest.longitude + ")");
                        const url = 'https://www.google.com/maps/dir/?api=1&origin=' + oLat + ',' + oLng + '&destination=' + dest.latitude + ',' + dest.longitude + '&travelmode=driving';
                        if (navWindow) {
                            navWindow.location.href = url;
                        } else {
                            window.open(url, '_blank');
                        }
                        window.hideNavOverlays();
                        console.log("[CAMPNAV][NAV] navigation state reset");
                        window.onEndNav();
                    }, (err) => {
                        if (navWindow) navWindow.close();
                        console.warn("[CAMPNAV] Geolocation failed:", err);
                        alert("We couldn't access your current location. Please allow location permissions in your browser or select a starting point manually.");
                        window.hideNavOverlays();
                        window.onEndNav();
                    });
                };
                navPanel.appendChild(currentLocBtn);

                const selectOnMapBtn = document.createElement("button");
                selectOnMapBtn.innerHTML = "Select starting point";
                selectOnMapBtn.style.cssText = btnStyle;
                selectOnMapBtn.onclick = () => {
                    console.log("[CAMPNAV][NAV] origin selection: select starting point");
                    window.hideNavOverlays();
                    window.onStatusChange("SELECTING_START_POINT");
                };
                navPanel.appendChild(selectOnMapBtn);

                const cancelBtn = document.createElement("button");
                cancelBtn.innerText = "Cancel";
                cancelBtn.style.cssText = "padding:12px; background:transparent; color:#FF4B4B; border:none; cursor:pointer; font-weight: bold; font-size: 16px; margin-top: 8px;";
                cancelBtn.onclick = () => { 
                    console.log("[CAMPNAV][NAV] navigation state reset");
                    window.hideNavOverlays(); 
                    window.onEndNav(); 
                };
                navPanel.appendChild(cancelBtn);
            };

            window.showStartPointSelectionOverlay = () => {
                console.log("[CAMPNAV][NAV] starting-point dialog opened (manual search)");
                const mapDiv = document.getElementById("campus-map");
                if (!mapDiv) return;

                let selOverlay = document.getElementById("select-point-overlay");
                if (!selOverlay) {
                    selOverlay = document.createElement("div");
                    selOverlay.id = "select-point-overlay";
                    mapDiv.appendChild(selOverlay);
                }
                selOverlay.style.display = "flex";
                selOverlay.style.flexDirection = "column";
                selOverlay.style.gap = "12px";
                selOverlay.style.position = "absolute";
                selOverlay.style.top = "20px";
                selOverlay.style.left = "50%";
                selOverlay.style.transform = "translateX(-50%)";
                selOverlay.style.zIndex = "1000";
                selOverlay.style.background = "white";
                selOverlay.style.color = "#3C4043";
                selOverlay.style.padding = "24px";
                selOverlay.style.borderRadius = "28px";
                selOverlay.style.boxShadow = "0 12px 48px rgba(0,0,0,0.25)";
                selOverlay.style.fontWeight = "bold";
                selOverlay.style.fontFamily = "sans-serif";
                selOverlay.style.width = "90%";
                selOverlay.style.maxWidth = "380px";

                selOverlay.innerHTML = 
                    '<div style="margin-bottom: 12px; text-align: center; color: #0F0F23; font-size: 20px; font-weight: 800;">Select a starting point</div>' +
                    '<div style="position: relative; width: 100%; margin-bottom: 8px;">' +
                    '<input id="start-point-search" type="text" placeholder="Search location..." style="padding: 16px 44px 16px 16px; border-radius: 16px; border: 2px solid #F0F0F0; width: 100%; box-sizing: border-box; font-size: 16px; outline: none; transition: all 0.2s; background: #F8F9FA;">' +
                    '<span style="position: absolute; right: 16px; top: 50%; transform: translateY(-50%); color: #AAA; font-size: 18px;">🔍</span>' +
                    '</div>' +
                    '<div id="start-point-results" style="display: none; flex-direction: column; gap: 4px; max-height: 180px; overflow-y: auto; background: white; border-radius: 16px; padding: 8px; border: 1px solid #EEE; font-weight: normal; margin-bottom: 12px; box-shadow: inset 0 2px 4px rgba(0,0,0,0.02);"></div>' +
                    '<button id="start-point-action-btn" style="width: 100%; padding: 16px; border-radius: 16px; border: none; background: #6C63FF; color: white; cursor: pointer; font-weight: 800; display: flex; align-items: center; justify-content: center; gap: 10px; font-size: 16px; box-shadow: 0 4px 12px rgba(108, 99, 255, 0.3);">Start</button>' +
                    '<div style="display: flex; gap: 10px; width: 100%; margin-top: 4px;">' +
                    '<button id="start-point-tap-map" style="flex: 1; padding: 14px; border-radius: 16px; border: none; background: #6C63FF; color: white; cursor: pointer; font-weight: 700; display: flex; align-items: center; justify-content: center; gap: 8px; font-size: 14px;">Tap on the Map</button>' +
                    '<button id="start-point-cancel" style="padding: 14px; border-radius: 16px; border: 2px solid #F0F0F0; background: white; color: #666; cursor: pointer; font-weight: 700; font-size: 14px;">Cancel</button>' +
                    '</div>';

                const searchInput = document.getElementById("start-point-search");
                const resultsDiv = document.getElementById("start-point-results");
                const actionBtn = document.getElementById("start-point-action-btn");
                const tapMapBtn = document.getElementById("start-point-tap-map");
                const cancelBtn = document.getElementById("start-point-cancel");
                
                let selectedStartLoc = null;

                searchInput.focus();
                searchInput.oninput = (e) => {
                    const query = e.target.value.toLowerCase().trim();
                    const locations = window.campusLocationsData || [];

                    if (query.length < 1) {
                        resultsDiv.style.display = "none";
                        return;
                    }
                    const filtered = locations.filter(l => 
                        l.name.toLowerCase().includes(query) || 
                        (l.officialName && l.officialName.toLowerCase().includes(query)) ||
                        (l.buildingCode && l.buildingCode.toLowerCase().includes(query))
                    ).slice(0, 8);
                    
                    if (filtered.length > 0) {
                        resultsDiv.style.display = "flex";
                        resultsDiv.innerHTML = "";
                        filtered.forEach(loc => {
                            const btn = document.createElement("div");
                            btn.innerText = loc.name;
                            btn.style.padding = "12px 14px";
                            btn.style.cursor = "pointer";
                            btn.style.fontSize = "14px";
                            btn.style.borderBottom = "1px solid #F5F5F5";
                            btn.onmouseover = () => { btn.style.background = "#F8F9FA"; };
                            btn.onmouseout = () => { btn.style.background = "transparent"; };
                            btn.onclick = () => {
                                selectedStartLoc = loc;
                                searchInput.value = loc.name;
                                resultsDiv.style.display = "none";
                            };
                            resultsDiv.appendChild(btn);
                        });
                    } else {
                        resultsDiv.style.display = "none";
                    }
                };

                actionBtn.onclick = () => {
                    const query = searchInput.value.toLowerCase().trim();
                    const locations = window.campusLocationsData || [];
                    const finalDest = window.lastSelectedDest;

                    let startLoc = selectedStartLoc;
                    if (!startLoc && query.length > 0) {
                        const matched = locations.find(l => l.name.toLowerCase() === query || l.name.toLowerCase().includes(query));
                        if (matched) startLoc = matched;
                    }

                    if (startLoc && finalDest) {
                        console.log("[CAMPNAV][NAV] origin selection: manual search (" + startLoc.name + ")");
                        console.log("[CAMPNAV][NAV] origin latitude: " + startLoc.latitude);
                        console.log("[CAMPNAV][NAV] origin longitude: " + startLoc.longitude);
                        console.log("[CAMPNAV][NAV] destination consumed: " + finalDest.name + " (" + finalDest.latitude + ", " + finalDest.longitude + ")");
                        window.openGoogleMapsNavigation(startLoc.latitude, startLoc.longitude, finalDest.latitude, finalDest.longitude);
                        window.hideNavOverlays();
                        console.log("[CAMPNAV][NAV] navigation state reset");
                        window.onEndNav();
                    } else if (!finalDest) {
                        alert("Destination missing.");
                    } else {
                        const navWindow = window.open('about:blank', '_blank');
                        window.getPositionWithFallback((pos) => {
                            console.log("[CAMPNAV][NAV] origin selection: fallback current location");
                            console.log("[CAMPNAV][NAV] origin latitude: " + pos.coords.latitude);
                            console.log("[CAMPNAV][NAV] origin longitude: " + pos.coords.longitude);
                            console.log("[CAMPNAV][NAV] destination consumed: " + finalDest.name);
                            const url = 'https://www.google.com/maps/dir/?api=1&origin=' + pos.coords.latitude + ',' + pos.coords.longitude + '&destination=' + finalDest.latitude + ',' + finalDest.longitude + '&travelmode=driving';
                            if (navWindow) {
                                navWindow.location.href = url;
                            } else {
                                window.open(url, '_blank');
                            }
                            window.hideNavOverlays();
                            console.log("[CAMPNAV][NAV] navigation state reset");
                            window.onEndNav();
                        }, (err) => {
                            if (navWindow) navWindow.close();
                            alert("Please select or search for a starting point.");
                        });
                    }
                };
                
                tapMapBtn.onclick = () => {
                    window.hideNavOverlays();
                };
                
                cancelBtn.onclick = () => {
                    console.log("[CAMPNAV][NAV] navigation state reset");
                    window.hideNavOverlays();
                    window.onEndNav();
                };
            };

            window.showOriginSelectionPopup = (destId) => {
                const locations = window.campusLocationsData || [];
                let dest = locations.find(l => String(l.id) === String(destId));
                if (!dest && window.lastSelectedDest) dest = window.lastSelectedDest;
                if (!dest) return;

                const mapDiv = document.getElementById("campus-map");
                if (!mapDiv) return;

                let overlay = document.getElementById("native-map-overlay");
                if (!overlay) {
                    overlay = document.createElement("div");
                    overlay.id = "native-map-overlay";
                    mapDiv.appendChild(overlay);
                }
                overlay.style.cssText = "position:absolute; top:0; left:0; right:0; bottom:0; z-index:1000; background:rgba(0,0,0,0.7); display:block; pointer-events:auto; backdrop-filter: blur(2px);";

                let navPanel = document.getElementById("native-nav-panel");
                if (!navPanel) {
                    navPanel = document.createElement("div");
                    navPanel.id = "native-nav-panel";
                    mapDiv.appendChild(navPanel);
                }

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

                const selectionContainer = document.createElement("div");
                selectionContainer.style.cssText = "display: flex; flex-direction: column; gap: 12px;";
                navPanel.appendChild(selectionContainer);

                const currentLocBtn = document.createElement("button");
                currentLocBtn.innerText = "Use My Current Location";
                currentLocBtn.style.cssText = "padding: 16px; background:#1A1A35; color:white; border:1px solid #6C63FF; border-radius:12px; cursor:pointer; font-weight: bold; font-size: 16px; text-align: left;";
                
                currentLocBtn.onclick = () => {
                    const navWindow = window.open('about:blank', '_blank');
                    window.getPositionWithFallback((pos) => {
                        window.hideNavOverlays();
                        const url = 'https://www.google.com/maps/dir/?api=1&origin=' + pos.coords.latitude + ',' + pos.coords.longitude + '&destination=' + dest.latitude + ',' + dest.longitude + '&travelmode=driving';
                        if (navWindow) {
                            navWindow.location.href = url;
                        } else {
                            window.open(url, '_blank');
                        }
                        console.log("[CAMPNAV][NAV] navigation state reset");
                        window.onEndNav();
                    }, (err) => {
                        if (navWindow) navWindow.close();
                        alert("Location tracking failed.");
                    });
                };
                selectionContainer.appendChild(currentLocBtn);
                
                const cancelBtn = document.createElement("button");
                cancelBtn.innerText = "Cancel";
                cancelBtn.style.cssText = "padding:12px; background:transparent; color:#FF4B4B; border:none; cursor:pointer; font-weight: bold;";
                cancelBtn.onclick = () => { 
                    console.log("[CAMPNAV][NAV] navigation state reset");
                    window.hideNavOverlays(); 
                    window.onEndNav(); 
                };
                navPanel.appendChild(cancelBtn);
            };

            let backBtn = document.getElementById("native-back-btn");
            if (!backBtn) {
                backBtn = document.createElement("button");
                backBtn.id = "native-back-btn";
                backBtn.innerHTML = "<span>⬅</span>";
                backBtn.style.cssText = "position:absolute; top:20px; left:20px; z-index:1100; width:48px; height:48px; border-radius:24px; background:white; border:none; box-shadow: 0 4px 12px rgba(0,0,0,0.2); cursor:pointer; display:flex; align-items:center; justify-content:center; font-size: 20px; color:#3C4043; transition: transform 0.1s;";
                backBtn.onclick = () => { onBack(); };
                backBtn.onmousedown = () => { backBtn.style.transform = "scale(0.9)"; };
                backBtn.onmouseup = () => { backBtn.style.transform = "scale(1)"; };
                element.appendChild(backBtn);
            }

            if (!window.mapClickListener) {
                window.mapClickListener = window.campusMap.addListener("click", (e) => {
                    if (window.onMapClick) {
                        window.onMapClick(e.latLng.lat(), e.latLng.lng());
                    }
                });
            }

            window.AdvancedMarkerElement = AdvancedMarkerElement;
            window.mapInitialized = true;
        });
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
    onEndNav: () -> Unit,
    onMapClick: (Double, Double) -> Unit,
    onStatusChange: (String) -> Unit
)

private fun startWebMapLifecycle(
    element: HTMLElement, 
    lat: Double, 
    lng: Double, 
    zoom: Float, 
    onBack: () -> Unit,
    onLocationUpdate: (Double, Double) -> Unit,
    onEndNav: () -> Unit,
    onMapClick: (Double, Double) -> Unit,
    onStatusChange: (String) -> Unit
) = startWebMapLifecycleInternal(element, lat, lng, zoom, onBack, onLocationUpdate, onEndNav, onMapClick, onStatusChange)

@JsFun("""() => { if (window.mapMarkers) { window.mapMarkers.forEach(m => m.map = null); } window.mapMarkers = []; }""")
private external fun clearWebMarkers()

@JsFun("""(lat, lng, title, id, isSelected, onLocationSelected) => {
    const createMarker = () => {
        if (!window.mapInitialized || !window.AdvancedMarkerElement) {
            setTimeout(createMarker, 50);
            return;
        }

        const marker = new window.AdvancedMarkerElement({
            map: window.campusMap,
            position: { lat: Number(lat), lng: Number(lng) },
            title: title
        });
        
        const infoWindow = new google.maps.InfoWindow({
            content: '<div style="color:black; padding:12px; font-family: sans-serif; min-width: 180px;"><div style="font-weight:bold; margin-bottom:12px; font-size:16px; color:#0F0F23;">' + title + '</div><button id="nav-btn-js-' + id + '" style="width:100%; padding:12px; background:#6C63FF; color:white; border:none; border-radius:10px; cursor:pointer; font-weight:bold; font-size:14px; box-shadow: 0 2px 4px rgba(0,0,0,0.2);">NAVIGATE</button></div>'
        });

        const setupNavBtn = () => {
            const btn = document.getElementById('nav-btn-js-' + id);
            if (btn) {
                btn.onclick = (e) => {
                    e.stopPropagation();
                    console.log("[CAMPNAV][NAV] navigate clicked: " + title);
                    console.log("[CAMPNAV][NAV] destination id: " + id);
                    console.log("[CAMPNAV][NAV] destination latitude: " + lat);
                    console.log("[CAMPNAV][NAV] destination longitude: " + lng);

                    const locations = window.campusLocationsData || [];
                    const dest = locations.find(l => String(l.id) === String(id)) || { id: id, name: title, latitude: Number(lat), longitude: Number(lng) };
                    window.lastSelectedDest = dest;

                    if (window.onStatusChange) {
                        window.onStatusChange("SHOWING_NAV_CHOICE");
                    }
                    if (window.showNavChoicePopup) {
                        window.showNavChoicePopup(String(id), title);
                    } else if (window.showOriginSelectionPopup) {
                        window.showOriginSelectionPopup(String(id));
                    }
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
    };
    createMarker();
}""")
private external fun addWebMarkerInternal(lat: Double, lng: Double, title: String, id: String, isSelected: Boolean, onLocationSelected: (String) -> Unit)

private fun addWebMarker(lat: Double, lng: Double, title: String, id: String, isSelected: Boolean, onLocationSelected: (String) -> Unit) = 
    addWebMarkerInternal(lat, lng, title, id, isSelected, onLocationSelected)

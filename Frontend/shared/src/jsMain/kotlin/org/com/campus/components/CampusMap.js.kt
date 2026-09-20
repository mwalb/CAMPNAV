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

    LaunchedEffect(university, locations, navigationState.destination, navigationState.selectedStartPoint) {
        val mapDiv = document.getElementById("campus-map") as? HTMLElement
        if (mapDiv != null) {
            mapDiv.style.display = "block"
            onEndNavInternal = onEndNavigation
            onStatusChangeInternal = onStatusChange
            
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
                { onEndNavInternal() },
                { lat, lng -> onMapClick(RoutePoint(lat, lng)) },
                { status -> onStatusChangeInternal(NavigationStatus.valueOf(status)) }
            )
            
            syncWebMarkers(locations, selectedDest?.id, navigationState.selectedStartPoint, onLocationSelected)
        }
    }

    LaunchedEffect(navigationState, locations) {
        val locationsJson = Json.encodeToString(locations)
        syncWebLocations(locationsJson)
        
        when (navigationState.status) {
            NavigationStatus.SHOWING_NAV_CHOICE -> {
                navigationState.destination?.let { dest ->
                    triggerNavChoiceJS(dest.id.toString(), dest.name)
                }
            }
            NavigationStatus.SELECTING_START_POINT -> {
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
                stopTrackingUserLocation()
            }
        }
    }
}

private var onEndNavInternal: () -> Unit = {}
private var onStatusChangeInternal: (NavigationStatus) -> Unit = {}

private fun syncWebMarkers(locations: List<CampusLocation>, selectedId: Long?, startPoint: RoutePoint?, onLocationSelected: (CampusLocation) -> Unit) {
    clearWebMarkers()
    val selected = locations.find { it.id == selectedId }
    if (selected != null) {
        addWebMarker(
            selected.latitude, 
            selected.longitude, 
            selected.name, 
            selected.id.toString(), 
            true, 
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

@Suppress("UNUSED_PARAMETER")
private fun triggerNavChoiceJS(destId: String, destName: String): Unit = js("{ if (window.showNavChoicePopup) window.showNavChoicePopup(destId, destName); }")

@Suppress("UNUSED_PARAMETER")
private fun triggerStartPointSelectionJS(): Unit = js("{ if (window.showStartPointSelectionOverlay) window.showStartPointSelectionOverlay(); }")

private fun hideNavOverlaysJS(): Unit = js("{ if (window.hideNavOverlays) window.hideNavOverlays(); }")

@Suppress("UNUSED_PARAMETER")
private fun triggerOriginSelectionJS(destId: String): Unit = js("{ if (window.showOriginSelectionPopup) window.showOriginSelectionPopup(destId); }")

@Suppress("UNUSED_PARAMETER")
private fun syncWebLocations(locationsJson: String): Unit = js("""{
    window.campusLocationsData = JSON.parse(locationsJson);
}""")

private fun startTrackingUserLocation(): Unit = js("""{
    console.log("[CAMPNAV] Starting user location tracking...");
    if (navigator.geolocation && !window.watchId) {
        const options = { enableHighAccuracy: true, timeout: 15000, maximumAge: 3000 };
        window.watchId = navigator.geolocation.watchPosition((pos) => {
            const lat = Number(pos.coords.latitude);
            const lng = Number(pos.coords.longitude);
            console.log("[CAMPNAV] Location update:", lat, lng);
            if (window.onLocationUpdate) window.onLocationUpdate(lat, lng);
            if (window.userMarker && window.campusMap) {
                window.userMarker.position = { lat: lat, lng: lng };
                if (!window.userMarker.map) window.userMarker.map = window.campusMap;
            }
        }, (err) => {
            console.error("[CAMPNAV] Location tracking error (" + err.code + "): " + err.message);
        }, options);
    } else if (!navigator.geolocation) {
        console.error("[CAMPNAV] Geolocation is not supported by this browser.");
    }
}""")

private fun stopTrackingUserLocation(): Unit = js("""{
    if (window.watchId) {
        navigator.geolocation.clearWatch(window.watchId);
        window.watchId = null;
    }
    if (window.navWatchId) {
        navigator.geolocation.clearWatch(window.navWatchId);
        window.navWatchId = null;
    }
}""")

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
): Unit = js("""eval(`(async () => {
    const init = async () => {
        while (element.clientWidth === 0 || element.clientHeight === 0) {
            await new Promise(resolve => setTimeout(resolve, 50));
        }

        while (!window.google || !window.google.maps || !window.google.maps.importLibrary) {
            await new Promise(resolve => setTimeout(resolve, 100));
        }

        const { Map, Polyline, LatLngBounds } = await google.maps.importLibrary("maps");
        const { AdvancedMarkerElement, PinElement } = await google.maps.importLibrary("marker");
        const { Route } = await google.maps.importLibrary("routes");
        const { encoding, spherical } = await google.maps.importLibrary("geometry");

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
            window.onStatusChange = onStatusChange;
            window.onMapClick = onMapClick;

            window.openGoogleMapsNavigation = (oLat, oLng, dLat, dLng) => {
                const url = `https://www.google.com/maps/dir/?api=1&origin=${'$'}{oLat},${'$'}{oLng}&destination=${'$'}{dLat},${'$'}{dLng}&travelmode=driving`;
                window.open(url, '_blank');
            };

            window.hideNavOverlays = () => {
                overlay.style.display = "none";
                navPanel.style.display = "none";
                const selOverlay = document.getElementById("select-point-overlay");
                if (selOverlay) selOverlay.style.display = "none";
            };

            window.showStartPointSelectionOverlay = () => {
                let selOverlay = document.getElementById("select-point-overlay");
                if (!selOverlay) {
                    selOverlay = document.createElement("div");
                    selOverlay.id = "select-point-overlay";
                    element.appendChild(selOverlay);
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

                selOverlay.innerHTML = `
                    <div style="margin-bottom: 12px; text-align: center; color: #0F0F23; font-size: 20px; font-weight: 800;">Select a starting point</div>
                    
                    <div style="position: relative; width: 100%; margin-bottom: 8px;">
                        <input id="start-point-search" type="text" placeholder="Search location..." 
                               style="padding: 16px 44px 16px 16px; border-radius: 16px; border: 2px solid #F0F0F0; width: 100%; box-sizing: border-box; font-size: 16px; outline: none; transition: all 0.2s; background: #F8F9FA;">
                        <span style="position: absolute; right: 16px; top: 50%; transform: translateY(-50%); color: #AAA; font-size: 18px;">🔍</span>
                    </div>
                    
                    <div id="start-point-results" style="display: none; flex-direction: column; gap: 4px; max-height: 180px; overflow-y: auto; background: white; border-radius: 16px; padding: 8px; border: 1px solid #EEE; font-weight: normal; margin-bottom: 12px; box-shadow: inset 0 2px 4px rgba(0,0,0,0.02);"></div>
                    
                    <button id="start-point-action-btn" style="width: 100%; padding: 16px; border-radius: 16px; border: none; background: #6C63FF; color: white; cursor: pointer; font-weight: 800; display: flex; align-items: center; justify-content: center; gap: 10px; font-size: 16px; box-shadow: 0 4px 12px rgba(108, 99, 255, 0.3); transition: transform 0.1s;">
                        <span>🚀</span> Start
                    </button>
                    
                    <div style="display: flex; gap: 10px; width: 100%; margin-top: 4px;">
                        <button id="start-point-tap-map" style="flex: 1; padding: 14px; border-radius: 16px; border: none; background: #6C63FF; color: white; cursor: pointer; font-weight: 700; display: flex; align-items: center; justify-content: center; gap: 8px; font-size: 14px; box-shadow: 0 4px 10px rgba(108, 99, 255, 0.2);">
                            <span>📍</span> Tap on the Map
                        </button>
                        <button id="start-point-cancel" style="padding: 14px; border-radius: 16px; border: 2px solid #F0F0F0; background: white; color: #666; cursor: pointer; font-weight: 700; font-size: 14px;">
                            Cancel
                        </button>
                    </div>
                `;

                const searchInput = document.getElementById("start-point-search");
                const resultsDiv = document.getElementById("start-point-results");
                const actionBtn = document.getElementById("start-point-action-btn");
                const tapMapBtn = document.getElementById("start-point-tap-map");
                const cancelBtn = document.getElementById("start-point-cancel");
                
                searchInput.focus();
                searchInput.onfocus = () => { 
                    searchInput.style.borderColor = "#6C63FF"; 
                    searchInput.style.background = "white";
                };
                searchInput.onblur = () => { 
                    searchInput.style.borderColor = "#F0F0F0";
                    searchInput.style.background = "#F8F9FA";
                };

                searchInput.oninput = (e) => {
                    const query = e.target.value.toLowerCase();
                    const locations = window.campusLocationsData || [];

                    if (query.length < 1) {
                        resultsDiv.style.display = "none";
                        return;
                    }
                    const filtered = locations.filter(l => l.name.toLowerCase().includes(query)).slice(0, 8);
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
                                window.onMapClick(loc.latitude, loc.longitude);
                                window.hideNavOverlays();
                            };
                            resultsDiv.appendChild(btn);
                        });
                    } else {
                        resultsDiv.style.display = "none";
                    }
                };

                actionBtn.onclick = () => {
                    const query = searchInput.value.toLowerCase();
                    const locations = window.campusLocationsData || [];
                    const finalDest = window.lastSelectedDest;

                    if (query.length > 0) {
                        const filtered = locations.filter(l => l.name.toLowerCase().includes(query));
                        if (filtered.length > 0 && finalDest) {
                            window.openGoogleMapsNavigation(filtered[0].latitude, filtered[0].longitude, finalDest.latitude, finalDest.longitude);
                            window.hideNavOverlays();
                            window.onEndNav();
                        } else {
                            alert("Location not found or destination missing.");
                        }
                    } else {
                        console.log("[CAMPNAV] 'Start' clicked with empty search - using current location...");
                        navigator.geolocation.getCurrentPosition((pos) => {
                            if (finalDest) {
                                window.openGoogleMapsNavigation(pos.coords.latitude, pos.coords.longitude, finalDest.latitude, finalDest.longitude);
                                window.hideNavOverlays();
                                window.onEndNav();
                            }
                        }, (err) => {
                            console.error("[CAMPNAV] Geolocation error: " + err.message);
                            alert("Could not get location. Please allow location access or search for a place.");
                        });
                    }
                };
                
                tapMapBtn.onclick = () => {
                    window.hideNavOverlays();
                };
                
                cancelBtn.onclick = () => {
                    window.hideNavOverlays();
                    window.onEndNav();
                };
            };

            window.showNavChoicePopup = (destId, destName) => {
                const locations = window.campusLocationsData || [];
                const dest = locations.find(l => String(l.id) === String(destId));
                if (!dest) return;
                window.lastSelectedDest = dest;

                overlay.style.display = "block";
                navPanel.style.display = "flex";
                navPanel.style.cssText = "position:absolute; top:50%; left:50%; transform:translate(-50%, -50%); z-index:1200; background:#0F0F23; color:white; padding:32px; border-radius:24px; border:2px solid #6C63FF; display:flex; flex-direction:column; gap:16px; font-family: sans-serif; width: 85%; max-width: 400px; box-shadow: 0 8px 32px rgba(0,0,0,0.8);";
                
                navPanel.innerHTML = `
                    <div style="text-align: center;">
                        <div style="font-size: 14px; color: #AAA;">Navigate to</div>
                        <div style="font-weight:bold; font-size: 24px; color: white; margin-top: 4px;">${'$'}{destName}</div>
                        <div style="font-size: 16px; color: white; margin-top: 16px; margin-bottom: 24px;">How would you like to start?</div>
                    </div>
                `;

                const btnStyle = "padding: 16px; background:#1A1A35; color:white; border:1px solid #6C63FF; border-radius:12px; cursor:pointer; font-weight: bold; font-size: 16px; text-align: left; display: flex; align-items: center; gap: 12px;";
                
                const currentLocBtn = document.createElement("button");
                currentLocBtn.innerHTML = "<span>📍</span> Use my current location";
                currentLocBtn.style.cssText = btnStyle;
                currentLocBtn.onclick = () => {
                    console.log("[CAMPNAV] Requesting current location for navigation...");
                    navigator.geolocation.getCurrentPosition((pos) => {
                        console.log("[CAMPNAV] Got current location:", pos.coords.latitude, pos.coords.longitude);
                        window.openGoogleMapsNavigation(pos.coords.latitude, pos.coords.longitude, dest.latitude, dest.longitude);
                        window.hideNavOverlays();
                        window.onEndNav();
                    }, (err) => {
                        console.error("[CAMPNAV] Geolocation error (" + err.code + "): " + err.message);
                        alert("Could not get location. Please allow location access.");
                    });
                };
                navPanel.appendChild(currentLocBtn);

                const selectOnMapBtn = document.createElement("button");
                selectOnMapBtn.innerHTML = "<span>📌</span> Select starting point";
                selectOnMapBtn.style.cssText = btnStyle;
                selectOnMapBtn.onclick = () => {
                    window.hideNavOverlays();
                    window.onStatusChange("SELECTING_START_POINT");
                };
                navPanel.appendChild(selectOnMapBtn);

                const cancelBtn = document.createElement("button");
                cancelBtn.innerText = "Cancel";
                cancelBtn.style.cssText = "padding:12px; background:transparent; color:#FF4B4B; border:none; cursor:pointer; font-weight: bold; font-size: 16px; margin-top: 8px;";
                cancelBtn.onclick = () => { 
                    window.hideNavOverlays(); 
                    window.onEndNav(); 
                };
                navPanel.appendChild(cancelBtn);
            };

            window.showOriginSelectionPopup = (destId) => {
                const locations = window.campusLocationsData || [];
                const dest = locations.find(l => String(l.id) === String(destId));
                if (!dest) return;

                overlay.style.display = "block";
                navPanel.style.display = "flex";
                navPanel.style.cssText = "position:absolute; top:50%; left:50%; transform:translate(-50%, -50%); z-index:1000; background:#0F0F23; color:white; padding:24px; border-radius:24px; border:2px solid #6C63FF; display:flex; flex-direction:column; gap:16px; font-family: sans-serif; width: 85%; max-width: 400px; box-shadow: 0 8px 32px rgba(0,0,0,0.8);";
                
                navPanel.innerHTML = "";
                const title = document.createElement("div");
                title.innerText = "Set Starting Point (Internal)";
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
                currentLocBtn.innerText = "📍 Use My Current Location";
                currentLocBtn.style.cssText = "padding: 16px; background:#1A1A35; color:white; border:1px solid #6C63FF; border-radius:12px; cursor:pointer; font-weight: bold; font-size: 16px; text-align: left;";
                
                currentLocBtn.onclick = async () => {
                    navigator.geolocation.getCurrentPosition((pos) => {
                        window.hideNavOverlays();
                        window.runRoutingJS({ lat: pos.coords.latitude, lng: pos.coords.longitude }, dest);
                    }, (err) => {
                        alert("Location tracking failed.");
                    });
                };
                selectionContainer.appendChild(currentLocBtn);
                
                const cancelBtn = document.createElement("button");
                cancelBtn.innerText = "Cancel";
                cancelBtn.style.cssText = "padding:12px; background:transparent; color:#FF4B4B; border:none; cursor:pointer; font-weight: bold;";
                cancelBtn.onclick = () => { window.hideNavOverlays(); window.onEndNav(); };
                navPanel.appendChild(cancelBtn);
            };

            window.campusMap.addListener("click", (e) => {
                if (window.onMapClick) {
                    window.onMapClick(e.latLng.lat(), e.latLng.lng());
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
        window.PinElement = PinElement;
        window.mapInitialized = true;
    };
    init();
})()`);""")

@Suppress("UNUSED_PARAMETER")
private fun clearWebMarkers(): Unit = js("""{
    if (window.mapMarkers) {
        window.mapMarkers.forEach(m => m.map = null);
    }
    window.mapMarkers = [];
}""")

@Suppress("UNUSED_PARAMETER")
private fun addWebMarker(lat: Double, lng: Double, title: String, id: String, isSelected: Boolean, onLocationSelected: (String) -> Unit): Unit = js("""{
    const checkInit = () => {
        if (!window.mapInitialized || !window.AdvancedMarkerElement) {
            setTimeout(checkInit, 50);
            return;
        }

        const marker = new window.AdvancedMarkerElement({
            map: window.campusMap,
            position: { lat: Number(lat), lng: Number(lng) },
            title: title
        });
        
        const infoWindow = new google.maps.InfoWindow({
            content: `
                <div style="color:black; padding:12px; font-family: sans-serif; min-width: 180px;">
                    <div style="font-weight:bold; margin-bottom:12px; font-size:16px; color:#0F0F23;">${'$'}{title}</div>
                    <button id="nav-btn-js-${'$'}{id}" style="width:100%; padding:12px; background:#6C63FF; color:white; border:none; border-radius:10px; cursor:pointer; font-weight:bold; font-size:14px; box-shadow: 0 2px 4px rgba(0,0,0,0.2);">
                        NAVIGATE
                    </button>
                </div>
            `
        });

        const setupNavBtn = () => {
            const btn = document.getElementById(`nav-btn-js-${'$'}{id}`);
            if (btn) {
                btn.onclick = (e) => {
                    e.stopPropagation();
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
    checkInit();
}""")

@file:OptIn(ExperimentalWasmJsInterop::class)
package org.com.community.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.browser.document
import kotlinx.serialization.json.Json
import org.com.campus.data.University
import org.com.community.model.IssueReport
import org.w3c.dom.HTMLElement

@Composable
actual fun IssuesMap(
    modifier: Modifier,
    university: University?,
    issues: List<IssueReport>,
    selectedIssue: IssueReport?,
    onIssueSelected: (IssueReport?) -> Unit,
    onBack: () -> Unit
) {
    Box(modifier = modifier)

    val currentIssues by rememberUpdatedState(issues)
    val currentOnIssueSelected by rememberUpdatedState(onIssueSelected)
    val currentOnBack by rememberUpdatedState(onBack)

    LaunchedEffect(Unit) {
        initWebIssuesBridge(
            onBackCallback = { currentOnBack() },
            onSelectCallback = { id ->
                val found = currentIssues.find { it.id == id }
                currentOnIssueSelected(found)
            }
        )
    }

    LaunchedEffect(university, issues, selectedIssue) {
        val mapDiv = document.getElementById("campus-map") as? HTMLElement
        if (mapDiv != null) {
            mapDiv.style.display = "block"
            mapDiv.style.zIndex = "1"

            val startLat = university?.latitude ?: -6.780543
            val startLng = university?.longitude ?: 39.204593
            val startZoom = university?.defaultZoom ?: 15f

            initWebIssuesMap(mapDiv, startLat, startLng, startZoom)

            val markers = issues.map { issue ->
                MapIssueMarker(
                    id = issue.id.ifBlank { "CAM-ISSUE" },
                    lat = issue.issueLocation.latitude,
                    lng = issue.issueLocation.longitude,
                    cat = issue.category.displayName,
                    desc = issue.description.take(100),
                    status = issue.status.name
                )
            }
            val jsonArrayStr = Json.encodeToString(markers)

            renderWebIssueMarkers(jsonArrayStr, selectedIssue?.id ?: "")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val mapDiv = document.getElementById("campus-map") as? HTMLElement
            if (mapDiv != null) {
                mapDiv.style.display = "none"
            }
        }
    }
}

@JsFun("""(onBackCallback, onSelectCallback) => {
    window.onIssuesMapBackInternal = onBackCallback;
    window.onIssueSelectedInternal = onSelectCallback;

    window.initWebIssuesMapJS = (element, lat, lng, zoom) => {
        if (!window.google || !window.google.maps || !window.google.maps.importLibrary) {
            setTimeout(() => window.initWebIssuesMapJS(element, lat, lng, zoom), 100);
            return;
        }
        Promise.all([
            google.maps.importLibrary("maps"),
            google.maps.importLibrary("marker")
        ]).then(([mapsLib, markerLib]) => {
            const { Map } = mapsLib;
            if (!window.campusMap) {
                window.campusMap = new Map(element, {
                    center: { lat: lat, lng: lng },
                    zoom: zoom,
                    mapId: "DEMO_MAP_ID",
                    disableDefaultUI: false
                });

                // Native Google Maps custom control for back navigation
                const backDiv = document.createElement('div');
                backDiv.style.margin = '12px';
                backDiv.style.padding = '8px 14px';
                backDiv.style.backgroundColor = 'rgba(26, 26, 62, 0.95)';
                backDiv.style.color = '#FFFFFF';
                backDiv.style.borderRadius = '10px';
                backDiv.style.cursor = 'pointer';
                backDiv.style.fontFamily = 'system-ui, -apple-system, sans-serif';
                backDiv.style.fontSize = '14px';
                backDiv.style.fontWeight = '700';
                backDiv.style.boxShadow = '0 4px 12px rgba(0,0,0,0.35)';
                backDiv.style.display = 'flex';
                backDiv.style.alignItems = 'center';
                backDiv.style.gap = '8px';
                backDiv.style.border = '1px solid rgba(255,255,255,0.2)';
                backDiv.innerHTML = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="19" y1="12" x2="5" y2="12"></line><polyline points="12 19 5 12 12 5"></polyline></svg> Back';

                backDiv.addEventListener('click', () => {
                    if (window.onIssuesMapBackInternal) {
                        window.onIssuesMapBackInternal();
                    }
                });

                window.campusMap.controls[google.maps.ControlPosition.TOP_LEFT].push(backDiv);
            } else {
                window.campusMap.setCenter({ lat: lat, lng: lng });
                window.campusMap.setZoom(zoom);
            }
            window.AdvancedMarkerElement = markerLib.AdvancedMarkerElement;
            window.mapInitialized = true;
        });
    };

    window.renderWebIssueMarkersJS = (issuesJsonStr, selectedId) => {
        if (!window.mapInitialized || !window.AdvancedMarkerElement || !window.campusMap) {
            setTimeout(() => window.renderWebIssueMarkersJS(issuesJsonStr, selectedId), 100);
            return;
        }
        if (window.issueMapMarkers) {
            window.issueMapMarkers.forEach(m => m.map = null);
        }
        window.issueMapMarkers = [];

        let issuesData = [];
        try {
            issuesData = JSON.parse(issuesJsonStr || "[]");
        } catch (e) {
            console.error("[CAMPNAV] Failed to parse issues JSON string:", e, issuesJsonStr);
            return;
        }

        if (!Array.isArray(issuesData)) return;

        issuesData.forEach((item) => {
            try {
                if (item && typeof item === 'object' && (item.lat !== 0 || item.lng !== 0)) {
                    const cat = item.cat || "Issue";
                    const desc = item.desc || "";
                    const status = item.status || "REPORTED";
                    const marker = new window.AdvancedMarkerElement({
                        map: window.campusMap,
                        position: { lat: Number(item.lat), lng: Number(item.lng) },
                        title: cat + ": " + desc
                    });

                    const infoWindow = new google.maps.InfoWindow({
                        content: '<div style="color:black; padding:8px; font-family:sans-serif;"><b>' + escapeHtml(cat) + '</b><br/>' + escapeHtml(desc) + '<br/><small>Status: ' + escapeHtml(status) + '</small></div>'
                    });

                    marker.addListener("click", () => {
                        infoWindow.open(window.campusMap, marker);
                        if (window.onIssueSelectedInternal && item.id) {
                            window.onIssueSelectedInternal(item.id);
                        }
                    });

                    if (selectedId && selectedId === item.id) {
                        infoWindow.open(window.campusMap, marker);
                    }

                    window.issueMapMarkers.push(marker);
                }
            } catch (itemErr) {
                console.warn("[CAMPNAV] Skipping invalid individual issue record:", itemErr, item);
            }
        });
    };

    function escapeHtml(str) {
        if (!str) return '';
        return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }
}""")
private external fun initWebIssuesBridge(
    onBackCallback: () -> Unit,
    onSelectCallback: (String) -> Unit
)

@JsFun("(element, lat, lng, zoom) => { if (window.initWebIssuesMapJS) window.initWebIssuesMapJS(element, Number(lat), Number(lng), Number(zoom)); }")
private external fun initWebIssuesMap(element: HTMLElement, lat: Double, lng: Double, zoom: Float)

@JsFun("(issuesJsonStr, selectedId) => { if (window.renderWebIssueMarkersJS) window.renderWebIssueMarkersJS(issuesJsonStr, selectedId); }")
private external fun renderWebIssueMarkers(issuesJsonStr: String, selectedId: String)

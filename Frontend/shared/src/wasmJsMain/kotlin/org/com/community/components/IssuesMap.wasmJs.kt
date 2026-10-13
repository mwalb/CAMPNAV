@file:OptIn(ExperimentalWasmJsInterop::class)
package org.com.community.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.browser.document
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

    LaunchedEffect(Unit) {
        initWebIssuesBridge()
        onIssueSelectedInternal = { id ->
            val found = currentIssues.find { it.id == id }
            currentOnIssueSelected(found)
        }
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

            val issuesJsonList = issues.map { issue ->
                val safeCategory = issue.category.displayName.replace("'", "\\'")
                val safeDesc = issue.description.take(30).replace("'", "\\'")
                val safeId = issue.id.ifBlank { "CAM-ISSUE" }
                "{ \"id\": \"$safeId\", \"lat\": ${issue.issueLocation.latitude}, \"lng\": ${issue.issueLocation.longitude}, \"cat\": \"$safeCategory\", \"desc\": \"$safeDesc\", \"status\": \"${issue.status.name}\" }"
            }
            val jsonArrayStr = "[ " + issuesJsonList.joinToString(",") + " ]"

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

private var onIssueSelectedInternal: (String) -> Unit = {}

@JsFun("""() => {
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

        const issuesData = JSON.parse(issuesJsonStr || "[]");
        issuesData.forEach((item) => {
            if (item.lat !== 0 || item.lng !== 0) {
                const marker = new window.AdvancedMarkerElement({
                    map: window.campusMap,
                    position: { lat: Number(item.lat), lng: Number(item.lng) },
                    title: item.cat + ": " + item.desc
                });

                const infoWindow = new google.maps.InfoWindow({
                    content: '<div style="color:black; padding:8px; font-family:sans-serif;"><b>' + item.cat + '</b><br/>' + item.desc + '<br/><small>Status: ' + item.status + '</small></div>'
                });

                marker.addListener("click", () => {
                    infoWindow.open(window.campusMap, marker);
                });

                if (selectedId && selectedId === item.id) {
                    infoWindow.open(window.campusMap, marker);
                }

                window.issueMapMarkers.push(marker);
            }
        });
    };
}""")
private external fun initWebIssuesBridge()

@JsFun("(element, lat, lng, zoom) => { if (window.initWebIssuesMapJS) window.initWebIssuesMapJS(element, Number(lat), Number(lng), Number(zoom)); }")
private external fun initWebIssuesMap(element: HTMLElement, lat: Double, lng: Double, zoom: Float)

@JsFun("(issuesJsonStr, selectedId) => { if (window.renderWebIssueMarkersJS) window.renderWebIssueMarkersJS(issuesJsonStr, selectedId); }")
private external fun renderWebIssueMarkers(issuesJsonStr: String, selectedId: String)

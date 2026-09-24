@file:OptIn(ExperimentalWasmJsInterop::class)
package org.com.community.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntSize
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

@Composable
actual fun LocationPickerMap(
    modifier: Modifier,
    initialLatitude: Double?,
    initialLongitude: Double?,
    onLocationSelected: (Double, Double) -> Unit
) {
    val currentOnLocationSelected by rememberUpdatedState(onLocationSelected)
    var boxPosition by remember { mutableStateOf<Offset?>(null) }
    var boxSize by remember { mutableStateOf<IntSize?>(null) }

    Box(
        modifier = modifier.onGloballyPositioned {
            boxPosition = it.positionInWindow()
            boxSize = it.size
        }
    )

    LaunchedEffect(boxPosition, boxSize, initialLatitude, initialLongitude) {
        val pos = boxPosition
        val size = boxSize
        if (pos != null && size != null) {
            val mapDiv = document.getElementById("campus-map") as? HTMLElement
            if (mapDiv != null) {
                mapDiv.style.display = "block"
                mapDiv.style.position = "absolute"
                mapDiv.style.left = "${pos.x}px"
                mapDiv.style.top = "${pos.y}px"
                mapDiv.style.width = "${size.width}px"
                mapDiv.style.height = "${size.height}px"
                mapDiv.style.zIndex = "5"
                mapDiv.style.setProperty("border-radius", "20px")
                mapDiv.style.setProperty("overflow", "hidden")
                
                document.getElementById("native-back-btn")?.let { it as HTMLElement }?.style?.display = "none"
                document.getElementById("native-info-card")?.let { it as HTMLElement }?.style?.display = "none"
                
                val startLat = initialLatitude ?: -6.7924
                val startLng = initialLongitude ?: 39.2083
                
                startWebPickerLifecycle(
                    mapDiv,
                    startLat,
                    startLng,
                    { lat, lng -> currentOnLocationSelected(lat, lng) }
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val mapDiv = document.getElementById("campus-map") as? HTMLElement
            if (mapDiv != null) {
                mapDiv.style.display = "none"
                mapDiv.style.position = "absolute"
                mapDiv.style.left = "0"
                mapDiv.style.top = "0"
                mapDiv.style.width = "100%"
                mapDiv.style.height = "100%"
                mapDiv.style.zIndex = "2"
                mapDiv.style.setProperty("border-radius", "0")
            }
        }
    }
}

@JsFun("""(element, lat, lng, onSelected) => {
    const init = async () => {
        while (!window.google || !window.google.maps || !window.google.maps.importLibrary) {
            await new Promise(resolve => setTimeout(resolve, 100));
        }

        const { Map } = await google.maps.importLibrary("maps");
        const { AdvancedMarkerElement } = await google.maps.importLibrary("marker");
        
        const mapOptions = {
            center: { lat: Number(lat), lng: Number(lng) },
            zoom: 16,
            mapId: "DEMO_MAP_ID",
            disableDefaultUI: true,
            zoomControl: true
        };

        const map = new Map(element, mapOptions);
        window.currentPickerMap = map;
        
        let marker = new AdvancedMarkerElement({
            map: map,
            position: { lat: Number(lat), lng: Number(lng) },
            gmpDraggable: true,
            title: "Plant Location"
        });
        window.currentPickerMarker = marker;

        setTimeout(() => {
            google.maps.event.trigger(map, "resize");
            map.setCenter({ lat: Number(lat), lng: Number(lng) });
        }, 150);

        marker.addListener("dragend", (event) => {
            const pos = marker.position;
            onSelected(pos.lat, pos.lng);
        });

        map.addListener("click", (event) => {
            marker.position = event.latLng;
            onSelected(event.latLng.lat(), event.latLng.lng());
        });
        
        onSelected(Number(lat), Number(lng));
    };
    init();
}""")
private external fun startWebPickerLifecycle(element: HTMLElement, lat: Double, lng: Double, onSelected: (Double, Double) -> Unit)

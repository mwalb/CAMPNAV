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
    onBack: () -> Unit,
    onConfirm: (Double, Double) -> Unit
) {
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnConfirm by rememberUpdatedState(onConfirm)

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
                    { currentOnBack() },
                    { lat, lng -> currentOnConfirm(lat, lng) }
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

@JsFun("""(element, lat, lng, onBack, onConfirm) => {
    const init = async () => {
        while (!window.google || !window.google.maps || !window.google.maps.importLibrary) {
            await new Promise(resolve => setTimeout(resolve, 100));
        }

        const { Map } = await google.maps.importLibrary("maps");
        const { AdvancedMarkerElement } = await google.maps.importLibrary("marker");
        
        const startLat = Number(lat);
        const startLng = Number(lng);

        const mapOptions = {
            center: { lat: startLat, lng: startLng },
            zoom: 16,
            mapId: "DEMO_MAP_ID",
            disableDefaultUI: true,
            zoomControl: true
        };

        const map = new Map(element, mapOptions);
        window.currentPickerMap = map;
        
        let selectedLat = startLat;
        let selectedLng = startLng;

        let marker = new AdvancedMarkerElement({
            map: map,
            position: { lat: selectedLat, lng: selectedLng },
            gmpDraggable: true,
            title: "Pinned Location"
        });
        window.currentPickerMarker = marker;

        setTimeout(() => {
            google.maps.event.trigger(map, "resize");
            map.setCenter({ lat: selectedLat, lng: selectedLng });
        }, 150);

        const backBtn = document.createElement("button");
        backBtn.style.cssText = "margin: 16px; width: 44px; height: 44px; border-radius: 50%; background: #ffffff; border: none; box-shadow: 0 4px 12px rgba(0,0,0,0.3); cursor: pointer; display: flex; align-items: center; justify-content: center; font-size: 22px; color: #3C4043; font-weight: bold; font-family: sans-serif;";
        backBtn.innerHTML = "←";
        backBtn.onclick = () => { onBack(); };

        const confirmContainer = document.createElement("div");
        confirmContainer.style.cssText = "margin-bottom: 24px; padding: 16px 20px; background: rgba(26,27,46,0.95); border-radius: 20px; border: 1px solid rgba(255,255,255,0.1); box-shadow: 0 8px 24px rgba(0,0,0,0.5); text-align: center; font-family: sans-serif; color: white; min-width: 280px; max-width: 90vw;";

        const updateCoordsUI = () => {
            const latEl = document.getElementById("picker-lat-val-wasm");
            const lngEl = document.getElementById("picker-lng-val-wasm");
            if (latEl) latEl.innerText = selectedLat.toFixed(6);
            if (lngEl) lngEl.innerText = selectedLng.toFixed(6);
        };

        confirmContainer.innerHTML = '<div style="font-size: 11px; font-weight: bold; color: #6C5CE7; letter-spacing: 1px; margin-bottom: 8px;">TAP MAP TO PICK LOCATION</div>' +
            '<div style="display: flex; justify-content: space-around; font-size: 13px; font-weight: bold; margin-bottom: 12px;">' +
            '<span>Lat: <span id="picker-lat-val-wasm">' + selectedLat.toFixed(6) + '</span></span>' +
            '<span>Lng: <span id="picker-lng-val-wasm">' + selectedLng.toFixed(6) + '</span></span>' +
            '</div>' +
            '<button id="picker-confirm-btn-wasm" style="width: 100%; height: 44px; background: #6C5CE7; color: white; border: none; border-radius: 12px; font-weight: bold; font-size: 15px; cursor: pointer;">' +
            'Confirm Location</button>';

        const posTopLeft = google.maps.ControlPosition.TOP_LEFT;
        const posBottomCenter = google.maps.ControlPosition.BOTTOM_CENTER;

        map.controls[posTopLeft].clear();
        map.controls[posBottomCenter].clear();

        map.controls[posTopLeft].push(backBtn);
        map.controls[posBottomCenter].push(confirmContainer);

        const confirmBtn = confirmContainer.querySelector("#picker-confirm-btn-wasm");
        if (confirmBtn) {
            confirmBtn.onclick = () => {
                onConfirm(selectedLat, selectedLng);
            };
        }

        const handleNewPosition = (newLat, newLng) => {
            selectedLat = Number(newLat);
            selectedLng = Number(newLng);
            updateCoordsUI();
        };

        marker.addListener("dragend", (event) => {
            const pos = marker.position;
            handleNewPosition(pos.lat, pos.lng);
        });

        map.addListener("click", (event) => {
            marker.position = event.latLng;
            handleNewPosition(event.latLng.lat(), event.latLng.lng());
        });
    };
    init();
}""")
private external fun startWebPickerLifecycle(
    element: HTMLElement, 
    lat: Double, 
    lng: Double, 
    onBack: () -> Unit, 
    onConfirm: (Double, Double) -> Unit
)

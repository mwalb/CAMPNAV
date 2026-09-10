package org.com

import androidx.compose.runtime.*
import org.com.campus.data.CampusLocation
import org.com.campus.data.University
import org.com.campus.presentation.CampusDestinationScreen
import org.com.campus.presentation.CampusMapScreen
import org.com.campus.presentation.UniversitySelectionScreen
import org.com.creator.presentation.ContentCreatorScreen
import org.com.entertainment.presentation.IPTVPlayerApp
import org.com.core.ui.theme.CampNavTheme

enum class AppMode {
    UniversitySelection,
    CampusDestination,
    CampusNav,
    Entertainment,
    ContentCreator
}

@Composable
fun MainApp() {
    var mode by remember { mutableStateOf(AppMode.UniversitySelection) }
    var selectedUniversity by remember { mutableStateOf<University?>(null) }
    var selectedLocation by remember { mutableStateOf<CampusLocation?>(null) }

    CampNavTheme {
        when (mode) {
            AppMode.UniversitySelection -> UniversitySelectionScreen(
                onUniversitySelected = { university ->
                    selectedUniversity = university
                    mode = AppMode.CampusDestination
                }
            )
            AppMode.CampusDestination -> selectedUniversity?.let { university ->
                CampusDestinationScreen(
                    university = university,
                    onBack = { mode = AppMode.UniversitySelection },
                    onFindOnMap = { location ->
                        selectedLocation = location
                        mode = AppMode.CampusNav
                    }
                )
            }
            AppMode.CampusNav -> selectedUniversity?.let { university ->
                CampusMapScreen(
                    university = university,
                    initialSelectedLocation = selectedLocation,
                    onBack = { mode = AppMode.CampusDestination }
                )
            }
            AppMode.Entertainment -> IPTVPlayerApp(
                onBackToSelection = { mode = AppMode.CampusNav }
            )
            AppMode.ContentCreator -> ContentCreatorScreen(
                onBack = { mode = AppMode.CampusNav },
                onSave = { /* Handle project save */ }
            )
        }
    }
}

package org.com

import androidx.compose.runtime.*
import org.com.campus.data.CampusLocation
import org.com.campus.data.University
import org.com.campus.presentation.CampusDestinationScreen
import org.com.campus.presentation.CampusMapScreen
import org.com.campus.presentation.UniversitySelectionScreen
import org.com.core.ui.theme.CampNavTheme

enum class AppMode {
    UniversitySelection,
    CampusDestination,
    CampusNav
}

@Composable
fun MainApp() {
    var mode by remember { mutableStateOf(AppMode.UniversitySelection) }
    var selectedUniversity by remember { mutableStateOf<University?>(null) }
    var selectedLocation by remember { mutableStateOf<CampusLocation?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }

    CampNavTheme {
        when (mode) {
            AppMode.UniversitySelection -> UniversitySelectionScreen(
                onUniversitySelected = { university ->
                    selectedUniversity = university
                    selectedLocation = null
                    selectedCategoryId = null
                    mode = AppMode.CampusDestination
                }
            )
            AppMode.CampusDestination -> selectedUniversity?.let { university ->
                CampusDestinationScreen(
                    university = university,
                    onBack = { mode = AppMode.UniversitySelection },
                    onFindOnMap = { location, categoryId ->
                        selectedLocation = location
                        selectedCategoryId = categoryId
                        mode = AppMode.CampusNav
                    }
                )
            }
            AppMode.CampusNav -> selectedUniversity?.let { university ->
                CampusMapScreen(
                    university = university,
                    initialSelectedLocation = selectedLocation,
                    selectedCategoryId = selectedCategoryId,
                    onBack = { mode = AppMode.CampusDestination }
                )
            }
        }
    }
}

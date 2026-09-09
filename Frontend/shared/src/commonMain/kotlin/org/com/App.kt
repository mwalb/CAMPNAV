package org.com

import androidx.compose.runtime.*
import org.com.campus.data.University
import org.com.campus.presentation.CampusMapScreen
import org.com.campus.presentation.UniversitySelectionScreen
import org.com.creator.presentation.ContentCreatorScreen
import org.com.entertainment.presentation.IPTVPlayerApp
import org.com.core.ui.theme.CampNavTheme

enum class AppMode {
    UniversitySelection,
    CampusNav,
    Entertainment,
    ContentCreator
}

@Composable
fun MainApp() {
    var mode by remember { mutableStateOf(AppMode.UniversitySelection) }
    var selectedUniversity by remember { mutableStateOf<University?>(null) }

    CampNavTheme {
        when (mode) {
            AppMode.UniversitySelection -> UniversitySelectionScreen(
                onUniversitySelected = { university ->
                    selectedUniversity = university
                    mode = AppMode.CampusNav
                }
            )
            AppMode.CampusNav -> selectedUniversity?.let {
                CampusMapScreen(
                    university = it,
                    onBack = { mode = AppMode.UniversitySelection }
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

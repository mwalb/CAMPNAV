package org.com

import androidx.compose.runtime.*
import org.com.campus.data.CampusLocation
import org.com.campus.data.University
import org.com.campus.presentation.CampusDestinationScreen
import org.com.campus.presentation.CampusMapScreen
import org.com.campus.presentation.LandingScreen
import org.com.campus.presentation.UniversitySelectionScreen
import org.com.community.model.UserRole
import org.com.community.presentation.CommunityDashboardScreen
import org.com.community.presentation.CommunityMapScreen
import org.com.community.presentation.CommunityReportScreen
import org.com.community.presentation.IssueDetailScreen
import org.com.community.presentation.ReportingLandingScreen
import org.com.core.ui.theme.CampNavTheme
import org.com.creator.presentation.ContentCreatorScreen
import org.com.entertainment.presentation.IPTVPlayerApp
import org.com.plant.data.PlantRepository
import org.com.plant.presentation.PlantDashboardScreen
import org.com.plant.presentation.PlantDetailScreen
import org.com.plant.presentation.PlantRegistrationScreen

enum class AppMode {
    Landing,
    UniversitySelection,
    CampusDestination,
    CampusNav,
    ReportingLanding,
    CommunityReport,
    CommunityDashboard,
    CommunityMap,
    IssueDetail,
    PlantManagement,
    PlantRegistration,
    PlantDetail,
    ContentCreator,
    Entertainment
}

@Composable
fun MainApp() {
    var mode by remember { mutableStateOf(AppMode.Landing) }
    var selectedUniversity by remember { mutableStateOf<University?>(null) }
    var selectedLocation by remember { mutableStateOf<CampusLocation?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedIssueId by remember { mutableStateOf<String?>(null) }
    var selectedPlantId by remember { mutableStateOf<String?>(null) }

    val plantRepository = remember { PlantRepository() }

    CampNavTheme {
        when (mode) {
            AppMode.Landing -> LandingScreen(
                onExploreCampuses = { mode = AppMode.UniversitySelection },
                onReportIssue = { mode = AppMode.ReportingLanding },
                onManagePlants = { mode = AppMode.PlantManagement },
                onContentCreator = { mode = AppMode.ContentCreator },
                onEntertainment = { mode = AppMode.Entertainment }
            )
            AppMode.Entertainment -> IPTVPlayerApp(
                onBackToSelection = { mode = AppMode.Landing }
            )
            AppMode.ContentCreator -> ContentCreatorScreen(
                onBack = { mode = AppMode.Landing },
                onSave = { _ ->
                    mode = AppMode.Landing
                }
            )
            AppMode.PlantManagement -> PlantDashboardScreen(
                repository = plantRepository,
                onBack = { mode = AppMode.Landing },
                onPlantNewTree = { mode = AppMode.PlantRegistration },
                onTreeSelected = { id ->
                    selectedPlantId = id
                    mode = AppMode.PlantDetail
                }
            )
            AppMode.PlantRegistration -> PlantRegistrationScreen(
                repository = plantRepository,
                onBack = { mode = AppMode.PlantManagement },
                onPlantCreated = { id ->
                    selectedPlantId = id
                    mode = AppMode.PlantDetail
                }
            )
            AppMode.PlantDetail -> selectedPlantId?.let { id ->
                PlantDetailScreen(
                    plantId = id,
                    repository = plantRepository,
                    onBack = { mode = AppMode.PlantManagement }
                )
            } ?: run {
                mode = AppMode.PlantManagement
            }
            AppMode.ReportingLanding -> ReportingLandingScreen(
                onReportNew = { mode = AppMode.CommunityReport },
                onViewDashboard = { mode = AppMode.CommunityDashboard },
                onBack = { mode = AppMode.Landing }
            )
            AppMode.CommunityReport -> CommunityReportScreen(
                onBack = { mode = AppMode.ReportingLanding }
            )
            AppMode.CommunityDashboard -> CommunityDashboardScreen(
                userRole = UserRole.CITIZEN,
                onBack = { mode = AppMode.ReportingLanding },
                onNavigateToMap = { mode = AppMode.CommunityMap },
                onReportSelected = { id ->
                    selectedIssueId = id
                    mode = AppMode.IssueDetail
                }
            )
            AppMode.CommunityMap -> CommunityMapScreen(
                onBack = { mode = AppMode.CommunityDashboard }
            )
            AppMode.IssueDetail -> selectedIssueId?.let { id ->
                IssueDetailScreen(
                    issueId = id,
                    onBack = { mode = AppMode.CommunityDashboard }
                )
            }
            AppMode.UniversitySelection -> UniversitySelectionScreen(
                onUniversitySelected = { university ->
                    selectedUniversity = university
                    selectedLocation = null
                    selectedCategoryId = null
                    mode = AppMode.CampusDestination
                },
                onBack = { mode = AppMode.Landing }
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

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
import org.com.entertainment.presentation.IPTVPlayerApp

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
    Entertainment
}

@Composable
fun MainApp() {
    var mode by remember { mutableStateOf(AppMode.Landing) }
    var selectedUniversity by remember { mutableStateOf<University?>(null) }
    var selectedLocation by remember { mutableStateOf<CampusLocation?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedIssueId by remember { mutableStateOf<String?>(null) }

    CampNavTheme {
        when (mode) {
            AppMode.Landing -> LandingScreen(
                onExploreCampuses = { mode = AppMode.UniversitySelection },
                onReportIssue = { mode = AppMode.ReportingLanding },
                onEntertainment = { mode = AppMode.Entertainment }
            )
            AppMode.Entertainment -> IPTVPlayerApp(
                onBackToSelection = { mode = AppMode.Landing }
            )
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

package org.com

import androidx.compose.runtime.*
import org.com.campus.data.CampusLocation
import org.com.campus.data.University
import org.com.campus.presentation.CampusDestinationScreen
import org.com.campus.presentation.CampusMapScreen
import org.com.campus.presentation.LandingScreen
import org.com.campus.presentation.UniversitySelectionScreen
import org.com.community.model.CommunityContribution
import org.com.community.model.UserRole
import org.com.community.presentation.*
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
    Entertainment,
    CommunityHub,
    CommunityAddLocation,
    CommunityReviews,
    CommunityFeedback,
    ContactUs,
    ContributionStatus,
    CommercialVerification,
    AdminLogin,
    AdminDashboard
}

@Composable
fun MainApp() {
    LaunchedEffect(Unit) {
        println("[CAMPNAV] [DIAGNOSTIC] APP INITIALIZED")
    }
    var mode by remember { mutableStateOf(AppMode.Landing) }
    var selectedUniversity by remember { mutableStateOf<University?>(null) }
    var selectedLocation by remember { mutableStateOf<CampusLocation?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedIssueId by remember { mutableStateOf<String?>(null) }
    var contributionReference by remember { mutableStateOf<String?>(null) }
    var submittedCommercialContribution by remember { mutableStateOf<CommunityContribution?>(null) }
    var adminToken by remember { mutableStateOf<String?>(null) }

    CampNavTheme {
        when (mode) {
            AppMode.Landing -> LandingScreen(
                onExploreCampuses = { mode = AppMode.UniversitySelection },
                onReportIssue = { mode = AppMode.ReportingLanding },
                onEntertainment = { mode = AppMode.Entertainment },
                onCommunity = { mode = AppMode.CommunityHub }
            )
            AppMode.Entertainment -> IPTVPlayerApp(
                onBackToSelection = { mode = AppMode.Landing }
            )
            AppMode.CommunityHub -> CommunityHubScreen(
                onBack = { mode = AppMode.Landing },
                onNavigateToAddLocation = { mode = AppMode.CommunityAddLocation },
                onNavigateToReviews = { mode = AppMode.CommunityReviews },
                onNavigateToFeedback = { mode = AppMode.CommunityFeedback },
                onNavigateToContact = { mode = AppMode.ContactUs },
                onNavigateToAdmin = {
                    if (adminToken != null) mode = AppMode.AdminDashboard
                    else mode = AppMode.AdminLogin
                }
            )
            AppMode.CommunityAddLocation -> AddLocationScreen(
                onBack = { mode = AppMode.CommunityHub },
                onSubmissionSuccess = { ref ->
                    contributionReference = ref
                    mode = AppMode.ContributionStatus
                },
                onCommercialSubmitted = { contrib ->
                    submittedCommercialContribution = contrib
                    mode = AppMode.CommercialVerification
                }
            )
            AppMode.CommercialVerification -> submittedCommercialContribution?.let { contrib ->
                CommercialVerificationScreen(
                    contribution = contrib,
                    onBackToHub = { mode = AppMode.CommunityHub }
                )
            } ?: run {
                CommunityHubScreen(
                    onBack = { mode = AppMode.Landing },
                    onNavigateToAddLocation = { mode = AppMode.CommunityAddLocation },
                    onNavigateToReviews = { mode = AppMode.CommunityReviews },
                    onNavigateToFeedback = { mode = AppMode.CommunityFeedback },
                    onNavigateToContact = { mode = AppMode.ContactUs }
                )
            }
            AppMode.CommunityReviews -> AreaReviewsScreen(
                onBack = { mode = AppMode.CommunityHub },
                onNavigateToAdmin = {
                    if (adminToken != null) mode = AppMode.AdminDashboard
                    else mode = AppMode.AdminLogin
                }
            )
            AppMode.CommunityFeedback -> UserFeedbackScreen(
                onBack = { mode = AppMode.CommunityHub }
            )
            AppMode.ContactUs -> ContactUsScreen(
                onBack = { mode = AppMode.CommunityHub }
            )
            AppMode.ContributionStatus -> ContributionStatusScreen(
                initialReference = contributionReference,
                onBack = { mode = AppMode.CommunityHub }
            )
            AppMode.AdminLogin -> AdminLoginScreen(
                onBack = { mode = AppMode.CommunityHub },
                onLoginSuccess = { token ->
                    adminToken = token
                    mode = AppMode.AdminDashboard
                }
            )
            AppMode.AdminDashboard -> adminToken?.let { token ->
                AdminDashboardScreen(
                    token = token,
                    onBack = { mode = AppMode.CommunityHub },
                    onLogout = {
                        adminToken = null
                        mode = AppMode.AdminLogin
                    }
                )
            } ?: run {
                AdminLoginScreen(
                    onBack = { mode = AppMode.CommunityHub },
                    onLoginSuccess = { token ->
                        adminToken = token
                        mode = AppMode.AdminDashboard
                    }
                )
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
                    println("[CAMPNAV] [DIAGNOSTIC] University selected: ${university.name} (ID: ${university.id})")
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
                        println("[CAMPNAV] [DIAGNOSTIC] Destination selected: ${location.name}, Destination ID: ${location.id}, Lat: ${location.latitude}, Lng: ${location.longitude}")
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

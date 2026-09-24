package org.com.community.presentation

import androidx.compose.runtime.*
import org.com.community.model.*
import org.com.community.data.CommunityRepository
import org.com.getCurrentEpochMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CommunityReportViewModel {
    private val repository = CommunityRepository()
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    var currentStep by mutableIntStateOf(1)

    var selectedCategory by mutableStateOf<IssueCategory?>(null)
    var description by mutableStateOf("")
    var evidenceUris = mutableStateListOf<String>()

    fun addEvidence(uri: String) {
        evidenceUris.add(uri)
    }

    fun removeEvidence(uri: String) {
        evidenceUris.remove(uri)
    }

    var issueLocation by mutableStateOf<GeoLocation?>(null)
    var reporterLocation by mutableStateOf<GeoLocation?>(null)

    fun onLocationSelected(lat: Double, lng: Double) {
        println("[CAMPNAV] Location selected: $lat, $lng")
        issueLocation = GeoLocation(latitude = lat, longitude = lng)
    }

    var userPriority by mutableStateOf(IssuePriority.MEDIUM)
    var isEmergency by mutableStateOf(false)

    var isSubmitting by mutableStateOf(false)
    var submissionResult by mutableStateOf<String?>(null)

    var isOffline by mutableStateOf(false)
    var pendingReports = mutableStateListOf<IssueReport>()

    var showDuplicateDialog by mutableStateOf(false)

    fun nextStep() {
        if (currentStep == 2) {
            // Simulate duplicate check before location confirmation
            showDuplicateDialog = true
        }
        if (currentStep < 5) currentStep++
    }

    fun prevStep() {
        if (currentStep > 1) currentStep--
    }

    fun submitReport() {
        isSubmitting = true

        val report = IssueReport(
            id = "", // Backend generates ID
            category = selectedCategory ?: IssueCategory.OTHER,
            description = description,
            issueLocation = issueLocation ?: GeoLocation(0.0, 0.0),
            reporterLocation = reporterLocation ?: GeoLocation(0.0, 0.0),
            distanceToIssue = 0.0,
            evidenceUris = evidenceUris,
            userPriority = userPriority,
            calculatedPriority = IssuePriority.MEDIUM, // Backend calculates
            impactScore = 0, // Backend calculates
            reporterId = "citizen_1", // Mocked for now
            timestamp = getCurrentEpochMillis(),
            lastUpdated = getCurrentEpochMillis(),
            isEmergency = isEmergency
        )

        viewModelScope.launch {
            try {
                val result = repository.createReport(report)
                submissionResult = result.id
                currentStep = 5
            } catch (e: Exception) {
                // Offline support: Store locally if submission fails
                submissionResult = "OFFLINE-REPORT-${getCurrentEpochMillis().toString().takeLast(4)}"
                pendingReports.add(report)
                isOffline = true
                currentStep = 5
            } finally {
                isSubmitting = false
            }
        }
    }

    private fun calculatePriority(): IssuePriority {
        // Simple logic based on category and user selection
        if (isEmergency) return IssuePriority.CRITICAL
        return userPriority
    }

    private fun calculateImpactScore(): Int {
        // Mock score calculation
        return (10..100).random()
    }
}

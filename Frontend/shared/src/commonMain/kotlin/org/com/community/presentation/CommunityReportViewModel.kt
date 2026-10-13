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

    // University & Campus Context
    var selectedUniversityId by mutableStateOf<Long?>(1L) // Default UDSM
    var selectedUniversityName by mutableStateOf("University of Dar es Salaam")
    var campusName by mutableStateOf("Main Campus")

    // Quick-Report Primary Fields
    var selectedCategory by mutableStateOf<IssueCategory?>(null)
    var description by mutableStateOf("")
    var urgency by mutableStateOf("NORMAL") // LOW, NORMAL, HIGH, EMERGENCY
    var locationSource by mutableStateOf("CURRENT_LOCATION")
    var issueLocation by mutableStateOf<GeoLocation?>(GeoLocation(-6.780543, 39.204593))
    var reporterLocation by mutableStateOf<GeoLocation?>(GeoLocation(-6.780543, 39.204593))

    // Required Reporter Name & Optional Details (Expanded by Default)
    var showOptionalDetails by mutableStateOf(true) // Kept expanded by default
    var showMapPicker by mutableStateOf(false)
    var isFetchingCurrentLocation by mutableStateOf(false)
    var detailedLocationNote by mutableStateOf("")
    
    // Identity Fields: Name is Required, Email is Optional
    var reporterName by mutableStateOf("")
    var reporterEmail by mutableStateOf("")
    var reporterPhone by mutableStateOf("")
    var evidenceUris = mutableStateListOf<String>()

    // Derived Title
    val derivedTitle: String
        get() {
            val text = description.trim()
            if (text.isEmpty()) return ""
            return if (text.length > 40) text.take(37) + "..." else text
        }

    // Email Validation Helper (valid if empty, or if formatted correctly when provided)
    val isEmailValid: Boolean
        get() {
            val email = reporterEmail.trim()
            return email.isEmpty() || (email.contains("@") && email.contains("."))
        }

    // Form Validation (Category, Description, Location, and Reporter Name are REQUIRED)
    val isFormValid: Boolean
        get() = selectedCategory != null &&
                description.trim().length >= 3 &&
                issueLocation != null &&
                reporterName.trim().isNotEmpty() &&
                isEmailValid

    // Submission & State Guards
    var isSubmitting by mutableStateOf(false)
    var isSubmitted by mutableStateOf(false)
    var submissionResult by mutableStateOf<String?>(null)
    var routingResult by mutableStateOf<String?>(null)
    var submissionError by mutableStateOf<String?>(null)
    var isOffline by mutableStateOf(false)
    var pendingReports = mutableStateListOf<IssueReport>()
    
    private var lastSubmitTimeMillis = 0L

    fun addEvidence(uri: String) {
        evidenceUris.add(uri)
    }

    fun removeEvidence(uri: String) {
        evidenceUris.remove(uri)
    }

    fun onLocationSelected(lat: Double, lng: Double, source: String = "PICK_FROM_MAP") {
        issueLocation = GeoLocation(latitude = lat, longitude = lng)
        locationSource = source
        showMapPicker = false
    }

    fun submitReport() {
        val now = getCurrentEpochMillis()
        if (isSubmitting || (now - lastSubmitTimeMillis < 2000)) {
            println("[CAMPNAV] Prevented duplicate submit attempt")
            return
        }

        if (reporterName.trim().isEmpty()) {
            submissionError = "Your name is required."
            return
        }

        if (!isEmailValid) {
            submissionError = "Please enter a valid email address or leave it blank."
            return
        }

        if (!isFormValid) {
            submissionError = "Please select a category, enter a description, and fill in all required fields."
            return
        }

        isSubmitting = true
        submissionError = null
        lastSubmitTimeMillis = now

        val finalDescription = if (detailedLocationNote.isNotBlank()) {
            "${description.trim()}\n\n[Location Note: ${detailedLocationNote.trim()}]"
        } else {
            description.trim()
        }

        val report = IssueReport(
            id = "", // Backend generates tracking reference ID
            universityId = selectedUniversityId,
            universityName = selectedUniversityName,
            campusName = campusName,
            category = selectedCategory ?: IssueCategory.OTHER,
            description = finalDescription,
            urgency = urgency,
            locationSource = locationSource,
            issueLocation = issueLocation ?: GeoLocation(-6.780543, 39.204593),
            reporterLocation = reporterLocation ?: GeoLocation(-6.780543, 39.204593),
            distanceToIssue = 0.0,
            evidenceUris = evidenceUris.toList(),
            userPriority = when(urgency) {
                "EMERGENCY" -> IssuePriority.CRITICAL
                "HIGH" -> IssuePriority.HIGH
                "LOW" -> IssuePriority.LOW
                else -> IssuePriority.MEDIUM
            },
            calculatedPriority = IssuePriority.MEDIUM,
            impactScore = 0,
            reporterId = "student_user",
            reporterName = reporterName.trim(),
            reporterEmail = reporterEmail.trim().ifBlank { null },
            reporterPhone = reporterPhone.trim().ifBlank { null },
            timestamp = now,
            lastUpdated = now,
            isEmergency = (urgency == "EMERGENCY")
        )

        viewModelScope.launch {
            try {
                val result = repository.createReport(report)
                submissionResult = if (!result.id.isNull_or_blank()) result.id else "CAM-${now.toString().takeLast(8)}"
                routingResult = result.routingStatus
                isSubmitted = true
                isSubmitting = false
            } catch (e: Exception) {
                println("[CAMPNAV] Submission network failed: ${e.message}")
                submissionError = "Network submission failed. Check connection and tap Retry."
                isSubmitting = false
            }
        }
    }

    fun retrySubmit() {
        submitReport()
    }

    fun resetForm() {
        selectedCategory = null
        description = ""
        urgency = "NORMAL"
        locationSource = "CURRENT_LOCATION"
        showOptionalDetails = true
        showMapPicker = false
        detailedLocationNote = ""
        reporterName = ""
        reporterEmail = ""
        reporterPhone = ""
        evidenceUris.clear()
        isSubmitting = false
        isSubmitted = false
        submissionResult = null
        routingResult = null
        submissionError = null
    }
}

private fun String?.isNull_or_blank(): Boolean {
    return this == null || this.trim().isEmpty()
}

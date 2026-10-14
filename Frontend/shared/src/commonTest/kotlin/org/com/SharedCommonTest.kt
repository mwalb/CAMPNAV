package org.com

import org.com.community.model.GeoLocation
import org.com.community.model.IssueCategory
import org.com.community.model.toDomain
import org.com.community.presentation.CommunityReportViewModel
import org.com.entertainment.model.Channel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SharedCommonTest {

    @Test
    fun testThemeStateToggleAndPersistence() {
        saveThemeIsDark(false)
        assertEquals(false, getSavedThemeIsDark())

        saveThemeIsDark(true)
        assertEquals(true, getSavedThemeIsDark())
    }

    @Test
    fun testReportFormValidationMandatoryNameAndOptionalEmail() {
        val viewModel = CommunityReportViewModel()

        // Initially invalid because category, description, and name are missing
        assertFalse(viewModel.isFormValid)

        // Mandatory Reporter Name validation: Blank name must fail
        viewModel.selectedCategory = IssueCategory.WATER
        viewModel.description = "Pipe burst outside library"
        viewModel.issueLocation = GeoLocation(-6.78, 39.20)
        viewModel.reporterName = "   "
        assertFalse(viewModel.isFormValid)

        // Valid name + Blank email MUST pass
        viewModel.reporterName = "Raphael Frank"
        viewModel.reporterEmail = ""
        assertTrue(viewModel.isEmailValid)
        assertTrue(viewModel.isFormValid)

        // Valid name + Invalid email MUST fail
        viewModel.reporterEmail = "not-an-email"
        assertFalse(viewModel.isEmailValid)
        assertFalse(viewModel.isFormValid)

        // Valid name + Valid email MUST pass
        viewModel.reporterEmail = "raphael@example.com"
        assertTrue(viewModel.isEmailValid)
        assertTrue(viewModel.isFormValid)
    }

    @Test
    fun testChannelCategoryAndSearchFilter() {
        val channels = listOf(
            Channel(name = "TBC 1", url = "http://tbc.stream", category = "Tanzania"),
            Channel(name = "BBC News", url = "http://bbc.stream", category = "News"),
            Channel(name = "Wasafi TV", url = "http://wasafi.stream", category = "Tanzania")
        )

        val tanzaniaChannels = channels.filter { it.category == "Tanzania" }
        assertEquals(2, tanzaniaChannels.size)

        val searchChannels = channels.filter { it.name.contains("BBC", ignoreCase = true) }
        assertEquals(1, searchChannels.size)
        assertEquals("BBC News", searchChannels.first().name)
    }

    @Test
    fun testDistinctChannelsSharingUrlArePreserved() {
        val sharedUrl = "https://ntv1.akamaized.net/hls/live/2014075/NASA-NTV1-HLS/master.m3u8"
        val channels = listOf(
            Channel(name = "NASA TV Science Live", url = sharedUrl, category = "Science"),
            Channel(name = "Tanzania Safari Channel", url = sharedUrl, category = "Safari")
        )

        val distinctByName = channels.distinctBy { it.name }
        assertEquals(2, distinctByName.size)
        assertEquals("NASA TV Science Live", distinctByName[0].name)
        assertEquals("Tanzania Safari Channel", distinctByName[1].name)
        assertEquals(sharedUrl, distinctByName[0].url)
        assertEquals(sharedUrl, distinctByName[1].url)
    }

    @Test
    fun testTimestampAndIssueReportResponseDeserializationWithIso8601String() {
        val jsonString = """
            {
              "id": "CAM-20261014-04504",
              "dbId": 42,
              "universityId": 1,
              "universityName": "University of Dar es Salaam",
              "campusName": "Mlimani Campus",
              "category": "WATER",
              "subCategory": "Burst Pipe",
              "description": "Burst pipe near science faculty",
              "status": "SUBMITTED",
              "routingStatus": "ASSIGNED_TO_LEADER",
              "urgency": "NORMAL",
              "userSeverity": "HIGH",
              "calculatedPriority": "HIGH",
              "impactScore": 50,
              "latitude": -6.780543,
              "longitude": 39.204593,
              "address": "CoICT, Kijitonyama",
              "street": "Ali Hassan Mwinyi Rd",
              "ward": "Kijitonyama",
              "district": "Kinondoni",
              "region": "Dar es Salaam",
              "nearbyLandmark": "Library",
              "reporterLatitude": -6.7805,
              "reporterLongitude": 39.2045,
              "locationAccuracy": 5.0,
              "issueLocation": {
                "latitude": -6.780543,
                "longitude": 39.204593,
                "address": "CoICT, Kijitonyama"
              },
              "reporterLocation": {
                "latitude": -6.7805,
                "longitude": 39.2045
              },
              "evidenceUrls": ["https://example.com/evidence1.jpg"],
              "evidenceUris": ["https://example.com/evidence1.jpg"],
              "locationSource": "CURRENT_LOCATION",
              "reporterName": "Raphael Frank",
              "reporterEmail": "raphael@example.com",
              "reporterPhone": "+255712345678",
              "supportsCount": 3,
              "verificationCount": 1,
              "isEmergency": false,
              "timestamp": "2026-10-14T04:50:49.225667866",
              "lastUpdated": "2026-10-14T04:50:49.225667866",
              "updatedAt": "2026-10-14T04:50:49.225667866",
              "timeline": [
                {
                  "timestamp": "2026-10-14T04:50:49.225667866",
                  "status": "SUBMITTED",
                  "message": "Report successfully submitted",
                  "actorId": "citizen"
                }
              ]
            }
        """.trimIndent()

        val jsonParser = kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        val responseDto = jsonParser.decodeFromString<org.com.community.model.IssueReportResponse>(jsonString)
        assertEquals("CAM-20261014-04504", responseDto.id)
        assertTrue(responseDto.timestamp > 0L, "Timestamp should be parsed into epoch millis > 0")

        val domain = responseDto.toDomain()
        assertEquals("CAM-20261014-04504", domain.id)
        assertEquals(org.com.community.model.IssueCategory.WATER, domain.category)
        assertEquals(-6.780543, domain.issueLocation.latitude)
        assertEquals(39.204593, domain.issueLocation.longitude)
        assertEquals("CoICT, Kijitonyama", domain.issueLocation.address)
        assertEquals(1, domain.evidenceUris.size)
        assertEquals("https://example.com/evidence1.jpg", domain.evidenceUris.first())
        assertEquals(1, domain.timeline.size)
        assertTrue(domain.timestamp > 0L)
        assertTrue(domain.lastUpdated > 0L)
    }

    @Test
    fun testIsoTimestampSerializerWithNumericAndIsoFormats() {
        val jsonParser = kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        val isoJson = """{"timestamp": "2026-10-14T04:50:49.225667866"}"""
        val isoResult = jsonParser.decodeFromString<org.com.community.model.IssueReportResponse>(isoJson)
        assertTrue(isoResult.timestamp > 0L)

        val numJson = """{"timestamp": 1728881449225}"""
        val numResult = jsonParser.decodeFromString<org.com.community.model.IssueReportResponse>(numJson)
        assertEquals(1728881449225L, numResult.timestamp)

        val isoZJson = """{"timestamp": "2026-10-14T04:50:49.225Z"}"""
        val isoZResult = jsonParser.decodeFromString<org.com.community.model.IssueReportResponse>(isoZJson)
        assertTrue(isoZResult.timestamp > 0L)
    }

    @Test
    fun testUniversityReportFilteringAndExclusion() {
        val udsmReport = org.com.community.model.IssueReport(
            id = "CAM-UDSM-001",
            universityId = 1L,
            universityName = "University of Dar es Salaam",
            category = IssueCategory.WATER,
            description = "Pipe leak at UDSM library"
        )

        val udomReport = org.com.community.model.IssueReport(
            id = "CAM-UDOM-002",
            universityId = 2L,
            universityName = "University of Dodoma",
            category = IssueCategory.ELECTRICITY,
            description = "Power outage at UDOM Social Sciences"
        )

        val reports = listOf(udsmReport, udomReport)

        // Filter for UDSM
        val udsmFiltered = reports.filter { it.universityId == 1L }
        assertEquals(1, udsmFiltered.size)
        assertEquals("CAM-UDSM-001", udsmFiltered.first().id)

        // Filter for UDOM
        val udomFiltered = reports.filter { it.universityId == 2L }
        assertEquals(1, udomFiltered.size)
        assertEquals("CAM-UDOM-002", udomFiltered.first().id)

        // Excludes non-matching university
        assertFalse(udsmFiltered.any { it.universityId == 2L })
        assertFalse(udomFiltered.any { it.universityId == 1L })
    }

    @Test
    fun testValidReportCoordinatesFilteringForMapMarkers() {
        val validReport = org.com.community.model.IssueReport(
            id = "CAM-VALID-01",
            universityId = 1L,
            issueLocation = GeoLocation(latitude = -6.780543, longitude = 39.204593)
        )

        val invalidReport = org.com.community.model.IssueReport(
            id = "CAM-INVALID-02",
            universityId = 1L,
            issueLocation = GeoLocation(latitude = 0.0, longitude = 0.0)
        )

        val reports = listOf(validReport, invalidReport)
        val mapMarkerReports = reports.filter { it.issueLocation.latitude != 0.0 || it.issueLocation.longitude != 0.0 }

        assertEquals(1, mapMarkerReports.size)
        assertEquals("CAM-VALID-01", mapMarkerReports.first().id)
    }

    @Test
    fun testIssuesMapJsonSerializationWithSpecialCharacters() {
        val issueWithSpecialChars = org.com.community.model.IssueReport(
            id = "CAM-SPEC-01",
            universityId = 1L,
            category = IssueCategory.SAFETY,
            description = "Dangerous wire hanging!\n\"Careful\" \\ backslash \\r tab\t and 100% salama & amani 🏛️",
            status = org.com.community.model.IssueStatus.SUBMITTED,
            issueLocation = GeoLocation(latitude = -6.78, longitude = 39.20)
        )

        val issueEmptyOptional = org.com.community.model.IssueReport(
            id = "",
            universityId = 1L,
            category = IssueCategory.WATER,
            description = "",
            status = org.com.community.model.IssueStatus.SUBMITTED,
            issueLocation = GeoLocation(latitude = 0.0, longitude = 0.0)
        )

        val issues = listOf(issueWithSpecialChars, issueEmptyOptional)
        val markers = issues.map { issue ->
            org.com.community.components.MapIssueMarker(
                id = issue.id.ifBlank { "CAM-ISSUE" },
                lat = issue.issueLocation.latitude,
                lng = issue.issueLocation.longitude,
                cat = issue.category.displayName,
                desc = issue.description.take(50),
                status = issue.status.name
            )
        }

        val jsonStr = kotlinx.serialization.json.Json.encodeToString(markers)
        assertTrue(jsonStr.contains("Dangerous wire hanging!"))

        val decoded = kotlinx.serialization.json.Json.decodeFromString<List<org.com.community.components.MapIssueMarker>>(jsonStr)
        assertEquals(2, decoded.size)
        assertEquals("CAM-SPEC-01", decoded[0].id)
        assertEquals("CAM-ISSUE", decoded[1].id)
    }
}
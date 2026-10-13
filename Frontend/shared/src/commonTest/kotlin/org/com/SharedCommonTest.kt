package org.com

import org.com.community.model.GeoLocation
import org.com.community.model.IssueCategory
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
}
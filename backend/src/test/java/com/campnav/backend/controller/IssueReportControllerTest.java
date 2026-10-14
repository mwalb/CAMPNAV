package com.campnav.backend.controller;

import com.campnav.backend.dto.CreateIssueReportRequest;
import com.campnav.backend.dto.IssueReportResponse;
import com.campnav.backend.model.IssueReport;
import com.campnav.backend.repository.IssueReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class IssueReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IssueReportRepository reportRepository;

    @Test
    public void testGetAllReportsPermittedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/community/reports"))
                .andExpect(status().isOk());
    }

    @Test
    public void testCreateReportValidRequestAndCompleteLifecycle() throws Exception {
        CreateIssueReportRequest request = CreateIssueReportRequest.builder()
                .reporterName("Raphael Frank")
                .reporterEmail("raphael@example.com")
                .description("Burst water pipe near library")
                .category("WATER")
                .latitude(-6.780543)
                .longitude(39.204593)
                .address("Mlimani Campus")
                .urgency("NORMAL")
                .userSeverity("HIGH")
                .build();

        // 1. Submit Report (Verify HTTP 200 OK and NO 403 Forbidden)
        MvcResult postResult = mockMvc.perform(post("/api/community/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.reporterName").value("Raphael Frank"))
                .andExpect(jsonPath("$.category").value("WATER"))
                .andExpect(jsonPath("$.latitude").value(-6.780543))
                .andExpect(jsonPath("$.longitude").value(39.204593))
                .andExpect(jsonPath("$.issueLocation.latitude").value(-6.780543))
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.lastUpdated").isString())
                .andReturn();

        IssueReportResponse response = objectMapper.readValue(
                postResult.getResponse().getContentAsString(),
                IssueReportResponse.class
        );

        assertNotNull(response.getId());
        assertTrue(response.getId().startsWith("CAM-"));
        assertNotNull(response.getTimestamp());
        assertTrue(response.getTimestamp().contains("T"), "Timestamp should be an ISO-8601 string containing 'T'");

        // 2. Verify Report Retrieval via GET /api/community/reports/{externalId}
        mockMvc.perform(get("/api/community/reports/" + response.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.reporterName").value("Raphael Frank"))
                .andExpect(jsonPath("$.description").value("Burst water pipe near library"));

        // 3. Verify Database Persistence in Repository
        Optional<IssueReport> dbReport = reportRepository.findByExternalId(response.getId());
        assertTrue(dbReport.isPresent(), "Report should be persisted in PostgreSQL/database");
        assertEquals("Raphael Frank", dbReport.get().getReporterName());
        assertEquals("WATER", dbReport.get().getCategory());
        assertEquals(-6.780543, dbReport.get().getLatitude());
    }

    @Test
    public void testCreateReportValidationFailureMissingReporterName() throws Exception {
        CreateIssueReportRequest request = CreateIssueReportRequest.builder()
                .reporterName("")
                .description("Burst pipe")
                .category("WATER")
                .latitude(-6.780543)
                .longitude(39.204593)
                .build();

        mockMvc.perform(post("/api/community/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateReportLinkedToUniversityAndFilteringByUniversityId() throws Exception {
        // Create UDOM report (universityId = 2)
        CreateIssueReportRequest udomRequest = CreateIssueReportRequest.builder()
                .universityId(2L)
                .reporterName("Amina Said")
                .description("Power failure in College of Education")
                .category("ELECTRICITY")
                .latitude(-6.1700)
                .longitude(35.7981)
                .urgency("HIGH")
                .build();

        MvcResult udomPostResult = mockMvc.perform(post("/api/community/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(udomRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.universityId").value(2))
                .andReturn();

        IssueReportResponse udomResponse = objectMapper.readValue(
                udomPostResult.getResponse().getContentAsString(),
                IssueReportResponse.class
        );

        // Fetch reports specifically for UDOM (universityId = 2)
        mockMvc.perform(get("/api/universities/2/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + udomResponse.getId() + "')].universityId").value(2));

        mockMvc.perform(get("/api/community/reports?universityId=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + udomResponse.getId() + "')].universityId").value(2));
    }

    @Test
    public void testGetReportsInvalidUniversityIdReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/universities/9999/reports"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        mockMvc.perform(get("/api/community/reports?universityId=9999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    public void testCreateReportWithNonexistentUniversityIdReturnsBadRequest() throws Exception {
        CreateIssueReportRequest request = CreateIssueReportRequest.builder()
                .universityId(9999L)
                .reporterName("John Doe")
                .description("Invalid university report test")
                .category("WATER")
                .latitude(-6.78)
                .longitude(39.20)
                .build();

        mockMvc.perform(post("/api/community/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}

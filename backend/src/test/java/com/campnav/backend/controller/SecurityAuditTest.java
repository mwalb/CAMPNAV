package com.campnav.backend.controller;

import com.campnav.backend.dto.CreateIssueReportRequest;
import com.campnav.backend.model.CommunityReview;
import com.campnav.backend.model.UserFeedback;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityAuditTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testAdminEndpointUnauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/admin/issues"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDebugAllRawEndpointUnauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/universities/debug/all-raw"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAdminEndpointAllowedWithAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin/issues"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDebugAllRawEndpointAllowedWithAdminRole() throws Exception {
        mockMvc.perform(get("/api/universities/debug/all-raw"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testAdminEndpointForbiddenWithOrdinaryUserRole() throws Exception {
        mockMvc.perform(get("/api/admin/issues"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testDebugAllRawEndpointForbiddenWithOrdinaryUserRole() throws Exception {
        mockMvc.perform(get("/api/universities/debug/all-raw"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testReviewValidationFailureInvalidRating() throws Exception {
        CommunityReviewCommunityRequestDto reviewRequest = new CommunityReviewCommunityRequestDto();
        reviewRequest.setUniversityId(1L);
        reviewRequest.setRating(6); // Invalid rating > 5
        reviewRequest.setComment("Great campus");
        reviewRequest.setContributorName("Test User");

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testFeedbackValidationFailureBlankText() throws Exception {
        UserFeedbackController.FeedbackRequest feedbackRequest = new UserFeedbackController.FeedbackRequest();
        feedbackRequest.setFeedbackText(""); // Blank
        feedbackRequest.setContributorName("Test User");

        mockMvc.perform(post("/api/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(feedbackRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLoginWithValidAdminCredentials() throws Exception {
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest();
        loginRequest.setUsername("mwalb66");
        loginRequest.setPassword("CAMPNAV1111()");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    public static class CommunityReviewCommunityRequestDto {
        private Long universityId;
        private Long locationId;
        private Integer rating;
        private String comment;
        private String contributorName;
        private String contributorContact;

        public Long getUniversityId() { return universityId; }
        public void setUniversityId(Long universityId) { this.universityId = universityId; }
        public Long getLocationId() { return locationId; }
        public void setLocationId(Long locationId) { this.locationId = locationId; }
        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
        public String getContributorName() { return contributorName; }
        public void setContributorName(String contributorName) { this.contributorName = contributorName; }
        public String getContributorContact() { return contributorContact; }
        public void setContributorContact(String contributorContact) { this.contributorContact = contributorContact; }
    }
}

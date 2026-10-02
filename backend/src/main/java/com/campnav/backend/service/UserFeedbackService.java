package com.campnav.backend.service;

import com.campnav.backend.model.AdminActivityLog;
import com.campnav.backend.model.UserFeedback;
import com.campnav.backend.repository.AdminActivityLogRepository;
import com.campnav.backend.repository.UserFeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserFeedbackService {
    private final UserFeedbackRepository feedbackRepository;
    private final AdminActivityLogRepository activityLogRepository;

    public UserFeedback submitFeedback(String feedbackText, String contributorName, String contributorContact) {
        UserFeedback feedback = UserFeedback.builder()
                .feedbackText(feedbackText)
                .contributorName(contributorName != null && !contributorName.isBlank() ? contributorName : "Anonymous")
                .contributorContact(contributorContact)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
        return feedbackRepository.save(feedback);
    }

    public List<UserFeedback> getApprovedFeedback() {
        return feedbackRepository.findByStatus("APPROVED");
    }

    public List<UserFeedback> getAllFeedback() {
        return feedbackRepository.findAll();
    }

    public UserFeedback approveFeedback(Long id, String adminUsername) {
        UserFeedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found with id: " + id));
        feedback.setStatus("APPROVED");
        UserFeedback saved = feedbackRepository.save(feedback);

        AdminActivityLog log = AdminActivityLog.builder()
                .action("APPROVE_FEEDBACK")
                .actorUsername(adminUsername)
                .description("Approved user feedback ID: " + id)
                .relatedEntityId("FEEDBACK-" + id)
                .build();
        activityLogRepository.save(log);

        return saved;
    }

    public UserFeedback rejectFeedback(Long id, String reason, String adminUsername) {
        UserFeedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found with id: " + id));
        feedback.setStatus("REJECTED");
        feedback.setRejectionReason(reason);
        UserFeedback saved = feedbackRepository.save(feedback);

        AdminActivityLog log = AdminActivityLog.builder()
                .action("REJECT_FEEDBACK")
                .actorUsername(adminUsername)
                .description("Rejected user feedback ID: " + id + ", Reason: " + reason)
                .relatedEntityId("FEEDBACK-" + id)
                .build();
        activityLogRepository.save(log);

        return saved;
    }
}

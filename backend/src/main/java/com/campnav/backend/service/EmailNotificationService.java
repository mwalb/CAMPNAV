package com.campnav.backend.service;

import com.campnav.backend.model.AdminActivityLog;
import com.campnav.backend.model.CommunityUser;
import com.campnav.backend.model.EmailNotification;
import com.campnav.backend.model.EmailNotification.DeliveryState;
import com.campnav.backend.model.IssueReport;
import com.campnav.backend.repository.AdminActivityLogRepository;
import com.campnav.backend.repository.EmailNotificationRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class EmailNotificationService {

    private final EmailNotificationRepository notificationRepository;
    private final AdminActivityLogRepository activityLogRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${campnav.app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${campnav.admin.email:admin@campnav.org}")
    private String adminEmail;

    @Value("${campnav.email.enabled:true}")
    private boolean emailEnabled;

    @Transactional
    public EmailNotification queueNewReportLeaderNotification(IssueReport report, CommunityUser leader) {
        String univName = report.getUniversity() != null ? report.getUniversity().getName() : "University";
        String subject = "[CAMPNAV] New Campus Issue Reported: " + report.getExternalId() + " (" + univName + ")";

        String dashboardUrl = baseUrl + "/#leader-dashboard";

        String textBody = "Hello " + leader.getFullName() + ",\n\n" +
                "A new issue has been reported for " + univName + ".\n" +
                "Reference: " + report.getExternalId() + "\n" +
                "Category: " + report.getCategory() + "\n" +
                "Urgency: " + (report.getUrgency() != null ? report.getUrgency() : "NORMAL") + "\n" +
                "Description: " + report.getDescription() + "\n\n" +
                "Please review and acknowledge this report in your Student Leader Dashboard:\n" +
                dashboardUrl + "\n\n" +
                "Thank you,\nCAMPNAV System";

        String htmlBody = "<div style='font-family: Arial, sans-serif; padding: 20px; background-color: #f4f6f8;'>" +
                "<div style='max-width: 600px; margin: auto; background: #ffffff; border-radius: 8px; padding: 24px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>" +
                "<h2 style='color: #1E3A8A; margin-top: 0;'>CAMPNAV Community Issue Alert</h2>" +
                "<p>Hello <strong>" + leader.getFullName() + "</strong>,</p>" +
                "<p>A new campus issue report has been submitted for <strong>" + univName + "</strong> and assigned to you as the designated Student Leader.</p>" +
                "<table style='width: 100%; border-collapse: collapse; margin: 16px 0;'>" +
                "<tr><td style='padding: 8px; background: #f8fafc;'><strong>Report Reference:</strong></td><td style='padding: 8px; background: #f8fafc;'>" + report.getExternalId() + "</td></tr>" +
                "<tr><td style='padding: 8px;'><strong>Category:</strong></td><td style='padding: 8px;'>" + report.getCategory() + "</td></tr>" +
                "<tr><td style='padding: 8px; background: #f8fafc;'><strong>Urgency:</strong></td><td style='padding: 8px; background: #f8fafc;'><span style='color: " + getUrgencyColor(report.getUrgency() != null ? report.getUrgency().name() : "NORMAL") + "; font-weight: bold;'>" + (report.getUrgency() != null ? report.getUrgency() : "NORMAL") + "</span></td></tr>" +
                "<tr><td style='padding: 8px;'><strong>Description:</strong></td><td style='padding: 8px;'>" + report.getDescription() + "</td></tr>" +
                "</table>" +
                "<div style='text-align: center; margin-top: 24px;'>" +
                "<a href='" + dashboardUrl + "' style='background-color: #2563EB; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;'>View in Leader Dashboard</a>" +
                "</div>" +
                "<hr style='margin-top: 30px; border: 0; border-top: 1px solid #e2e8f0;'/>" +
                "<p style='font-size: 12px; color: #64748b; text-align: center;'>CAMPNAV University Student Resolution System</p>" +
                "</div></div>";

        EmailNotification notification = EmailNotification.builder()
                .report(report)
                .recipientEmail(leader.getEmail())
                .recipientRole("LEADER")
                .notificationType("NEW_REPORT_LEADER")
                .subject(subject)
                .bodyText(textBody)
                .bodyHtml(htmlBody)
                .deliveryState(DeliveryState.PENDING)
                .attemptCount(0)
                .createdAt(LocalDateTime.now())
                .nextRetryAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    @Transactional
    public EmailNotification queueNewReportAdminNotification(IssueReport report) {
        String univName = report.getUniversity() != null ? report.getUniversity().getName() : "University";
        String leaderName = report.getAssignedLeader() != null ? report.getAssignedLeader().getFullName() : "Unassigned";
        String subject = "[CAMPNAV Admin Alert] New Report " + report.getExternalId() + " (" + univName + ") - Leader: " + leaderName;

        String dashboardUrl = baseUrl + "/#admin-dashboard";

        String textBody = "Attention CAMPNAV Admin,\n\n" +
                "A new campus issue report has been submitted.\n" +
                "Reference: " + report.getExternalId() + "\n" +
                "University: " + univName + "\n" +
                "Assigned Leader: " + leaderName + "\n" +
                "Category: " + report.getCategory() + "\n" +
                "Urgency: " + (report.getUrgency() != null ? report.getUrgency() : "NORMAL") + "\n" +
                "Description: " + report.getDescription() + "\n\n" +
                "You can monitor all reports in the Admin Dashboard:\n" +
                dashboardUrl + "\n\n" +
                "CAMPNAV System";

        String htmlBody = "<div style='font-family: Arial, sans-serif; padding: 20px; background-color: #f8fafc;'>" +
                "<div style='max-width: 600px; margin: auto; background: #ffffff; border-radius: 8px; padding: 24px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>" +
                "<h2 style='color: #1E3A8A; margin-top: 0;'>CAMPNAV Administrative Report Alert</h2>" +
                "<p>A new campus issue report has been registered in CAMPNAV.</p>" +
                "<table style='width: 100%; border-collapse: collapse; margin: 16px 0;'>" +
                "<tr><td style='padding: 8px; background: #f8fafc;'><strong>Reference:</strong></td><td style='padding: 8px; background: #f8fafc;'>" + report.getExternalId() + "</td></tr>" +
                "<tr><td style='padding: 8px;'><strong>University:</strong></td><td style='padding: 8px;'>" + univName + "</td></tr>" +
                "<tr><td style='padding: 8px; background: #f8fafc;'><strong>Assigned Leader:</strong></td><td style='padding: 8px; background: #f8fafc;'>" + leaderName + "</td></tr>" +
                "<tr><td style='padding: 8px;'><strong>Category:</strong></td><td style='padding: 8px;'>" + report.getCategory() + "</td></tr>" +
                "<tr><td style='padding: 8px; background: #f8fafc;'><strong>Urgency:</strong></td><td style='padding: 8px; background: #f8fafc;'><span style='color: " + getUrgencyColor(report.getUrgency() != null ? report.getUrgency().name() : "NORMAL") + "; font-weight: bold;'>" + (report.getUrgency() != null ? report.getUrgency() : "NORMAL") + "</span></td></tr>" +
                "<tr><td style='padding: 8px;'><strong>Description:</strong></td><td style='padding: 8px;'>" + report.getDescription() + "</td></tr>" +
                "</table>" +
                "<div style='text-align: center; margin-top: 24px;'>" +
                "<a href='" + dashboardUrl + "' style='background-color: #1E3A8A; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;'>Open Admin Dashboard</a>" +
                "</div>" +
                "</div></div>";

        EmailNotification notification = EmailNotification.builder()
                .report(report)
                .recipientEmail(adminEmail)
                .recipientRole("ADMIN")
                .notificationType("NEW_REPORT_ADMIN")
                .subject(subject)
                .bodyText(textBody)
                .bodyHtml(htmlBody)
                .deliveryState(DeliveryState.PENDING)
                .attemptCount(0)
                .createdAt(LocalDateTime.now())
                .nextRetryAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    @Transactional
    public EmailNotification queueMissingLeaderAdminNotification(IssueReport report) {
        String univName = report.getUniversity() != null ? report.getUniversity().getName() : "Unknown University";
        String subject = "[CAMPNAV ALERT] Unassigned Report - No Active Student Leader for " + univName;

        String textBody = "Attention CAMPNAV Admin,\n\n" +
                "A new report (" + report.getExternalId() + ") was submitted for " + univName + " but no active verified Student Leader is currently assigned to this university.\n\n" +
                "Report Details:\n" +
                "Category: " + report.getCategory() + "\n" +
                "Description: " + report.getDescription() + "\n\n" +
                "Please assign a Student Leader in the CAMPNAV Admin Dashboard to route this report.\n" +
                baseUrl + "/#admin-dashboard";

        String htmlBody = "<div style='font-family: Arial, sans-serif; padding: 20px; background-color: #fef2f2;'>" +
                "<div style='max-width: 600px; margin: auto; background: #ffffff; border-radius: 8px; padding: 24px; border: 1px solid #fca5a5;'>" +
                "<h2 style='color: #DC2626; margin-top: 0;'>CAMPNAV Action Required: Missing Leader Assignment</h2>" +
                "<p>A report was submitted for <strong>" + univName + "</strong>, but there is currently no active verified University Student Leader configured.</p>" +
                "<p><strong>Report Reference:</strong> " + report.getExternalId() + "</p>" +
                "<p><strong>Category:</strong> " + report.getCategory() + "</p>" +
                "<p><strong>Description:</strong> " + report.getDescription() + "</p>" +
                "<div style='text-align: center; margin-top: 20px;'>" +
                "<a href='" + baseUrl + "/#admin-dashboard' style='background-color: #DC2626; color: #ffffff; padding: 10px 20px; text-decoration: none; border-radius: 6px; font-weight: bold;'>Appoint Leader / Route Issue</a>" +
                "</div>" +
                "</div></div>";

        EmailNotification notification = EmailNotification.builder()
                .report(report)
                .recipientEmail(adminEmail)
                .recipientRole("ADMIN")
                .notificationType("MISSING_LEADER_ADMIN")
                .subject(subject)
                .bodyText(textBody)
                .bodyHtml(htmlBody)
                .deliveryState(DeliveryState.PENDING)
                .attemptCount(0)
                .createdAt(LocalDateTime.now())
                .nextRetryAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    @Transactional
    public EmailNotification queueStatusUpdateReporterNotification(IssueReport report, String recipientEmail, String statusTitle, String updateDetails) {
        if (recipientEmail == null || recipientEmail.isBlank()) return null;

        String subject = "[CAMPNAV Update] Status Update for Report " + report.getExternalId() + ": " + statusTitle;
        String trackUrl = baseUrl + "/#track-report?id=" + report.getExternalId();

        String textBody = "Hello,\n\n" +
                "Your issue report " + report.getExternalId() + " has an update:\n" +
                "Status: " + statusTitle + "\n" +
                "Details: " + updateDetails + "\n\n" +
                "You can track progress at:\n" + trackUrl + "\n\n" +
                "Thank you for improving our campus community,\nCAMPNAV";

        String htmlBody = "<div style='font-family: Arial, sans-serif; padding: 20px; background-color: #f8fafc;'>" +
                "<div style='max-width: 600px; margin: auto; background: #ffffff; border-radius: 8px; padding: 24px;'>" +
                "<h2 style='color: #0284C7;'>CAMPNAV Issue Status Update</h2>" +
                "<p>Your reported issue <strong>" + report.getExternalId() + "</strong> has been updated.</p>" +
                "<p><strong>New Status:</strong> " + statusTitle + "</p>" +
                "<p><strong>Notes / Updates:</strong> " + updateDetails + "</p>" +
                "<div style='margin-top: 20px;'>" +
                "<a href='" + trackUrl + "' style='background-color: #0284C7; color: #ffffff; padding: 10px 20px; text-decoration: none; border-radius: 6px;'>Track Progress Online</a>" +
                "</div>" +
                "</div></div>";

        EmailNotification notification = EmailNotification.builder()
                .report(report)
                .recipientEmail(recipientEmail)
                .recipientRole("REPORTER")
                .notificationType("REPORT_STATUS_UPDATE_REPORTER")
                .subject(subject)
                .bodyText(textBody)
                .bodyHtml(htmlBody)
                .deliveryState(DeliveryState.PENDING)
                .attemptCount(0)
                .createdAt(LocalDateTime.now())
                .nextRetryAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    @Transactional
    public EmailNotification queueDeliveryFailureAdminAlert(EmailNotification failedNotification) {
        String subject = "[CAMPNAV ALERT] Email Delivery Failed for " + failedNotification.getRecipientRole() + " (" + failedNotification.getRecipientEmail() + ")";

        String textBody = "Email notification delivery failed permanently after " + failedNotification.getAttemptCount() + " attempts.\n" +
                "Recipient: " + failedNotification.getRecipientEmail() + "\n" +
                "Notification Type: " + failedNotification.getNotificationType() + "\n" +
                "Last Error: " + failedNotification.getLastError() + "\n\n" +
                "Please check the Email Notifications monitor in the Admin Dashboard.";

        EmailNotification alert = EmailNotification.builder()
                .report(failedNotification.getReport())
                .recipientEmail(adminEmail)
                .recipientRole("ADMIN")
                .notificationType("DELIVERY_FAILURE_ADMIN")
                .subject(subject)
                .bodyText(textBody)
                .deliveryState(DeliveryState.PENDING)
                .attemptCount(0)
                .createdAt(LocalDateTime.now())
                .nextRetryAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(alert);
    }

    @Scheduled(fixedDelay = 30000) // Poll outbox every 30 seconds
    public void scheduledOutboxProcessor() {
        if (!emailEnabled) return;
        processOutboxQueue();
    }

    @Transactional
    public void processOutboxQueue() {
        List<EmailNotification> pendingOrRetry = notificationRepository.findByDeliveryStateInAndNextRetryAtBefore(
                List.of(DeliveryState.PENDING, DeliveryState.RETRY_PENDING),
                LocalDateTime.now()
        );

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

        for (EmailNotification notification : pendingOrRetry) {
            sendNotification(notification, mailSender);
        }
    }

    @Transactional
    public boolean retryNotification(Long notificationId) {
        EmailNotification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null) return false;

        notification.setDeliveryState(DeliveryState.PENDING);
        notification.setNextRetryAt(LocalDateTime.now());
        notificationRepository.save(notification);

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        sendNotification(notification, mailSender);
        return notification.getDeliveryState() == DeliveryState.DELIVERED || notification.getDeliveryState() == DeliveryState.SENT_TO_PROVIDER;
    }

    private void sendNotification(EmailNotification notification, JavaMailSender mailSender) {
        notification.setAttemptCount(notification.getAttemptCount() + 1);
        notification.setLastAttemptAt(LocalDateTime.now());
        notification.setDeliveryState(DeliveryState.PROCESSING);

        try {
            if (mailSender != null) {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setTo(notification.getRecipientEmail());
                helper.setSubject(notification.getSubject());

                if (notification.getBodyHtml() != null && !notification.getBodyHtml().isBlank()) {
                    helper.setText(notification.getBodyText(), notification.getBodyHtml());
                } else {
                    helper.setText(notification.getBodyText(), false);
                }

                mailSender.send(message);
                notification.setDeliveryState(DeliveryState.SENT_TO_PROVIDER);
                notification.setSubmittedAt(LocalDateTime.now());
                log.info("Email successfully sent to {}", notification.getRecipientEmail());
            } else {
                // JavaMailSender not configured in dev/test environment - simulated successful outbox processing
                log.info("[SIMULATED EMAIL OUTBOX] To: {} | Subject: {}", notification.getRecipientEmail(), notification.getSubject());
                notification.setDeliveryState(DeliveryState.SENT_TO_PROVIDER);
                notification.setSubmittedAt(LocalDateTime.now());
                notification.setProviderMessageId("SIMULATED-" + System.currentTimeMillis());
            }

            if (notification.getReport() != null) {
                notification.getReport().setNotificationStatus("SENT");
            }
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", notification.getRecipientEmail(), e.getMessage());
            notification.setLastError(e.getMessage());
            notification.setFailureReason(e.getClass().getSimpleName() + ": " + e.getMessage());

            if (notification.getAttemptCount() >= 5) {
                notification.setDeliveryState(DeliveryState.FAILED);
                if (notification.getReport() != null) {
                    notification.getReport().setNotificationStatus("FAILED");
                }
                // Alert admin on permanent delivery failure
                if (!"ADMIN".equals(notification.getRecipientRole())) {
                    queueDeliveryFailureAdminAlert(notification);
                }
            } else {
                notification.setDeliveryState(DeliveryState.RETRY_PENDING);
                // Exponential backoff: 5 minutes * 2^(attempt-1)
                long delayMinutes = (long) Math.pow(2, notification.getAttemptCount() - 1) * 5;
                notification.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));
            }
        }

        notificationRepository.save(notification);
    }

    private String getUrgencyColor(String urgency) {
        return switch (urgency.toUpperCase()) {
            case "EMERGENCY" -> "#DC2626";
            case "HIGH" -> "#EA580C";
            case "NORMAL" -> "#2563EB";
            default -> "#64748B";
        };
    }

    public List<EmailNotification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public long getFailedCount() {
        return notificationRepository.countByDeliveryState(DeliveryState.FAILED);
    }
}

package com.campnav.backend.repository;

import com.campnav.backend.model.EmailNotification;
import com.campnav.backend.model.EmailNotification.DeliveryState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailNotificationRepository extends JpaRepository<EmailNotification, Long> {
    List<EmailNotification> findByDeliveryStateInAndNextRetryAtBefore(List<DeliveryState> states, LocalDateTime time);

    List<EmailNotification> findByDeliveryState(DeliveryState state);

    List<EmailNotification> findByReportId(Long reportId);

    long countByDeliveryState(DeliveryState state);
}

package com.campnav.backend.repository;

import com.campnav.backend.model.IssueReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueReportRepository extends JpaRepository<IssueReport, Long> {
    Optional<IssueReport> findByExternalId(String externalId);

    List<IssueReport> findByStatus(IssueReport.IssueStatus status);

    @Query("SELECT r FROM IssueReport r WHERE r.university.id = :universityId")
    List<IssueReport> findByUniversityId(@Param("universityId") Long universityId);

    @Query("SELECT r FROM IssueReport r WHERE r.university.id = :universityId AND r.routingStatus = :routingStatus")
    List<IssueReport> findByUniversityIdAndRoutingStatus(@Param("universityId") Long universityId, @Param("routingStatus") IssueReport.RoutingStatus routingStatus);

    @Query("SELECT r FROM IssueReport r WHERE r.assignedLeader.id = :assignedLeaderId")
    List<IssueReport> findByAssignedLeaderId(@Param("assignedLeaderId") Long assignedLeaderId);

    List<IssueReport> findByRoutingStatus(IssueReport.RoutingStatus routingStatus);

    @Query(value = "SELECT *, (6371 * acos(cos(radians(:lat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(latitude)))) AS distance " +
            "FROM issue_report " +
            "HAVING distance < :radius " +
            "ORDER BY distance", nativeQuery = true)
    List<IssueReport> findNearbyIssues(@Param("lat") Double lat, @Param("lng") Double lng, @Param("radius") Double radius);

    @Query("SELECT r FROM IssueReport r WHERE r.category = :category AND r.status NOT IN ('CLOSED', 'REJECTED')")
    List<IssueReport> findActiveByCategory(@Param("category") String category);
}

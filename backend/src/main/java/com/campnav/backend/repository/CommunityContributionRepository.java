package com.campnav.backend.repository;

import com.campnav.backend.model.CommunityContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityContributionRepository extends JpaRepository<CommunityContribution, Long> {
    Optional<CommunityContribution> findByReference(String reference);
    List<CommunityContribution> findByStatus(CommunityContribution.ContributionStatus status);
    List<CommunityContribution> findByUniversityId(Long universityId);
    boolean existsByUniversityIdAndLocationNameIgnoreCaseAndLatitudeAndLongitude(
            Long universityId, String locationName, Double latitude, Double longitude);
}

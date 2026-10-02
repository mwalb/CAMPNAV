package com.campnav.backend.repository;

import com.campnav.backend.model.CommunityReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityReviewRepository extends JpaRepository<CommunityReview, Long> {
    List<CommunityReview> findByUniversityId(Long universityId);
    List<CommunityReview> findByUniversityIdAndStatus(Long universityId, String status);
    List<CommunityReview> findByLocationId(Long locationId);
}

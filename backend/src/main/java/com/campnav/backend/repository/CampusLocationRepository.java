package com.campnav.backend.repository;

import com.campnav.backend.model.CampusLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CampusLocationRepository extends JpaRepository<CampusLocation, Long> {
    List<CampusLocation> findByUniversityId(Long universityId);
    List<CampusLocation> findByUniversityIdAndCategoryId(Long universityId, Long categoryId);
    List<CampusLocation> findByUniversityIdAndCategoryIdIn(Long universityId, List<Long> categoryIds);
    List<CampusLocation> findByUniversityIdAndNameContainingIgnoreCase(Long universityId, String name);
    List<CampusLocation> findByUniversityIdAndCategoryIdAndNameContainingIgnoreCase(Long universityId, Long categoryId, String name);
    List<CampusLocation> findByUniversityIdAndCategoryIdInAndNameContainingIgnoreCase(Long universityId, List<Long> categoryIds, String name);
}

package com.campnav.backend.repository;

import com.campnav.backend.model.CampusLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CampusLocationRepository extends JpaRepository<CampusLocation, Long> {
    List<CampusLocation> findByUniversityId(Long universityId);
    Optional<CampusLocation> findByExternalId(String externalId);
    List<CampusLocation> findByUniversityIdAndCategoryId(Long universityId, Long categoryId);
    List<CampusLocation> findByUniversityIdAndCategoryIdIn(Long universityId, List<Long> categoryIds);
    List<CampusLocation> findByUniversityIdAndNameContainingIgnoreCaseOrUniversityIdAndOfficialNameContainingIgnoreCaseOrUniversityIdAndAliasesContainingIgnoreCase(Long universityId1, String name, Long universityId2, String officialName, Long universityId3, String aliases);
    
    default List<CampusLocation> searchCanonical(Long universityId, String query) {
        return findByUniversityIdAndNameContainingIgnoreCaseOrUniversityIdAndOfficialNameContainingIgnoreCaseOrUniversityIdAndAliasesContainingIgnoreCase(universityId, query, universityId, query, universityId, query);
    }
    List<CampusLocation> findByUniversityIdAndCategoryIdAndNameContainingIgnoreCase(Long universityId, Long categoryId, String name);
    List<CampusLocation> findByUniversityIdAndCategoryIdInAndNameContainingIgnoreCase(Long universityId, List<Long> categoryIds, String name);
}

package com.campnav.backend.repository;

import com.campnav.backend.model.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UniversityRepository extends JpaRepository<University, Long> {
    List<University> findAllByIsActiveTrueOrderBySortOrderAsc();
    Optional<University> findByShortName(String shortName);
    Optional<University> findByExternalId(String externalId);
}

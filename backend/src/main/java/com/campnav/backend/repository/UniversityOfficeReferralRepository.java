package com.campnav.backend.repository;

import com.campnav.backend.model.UniversityOfficeReferral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UniversityOfficeReferralRepository extends JpaRepository<UniversityOfficeReferral, Long> {
    List<UniversityOfficeReferral> findByReportId(Long reportId);

    List<UniversityOfficeReferral> findByReferringLeaderId(Long leaderId);
}

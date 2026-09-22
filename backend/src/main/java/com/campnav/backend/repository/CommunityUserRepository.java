package com.campnav.backend.repository;

import com.campnav.backend.model.CommunityUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommunityUserRepository extends JpaRepository<CommunityUser, Long> {
    Optional<CommunityUser> findByUsername(String username);
    Optional<CommunityUser> findByEmail(String email);
    Optional<CommunityUser> findByExternalId(String externalId);
}

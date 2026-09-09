package com.campnav.backend.repository;

import com.campnav.backend.model.MediaProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaProjectRepository extends JpaRepository<MediaProject, String> {
}

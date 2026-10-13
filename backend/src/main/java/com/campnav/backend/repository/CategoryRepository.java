package com.campnav.backend.repository;

import com.campnav.backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);

    @Query("SELECT c FROM Category c WHERE c.university.id = :universityId")
    List<Category> findByUniversityId(@Param("universityId") Long universityId);

    @Query("SELECT c FROM Category c WHERE c.university.id = :universityId AND LOWER(c.name) = LOWER(:name)")
    Optional<Category> findByUniversityIdAndNameIgnoreCase(@Param("universityId") Long universityId, @Param("name") String name);
}

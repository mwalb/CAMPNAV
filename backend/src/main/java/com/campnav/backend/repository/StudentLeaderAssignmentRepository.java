package com.campnav.backend.repository;

import com.campnav.backend.model.StudentLeaderAssignment;
import com.campnav.backend.model.StudentLeaderAssignment.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentLeaderAssignmentRepository extends JpaRepository<StudentLeaderAssignment, Long> {
    @Query("SELECT a FROM StudentLeaderAssignment a WHERE a.university.id = :universityId AND a.status = :status")
    List<StudentLeaderAssignment> findByUniversityIdAndStatus(@Param("universityId") Long universityId, @Param("status") AssignmentStatus status);

    @Query("SELECT a FROM StudentLeaderAssignment a WHERE a.university.id = :universityId AND a.status = :status ORDER BY a.id DESC")
    List<StudentLeaderAssignment> findAllByUniversityIdAndStatusOrderByIdDesc(@Param("universityId") Long universityId, @Param("status") AssignmentStatus status);

    default Optional<StudentLeaderAssignment> findFirstByUniversityIdAndStatusOrderByIdDesc(Long universityId, AssignmentStatus status) {
        List<StudentLeaderAssignment> list = findAllByUniversityIdAndStatusOrderByIdDesc(universityId, status);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Query("SELECT a FROM StudentLeaderAssignment a WHERE a.leader.username = :username AND a.status = :status")
    List<StudentLeaderAssignment> findByLeaderUsernameAndStatus(@Param("username") String username, @Param("status") AssignmentStatus status);

    @Query("SELECT a FROM StudentLeaderAssignment a WHERE a.leader.id = :leaderId AND a.status = :status")
    List<StudentLeaderAssignment> findByLeaderIdAndStatus(@Param("leaderId") Long leaderId, @Param("status") AssignmentStatus status);

    List<StudentLeaderAssignment> findAllByStatus(AssignmentStatus status);

    @Query("SELECT a FROM StudentLeaderAssignment a WHERE a.university.id = :universityId")
    List<StudentLeaderAssignment> findByUniversityId(@Param("universityId") Long universityId);
}

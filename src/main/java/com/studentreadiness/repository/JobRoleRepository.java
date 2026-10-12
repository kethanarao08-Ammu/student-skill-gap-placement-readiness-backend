package com.studentreadiness.repository;

import com.studentreadiness.entity.JobRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRoleRepository extends JpaRepository<JobRole, Long> {
    @EntityGraph(attributePaths = {"requiredSkills", "requiredSkills.skill"})
    List<JobRole> findByActiveTrue();
    Optional<JobRole> findByTitleIgnoreCase(String title);
    boolean existsByTitleIgnoreCase(String title);
}

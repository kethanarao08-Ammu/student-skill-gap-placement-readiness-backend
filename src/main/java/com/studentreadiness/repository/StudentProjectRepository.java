package com.studentreadiness.repository;

import com.studentreadiness.entity.StudentProject;
import com.studentreadiness.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentProjectRepository extends JpaRepository<StudentProject, Long> {
    List<StudentProject> findByUser(User user);
    List<StudentProject> findByUserId(Long userId);
    Optional<StudentProject> findByIdAndUserId(Long id, Long userId);
    long countByUserId(Long userId);
}

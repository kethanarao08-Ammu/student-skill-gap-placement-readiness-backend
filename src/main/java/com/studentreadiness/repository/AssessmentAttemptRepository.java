package com.studentreadiness.repository;

import com.studentreadiness.entity.AssessmentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, Long> {
    List<AssessmentAttempt> findByUserIdOrderByAttemptedAtDesc(Long userId);
    Optional<AssessmentAttempt> findTopByUserIdOrderByAttemptedAtDesc(Long userId);
    long countByUserId(Long userId);
}

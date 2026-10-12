package com.studentreadiness.repository;

import com.studentreadiness.entity.LearningRecommendation;
import com.studentreadiness.enums.LearningStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningRecommendationRepository extends JpaRepository<LearningRecommendation, Long> {
    List<LearningRecommendation> findByUserIdOrderByPriorityAsc(Long userId);
    List<LearningRecommendation> findByUserIdAndJobRoleId(Long userId, Long jobRoleId);
    List<LearningRecommendation> findByUserIdAndStatus(Long userId, LearningStatus status);
    void deleteByUserIdAndJobRoleId(Long userId, Long jobRoleId);
    long countByUserIdAndStatus(Long userId, LearningStatus status);
}

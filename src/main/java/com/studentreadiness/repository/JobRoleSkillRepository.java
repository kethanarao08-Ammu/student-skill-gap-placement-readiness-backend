package com.studentreadiness.repository;

import com.studentreadiness.entity.JobRoleSkill;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface JobRoleSkillRepository extends JpaRepository<JobRoleSkill, Long> {
    List<JobRoleSkill> findByJobRoleId(Long jobRoleId);
    void deleteByJobRoleId(Long jobRoleId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE JobRoleSkill roleSkill SET roleSkill.requiredLevel = roleSkill.requiredProficiency " +
            "WHERE roleSkill.requiredLevel <= 0 AND roleSkill.requiredProficiency BETWEEN 1 AND 5")
    int restoreRequiredLevelsFromLegacyValues();
}

package com.studentreadiness.repository;

import com.studentreadiness.entity.Skill;
import com.studentreadiness.entity.StudentSkill;
import com.studentreadiness.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentSkillRepository extends JpaRepository<StudentSkill, Long> {
    List<StudentSkill> findByUser(User user);
    List<StudentSkill> findByUserId(Long userId);
    Optional<StudentSkill> findByUserAndSkill(User user, Skill skill);
    Optional<StudentSkill> findByUserIdAndSkillId(Long userId, Long skillId);
    boolean existsByUserAndSkill(User user, Skill skill);
}

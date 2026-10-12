package com.studentreadiness.service;

import com.studentreadiness.entity.JobRole;
import com.studentreadiness.entity.JobRoleSkill;
import com.studentreadiness.entity.Skill;
import com.studentreadiness.exception.DuplicateResourceException;
import com.studentreadiness.exception.ResourceNotFoundException;
import com.studentreadiness.repository.JobRoleRepository;
import com.studentreadiness.repository.JobRoleSkillRepository;
import com.studentreadiness.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobRoleService {

    private final JobRoleRepository jobRoleRepository;
    private final JobRoleSkillRepository jobRoleSkillRepository;
    private final SkillRepository skillRepository;

    public JobRoleService(JobRoleRepository jobRoleRepository,
                          JobRoleSkillRepository jobRoleSkillRepository,
                          SkillRepository skillRepository) {
        this.jobRoleRepository = jobRoleRepository;
        this.jobRoleSkillRepository = jobRoleSkillRepository;
        this.skillRepository = skillRepository;
    }

    @Transactional(readOnly = true)
    public List<JobRole> getAllActiveJobRoles() {
        return jobRoleRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<JobRole> getAllJobRoles() {
        return jobRoleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public JobRole getJobRoleById(Long id) {
        JobRole role = jobRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobRole", "id", id));
        // Force initialization of required skills
        role.getRequiredSkills().size();
        return role;
    }

    @Transactional
    public JobRole createJobRole(JobRole role) {
        if (jobRoleRepository.existsByTitleIgnoreCase(role.getTitle())) {
            throw new DuplicateResourceException("Job role with title '" + role.getTitle() + "' already exists");
        }
        return jobRoleRepository.save(role);
    }

    @Transactional
    public JobRole updateJobRole(Long id, JobRole updated) {
        JobRole existing = getJobRoleById(id);
        if (!existing.getTitle().equalsIgnoreCase(updated.getTitle()) &&
                jobRoleRepository.existsByTitleIgnoreCase(updated.getTitle())) {
            throw new DuplicateResourceException("Job role with title '" + updated.getTitle() + "' already exists");
        }
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setActive(updated.isActive());
        return jobRoleRepository.save(existing);
    }

    @Transactional
    public void deleteJobRole(Long id) {
        JobRole existing = getJobRoleById(id);
        jobRoleRepository.delete(existing);
    }

    @Transactional
    public JobRole addSkillToRole(Long roleId, Long skillId, int requiredLevel, int importanceWeight) {
        JobRole role = getJobRoleById(roleId);
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", skillId));

        // Check if skill already assigned
        role.getRequiredSkills().stream()
                .filter(jrs -> jrs.getSkill().getId().equals(skillId))
                .findFirst()
                .ifPresentOrElse(
                        existing -> {
                            existing.setRequiredLevel(requiredLevel);
                            existing.setImportanceWeight(importanceWeight);
                        },
                        () -> {
                            JobRoleSkill jrs = new JobRoleSkill(role, skill, requiredLevel, importanceWeight);
                            role.getRequiredSkills().add(jrs);
                        }
                );

        return jobRoleRepository.save(role);
    }

    @Transactional
    public JobRole removeSkillFromRole(Long roleId, Long skillId) {
        JobRole role = getJobRoleById(roleId);
        role.getRequiredSkills().removeIf(jrs -> jrs.getSkill().getId().equals(skillId));
        return jobRoleRepository.save(role);
    }
}

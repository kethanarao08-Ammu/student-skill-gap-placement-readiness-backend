package com.studentreadiness.service;

import com.studentreadiness.dto.StudentSkillRequest;
import com.studentreadiness.dto.StudentSkillResponse;
import com.studentreadiness.entity.Skill;
import com.studentreadiness.entity.StudentProfile;
import com.studentreadiness.entity.StudentSkill;
import com.studentreadiness.entity.User;
import com.studentreadiness.enums.SkillCategory;
import com.studentreadiness.exception.DuplicateResourceException;
import com.studentreadiness.exception.ResourceNotFoundException;
import com.studentreadiness.repository.SkillRepository;
import com.studentreadiness.repository.StudentProfileRepository;
import com.studentreadiness.repository.StudentSkillRepository;
import com.studentreadiness.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;

    public SkillService(SkillRepository skillRepository,
                        StudentSkillRepository studentSkillRepository,
                        UserRepository userRepository,
                        StudentProfileRepository studentProfileRepository) {
        this.skillRepository = skillRepository;
        this.studentSkillRepository = studentSkillRepository;
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
    }

    @Transactional(readOnly = true)
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Skill> getSkillsByCategory(SkillCategory category) {
        return skillRepository.findByCategory(category);
    }

    @Transactional(readOnly = true)
    public Skill getSkillById(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", id));
    }

    @Transactional
    public Skill createSkill(Skill skill) {
        if (skillRepository.existsByNameIgnoreCase(skill.getName())) {
            throw new DuplicateResourceException("Skill with name '" + skill.getName() + "' already exists");
        }
        return skillRepository.save(skill);
    }

    @Transactional
    public Skill updateSkill(Long id, Skill updatedSkill) {
        Skill existing = getSkillById(id);
        if (!existing.getName().equalsIgnoreCase(updatedSkill.getName()) &&
                skillRepository.existsByNameIgnoreCase(updatedSkill.getName())) {
            throw new DuplicateResourceException("Skill with name '" + updatedSkill.getName() + "' already exists");
        }
        existing.setName(updatedSkill.getName());
        existing.setCategory(updatedSkill.getCategory());
        existing.setDescription(updatedSkill.getDescription());
        return skillRepository.save(existing);
    }

    @Transactional
    public void deleteSkill(Long id) {
        Skill existing = getSkillById(id);
        skillRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public List<StudentSkillResponse> getStudentSkills(Long userId) {
        return studentSkillRepository.findByUserId(userId)
                .stream()
                .map(this::mapToStudentSkillResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public StudentSkillResponse addOrUpdateStudentSkill(Long userId, StudentSkillRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", "userId", userId));
        Skill skill = getSkillById(request.getSkillId());

        StudentSkill studentSkill = studentSkillRepository.findByUserAndSkill(user, skill)
                .orElseGet(() -> new StudentSkill(user, profile, skill, request.getProficiencyLevel()));

        studentSkill.setProficiencyLevel(request.getProficiencyLevel());
        StudentSkill saved = studentSkillRepository.save(studentSkill);
        return mapToStudentSkillResponse(saved);
    }

    @Transactional
    public void deleteStudentSkill(Long userId, Long skillId) {
        StudentSkill studentSkill = studentSkillRepository.findByUserIdAndSkillId(userId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentSkill", "skillId", skillId));
        studentSkillRepository.delete(studentSkill);
    }

    private StudentSkillResponse mapToStudentSkillResponse(StudentSkill ss) {
        StudentSkillResponse response = new StudentSkillResponse();
        response.setId(ss.getId());
        response.setSkillId(ss.getSkill().getId());
        response.setSkillName(ss.getSkill().getName());
        response.setSkillCategory(ss.getSkill().getCategory() != null ? ss.getSkill().getCategory().name() : "");
        response.setProficiencyLevel(ss.getProficiencyLevel());
        return response;
    }
}

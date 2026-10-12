package com.studentreadiness.service;

import com.studentreadiness.dto.StudentProfileRequest;
import com.studentreadiness.dto.StudentProfileResponse;
import com.studentreadiness.entity.JobRole;
import com.studentreadiness.entity.StudentProfile;
import com.studentreadiness.entity.User;
import com.studentreadiness.exception.ResourceNotFoundException;
import com.studentreadiness.repository.JobRoleRepository;
import com.studentreadiness.repository.StudentProfileRepository;
import com.studentreadiness.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

    private final UserRepository userRepository;
    private final StudentProfileRepository profileRepository;
    private final JobRoleRepository jobRoleRepository;

    public StudentService(UserRepository userRepository,
                          StudentProfileRepository profileRepository,
                          JobRoleRepository jobRoleRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.jobRoleRepository = jobRoleRepository;
    }

    @Transactional
    public StudentProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        StudentProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // Auto-create profile if missing
                    StudentProfile newProfile = new StudentProfile(user);
                    return profileRepository.save(newProfile);
                });

        return mapToResponse(user, profile);
    }

    @Transactional
    public StudentProfileResponse updateProfile(Long userId, StudentProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        StudentProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> new StudentProfile(user));

        profile.setUser(user);
        if (request.getCollege() != null) profile.setCollege(request.getCollege().trim());
        if (request.getDegree() != null) profile.setDegree(request.getDegree().trim());
        if (request.getBranch() != null) profile.setBranch(request.getBranch().trim());
        if (request.getGraduationYear() != null) profile.setGraduationYear(request.getGraduationYear());
        if (request.getCgpa() != null) profile.setCgpa(request.getCgpa());
        if (request.getPhone() != null) profile.setPhone(request.getPhone().trim());
        if (request.getBio() != null) profile.setBio(request.getBio().trim());

        if (request.getTargetJobRoleId() != null) {
            JobRole role = jobRoleRepository.findById(request.getTargetJobRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("JobRole", "id", request.getTargetJobRoleId()));
            profile.setTargetJobRole(role);
        } else if (request.getTargetJobRoleId() == null && profile.getTargetJobRole() != null) {
            // Allow clearing target role
            // Only clear if explicitly set to null - but since null won't be included in JSON unless explicitly sent
            // we keep existing behavior (don't clear unless needed)
        }

        StudentProfile saved = profileRepository.save(profile);
        return mapToResponse(user, saved);
    }

    public int calculateProfileCompletion(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return 0;

        StudentProfile profile = profileRepository.findByUserId(userId).orElse(null);
        if (profile == null) return 10; // At least account exists

        int score = 10; // Base for having account
        if (profile.getCollege() != null && !profile.getCollege().isEmpty()) score += 15;
        if (profile.getDegree() != null && !profile.getDegree().isEmpty()) score += 10;
        if (profile.getBranch() != null && !profile.getBranch().isEmpty()) score += 10;
        if (profile.getGraduationYear() != null) score += 5;
        if (profile.getCgpa() != null) score += 5;
        if (profile.getPhone() != null && !profile.getPhone().isEmpty()) score += 5;
        if (profile.getBio() != null && !profile.getBio().isEmpty()) score += 10;
        if (profile.getTargetJobRole() != null) score += 30;
        return Math.min(score, 100);
    }

    private StudentProfileResponse mapToResponse(User user, StudentProfile profile) {
        StudentProfileResponse response = new StudentProfileResponse();
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setRole(user.getRole().name());
        response.setCollege(profile.getCollege());
        response.setDegree(profile.getDegree());
        response.setBranch(profile.getBranch());
        response.setGraduationYear(profile.getGraduationYear());
        response.setCgpa(profile.getCgpa());
        response.setPhone(profile.getPhone());
        response.setBio(profile.getBio());
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());

        if (profile.getTargetJobRole() != null) {
            response.setTargetJobRoleId(profile.getTargetJobRole().getId());
            response.setTargetJobRoleTitle(profile.getTargetJobRole().getTitle());
        }

        return response;
    }
}

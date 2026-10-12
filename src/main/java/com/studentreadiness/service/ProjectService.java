package com.studentreadiness.service;

import com.studentreadiness.dto.ProjectRequest;
import com.studentreadiness.dto.ProjectResponse;
import com.studentreadiness.entity.StudentProject;
import com.studentreadiness.entity.User;
import com.studentreadiness.exception.AccessDeniedException;
import com.studentreadiness.exception.ResourceNotFoundException;
import com.studentreadiness.repository.StudentProjectRepository;
import com.studentreadiness.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final StudentProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(StudentProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjects(Long userId) {
        return projectRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectResponse createProject(Long userId, ProjectRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        StudentProject project = new StudentProject();
        project.setUser(user);
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setTechnologies(request.getTechnologies());
        project.setProjectUrl(request.getProjectUrl());
        project.setGithubUrl(request.getGithubUrl());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());

        StudentProject saved = projectRepository.save(project);
        return mapToResponse(saved);
    }

    @Transactional
    public ProjectResponse updateProject(Long userId, Long projectId, ProjectRequest request) {
        StudentProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        if (!project.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to update this project");
        }

        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setTechnologies(request.getTechnologies());
        project.setProjectUrl(request.getProjectUrl());
        project.setGithubUrl(request.getGithubUrl());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());

        StudentProject saved = projectRepository.save(project);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteProject(Long userId, Long projectId) {
        StudentProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        if (!project.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to delete this project");
        }

        projectRepository.delete(project);
    }

    private ProjectResponse mapToResponse(StudentProject p) {
        ProjectResponse resp = new ProjectResponse();
        resp.setId(p.getId());
        resp.setTitle(p.getTitle());
        resp.setDescription(p.getDescription());
        resp.setTechnologies(p.getTechnologies());
        resp.setProjectUrl(p.getProjectUrl());
        resp.setGithubUrl(p.getGithubUrl());
        resp.setStartDate(p.getStartDate());
        resp.setEndDate(p.getEndDate());
        resp.setCreatedAt(p.getCreatedAt());
        return resp;
    }
}

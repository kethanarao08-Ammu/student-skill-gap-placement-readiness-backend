package com.studentreadiness.service;

import com.studentreadiness.dto.CertificationRequest;
import com.studentreadiness.dto.CertificationResponse;
import com.studentreadiness.entity.Certification;
import com.studentreadiness.entity.User;
import com.studentreadiness.exception.AccessDeniedException;
import com.studentreadiness.exception.ResourceNotFoundException;
import com.studentreadiness.repository.CertificationRepository;
import com.studentreadiness.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CertificationService {

    private final CertificationRepository certificationRepository;
    private final UserRepository userRepository;

    public CertificationService(CertificationRepository certificationRepository, UserRepository userRepository) {
        this.certificationRepository = certificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<CertificationResponse> getCertifications(Long userId) {
        return certificationRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CertificationResponse createCertification(Long userId, CertificationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Certification cert = new Certification();
        cert.setUser(user);
        cert.setTitle(request.getTitle());
        cert.setIssuingOrganization(request.getIssuingOrganization());
        cert.setIssueDate(request.getIssueDate());
        cert.setCredentialUrl(request.getCredentialUrl());
        cert.setCertificateId(request.getCertificateId());

        Certification saved = certificationRepository.save(cert);
        return mapToResponse(saved);
    }

    @Transactional
    public CertificationResponse updateCertification(Long userId, Long certId, CertificationRequest request) {
        Certification cert = certificationRepository.findById(certId)
                .orElseThrow(() -> new ResourceNotFoundException("Certification", "id", certId));

        if (!cert.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to update this certification");
        }

        cert.setTitle(request.getTitle());
        cert.setIssuingOrganization(request.getIssuingOrganization());
        cert.setIssueDate(request.getIssueDate());
        cert.setCredentialUrl(request.getCredentialUrl());
        cert.setCertificateId(request.getCertificateId());

        Certification saved = certificationRepository.save(cert);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteCertification(Long userId, Long certId) {
        Certification cert = certificationRepository.findById(certId)
                .orElseThrow(() -> new ResourceNotFoundException("Certification", "id", certId));

        if (!cert.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to delete this certification");
        }

        certificationRepository.delete(cert);
    }

    private CertificationResponse mapToResponse(Certification c) {
        CertificationResponse resp = new CertificationResponse();
        resp.setId(c.getId());
        resp.setTitle(c.getTitle());
        resp.setIssuingOrganization(c.getIssuingOrganization());
        resp.setIssueDate(c.getIssueDate());
        resp.setCredentialUrl(c.getCredentialUrl());
        resp.setCertificateId(c.getCertificateId());
        resp.setCreatedAt(c.getCreatedAt());
        return resp;
    }
}

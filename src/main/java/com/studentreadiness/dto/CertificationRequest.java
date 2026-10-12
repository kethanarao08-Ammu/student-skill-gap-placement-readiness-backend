package com.studentreadiness.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class CertificationRequest {

    @NotBlank(message = "Certification title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @NotBlank(message = "Issuing organization is required")
    @Size(max = 150, message = "Organization name must not exceed 150 characters")
    private String issuingOrganization;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @Size(max = 255, message = "Credential URL must not exceed 255 characters")
    private String credentialUrl;

    @Size(max = 100, message = "Certificate ID must not exceed 100 characters")
    private String certificateId;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getIssuingOrganization() { return issuingOrganization; }
    public void setIssuingOrganization(String issuingOrganization) { this.issuingOrganization = issuingOrganization; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public String getCredentialUrl() { return credentialUrl; }
    public void setCredentialUrl(String credentialUrl) { this.credentialUrl = credentialUrl; }

    public String getCertificateId() { return certificateId; }
    public void setCertificateId(String certificateId) { this.certificateId = certificateId; }
}

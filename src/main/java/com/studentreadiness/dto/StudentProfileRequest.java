package com.studentreadiness.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class StudentProfileRequest {

    @Size(max = 150, message = "College name must not exceed 150 characters")
    private String college;

    @Size(max = 100, message = "Degree must not exceed 100 characters")
    private String degree;

    @Size(max = 100, message = "Branch must not exceed 100 characters")
    private String branch;

    @Min(value = 2000, message = "Graduation year must be 2000 or later")
    @Max(value = 2035, message = "Graduation year must be 2035 or earlier")
    private Integer graduationYear;

    @DecimalMin(value = "0.0", message = "CGPA must be 0.0 or greater")
    @DecimalMax(value = "10.0", message = "CGPA must be 10.0 or less")
    private BigDecimal cgpa;

    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{7,20}$", message = "Please provide a valid phone number")
    private String phone;

    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;

    private Long targetJobRoleId;

    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }

    public BigDecimal getCgpa() { return cgpa; }
    public void setCgpa(BigDecimal cgpa) { this.cgpa = cgpa; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Long getTargetJobRoleId() { return targetJobRoleId; }
    public void setTargetJobRoleId(Long targetJobRoleId) { this.targetJobRoleId = targetJobRoleId; }
}

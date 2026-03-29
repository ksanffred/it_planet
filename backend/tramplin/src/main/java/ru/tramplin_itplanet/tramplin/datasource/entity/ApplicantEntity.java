package ru.tramplin_itplanet.tramplin.datasource.entity;

import jakarta.persistence.*;
import ru.tramplin_itplanet.tramplin.datasource.entity.converter.ApplicantVisibilityConverter;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantVisibility;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "applicants")
public class ApplicantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private String name;

    private String university;
    private String faculty;

    @Column(name = "current_field_of_study")
    private String currentFieldOfStudy;

    @Column(name = "desired_position")
    private String desiredPosition;

    private String major;

    @Column(name = "graduation_year")
    private Integer graduationYear;

    @Column(name = "additional_education_details", columnDefinition = "TEXT")
    private String additionalEducationDetails;

    @Column(name = "portfolio_url", columnDefinition = "TEXT")
    private String portfolioUrl;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(name = "resume_url", columnDefinition = "TEXT")
    private String resumeUrl;

    @Column
    @Convert(converter = ApplicantVisibilityConverter.class)
    private ApplicantVisibility visibility = ApplicantVisibility.PRIVATE;

    @ManyToMany
    @JoinTable(
            name = "applicant_skills",
            joinColumns = @JoinColumn(name = "applicant_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<TagEntity> skills = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }

    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }

    public String getCurrentFieldOfStudy() { return currentFieldOfStudy; }
    public void setCurrentFieldOfStudy(String currentFieldOfStudy) { this.currentFieldOfStudy = currentFieldOfStudy; }

    public String getDesiredPosition() { return desiredPosition; }
    public void setDesiredPosition(String desiredPosition) { this.desiredPosition = desiredPosition; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }

    public String getAdditionalEducationDetails() { return additionalEducationDetails; }
    public void setAdditionalEducationDetails(String additionalEducationDetails) {
        this.additionalEducationDetails = additionalEducationDetails;
    }

    public String getPortfolioUrl() { return portfolioUrl; }
    public void setPortfolioUrl(String portfolioUrl) { this.portfolioUrl = portfolioUrl; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getResumeUrl() { return resumeUrl; }
    public void setResumeUrl(String resumeUrl) { this.resumeUrl = resumeUrl; }

    public ApplicantVisibility getVisibility() { return visibility; }
    public void setVisibility(ApplicantVisibility visibility) { this.visibility = visibility; }

    public List<TagEntity> getSkills() { return skills; }
    public void setSkills(List<TagEntity> skills) { this.skills = skills; }
}

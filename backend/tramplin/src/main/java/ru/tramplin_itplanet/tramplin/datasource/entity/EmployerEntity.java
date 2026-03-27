package ru.tramplin_itplanet.tramplin.datasource.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "employers")
public class EmployerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "company_name")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String inn;

    @Column(columnDefinition = "TEXT")
    private String socials;

    private String logoUrl;
    private String website;

    @Column
    private String status = "pending";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInn() { return inn; }
    public void setInn(String inn) { this.inn = inn; }

    public String getSocials() { return socials; }
    public void setSocials(String socials) { this.socials = socials; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

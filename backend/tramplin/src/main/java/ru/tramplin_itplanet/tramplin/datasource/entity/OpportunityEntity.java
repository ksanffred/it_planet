package ru.tramplin_itplanet.tramplin.datasource.entity;

import jakarta.persistence.*;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityFormat;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityStatus;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "opportunities")
public class OpportunityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    private EmployerEntity employer;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpportunityType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpportunityFormat format;

    private String address;
    private String city;
    private Double lat;
    private Double lng;

    @Column(precision = 10, scale = 2)
    private BigDecimal salaryFrom;

    @Column(precision = 10, scale = 2)
    private BigDecimal salaryTo;

    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpportunityStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "opportunity_media", joinColumns = @JoinColumn(name = "opportunity_id"))
    @Column(name = "media_url")
    private List<String> media = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "opportunity_tags",
            joinColumns = @JoinColumn(name = "opportunity_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<TagEntity> tags = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public EmployerEntity getEmployer() { return employer; }
    public void setEmployer(EmployerEntity employer) { this.employer = employer; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public OpportunityType getType() { return type; }
    public void setType(OpportunityType type) { this.type = type; }

    public OpportunityFormat getFormat() { return format; }
    public void setFormat(OpportunityFormat format) { this.format = format; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public BigDecimal getSalaryFrom() { return salaryFrom; }
    public void setSalaryFrom(BigDecimal salaryFrom) { this.salaryFrom = salaryFrom; }

    public BigDecimal getSalaryTo() { return salaryTo; }
    public void setSalaryTo(BigDecimal salaryTo) { this.salaryTo = salaryTo; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public OpportunityStatus getStatus() { return status; }
    public void setStatus(OpportunityStatus status) { this.status = status; }

    public List<String> getMedia() { return media; }
    public void setMedia(List<String> media) { this.media = media; }

    public List<TagEntity> getTags() { return tags; }
    public void setTags(List<TagEntity> tags) { this.tags = tags; }
}

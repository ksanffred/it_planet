package ru.tramplin_itplanet.tramplin.datasource.entity;

import jakarta.persistence.*;
import ru.tramplin_itplanet.tramplin.domain.model.TagCategory;

@Entity
@Table(name = "tags")
public class TagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TagCategory category;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public TagCategory getCategory() { return category; }
    public void setCategory(TagCategory category) { this.category = category; }
}

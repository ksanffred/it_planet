package ru.tramplin_itplanet.tramplin.domain.model;

import java.io.Serializable;

public record Tag(Long id, String name, TagCategory category) implements Serializable {}

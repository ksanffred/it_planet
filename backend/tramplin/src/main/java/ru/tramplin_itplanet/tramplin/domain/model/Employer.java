package ru.tramplin_itplanet.tramplin.domain.model;

import java.io.Serializable;

public record Employer(Long id, String name, String logoUrl, String website, String contacts) implements Serializable {}

package ru.tramplin_itplanet.tramplin.domain.model;

import java.io.Serializable;

public record CreateTagCommand(String name, TagCategory category) implements Serializable {}

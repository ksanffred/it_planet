package ru.tramplin_itplanet.tramplin.domain.exception;

public class TagNotFoundException extends ResourceNotFoundException {

    public TagNotFoundException(Long id) {
        super("Tag not found with id: " + id);
    }
}

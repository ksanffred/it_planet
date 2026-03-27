package ru.tramplin_itplanet.tramplin.domain.exception;

public class TagAlreadyExistsException extends RuntimeException {

    public TagAlreadyExistsException(String name) {
        super("Tag already exists with name: " + name);
    }
}

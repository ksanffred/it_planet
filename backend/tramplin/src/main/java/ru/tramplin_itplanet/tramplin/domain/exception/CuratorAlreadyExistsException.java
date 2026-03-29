package ru.tramplin_itplanet.tramplin.domain.exception;

public class CuratorAlreadyExistsException extends RuntimeException {

    public CuratorAlreadyExistsException(Long userId) {
        super("Curator already exists for user id: " + userId);
    }
}

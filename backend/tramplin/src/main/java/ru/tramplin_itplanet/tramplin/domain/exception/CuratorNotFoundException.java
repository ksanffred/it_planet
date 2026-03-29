package ru.tramplin_itplanet.tramplin.domain.exception;

public class CuratorNotFoundException extends ResourceNotFoundException {

    public CuratorNotFoundException(Long curatorId) {
        super("Curator not found with id: " + curatorId);
    }
}

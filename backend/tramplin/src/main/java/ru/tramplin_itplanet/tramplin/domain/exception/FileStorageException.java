package ru.tramplin_itplanet.tramplin.domain.exception;

public class FileStorageException extends RuntimeException {

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

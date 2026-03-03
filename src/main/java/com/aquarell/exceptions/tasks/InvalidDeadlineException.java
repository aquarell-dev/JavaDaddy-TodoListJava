package com.aquarell.exceptions.tasks;

public class InvalidDeadlineException extends RuntimeException {
    public InvalidDeadlineException() {
        super("Невалидный дедлайн");
    }

    public InvalidDeadlineException(String message) {
        super(message);
    }

    public InvalidDeadlineException(String message, Throwable cause) {
        super(message, cause);
    }
}

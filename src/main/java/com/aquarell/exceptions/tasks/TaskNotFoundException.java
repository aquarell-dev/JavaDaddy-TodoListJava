package com.aquarell.exceptions.tasks;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException() {
        super("Задача не найдена");
    }

    public TaskNotFoundException(Throwable cause) {
        super("Задача не найдена", cause);
    }
}

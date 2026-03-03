package com.aquarell.entity.commands;

import lombok.Getter;

@Getter
public enum Command {
    ADD("Добавить новую задачу"),
    LIST("Показать все задачи"),
    EDIT("Редактировать существующую задачу"),
    DELETE("Удалить задачу"),
    FILTER("Показать задачи с определённым статусом"),
    SORT("Отсортировать задачи"),
    EXIT("Завершить работу приложения");

    private final String description;

    Command(String description) {
        this.description = description;
    }
}

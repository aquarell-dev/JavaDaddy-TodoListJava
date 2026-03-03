package com.aquarell.dto.tasks;

import com.aquarell.entity.tasks.TaskStatus;

import java.time.LocalDate;

public record TaskUpdate(String name, String description, TaskStatus status, LocalDate deadline) {
}

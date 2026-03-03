package com.aquarell.dto.tasks;

import java.time.LocalDate;

public record TaskCreate(String name, String description, LocalDate deadline) {
}

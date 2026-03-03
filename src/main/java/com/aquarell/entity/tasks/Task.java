package com.aquarell.entity.tasks;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class Task {
    private UUID id;
    private String name;
    private String description;
    private TaskStatus status;
    private LocalDate deadline;
}

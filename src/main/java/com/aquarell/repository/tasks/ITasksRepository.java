package com.aquarell.repository.tasks;

import com.aquarell.entity.tasks.Task;
import com.aquarell.entity.tasks.TaskStatus;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public interface ITasksRepository {
    List<Task> findAll();

    List<Task> findAll(Predicate<Task> filter);

    List<Task> findAll(Comparator<Task> sort);

    List<Task> findAll(Predicate<Task> filter, Comparator<Task> sort);

    Task findById(UUID id);

    Task save(Task task);

    void deleteById(UUID id);
}
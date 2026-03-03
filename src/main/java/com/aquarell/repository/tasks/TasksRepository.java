package com.aquarell.repository.tasks;

import com.aquarell.entity.tasks.Task;
import com.aquarell.entity.tasks.TaskStatus;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class TasksRepository implements ITasksRepository {
    private final Map<UUID, Task> tasks;

    public TasksRepository() {
        this.tasks = new ConcurrentHashMap<>();
    }

    public List<Task> findAll() {
        return this.tasks.values().stream().toList();
    }

    public List<Task> findAll(Predicate<Task> filter) {
        return this.tasks.values().stream().filter(filter).toList();
    }

    public List<Task> findAll(Comparator<Task> sort) {
        return this.tasks.values().stream().sorted(sort).toList();
    }

    public List<Task> findAll(Predicate<Task> filter, Comparator<Task> sort) {
        return this.tasks.values().stream().filter(filter).sorted(sort).toList();
    }

    public Task findById(UUID id) {
        return this.tasks.get(id);
    }

    public Task save(Task task) {
        this.tasks.put(task.getId(), task);
        return task;
    }

        public void deleteById(UUID id) {
        this.tasks.remove(id);
    }
}

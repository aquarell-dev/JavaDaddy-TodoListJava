package com.aquarell.service.tasks;

import com.aquarell.dto.tasks.TaskCreate;
import com.aquarell.dto.tasks.TaskUpdate;
import com.aquarell.entity.tasks.Task;
import com.aquarell.entity.tasks.TaskStatus;
import com.aquarell.exceptions.tasks.InvalidDeadlineException;
import com.aquarell.exceptions.tasks.TaskNotFoundException;
import com.aquarell.repository.tasks.ITasksRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public class TasksService implements ITasksService {
    private final ITasksRepository tasksRepository;

    public TasksService(ITasksRepository tasksRepository) {
        this.tasksRepository = tasksRepository;
    }

    @Override
    public Task create(TaskCreate taskCreate) {
        UUID id = UUID.randomUUID();

        if (taskCreate.deadline().isBefore(LocalDate.now()))
            throw new InvalidDeadlineException("Дедлайн не может быть в прошлом");

        return this.tasksRepository.save(new Task(id, taskCreate.name(), taskCreate.description(), TaskStatus.TODO, taskCreate.deadline()));
    }

    @Override
    public void deleteById(UUID id) {
        this.tasksRepository.deleteById(id);
    }

    @Override
    public List<Task> findAll() {
        return this.tasksRepository.findAll();
    }

    @Override
    public List<Task> sortByStatus() {
        return this.tasksRepository.findAll(Comparator.comparing(Task::getStatus));
    }

    @Override
    public List<Task> sortByDeadline() {
        return this.tasksRepository.findAll(Comparator.comparing(Task::getDeadline));
    }

    @Override
    public List<Task> filterByStatus(TaskStatus status) {
        return this.tasksRepository.findAll(task -> task.getStatus().equals(status));
    }

    @Override
    public Task update(UUID id, TaskUpdate taskUpdate) {
        UUID existingId;

        try {
            existingId = tasksRepository.findById(id).getId();
        } catch (NoSuchElementException e) {
            throw new TaskNotFoundException();
        }

        if (taskUpdate.deadline().isBefore(LocalDate.now()))
            throw new InvalidDeadlineException("Дедлайн не может быть в прошлом");

        return tasksRepository.save(new Task(
                existingId,
                taskUpdate.name(),
                taskUpdate.description(),
                taskUpdate.status(),
                taskUpdate.deadline()
        ));
    }
}

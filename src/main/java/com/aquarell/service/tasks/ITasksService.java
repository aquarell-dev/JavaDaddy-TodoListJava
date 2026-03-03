package com.aquarell.service.tasks;

import com.aquarell.dto.tasks.TaskCreate;
import com.aquarell.dto.tasks.TaskUpdate;
import com.aquarell.entity.tasks.Task;
import com.aquarell.entity.tasks.TaskStatus;

import java.util.List;
import java.util.UUID;

public interface ITasksService {
    public Task create(TaskCreate taskCreate);

    public void deleteById(UUID id);

    public List<Task> sortByStatus();

    public List<Task> sortByDeadline();

    public List<Task> filterByStatus(TaskStatus status);

    public List<Task> findAll();

    public Task update(UUID id, TaskUpdate taskUpdate);
}

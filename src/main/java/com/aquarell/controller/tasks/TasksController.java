package com.aquarell.controller.tasks;

import com.aquarell.dto.tasks.TaskCreate;
import com.aquarell.dto.tasks.TaskUpdate;
import com.aquarell.entity.tasks.Task;
import com.aquarell.entity.tasks.TaskStatus;
import com.aquarell.exceptions.tasks.InvalidDeadlineException;
import com.aquarell.exceptions.tasks.TaskNotFoundException;
import com.aquarell.service.tasks.ITasksService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

public class TasksController implements ITaskController {
    private final ITasksService tasksService;
    private final Scanner scanner;
    private final Set<String> ALLOWED_SORT_FLAGS = Set.of("deadline", "status");

    public TasksController(ITasksService tasksService) {
        this.tasksService = tasksService;
        this.scanner = new Scanner(System.in);
    }

    private String prompt(String caption) {
        System.out.print(caption + ": ");
        return scanner.nextLine();
    }

    private LocalDate parseDate(String caption) {
        while (true) {
            try {
                return LocalDate.parse(prompt(caption));
            } catch (DateTimeParseException _) {
                System.out.println("Неверный формат даты. Попробуйте еще раз...");
                continue;
            }
        }
    }

    private TaskStatus parseStatus() {
        TaskStatus status;

        System.out.printf("Доступные статусы: %s\n", String.join(", ", Arrays.stream(TaskStatus.values()).map(Enum::toString).toList()));

        while (true) {
            try {
                String inputStatus = prompt("Статус").toUpperCase();

                return TaskStatus.valueOf(inputStatus.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Неправильный статус. Попробуйте еще раз...");
            }
        }
    }

    private void listTasks(List<Task> tasks) {
        System.out.println(String.join(";\n", tasks.stream().map(Task::toString).toList()));
    }

    @Override
    public void add() {
        Task task;

        while (true) {
            try {
                task = tasksService.create(new TaskCreate(prompt("Название задачи"), prompt("Описание задачи"), parseDate("Дедлайн задачи (YYYY-MM-DD)")));
                break;
            } catch (InvalidDeadlineException e) {
                System.out.printf("Ошибка: %s. Попробуйте еще раз...\n", e.getMessage());
            }
        }

        System.out.printf("Задача#%s добавлена.\n", task.getId().toString());
    }

    @Override
    public void list() {
        System.out.println("Список задач:");

        listTasks(tasksService.findAll());
    }

    @Override
    public void delete() {
        while (true) {
            String id = prompt("Введите UUID задачи");

            try {
                tasksService.deleteById(UUID.fromString(id));
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Неправильный формат UUID. Попробуйте еще раз...");
            }
        }
    }

    @Override
    public void filter() {
        listTasks(tasksService.filterByStatus(parseStatus()));
    }

    @Override
    public void sort() {
        String sortFlag;

        System.out.printf("Доступные флаги сортировки: %s\n", String.join(", ", ALLOWED_SORT_FLAGS));

        while (true) {
            sortFlag = prompt("Флаг сортировки");

            if (ALLOWED_SORT_FLAGS.contains(sortFlag)) break;

            System.out.println("Неверный флаг сортировки. Попробуйте еще раз...");
        }

        List<Task> tasks;

        switch (sortFlag) {
            case "deadline": {
                tasks = tasksService.sortByDeadline();
                break;
            }
            case "status": {
                tasks = tasksService.sortByStatus();
                break;
            }
            default: {
                tasks = new ArrayList<>();
                break;
            }
        }

        listTasks(tasks);
    }

    @Override
    public void edit() {
        String rawId = prompt("Введите UUID");

        UUID id;
        while (true) {
            try {
                id = UUID.fromString(rawId);
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Невалидный UUID. Попробуйте еще раз...");
            }
        }


        String name = prompt("Новое название");
        String description = prompt("Новое описание");
        TaskStatus status = parseStatus();
        LocalDate deadline = parseDate("Введите дату (YYYY-MM-DD)");

        try {
            Task updated = tasksService.update(id, new TaskUpdate(name, description, status, deadline));
            System.out.println("Updated: " + updated);
        } catch (TaskNotFoundException e) {
            System.out.println("Task not found.");
        }
    }
}

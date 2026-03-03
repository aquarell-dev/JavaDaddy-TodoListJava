package com.aquarell;

import com.aquarell.controller.tasks.ITaskController;
import com.aquarell.controller.tasks.TasksController;
import com.aquarell.entity.commands.Command;
import com.aquarell.repository.tasks.ITasksRepository;
import com.aquarell.repository.tasks.TasksRepository;
import com.aquarell.service.tasks.ITasksService;
import com.aquarell.service.tasks.TasksService;

import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static void printHelp() {
        StringBuilder builder = new StringBuilder();

        builder.append("Список команд:\n");

        Arrays.stream(Command.values()).forEach(command -> builder.append(command.toString()).append(" - ").append(command.getDescription()).append("\n"));

        System.out.println(builder.toString());
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        ITasksRepository taskRepository = new TasksRepository();
        ITasksService taskService = new TasksService(taskRepository);

        ITaskController controller = new TasksController(taskService);

        Map<Command, Runnable> LOOKUP = Map.of(
                Command.ADD, controller::add,
                Command.LIST, controller::list,
                Command.FILTER, controller::filter,
                Command.SORT, controller::sort,
                Command.DELETE, controller::delete,
                Command.EDIT, controller::edit
        );

        printHelp();

        String currentCommand = "";

        while (true) {
            System.out.print("Введите команду: ");
            currentCommand = scanner.nextLine();
            Command command;

            try {
                command = Command.valueOf(currentCommand.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid command. Try again...");
                continue;
            }

            if (command.equals(Command.EXIT)) {
                break;
            }

            LOOKUP.get(command).run();
        }
    }
}
package app;

import models.TaskStatus;
import system.TaskManagementSystem;
import models.Task;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final TaskManagementSystem system = new TaskManagementSystem();

    public static void main(String[] args) {
        System.out.println("Personal Task Management System - PoC");
        System.out.println("System initialized (empty database).\n");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllTasks();
                case "2" -> searchTasks();
                case "3" -> importCsv();
                case "4" -> exportCsv();
                case "5" -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n+-----------------------------------------------+");
        System.out.println("|   Personal Task Management System - PoC       |");
        System.out.println("+-----------------------------------------------+");
        System.out.println("|  1. View All Tasks                            |");
        System.out.println("|  2. Search Tasks (with filters)               |");
        System.out.println("|  3. Import Tasks from CSV                     |");
        System.out.println("|  4. Export Tasks to CSV                       |");
        System.out.println("|  5. Exit                                      |");
        System.out.println("+-----------------------------------------------+");
        System.out.print("Select an option: ");
    }

    private static void viewAllTasks() {
        List<Task> all = system.getAllTasks();
        System.out.println("\n  All tasks (" + all.size() + "):\n");
        System.out.println(system.formatTaskTable(all));
    }

    private static void searchTasks() {
        System.out.println("\n-- Task Search --");
        System.out.println("Leave any field blank to skip that filter.\n");

        System.out.print("  Task name keyword: ");
        String keyword = scanner.nextLine().trim();

        System.out.print("  Status (open/completed/cancelled) [default: open]: ");
        String statusStr = scanner.nextLine().trim();
        TaskStatus status = null;
        if (!statusStr.isEmpty()) {
            status = TaskStatus.fromString(statusStr);
        }

        System.out.print("  From date (YYYY-MM-DD): ");
        LocalDate fromDate = readDate();

        System.out.print("  To date (YYYY-MM-DD): ");
        LocalDate toDate = readDate();

        System.out.print("  Day of week (e.g. MONDAY): ");
        String dayOfWeek = scanner.nextLine().trim();
        if (dayOfWeek.isEmpty()) dayOfWeek = null;

        List<Task> results = system.searchTasks(
                keyword.isEmpty() ? null : keyword,
                status, fromDate, toDate, dayOfWeek);

        System.out.println("\n  Found " + results.size() + " task(s):\n");
        System.out.println(system.formatTaskTable(results));
    }

    private static LocalDate readDate() {
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return null;
        try {
            return LocalDate.parse(input);
        } catch (DateTimeParseException e) {
            System.out.println("  Invalid date format, skipping.");
            return null;
        }
    }

    private static void importCsv() {
        System.out.print("\n  Enter CSV file path: ");
        String filepath = scanner.nextLine().trim();

        try {
            int count = system.importFromCsv(filepath);
            System.out.println("  Successfully imported " + count + " row(s).");
            System.out.println("  Total tasks in system: " + system.getTasks().size());
            System.out.println("  Total projects in system: " + system.getProjects().size());
        } catch (IOException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private static void exportCsv() {
        if (system.getTasks().isEmpty()) {
            System.out.println("\n  No tasks to export.");
            return;
        }

        File exportDir = new File("export_csv");
        if (!exportDir.exists()) {
            exportDir.mkdir();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "tasks_export_" + timestamp + ".csv";
        String filepath = new File(exportDir, filename).getPath();

        try {
            system.exportToCsv(filepath);
            System.out.println("  Exported " + system.getTasks().size() + " task(s) to '" + filepath + "'.");
        } catch (IOException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }
}

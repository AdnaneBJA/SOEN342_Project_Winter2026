import gateway.ICalGateway;
import models.PriorityLevel;
import models.RecurrencePattern;
import models.Task;
import models.TaskStatus;
import system.TaskManagementSystem;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final TaskManagementSystem system = new TaskManagementSystem();
    private static final ICalGateway icalGateway = new ICalGateway();

    /** Last search result — used by "Export search results to CSV". */
    private static List<Task> lastSearchResults = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Personal Task Management System");
        System.out.println("Data loaded from database.\n");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> createTask();
                case "2" -> createRecurringTask();
                case "3" -> viewAllTasks();
                case "4" -> searchTasks();
                case "5" -> importCsv();
                case "6" -> exportCsv();
                case "7" -> exportIcal();
                case "8" -> viewOverloadedCollaborators();
                case "9" -> {
                    system.saveToDatabase();
                    System.out.println("Data saved. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n+-----------------------------------------------+");
        System.out.println("|   Personal Task Management System             |");
        System.out.println("+-----------------------------------------------+");
        System.out.println("|  1. Create Task                               |");
        System.out.println("|  2. Create Recurring Task                     |");
        System.out.println("|  3. View All Tasks                            |");
        System.out.println("|  4. Search Tasks (with filters)               |");
        System.out.println("|  5. Import Tasks from CSV                     |");
        System.out.println("|  6. Export Tasks to CSV                       |");
        System.out.println("|  7. Export Tasks to iCal (.ics)               |");
        System.out.println("|  8. View Overloaded Collaborators             |");
        System.out.println("|  9. Save & Exit                               |");
        System.out.println("+-----------------------------------------------+");
        System.out.print("Select an option: ");
    }

    // ── 1. Create Task ──────────────────────────────────────

    private static void createTask() {
        System.out.println("\n-- Create New Task --\n");

        System.out.print("  Title: ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) {
            System.out.println("  Error: Task title cannot be empty.");
            return;
        }

        System.out.print("  Description: ");
        String description = scanner.nextLine().trim();

        System.out.print("  Priority (LOW/MEDIUM/HIGH) [default: MEDIUM]: ");
        String priorityStr = scanner.nextLine().trim();
        PriorityLevel priority;
        try {
            priority = priorityStr.isEmpty() ? PriorityLevel.MEDIUM : PriorityLevel.valueOf(priorityStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("  Invalid priority. Using MEDIUM.");
            priority = PriorityLevel.MEDIUM;
        }

        System.out.print("  Due date (YYYY-MM-DD) [optional]: ");
        LocalDate dueDate = readDate();

        System.out.print("  Project name [optional]: ");
        String projectName = scanner.nextLine().trim();

        Task task = system.createTask(title, description, priority, dueDate, projectName);
        system.saveToDatabase();
        System.out.println("  Task created: " + task.getTitle());
    }

    // ── 2. Create Recurring Task ────────────────────────────

    private static void createRecurringTask() {
        System.out.println("\n-- Create Recurring Task --\n");

        System.out.print("  Title: ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) {
            System.out.println("  Error: Task title cannot be empty.");
            return;
        }

        System.out.print("  Description: ");
        String description = scanner.nextLine().trim();

        System.out.print("  Priority (LOW/MEDIUM/HIGH) [default: MEDIUM]: ");
        String priorityStr = scanner.nextLine().trim();
        PriorityLevel priority;
        try {
            priority = priorityStr.isEmpty() ? PriorityLevel.MEDIUM : PriorityLevel.valueOf(priorityStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("  Invalid priority. Using MEDIUM.");
            priority = PriorityLevel.MEDIUM;
        }

        System.out.print("  Project name [optional]: ");
        String projectName = scanner.nextLine().trim();

        System.out.print("  Recurrence type (DAILY/WEEKLY/MONTHLY): ");
        String typeStr = scanner.nextLine().trim().toUpperCase();
        RecurrencePattern.RecurrenceType type;
        try {
            type = RecurrencePattern.RecurrenceType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            System.out.println("  Invalid recurrence type.");
            return;
        }

        System.out.print("  Interval (positive integer, e.g. 1 for every day/week/month): ");
        int interval;
        try {
            interval = Integer.parseInt(scanner.nextLine().trim());
            if (interval < 1) interval = 1;
        } catch (NumberFormatException e) {
            interval = 1;
        }

        System.out.print("  Start date (YYYY-MM-DD): ");
        LocalDate startDate = readDate();
        if (startDate == null) {
            System.out.println("  Error: start date is required.");
            return;
        }

        System.out.print("  End date (YYYY-MM-DD): ");
        LocalDate endDate = readDate();
        if (endDate == null) {
            System.out.println("  Error: end date is required.");
            return;
        }
        if (endDate.isBefore(startDate)) {
            System.out.println("  Error: end date must be on or after start date.");
            return;
        }

        List<DayOfWeek> weekdays = null;
        Integer dayOfMonth = null;

        if (type == RecurrencePattern.RecurrenceType.WEEKLY) {
            System.out.print("  Weekdays (comma-separated, e.g. MONDAY,WEDNESDAY) [optional]: ");
            String wdStr = scanner.nextLine().trim();
            if (!wdStr.isEmpty()) {
                weekdays = new ArrayList<>();
                for (String w : wdStr.split(",")) {
                    try {
                        weekdays.add(DayOfWeek.valueOf(w.trim().toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        System.out.println("  Ignoring invalid weekday: " + w.trim());
                    }
                }
                if (weekdays.isEmpty()) weekdays = null;
            }
        } else if (type == RecurrencePattern.RecurrenceType.MONTHLY) {
            System.out.print("  Day of month (1-31) [blank = use start-date day]: ");
            String domStr = scanner.nextLine().trim();
            if (!domStr.isEmpty()) {
                try {
                    int d = Integer.parseInt(domStr);
                    if (d >= 1 && d <= 31) dayOfMonth = d;
                } catch (NumberFormatException e) {
                    System.out.println("  Invalid day of month, using start-date day.");
                }
            }
        }

        RecurrencePattern pattern = new RecurrencePattern(
                type, weekdays, dayOfMonth, startDate, endDate, interval);

        List<Task> occurrences = system.createRecurringTask(
                title, description, priority, projectName, pattern);

        if (occurrences.isEmpty()) {
            System.out.println("  No occurrences generated (check dates and weekdays).");
            return;
        }

        system.saveToDatabase();
        System.out.println("  Created " + occurrences.size() + " occurrence(s) of '" + title + "'.");
    }

    // ── 3. View All Tasks ───────────────────────────────────

    private static void viewAllTasks() {
        List<Task> all = system.getAllTasks();
        System.out.println("\n  All tasks (" + all.size() + "):\n");
        System.out.println(system.formatTaskTable(all));
    }

    // ── 4. Search Tasks ─────────────────────────────────────

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

        lastSearchResults = new ArrayList<>(results);

        System.out.println("\n  Found " + results.size() + " task(s):\n");
        System.out.println(system.formatTaskTable(results));
        System.out.println("  Tip: use 'Export Tasks to CSV' and choose option 2 to export this result set.");
    }

    // ── 5. Import CSV ───────────────────────────────────────

    private static void importCsv() {
        System.out.print("\n  Enter CSV file path: ");
        String filepath = scanner.nextLine().trim();

        try {
            int count = system.importFromCsv(filepath);
            System.out.println("  Successfully imported " + count + " row(s).");
            System.out.println("  Total tasks in system: " + system.getTasks().size());
            System.out.println("  Total projects in system: " + system.getProjects().size());
            system.saveToDatabase();
        } catch (IOException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // ── 6. Export CSV ───────────────────────────────────────

    private static void exportCsv() {
        if (system.getTasks().isEmpty()) {
            System.out.println("\n  No tasks to export.");
            return;
        }

        System.out.println("\n-- Export Tasks to CSV --");
        System.out.println("  1. Export ALL tasks");
        System.out.println("  2. Export LAST SEARCH RESULTS ("
                + lastSearchResults.size() + " task(s))");
        System.out.print("  Select option [1]: ");
        String option = scanner.nextLine().trim();
        if (option.isEmpty()) option = "1";

        List<Task> toExport;
        String suffix;
        if ("2".equals(option)) {
            if (lastSearchResults.isEmpty()) {
                System.out.println("  No search results available. Run a search first.");
                return;
            }
            toExport = lastSearchResults;
            suffix = "search";
        } else {
            toExport = system.getTasks();
            suffix = "all";
        }

        File exportDir = new File("export_csv");
        if (!exportDir.exists()) exportDir.mkdir();

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filepath = new File(exportDir, "tasks_export_" + suffix + "_" + timestamp + ".csv").getPath();

        try {
            system.exportTasksToCsv(toExport, filepath);
            System.out.println("  Exported " + toExport.size() + " task(s) to '" + filepath + "'.");
        } catch (IOException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // ── 7. Export iCal ──────────────────────────────────────

    private static void exportIcal() {
        if (system.getTasks().isEmpty()) {
            System.out.println("\n  No tasks to export.");
            return;
        }

        System.out.println("\n-- iCal Export --");
        System.out.println("  1. Export a single task");
        System.out.println("  2. Export all tasks in a project");
        System.out.println("  3. Export open tasks due this week");
        System.out.println("  4. Export all tasks with due dates");
        System.out.print("  Select option: ");
        String option = scanner.nextLine().trim();

        File exportDir = new File("export_ical");
        if (!exportDir.exists()) exportDir.mkdir();

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        try {
            switch (option) {
                case "1" -> {
                    System.out.print("  Enter task title: ");
                    String title = scanner.nextLine().trim();
                    Task task = system.getTasks().stream()
                            .filter(t -> t.getTitle().equalsIgnoreCase(title))
                            .findFirst().orElse(null);
                    if (task == null) {
                        System.out.println("  Task not found.");
                        return;
                    }
                    if (task.getDueDate() == null) {
                        System.out.println("  Task has no due date. Cannot export to iCal.");
                        return;
                    }
                    String filepath = new File(exportDir, "task_" + timestamp + ".ics").getPath();
                    icalGateway.exportTask(task, filepath);
                    System.out.println("  Exported to '" + filepath + "'.");
                }
                case "2" -> {
                    System.out.print("  Enter project name: ");
                    String projectName = scanner.nextLine().trim();
                    String filepath = new File(exportDir, "project_" + timestamp + ".ics").getPath();
                    icalGateway.exportProjectTasks(system.getTasks(), projectName, filepath);
                    System.out.println("  Exported to '" + filepath + "'.");
                }
                case "3" -> {
                    LocalDate today = LocalDate.now();
                    LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
                    List<Task> filtered = system.getTasks().stream()
                            .filter(t -> t.getStatus() == TaskStatus.OPEN
                                    && t.getDueDate() != null
                                    && !t.getDueDate().isBefore(today)
                                    && !t.getDueDate().isAfter(endOfWeek))
                            .collect(Collectors.toList());
                    String filepath = new File(exportDir, "week_" + timestamp + ".ics").getPath();
                    icalGateway.exportFilteredTasks(filtered, filepath);
                    System.out.println("  Exported " + filtered.size() + " task(s) to '" + filepath + "'.");
                }
                case "4" -> {
                    String filepath = new File(exportDir, "all_" + timestamp + ".ics").getPath();
                    icalGateway.exportFilteredTasks(system.getTasks(), filepath);
                    System.out.println("  Exported to '" + filepath + "'.");
                }
                default -> System.out.println("  Invalid option.");
            }
        } catch (IOException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // ── 8. Overloaded Collaborators ─────────────────────────

    private static void viewOverloadedCollaborators() {
        System.out.println("\n-- Overloaded Collaborators --\n");
        System.out.println(system.formatOverloadedCollaborators());
    }

    // ── Utility ─────────────────────────────────────────────

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
}

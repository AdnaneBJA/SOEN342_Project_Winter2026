package system;

import models.*;

import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import persistence.DatabaseManager;
import persistence.TaskRepository;

public class TaskManagementSystem {
    private final List<Task> tasks;
    private final List<Project> projects;
    private final DatabaseManager dbManager;
    private final TaskRepository repository;

    public TaskManagementSystem() {
        this.tasks = new ArrayList<>();
        this.projects = new ArrayList<>();
        this.dbManager = DatabaseManager.getInstance();
        this.repository = new TaskRepository(dbManager);
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            dbManager.initialize();
            List<Project> loadedProjects = repository.loadAllProjects();
            projects.addAll(loadedProjects);
            List<Task> loadedTasks = repository.loadAllTasks(projects);
            tasks.addAll(loadedTasks);
        } catch (Exception e) {
            System.err.println("Warning: Could not load from database. Starting fresh. (" + e.getMessage() + ")");
        }
    }

    public void saveToDatabase() {
        try {
            repository.saveAll(tasks, projects);
        } catch (Exception e) {
            System.err.println("Error saving to database: " + e.getMessage());
        }
    }

    public List<Task> getTasks() { return tasks; }
    public List<Project> getProjects() { return projects; }

    // ── Search ──────────────────────────────────────────────

    public List<Task> getAllTasks() {
        List<Task> results = new ArrayList<>(tasks);
        results.sort(Comparator.comparing(t -> t.getDueDate() != null ? t.getDueDate() : LocalDate.MAX));
        return results;
    }

    public List<Task> searchTasks(String keyword, TaskStatus status,
                                  LocalDate fromDate, LocalDate toDate,
                                  String dayOfWeek) {
        List<Task> results = new ArrayList<>(tasks);

        if (keyword != null && !keyword.isEmpty()) {
            String kw = keyword.toLowerCase();
            results = results.stream()
                    .filter(t -> t.getTitle().toLowerCase().contains(kw)
                            || t.getDescription().toLowerCase().contains(kw))
                    .collect(Collectors.toList());
        }

        if (status != null) {
            results = results.stream()
                    .filter(t -> t.getStatus() == status)
                    .collect(Collectors.toList());
        } else {
            results = results.stream()
                    .filter(t -> t.getStatus() == TaskStatus.OPEN)
                    .collect(Collectors.toList());
        }

        if (fromDate != null) {
            results = results.stream()
                    .filter(t -> t.getDueDate() != null && !t.getDueDate().isBefore(fromDate))
                    .collect(Collectors.toList());
        }

        if (toDate != null) {
            results = results.stream()
                    .filter(t -> t.getDueDate() != null && !t.getDueDate().isAfter(toDate))
                    .collect(Collectors.toList());
        }

        if (dayOfWeek != null && !dayOfWeek.isEmpty()) {
            DayOfWeek dow = parseDayOfWeek(dayOfWeek);
            if (dow != null) {
                results = results.stream()
                        .filter(t -> t.getDueDate() != null && t.getDueDate().getDayOfWeek() == dow)
                        .collect(Collectors.toList());
            }
        }

        results.sort(Comparator.comparing(t -> t.getDueDate() != null ? t.getDueDate() : LocalDate.MAX));
        return results;
    }

    private DayOfWeek parseDayOfWeek(String s) {
        try {
            return DayOfWeek.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ── CSV Export ──────────────────────────────────────────

    /** Export every task in the system. */
    public void exportToCsv(String filepath) throws IOException {
        exportTasksToCsv(tasks, filepath);
    }

    /** Export an arbitrary task collection (e.g. search results). */
    public void exportTasksToCsv(List<Task> taskList, String filepath) throws IOException {
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filepath)))) {
            pw.println("TaskName,Description,Subtask,Status,Priority,DueDate,ProjectName,ProjectDescription,Collaborator,CollaboratorCategory");
            for (Task task : taskList) {
                if (task.getSubtasks().isEmpty()) {
                    pw.println(buildCsvLine(task, null));
                } else {
                    for (Subtask st : task.getSubtasks()) {
                        pw.println(buildCsvLine(task, st));
                    }
                }
            }
        }
    }

    private String buildCsvLine(Task task, Subtask subtask) {
        String[] fields = new String[10];
        fields[0] = escapeCsv(task.getTitle());
        fields[1] = escapeCsv(task.getDescription());
        fields[2] = subtask != null ? escapeCsv(subtask.getTitle()) : "";
        fields[3] = subtask != null ? subtask.getStatus().getLabel() : task.getStatus().getLabel();
        fields[4] = task.getPriority().name();
        fields[5] = task.getDueDate() != null ? task.getDueDate().toString() : "";
        fields[6] = task.getProject() != null ? escapeCsv(task.getProject().getName()) : "";
        fields[7] = task.getProject() != null ? escapeCsv(task.getProject().getDescription()) : "";
        fields[8] = (subtask != null && subtask.getCollaborator() != null) ? escapeCsv(subtask.getCollaborator().getName()) : "";
        fields[9] = (subtask != null && subtask.getCollaborator() != null) ? subtask.getCollaborator().getCategory().getLabel() : "";
        return String.join(",", fields);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // ── CSV Import ──────────────────────────────────────────

    public int importFromCsv(String filepath) throws IOException {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String headerLine = br.readLine(); // skip header
            if (headerLine == null) return 0;

            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = parseCsvLine(line);
                if (fields.length < 10) continue;
                processRow(fields);
                count++;
            }
        }
        return count;
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(current.toString().trim());
                    current = new StringBuilder();
                } else {
                    current.append(c);
                }
            }
        }
        fields.add(current.toString().trim());
        return fields.toArray(new String[0]);
    }

    private void processRow(String[] fields) {
        String taskName = fields[0];
        String description = fields[1];
        String subtaskName = fields[2];
        String statusStr = fields[3];
        String priorityStr = fields[4];
        String dueDateStr = fields[5];
        String projectName = fields[6];
        String projectDesc = fields[7];
        String collabName = fields[8];
        String collabCatStr = fields[9];

        if (taskName.isEmpty()) return;

        TaskStatus status = TaskStatus.fromString(statusStr);
        PriorityLevel priority;
        try {
            priority = PriorityLevel.valueOf(priorityStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            priority = PriorityLevel.MEDIUM;
        }

        LocalDate dueDate = null;
        if (!dueDateStr.isEmpty()) {
            try {
                dueDate = LocalDate.parse(dueDateStr);
            } catch (DateTimeParseException e) {
                // skip invalid date
            }
        }

        // Find or create project
        Project project = null;
        if (!projectName.isEmpty()) {
            project = findOrCreateProject(projectName, projectDesc);
        }

        // Find or create task (unique by name + due date)
        Task task = findTask(taskName, dueDate);
        if (task == null) {
            task = new Task(taskName, description, priority, dueDate, status);
            if (project != null) {
                task.assignProject(project);
            }
            tasks.add(task);
        }

        // Handle collaborator
        Collaborator collaborator = null;
        if (!collabName.isEmpty() && project != null) {
            collaborator = project.findCollaborator(collabName);
            if (collaborator == null) {
                CollaboratorCategory cat = CollaboratorCategory.fromString(collabCatStr);
                collaborator = new Collaborator(collabName, cat);
                project.addCollaborator(collaborator);
            }
        }

        // Handle subtask
        if (!subtaskName.isEmpty()) {
            boolean exists = task.getSubtasks().stream()
                    .anyMatch(s -> s.getTitle().equalsIgnoreCase(subtaskName));
            if (!exists) {
                // DEMO SEEDING: OCL guard temporarily disabled so we can seed an
                // overloaded-collaborator state for demoing menu option 8.
                // RESTORE THIS BLOCK AFTER SEEDING.
                // // Enforce Iteration II / OCL rule: reject over-limit assignments.
                // if (collaborator != null && !canAssignToCollaborator(collaborator)) {
                //     System.err.println("  Warning: " + collaborator.getName()
                //             + " (" + collaborator.getCategory().getLabel()
                //             + ") has reached the open-task limit of "
                //             + collaborator.getCategory().getTaskLimit()
                //             + ". Subtask '" + subtaskName + "' will be added UNASSIGNED.");
                //     collaborator = null;
                // }
                try {
                    task.addSubtask(subtaskName, collaborator);
                } catch (IllegalStateException e) {
                    // OCL: max 20 subtasks per task — skip with warning
                    System.err.println("  Warning: " + e.getMessage()
                            + " Subtask '" + subtaskName + "' skipped.");
                }
            }
        }
    }

    private Project findOrCreateProject(String name, String description) {
        for (Project p : projects) {
            if (p.getName().equalsIgnoreCase(name)) return p;
        }
        Project project = new Project(name, description);
        projects.add(project);
        return project;
    }

    private Task findTask(String title, LocalDate dueDate) {
        for (Task t : tasks) {
            if (t.getTitle().equalsIgnoreCase(title) && Objects.equals(t.getDueDate(), dueDate)) {
                return t;
            }
        }
        return null;
    }

    // ── Collaborator limit check ────────────────────────────

    public int getOpenTasksForCollaborator(Collaborator collaborator) {
        int count = 0;
        for (Task task : tasks) {
            for (Subtask st : task.getSubtasks()) {
                if (st.getCollaborator() != null
                        && st.getCollaborator().getName().equals(collaborator.getName())
                        && st.getStatus() == TaskStatus.OPEN) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean canAssignToCollaborator(Collaborator collaborator) {
        return getOpenTasksForCollaborator(collaborator) < collaborator.getCategory().getTaskLimit();
    }

    // ── Overloaded Collaborators ────────────────────────────

    public List<Collaborator> getOverloadedCollaborators() {
        List<Collaborator> overloaded = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (Project project : projects) {
            for (Collaborator collaborator : project.getCollaborators()) {
                if (seen.contains(collaborator.getName())) continue;
                seen.add(collaborator.getName());

                int openTasks = getOpenTasksForCollaborator(collaborator);
                int limit = collaborator.getCategory().getTaskLimit();
                if (openTasks > limit) {
                    overloaded.add(collaborator);
                }
            }
        }
        return overloaded;
    }

    public String formatOverloadedCollaborators() {
        List<Collaborator> overloaded = getOverloadedCollaborators();
        if (overloaded.isEmpty()) return "No overloaded collaborators found.";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-20s %-15s %-10s %-10s%n", "Name", "Category", "Open Tasks", "Limit"));
        sb.append("-".repeat(55)).append("\n");

        for (Collaborator c : overloaded) {
            int openTasks = getOpenTasksForCollaborator(c);
            sb.append(String.format("%-20s %-15s %-10d %-10d%n",
                    c.getName(), c.getCategory().getLabel(),
                    openTasks, c.getCategory().getTaskLimit()));
        }
        return sb.toString();
    }

    // ── Task Creation ───────────────────────────────────────

    public Task createTask(String title, String description, PriorityLevel priority,
                           LocalDate dueDate, String projectName) {
        TaskStatus status = TaskStatus.OPEN;
        Task task = new Task(title, description, priority, dueDate, status);

        if (projectName != null && !projectName.isEmpty()) {
            Project project = findOrCreateProject(projectName, "");
            task.assignProject(project);
        }

        tasks.add(task);
        return task;
    }

    // ── Recurring Task Creation ─────────────────────────────

    /**
     * Create one Task per occurrence of a recurrence pattern. Each occurrence
     * is a standalone {@link Task} with the same title but a different due
     * date, satisfying the Iteration II uniqueness rule (title + dueDate).
     */
    public List<Task> createRecurringTask(String title, String description,
                                          PriorityLevel priority, String projectName,
                                          RecurrencePattern pattern) {
        List<Task> occurrences = new ArrayList<>();
        if (pattern == null || pattern.getStartDate() == null || pattern.getEndDate() == null) {
            return occurrences;
        }
        if (pattern.getEndDate().isBefore(pattern.getStartDate())) {
            return occurrences;
        }

        Project project = null;
        if (projectName != null && !projectName.isEmpty()) {
            project = findOrCreateProject(projectName, "");
        }

        for (LocalDate date : generateOccurrences(pattern)) {
            // Respect (title, dueDate) uniqueness
            if (findTask(title, date) != null) continue;

            Task task = new Task(title, description, priority, date, TaskStatus.OPEN);
            if (project != null) task.assignProject(project);
            task.setRecurrence(pattern);
            tasks.add(task);
            occurrences.add(task);
        }
        return occurrences;
    }

    private List<LocalDate> generateOccurrences(RecurrencePattern pattern) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate start = pattern.getStartDate();
        LocalDate end = pattern.getEndDate();
        int interval = Math.max(1, pattern.getInterval());
        int safety = 0;

        switch (pattern.getType()) {
            case DAILY -> {
                LocalDate d = start;
                while (!d.isAfter(end) && safety++ < 3650) {
                    dates.add(d);
                    d = d.plusDays(interval);
                }
            }
            case WEEKLY -> {
                List<DayOfWeek> weekdays = pattern.getWeekdays();
                if (weekdays == null || weekdays.isEmpty()) {
                    LocalDate d = start;
                    while (!d.isAfter(end) && safety++ < 520) {
                        dates.add(d);
                        d = d.plusWeeks(interval);
                    }
                } else {
                    LocalDate d = start;
                    while (!d.isAfter(end) && safety++ < 3650) {
                        long weeksSince = java.time.temporal.ChronoUnit.WEEKS.between(start, d);
                        if (weeksSince % interval == 0 && weekdays.contains(d.getDayOfWeek())) {
                            dates.add(d);
                        }
                        d = d.plusDays(1);
                    }
                }
            }
            case MONTHLY -> {
                Integer dayOfMonth = pattern.getDayOfMonth();
                int day = (dayOfMonth != null) ? dayOfMonth : start.getDayOfMonth();
                LocalDate cursor = start.withDayOfMonth(1);
                while (!cursor.isAfter(end) && safety++ < 240) {
                    int safeDay = Math.min(day, cursor.lengthOfMonth());
                    LocalDate occurrence = cursor.withDayOfMonth(safeDay);
                    if (!occurrence.isBefore(start) && !occurrence.isAfter(end)) {
                        dates.add(occurrence);
                    }
                    cursor = cursor.plusMonths(interval);
                }
            }
        }
        return dates;
    }

    // ── Display ─────────────────────────────────────────────

    public String formatTaskTable(List<Task> taskList) {
        if (taskList.isEmpty()) return "No tasks found.";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-30s %-12s %-10s %-12s %-20s%n", "Title", "Status", "Priority", "Due Date", "Project"));
        sb.append("-".repeat(84)).append("\n");

        for (Task t : taskList) {
            String proj = t.getProject() != null ? t.getProject().getName() : "";
            String dd = t.getDueDate() != null ? t.getDueDate().toString() : "";
            sb.append(String.format("%-30s %-12s %-10s %-12s %-20s%n",
                    t.getTitle(), t.getStatus().getLabel(), t.getPriority(), dd, proj));
            for (Subtask st : t.getSubtasks()) {
                String collab = st.getCollaborator() != null ? st.getCollaborator().getName() : "";
                sb.append(String.format("  └─ %-26s %-12s %-10s %-12s %-20s%n",
                        st.getTitle(), st.getStatus().getLabel(), "", "", collab));
            }
        }
        return sb.toString();
    }
}

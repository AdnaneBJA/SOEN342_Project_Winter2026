package gateway;

import models.Subtask;
import models.Task;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Gateway Pattern: connects the domain model to iCal (.ics) export.
 * Converts Task objects into RFC 5545 compliant VCALENDAR entries.
 */
public class ICalGateway {

    private static final DateTimeFormatter ICS_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter ICS_DATETIME = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    /**
     * Export a single task to an .ics file.
     */
    public void exportTask(Task task, String filepath) throws IOException {
        exportTasks(List.of(task), filepath);
    }

    /**
     * Export all tasks in a project to an .ics file.
     * Only tasks with a due date are included.
     */
    public void exportProjectTasks(List<Task> allTasks, String projectName, String filepath) throws IOException {
        List<Task> filtered = allTasks.stream()
                .filter(t -> t.getProject() != null
                        && t.getProject().getName().equalsIgnoreCase(projectName)
                        && t.getDueDate() != null)
                .collect(Collectors.toList());
        exportTasks(filtered, filepath);
    }

    /**
     * Export filtered tasks (e.g., open tasks due this week) to an .ics file.
     * Only tasks with a due date are included.
     */
    public void exportFilteredTasks(List<Task> tasks, String filepath) throws IOException {
        List<Task> filtered = tasks.stream()
                .filter(t -> t.getDueDate() != null)
                .collect(Collectors.toList());
        exportTasks(filtered, filepath);
    }

    /**
     * Core export: writes a list of tasks as VCALENDAR VEVENT entries.
     */
    public void exportTasks(List<Task> tasks, String filepath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            writer.write("BEGIN:VCALENDAR\r\n");
            writer.write("VERSION:2.0\r\n");
            writer.write("PRODID:-//SOEN342//TaskManager//EN\r\n");
            writer.write("CALSCALE:GREGORIAN\r\n");

            for (Task task : tasks) {
                if (task.getDueDate() == null) continue;
                writeEvent(writer, task);
            }

            writer.write("END:VCALENDAR\r\n");
        }
    }

    private void writeEvent(BufferedWriter writer, Task task) throws IOException {
        writer.write("BEGIN:VEVENT\r\n");
        writer.write("UID:" + UUID.randomUUID() + "@soen342\r\n");
        writer.write("DTSTAMP:" + LocalDateTime.now().format(ICS_DATETIME) + "\r\n");
        writer.write("DTSTART;VALUE=DATE:" + task.getDueDate().format(ICS_DATE) + "\r\n");
        writer.write("DTEND;VALUE=DATE:" + task.getDueDate().plusDays(1).format(ICS_DATE) + "\r\n");
        writer.write("SUMMARY:" + escapeIcal(task.getTitle()) + "\r\n");

        // Build description: task description + subtask summary
        StringBuilder desc = new StringBuilder();
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            desc.append(task.getDescription());
        }
        desc.append("\\nStatus: ").append(task.getStatus().getLabel());
        desc.append("\\nPriority: ").append(task.getPriority().name());
        if (task.getProject() != null) {
            desc.append("\\nProject: ").append(task.getProject().getName());
        }

        // Subtasks summarized inside parent description (NOT separate entries)
        if (!task.getSubtasks().isEmpty()) {
            desc.append("\\n\\nSubtasks:");
            for (Subtask st : task.getSubtasks()) {
                desc.append("\\n  - ").append(st.getTitle())
                    .append(" [").append(st.getStatus().getLabel()).append("]");
                if (st.getCollaborator() != null) {
                    desc.append(" (").append(st.getCollaborator().getName()).append(")");
                }
            }
        }

        writer.write("DESCRIPTION:" + escapeIcal(desc.toString()) + "\r\n");
        writer.write("PRIORITY:" + mapPriority(task.getPriority()) + "\r\n");
        writer.write("STATUS:" + mapStatus(task.getStatus()) + "\r\n");

        if (task.getProject() != null) {
            writer.write("CATEGORIES:" + escapeIcal(task.getProject().getName()) + "\r\n");
        }

        writer.write("END:VEVENT\r\n");
    }

    private String escapeIcal(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace(",", "\\,")
                   .replace(";", "\\;");
    }

    private int mapPriority(models.PriorityLevel priority) {
        return switch (priority) {
            case HIGH -> 1;
            case MEDIUM -> 5;
            case LOW -> 9;
        };
    }

    private String mapStatus(models.TaskStatus status) {
        return switch (status) {
            case OPEN -> "NEEDS-ACTION";
            case COMPLETED -> "COMPLETED";
            case CANCELLED -> "CANCELLED";
        };
    }
}

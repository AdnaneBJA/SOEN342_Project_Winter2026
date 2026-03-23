package models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Task {
    private final String title;
    private String description;
    private PriorityLevel priority;
    private TaskStatus status;
    private LocalDate dueDate;
    private final LocalDateTime creationDate;
    private Project project;
    private final List<Tag> tags;
    private final List<Subtask> subtasks;
    private final List<ActivityEntry> activityHistory;
    private RecurrencePattern recurrence;

    public Task(String title, String description, PriorityLevel priority,
                LocalDate dueDate, TaskStatus status) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.status = status;
        this.creationDate = LocalDateTime.now();
        this.tags = new ArrayList<>();
        this.subtasks = new ArrayList<>();
        this.activityHistory = new ArrayList<>();
        this.activityHistory.add(new ActivityEntry("Task created: " + title));
    }

    public Task(String title, String description, PriorityLevel priority, LocalDate dueDate) {
        this(title, description, priority, dueDate, TaskStatus.OPEN);
    }

    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public PriorityLevel getPriority() { return priority; }
    public TaskStatus getStatus() { return status; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDateTime getCreationDate() { return creationDate; }
    public Project getProject() { return project; }
    public List<Tag> getTags() { return tags; }
    public List<Subtask> getSubtasks() { return subtasks; }
    public List<ActivityEntry> getActivityHistory() { return activityHistory; }
    public RecurrencePattern getRecurrence() { return recurrence; }

    // Setters
    public void setDescription(String description) { this.description = description; }
    public void setPriority(PriorityLevel priority) { this.priority = priority; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setRecurrence(RecurrencePattern recurrence) { this.recurrence = recurrence; }

    public void setStatus(TaskStatus newStatus) {
        TaskStatus old = this.status;
        this.status = newStatus;
        activityHistory.add(new ActivityEntry(
                "Status changed from " + old.getLabel() + " to " + newStatus.getLabel()));
    }

    public void assignProject(Project project) {
        this.project = project;
        activityHistory.add(new ActivityEntry("Assigned to project: " + project.getName()));
    }

    public Subtask addSubtask(String title, Collaborator collaborator) {
        Subtask subtask = new Subtask(title, collaborator);
        subtasks.add(subtask);
        activityHistory.add(new ActivityEntry("Subtask added: " + title));
        return subtask;
    }

    public Subtask addSubtask(String title) {
        return addSubtask(title, null);
    }

    public void addTag(Tag tag) {
        if (!tags.contains(tag)) tags.add(tag);
    }

    @Override
    public String toString() {
        return "Task(" + title + ", " + status.getLabel() + ", " + priority + ")";
    }
}

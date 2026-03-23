package models;

public class Subtask {
    private final String title;
    private TaskStatus status;
    private Collaborator collaborator;

    public Subtask(String title) {
        this.title = title;
        this.status = TaskStatus.OPEN;
    }

    public Subtask(String title, Collaborator collaborator) {
        this(title);
        this.collaborator = collaborator;
    }

    public String getTitle() { return title; }
    public TaskStatus getStatus() { return status; }
    public Collaborator getCollaborator() { return collaborator; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public void setCollaborator(Collaborator collaborator) { this.collaborator = collaborator; }
    public boolean isCompleted() { return status == TaskStatus.COMPLETED; }
}

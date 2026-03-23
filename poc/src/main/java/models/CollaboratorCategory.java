package models;

public enum CollaboratorCategory {
    SENIOR("Senior", 2),
    INTERMEDIATE("Intermediate", 5),
    JUNIOR("Junior", 10);

    private final String label;
    private final int taskLimit;

    CollaboratorCategory(String label, int taskLimit) {
        this.label = label;
        this.taskLimit = taskLimit;
    }

    public String getLabel() {
        return label;
    }

    public int getTaskLimit() {
        return taskLimit;
    }

    public static CollaboratorCategory fromString(String s) {
        for (CollaboratorCategory cc : values()) {
            if (cc.label.equalsIgnoreCase(s)) return cc;
        }
        return JUNIOR;
    }
}

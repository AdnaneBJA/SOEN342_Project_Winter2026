package models;

public enum TaskStatus {
    OPEN("open"), COMPLETED("completed"), CANCELLED("cancelled");

    private final String label;

    TaskStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static TaskStatus fromString(String s) {
        for (TaskStatus ts : values()) {
            if (ts.label.equalsIgnoreCase(s)) return ts;
        }
        return OPEN;
    }
}

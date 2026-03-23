package models;

public class Collaborator {
    private final String name;
    private CollaboratorCategory category;

    public Collaborator(String name, CollaboratorCategory category) {
        this.name = name;
        this.category = category;
    }

    public String getName() { return name; }
    public CollaboratorCategory getCategory() { return category; }
    public void setCategory(CollaboratorCategory category) { this.category = category; }

    @Override
    public String toString() {
        return name + " (" + category.getLabel() + ")";
    }
}

package models;

import java.util.ArrayList;
import java.util.List;

public class Project {
    private final String name;
    private String description;
    private final List<Collaborator> collaborators;

    public Project(String name, String description) {
        this.name = name;
        this.description = description;
        this.collaborators = new ArrayList<>();
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Collaborator> getCollaborators() { return collaborators; }

    public void addCollaborator(Collaborator collaborator) {
        collaborators.add(collaborator);
    }

    public Collaborator findCollaborator(String name) {
        for (Collaborator c : collaborators) {
            if (c.getName().equalsIgnoreCase(name)) return c;
        }
        return null;
    }

    @Override
    public String toString() { return name; }
}

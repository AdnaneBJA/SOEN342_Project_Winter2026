package persistence;

import models.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository {
    private final DatabaseManager db;

    public TaskRepository(DatabaseManager db) {
        this.db = db;
    }

    // ── Projects ────────────────────────────────────────────

    public int saveProject(Project project) throws SQLException {
        Connection conn = db.getConnection();
        // Check if exists
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM projects WHERE name = ?")) {
            ps.setString(1, project.getName());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }
        // Insert
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO projects (name, description) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, project.getName());
            ps.setString(2, project.getDescription());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            keys.next();
            return keys.getInt(1);
        }
    }

    // ── Collaborators ───────────────────────────────────────

    public int saveCollaborator(Collaborator collaborator, int projectId) throws SQLException {
        Connection conn = db.getConnection();
        // Check if exists
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM collaborators WHERE name = ? AND project_id = ?")) {
            ps.setString(1, collaborator.getName());
            ps.setInt(2, projectId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }
        // Insert
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO collaborators (name, category, project_id) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, collaborator.getName());
            ps.setString(2, collaborator.getCategory().name());
            ps.setInt(3, projectId);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            keys.next();
            return keys.getInt(1);
        }
    }

    // ── Tasks ───────────────────────────────────────────────

    public int saveTask(Task task) throws SQLException {
        Connection conn = db.getConnection();
        Integer projectId = null;
        if (task.getProject() != null) {
            projectId = saveProject(task.getProject());
            // Save collaborators
            for (Collaborator c : task.getProject().getCollaborators()) {
                saveCollaborator(c, projectId);
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO tasks (title, description, priority, status, due_date, creation_date, project_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getPriority().name());
            ps.setString(4, task.getStatus().getLabel());
            ps.setString(5, task.getDueDate() != null ? task.getDueDate().toString() : null);
            ps.setString(6, task.getCreationDate().toString());
            if (projectId != null) ps.setInt(7, projectId);
            else ps.setNull(7, Types.INTEGER);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            keys.next();
            int taskId = keys.getInt(1);

            // Save subtasks
            for (Subtask st : task.getSubtasks()) {
                saveSubtask(st, taskId, projectId);
            }
            return taskId;
        }
    }

    private void saveSubtask(Subtask subtask, int taskId, Integer projectId) throws SQLException {
        Connection conn = db.getConnection();
        Integer collabId = null;
        if (subtask.getCollaborator() != null && projectId != null) {
            collabId = saveCollaborator(subtask.getCollaborator(), projectId);
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO subtasks (title, status, task_id, collaborator_id) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, subtask.getTitle());
            ps.setString(2, subtask.getStatus().getLabel());
            ps.setInt(3, taskId);
            if (collabId != null) ps.setInt(4, collabId);
            else ps.setNull(4, Types.INTEGER);
            ps.executeUpdate();
        }
    }

    // ── Load All ────────────────────────────────────────────

    public List<Project> loadAllProjects() throws SQLException {
        List<Project> projects = new ArrayList<>();
        Connection conn = db.getConnection();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM projects")) {
            while (rs.next()) {
                Project p = new Project(rs.getString("name"), rs.getString("description"));
                int projectId = rs.getInt("id");
                // Load collaborators for this project
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT * FROM collaborators WHERE project_id = ?")) {
                    ps.setInt(1, projectId);
                    ResultSet crs = ps.executeQuery();
                    while (crs.next()) {
                        CollaboratorCategory cat = CollaboratorCategory.valueOf(crs.getString("category"));
                        p.addCollaborator(new Collaborator(crs.getString("name"), cat));
                    }
                }
                projects.add(p);
            }
        }
        return projects;
    }

    public List<Task> loadAllTasks(List<Project> projects) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        Connection conn = db.getConnection();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                "SELECT t.*, p.name as project_name FROM tasks t " +
                "LEFT JOIN projects p ON t.project_id = p.id")) {
            while (rs.next()) {
                PriorityLevel priority = PriorityLevel.valueOf(rs.getString("priority"));
                TaskStatus status = TaskStatus.fromString(rs.getString("status"));
                String dueDateStr = rs.getString("due_date");
                LocalDate dueDate = dueDateStr != null ? LocalDate.parse(dueDateStr) : null;

                LocalDateTime creationDate;
                try {
                    creationDate = LocalDateTime.parse(rs.getString("creation_date"));
                } catch (Exception e) {
                    creationDate = LocalDateTime.now();
                }

                Task task = new Task(rs.getString("title"), rs.getString("description"),
                        priority, dueDate, status, creationDate);

                // Assign project
                String projectName = rs.getString("project_name");
                if (projectName != null) {
                    for (Project p : projects) {
                        if (p.getName().equals(projectName)) {
                            task.assignProject(p);
                            break;
                        }
                    }
                }

                // Load subtasks
                int taskId = rs.getInt("id");
                loadSubtasks(task, taskId);

                tasks.add(task);
            }
        }
        return tasks;
    }

    private void loadSubtasks(Task task, int taskId) throws SQLException {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT s.*, c.name as collab_name, c.category as collab_cat " +
                "FROM subtasks s LEFT JOIN collaborators c ON s.collaborator_id = c.id " +
                "WHERE s.task_id = ?")) {
            ps.setInt(1, taskId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String collabName = rs.getString("collab_name");
                Collaborator collab = null;
                if (collabName != null) {
                    CollaboratorCategory cat = CollaboratorCategory.valueOf(rs.getString("collab_cat"));
                    collab = new Collaborator(collabName, cat);
                }
                Subtask st = new Subtask(rs.getString("title"), collab);
                st.setStatus(TaskStatus.fromString(rs.getString("status")));
                task.loadSubtask(st);
            }
        }
    }

    // ── Clear & Re-save ─────────────────────────────────────

    public void saveAll(List<Task> tasks, List<Project> projects) throws SQLException {
        Connection conn = db.getConnection();
        conn.setAutoCommit(false);
        try {
            // Clear existing data
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM subtasks");
                stmt.executeUpdate("DELETE FROM tasks");
                stmt.executeUpdate("DELETE FROM collaborators");
                stmt.executeUpdate("DELETE FROM projects");
            }
            // Save all projects first
            for (Project p : projects) {
                saveProject(p);
                int pid = saveProject(p);
                for (Collaborator c : p.getCollaborators()) {
                    saveCollaborator(c, pid);
                }
            }
            // Save all tasks
            for (Task t : tasks) {
                saveTask(t);
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}

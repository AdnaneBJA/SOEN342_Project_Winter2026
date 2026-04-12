package persistence;

import java.sql.*;

public class DatabaseManager {
    private static final String DB_FILE = "taskmanager.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
            connection.setAutoCommit(true);
        }
        return connection;
    }

    public void initialize() throws SQLException {
        Connection conn = getConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS projects (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  name TEXT NOT NULL UNIQUE," +
                "  description TEXT" +
                ")"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS collaborators (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  name TEXT NOT NULL," +
                "  category TEXT NOT NULL," +
                "  project_id INTEGER," +
                "  FOREIGN KEY (project_id) REFERENCES projects(id)" +
                ")"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS tasks (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  title TEXT NOT NULL," +
                "  description TEXT," +
                "  priority TEXT NOT NULL," +
                "  status TEXT NOT NULL," +
                "  due_date TEXT," +
                "  creation_date TEXT NOT NULL," +
                "  project_id INTEGER," +
                "  FOREIGN KEY (project_id) REFERENCES projects(id)" +
                ")"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS subtasks (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  title TEXT NOT NULL," +
                "  status TEXT NOT NULL," +
                "  task_id INTEGER NOT NULL," +
                "  collaborator_id INTEGER," +
                "  FOREIGN KEY (task_id) REFERENCES tasks(id)," +
                "  FOREIGN KEY (collaborator_id) REFERENCES collaborators(id)" +
                ")"
            );
        }
    }

}

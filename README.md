# SOEN342_Project_Winter2026

## Team Credentials

| Name |
|------|
| Adnane Bejja |
| Yanis Djeridi |
| Dylan Moos |

## About

A **Personal Task Management System** built as a Java CLI application for SOEN 342 (Winter 2026). The system allows a single user to organize, track, and manage personal tasks and projects from the command line.

## Features

### Core (Iteration 2)
1. **View All Tasks** -- Display every task sorted by due date
2. **Search Tasks** -- Filter by keyword, status, date range, or day of week
3. **Import from CSV** -- Load tasks from a CSV file (`sample_import.csv` included)
4. **Export to CSV** -- Export all tasks to a timestamped CSV in `export_csv/`

### New in Iteration 3
5. **Create Task** -- Interactively create tasks with title, description, priority, due date, and optional project
6. **Export to iCal (.ics)** -- Export tasks to iCal format with 4 options:
   - Single task export
   - All tasks in a project
   - Open tasks due this week
   - All tasks with due dates
   - Subtasks are summarized inside the parent task description (not exported separately)
7. **Overloaded Collaborators** -- View collaborators whose open task count exceeds their category limit
8. **SQLite Persistence** -- All data is saved to a local `taskmanager.db` file and loaded on startup
9. **OCL Constraints** -- Formal constraints defined for:
   - Max 20 subtasks per task
   - Max 50 open tasks without due date
   - Positive task limit per collaborator category
   - Collaborator overload prevention

## How to Run

Requires **Java 17+** and **Maven**.

```bash
cd poc
mvn compile exec:java
```

The application will create a `taskmanager.db` SQLite database file in the `poc/` directory on first run. Data persists across sessions.

## CSV Format

```
TaskName, Description, Subtask, Status, Priority, DueDate, ProjectName, ProjectDescription, Collaborator, CollaboratorCategory
```

## Project Structure

```
poc/
├── src/main/java/
│   ├── Main.java                          # CLI entry point
│   ├── models/
│   │   ├── Task.java                      # Task entity
│   │   ├── Subtask.java                   # Subtask entity
│   │   ├── Project.java                   # Project entity
│   │   ├── Collaborator.java              # Collaborator entity
│   │   ├── CollaboratorCategory.java      # Category enum with task limits
│   │   ├── TaskStatus.java                # Status enum
│   │   ├── PriorityLevel.java             # Priority enum
│   │   ├── Tag.java                       # Tag entity
│   │   ├── ActivityEntry.java             # Activity history entry
│   │   └── RecurrencePattern.java         # Recurrence pattern
│   ├── system/
│   │   └── TaskManagementSystem.java      # Core business logic
│   ├── gateway/
│   │   └── ICalGateway.java               # iCal export (Gateway Pattern)
│   └── persistence/
│       ├── DatabaseManager.java           # SQLite connection (Singleton)
│       └── TaskRepository.java            # Data access (Repository Pattern)
├── pom.xml
└── sample_import.csv
```

## Diagrams

- **Iteration 1** (`Iteration1.md`) -- Use case diagram, domain model, SSDs, fully-dressed use case, operation contracts
- **Iteration 3** (`diagram.md`) -- Updated use case diagram, class diagram, sequence diagrams, OCL constraints, persistence design

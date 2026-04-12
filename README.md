# SOEN342_Project_Winter2026

## Team Credentials

| Name |
|------|
| Adnane Bejja |
| Yanis Djeridi |
| Dylan Moos |

## About

A **Personal Task Management System** built as a Java CLI application for SOEN 342 (Winter 2026).
The system allows a single user to organize, track, and manage personal tasks, projects,
subtasks, and collaborators from the command line, with SQLite persistence and iCal export.

---

## Final Submission - Iteration IV

**Final artifacts** (Where to find the artifacts we worked on):

| Artifact | Location |
|---|---|
| Final code | `poc/src/main/java/` | 
| Final README | `README.md` | 
| Iteration I artifacts (use case, domain model, SSDs, fully-dressed UC, operation contracts) | `archives/Iteration1.md` | 
| Iteration II artifacts (updated use case, domain, SSDs, interaction diagrams, class diagram) | `archives/Iteration2.md` | 
| Iteration III artifacts (updated use case, class diagram, iCal sequence diagram, OCL) | `archives/Iteration3.md` | 

---

## Features

### Iteration I - Core
- Create, update, view, search, and tag tasks
- Subtasks, projects, activity history

### Iteration II - PoC additions
1. **View All Tasks** — display every task sorted by due date
2. **Search Tasks** — filter by keyword, status, date range, or day of week
3. **Import from CSV** — load tasks from a CSV file (`poc/sample_import.csv` included)
4. **Export to CSV** — export all tasks **or the last search result set** to a timestamped CSV in `export_csv/`
5. **Create Recurring Task** — interactively define a `DAILY`, `WEEKLY`, or `MONTHLY` recurrence (with custom interval, start/end date, selected weekdays or day-of-month); the system generates one `Task` per occurrence, enforcing `(title, dueDate)` uniqueness
6. **Collaborator categories** with open-task limits (enforced on CSV import — assignments over the limit are rejected and the subtask is saved unassigned)
7. **Collaborator assignment** automatically creates a linked subtask
8. **OCL enforcement at runtime** — `Task.addSubtask` rejects a 21st subtask (OCL `MaxSubTasks`)

### Iteration III - Integration & persistence
7. **Create Task** interactively (title, description, priority, due date, optional project)
8. **Export to iCal (.ics)** — via `ICalGateway` (Gateway pattern), with four options:
   - single task, project tasks, open tasks due this week, all tasks with due dates
   - subtasks are summarized inside the parent task DESCRIPTION (not exported separately)
   - only tasks that have a due date are eligible
9. **Overloaded Collaborators** — menu item lists collaborators whose open subtasks exceed their category limit
10. **SQLite Persistence** — all data is saved to `poc/taskmanager.db` and loaded on startup
11. **OCL Constraints** (see `archives/Iteration3.md`):
    - Max 20 subtasks per task
    - Max 50 open tasks without due date
    - Positive task limit per collaborator category (SENIOR=2, INTERMEDIATE=5, JUNIOR=10)
    - No collaborator may be overloaded

### Iteration IV - Finalization
12. **Protocol state machine for `Task`** — `plantuml.md` §1
13. **Data model in UML** — `plantuml.md` §2
14. Final consolidated PlantUML sources for all required diagrams
15. Demo video (see below)

---

## How to Run

Requires **Java 17+** and **Maven**.

```bash
cd poc
mvn compile exec:java
```

The application creates a `taskmanager.db` SQLite database file in the `poc/` directory on
first run. Data persists across sessions and is saved on every create/import and on exit.

## CSV Format

```
TaskName, Description, Subtask, Status, Priority, DueDate, ProjectName, ProjectDescription, Collaborator, CollaboratorCategory
```

Optional columns may be empty. Project names are unique; a project created during
import is re-used for later rows referencing the same name.

---

## Project Structure

```
.
├── README.md                       # This file (final)
├── plantuml.md                     # Final PlantUML sources for every required diagram
├── Sprint1.md … Sprint4.md         # Original iteration requirement specs
├── archives/                       # Historical per-iteration artifacts (superseded by plantuml.md)
│   ├── Iteration1.md               # Iteration I artifacts (fully-dressed UC + operation contracts)
│   ├── Iteration2.md               # Iteration II artifacts (use case, SSDs, interaction, class)
│   └── Iteration3.md               # Iteration III artifacts (use case, class, iCal seq, OCL)
├── taskmanager.db                  # SQLite DB (created at runtime; sample provided)
├── export_csv/                     # Sample CSV exports
└── poc/
    ├── pom.xml
    ├── sample_import.csv
    └── src/main/java/
        ├── Main.java                          # CLI entry point
        ├── models/
        │   ├── Task.java
        │   ├── Subtask.java
        │   ├── Project.java
        │   ├── Collaborator.java
        │   ├── CollaboratorCategory.java      # SENIOR(2), INTERMEDIATE(5), JUNIOR(10)
        │   ├── TaskStatus.java                # OPEN / COMPLETED / CANCELLED
        │   ├── PriorityLevel.java             # LOW / MEDIUM / HIGH
        │   ├── Tag.java
        │   ├── ActivityEntry.java             # Task history entry
        │   └── RecurrencePattern.java
        ├── system/
        │   └── TaskManagementSystem.java      # Core business logic
        ├── gateway/
        │   └── ICalGateway.java               # iCal export (Gateway pattern)
        └── persistence/
            ├── DatabaseManager.java           # SQLite connection (Singleton)
            └── TaskRepository.java            # Data access (Repository pattern)
```

---

---

## Design Patterns Used

- **Gateway** : `gateway/ICalGateway.java` (isolates the domain from iCal format details)
- **Singleton** : `persistence/DatabaseManager.java`
- **Repository** : `persistence/TaskRepository.java`

---

## Demo Video
- link: *(https://www.youtube.com/watch?v=XQerkwIhzf0)*



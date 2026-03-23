# SOEN342_Project_Winter2026

## Team Credentials

| Name |
|------|
| Adnane Bejja |
| Yanis Djeridi |
| Dylan Moos |

## About

A **Personal Task Management System** built as a Java CLI application for SOEN 342 (Winter 2026). The system allows a single user to organize, track, and manage personal tasks and projects from the command line.

## Features (Iteration 2 - PoC)
# The features below are what is currently completed as part of the POC
1) **View All Tasks** -- Display every task in the system sorted by due date
2) **Search Tasks** -- Filter tasks by keyword, status, date range, or day of week. Defaults to showing all open tasks when no criteria is specified
3) **Import from CSV** -- Load tasks from a CSV file. A `sample_import.csv` file is included to test the import feature.
4) **Export to CSV** -- Export the entire database to a timestamped CSV file in the `export_csv/` folder

## How to Run

Requires **Java 17+** and **Maven**.

## CSV Format

The system uses the following CSV column order for both import and export:

```
TaskName, Description, Subtask, Status, Priority, DueDate, ProjectName, ProjectDescription, Collaborator, CollaboratorCategory
```

## Diagrams

- **Iteration 1** (`Iteration1.md`) -- Use case diagram, domain model, SSDs, fully-dressed use case, operation contracts
- **Iteration 2** (`Iteration2.md`) -- Updated use case diagram, updated domain model, updated SSDs, interaction diagrams, class diagram, new operation contracts

# Screenshots of application
### Application upon startup


### Viewing all tasks


### Export to csv folder after exporting to CSV

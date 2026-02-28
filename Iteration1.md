## Use Case Diagram
<img width="669" height="681" alt="Image" src="https://github.com/user-attachments/assets/f0a12873-9c0c-4dd9-851a-6b57fd747584" />

## Domain model
<img width="1311" height="704" alt="Image" src="https://github.com/user-attachments/assets/29232909-0a73-4b37-a5b1-d6801a648190" />

## SSDs
**SSD for Create Task**
<img width="893" height="612" alt="image" src="https://github.com/user-attachments/assets/7ab3108a-cd72-4190-84f6-4a610303e1a5" />

**SSD for Update Task**
<img width="879" height="597" alt="image" src="https://github.com/user-attachments/assets/48bcbd98-1607-4e1b-bab8-804ae96a2f32" />

**SSD for View Tasks**
<img width="890" height="723" alt="image" src="https://github.com/user-attachments/assets/5c18f637-03d7-45a0-8ee7-841915c66a4d" />


# Fully-Dressed Use-Case: Create Task

---

## Use Case: Create Task

| Field | Details |
|---|---|
| **Use Case Name** | Create Task |
| **Scope** | Personal Task Management System |
| **Level** | User Goal |
| **Primary Actor** | User |
| **Stakeholders and Interests** | **User** — wants to quickly record a new task with relevant details so it can be tracked and managed over time. |
| **Preconditions** | The system is running and ready to accept input. |
| **Success Guarantee (Post-conditions)** | A new task is persisted in the system with a unique identifier, a creation timestamp, the provided title and optional description, a default status of `open`, and the specified (or default) priority level. A history entry recording the creation action is logged. |
| **Minimal Guarantee** | If the task cannot be created, no partial task record is stored and the system state remains unchanged. |
| **Trigger** | The user initiates the "Create Task" operation. |

---

## Main Success Scenario (Basic Flow)

| Step | Actor / System | Action |
|------|---------------|--------|
| 1 | **User** | Requests to create a new task. |
| 2 | **System** | Prompts the user to enter a task title and (optionally) a description, priority level, and due date. |
| 3 | **User** | Provides a non-empty title (e.g., `"Submit lab report"`), an optional description (`"Chapter 5 lab"`), a priority level (`HIGH`), and an optional due date (`2026-03-10`). |
| 4 | **System** | Validates that the title is non-empty and that the due date (if provided) is a valid calendar date not in the past. |
| 5 | **System** | Creates a new Task record with a unique ID, the provided title, description, priority, due date, status set to `open`, and a creation timestamp set to the current date and time. |
| 6 | **System** | Records a history entry: `"Task created: Submit lab report"` with the current timestamp. |
| 7 | **System** | Displays a confirmation message with the new task's ID and title. |

**Outcome:** The task is successfully created and accessible in the system.

---

## Extensions (Failure and Alternative Flows)

### 3a. User provides an empty title

| Step | Action |
|------|--------|
| 3a.1 | The user submits a blank or whitespace-only title. |
| 3a.2 | The system detects the empty title at validation (step 4). |
| 3a.3 | The system displays an error: `"Error: Task title cannot be empty."` |
| 3a.4 | The system re-prompts the user to enter a valid title (return to step 2). |

**Outcome:** No task is created. The system remains unchanged.

---

### 3b. User provides an invalid due date (past date or malformed)

| Step | Action |
|------|--------|
| 3b.1 | The user enters a due date that is in the past (e.g., `2025-01-01`) or in an unrecognised format (e.g., `"march tenth"`). |
| 3b.2 | The system detects the invalid due date at validation (step 4). |
| 3b.3 | The system displays an error: `"Error: Due date must be a valid future date (YYYY-MM-DD)."` |
| 3b.4 | The system re-prompts the user to enter a valid due date or skip it (return to step 2). |

**Outcome:** No task is created. The system remains unchanged.

---

### 3c. User provides an invalid priority value

| Step | Action |
|------|--------|
| 3c.1 | The user enters a priority value outside the accepted set (e.g., `"URGENT"` instead of `LOW`, `MEDIUM`, or `HIGH`). |
| 3c.2 | The system detects the invalid priority at validation (step 4). |
| 3c.3 | The system displays an error: `"Error: Priority must be one of LOW, MEDIUM, or HIGH."` |
| 3c.4 | The system re-prompts the user (return to step 2). |

**Outcome:** No task is created. The system remains unchanged.

---

### *a. System failure during persistence (at any point)

| Step | Action |
|------|--------|
| *a.1 | An unexpected system or I/O error occurs while saving the task. |
| *a.2 | The system rolls back any partial write, logs the error internally, and notifies the user: `"Error: Task could not be saved. Please try again."` |

**Outcome:** No task record is stored. The system remains in a consistent state.

---

## Special Requirements

- The title must support Unicode characters (multilingual input).
- All timestamps must use the system's local date/time (ISO 8601 format).
- The system must respond within 1 second of the user submitting the create request.

## Technology and Data Variations

| Step | Variation |
|------|-----------|
| 3 | Priority may default to `MEDIUM` if not provided by the user. |
| 3 | Due date is optional and if it is omitted the task has no due date. |

## Frequency of Occurrence

Multiple times per session, however this is the most frequently triggered use-case in the system.

---

# System Sequence Diagram — Create Task

<img width="475" height="488" alt="image" src="https://github.com/user-attachments/assets/32c7224d-e0b5-4c9c-8057-122311e86919" />


*Personal Task Management System — SOEN 342 W2026 — Iteration I*

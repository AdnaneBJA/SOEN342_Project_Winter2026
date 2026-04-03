## Use Case Diagram
<img width="1428" height="1938" alt="Iteration3_Use_Case" src="https://github.com/user-attachments/assets/46700e14-d424-44da-819a-a5e630b5a05b" />

## Formal Requirements in OCL

### A Task Cannot Have More Than 20 Sub-Tasks
context Task
inv MaxSubTasks:
  self.subTasks -> size() <= 20

### The Number of Open Tasks Without a Due Date Should Not Exceed 50
context TaskManagementSystem
inv MaxOpenTasksWithoutDueDate:
  self.tasks -> select(t | t.status = TaskStatus::OPEN and t.dueDate.oclIsUndefined()) -> size() <= 50

### The Limit for Open Tasks for Each Collaborator Category is a Positive Integer
context CollaboratorCategory
inv CategoryLimit:
  self.limit > 0
  and
  (self.type = CategoryType::SENIOR implies self.limit = 2)
  and
  (self.type = CategoryType::INTERMEDIATE implies self.limit = 5)
  and
  (self.type = CategoryType::JUNIOR implies self.limit = 10)
### No Collaborator Must be Overloaded
context Collaborator
inv NotOverloaded:
  self.assignedSubTasks -> select(subTask | subTask.status = TaskStatus::Open) -> size() <= self.category.limit

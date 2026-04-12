## Use Case Diagram
<img width="1428" height="1938" alt="Iteration3_Use_Case" src="https://github.com/user-attachments/assets/46700e14-d424-44da-819a-a5e630b5a05b" />

## Class Diagram
<img width="1097" height="833" alt="image" src="https://github.com/user-attachments/assets/5dfe2c2f-75e1-4735-8622-61598713a3a0" />

## Sequence Diagram — Export Tasks to iCal
<img width="597" height="842" alt="image" src="https://github.com/user-attachments/assets/f7e14691-6fd6-4009-aea1-1326fb63917a" />

## Formal Requirements in OCL

### A Task Cannot Have More Than 20 Sub-Tasks
context Task<br>
inv MaxSubTasks:<br>
  &emsp;self.subTasks -> size() <= 20

### The Number of Open Tasks Without a Due Date Should Not Exceed 50
context TaskManagementSystem<br>
inv MaxOpenTasksWithoutDueDate:<br>
  &emsp;self.tasks -> select(t | t.status = TaskStatus::OPEN and t.dueDate.oclIsUndefined()) -> size() <= 50

### The Limit for Open Tasks for Each Collaborator Category is a Positive Integer
context CollaboratorCategory<br>
inv CategoryLimit:<br>
  &emsp;self.limit > 0<br>
  &emsp;and<br>
  &emsp;(self.type = CategoryType::SENIOR implies self.limit = 2)<br>
  &emsp;and<br>
  &emsp;(self.type = CategoryType::INTERMEDIATE implies self.limit = 5)<br>
  &emsp;and<br>
  &emsp;(self.type = CategoryType::JUNIOR implies self.limit = 10)<br>
### No Collaborator Must be Overloaded
context Collaborator<br>
inv NotOverloaded:<br>
  &emsp;self.assignedSubTasks -> select(subTask | subTask.status = TaskStatus::Open) -> size() <= self.category.limit

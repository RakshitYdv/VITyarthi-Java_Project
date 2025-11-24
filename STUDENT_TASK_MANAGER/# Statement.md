# Student Task Manager – Statement

## Problem Statement
Students often forget deadlines, lose track of assignments, or struggle to manage their academic workload properly. Notes in notebooks or random phone reminders become messy and inconsistent over time. There is a need for a simple, structured, and offline tool that helps students organize their academic tasks in one place and track them easily. :contentReference[oaicite:0]{index=0}

## Scope of the Project
This project implements a Java-based, menu-driven console application that allows students to manage their tasks efficiently. The application focuses on:

- Creating, viewing, updating, and deleting tasks
- Storing tasks persistently using CSV files
- Providing a simple text-based user interface
- Supporting import/export of task data in CSV format
- Allowing users to create timestamped backups of their data

The project is designed as a standalone desktop/console program and does not use any external database or network connectivity. :contentReference[oaicite:1]{index=1}

## Target Users
- College and school students who want a lightweight offline tool to track:
  - Assignments
  - Projects
  - Study tasks
  - Exam preparation plans
- Any user who prefers a minimal console-based task manager without the complexity of full GUI or online tools.

## High-Level Features
- **Add Task** – Create a new task with title, description, due date, and status. :contentReference[oaicite:2]{index=2}  
- **Edit Task** – Update details of an existing task.
- **Delete Task** – Remove tasks that are no longer needed.
- **Mark / Unmark Completion** – Toggle task status between PENDING and COMPLETED.
- **View All Tasks** – List all tasks in a readable format.
- **Import / Export Tasks (CSV)** – Load tasks from CSV files and export current tasks for backup or sharing. :contentReference[oaicite:3]{index=3}  
- **Backup Data** – Create timestamped backup folders to safely store copies of task data.
- **Modular Architecture** – Uses separate packages/classes such as `model`, `dao`, `ui`, and `util` for better organization and maintainability. :contentReference[oaicite:4]{index=4}

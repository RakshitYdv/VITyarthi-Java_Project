package com.taskmgr.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Task {
    private int id;
    private String title;
    private String description;
    private LocalDate due;
    private LocalDateTime createdAt;
    private boolean completed;

    public Task() { this.createdAt = LocalDateTime.now(); }

    public Task(int id, String title, String description, LocalDate due, boolean completed) {
        this.id = id; this.title = title; this.description = description;
        this.due = due; this.completed = completed; this.createdAt = LocalDateTime.now();
    }

    // getters / setters
    public int getId(){ return id; }
    public void setId(int id){ this.id = id; }
    public String getTitle(){ return title; }
    public void setTitle(String title){ this.title = title; }
    public String getDescription(){ return description; }
    public void setDescription(String description){ this.description = description; }
    public LocalDate getDue(){ return due; }
    public void setDue(LocalDate due){ this.due = due; }
    public boolean isCompleted(){ return completed; }
    public void setCompleted(boolean completed){ this.completed = completed; }
    public LocalDateTime getCreatedAt(){ return createdAt; }

    @Override
    public String toString() {
        return String.format("[%d] %s (Due: %s) - %s",
            id, title, due == null ? "No due" : due.toString(), completed ? "COMPLETED" : "PENDING");
    }

    public String toCsvLine() {
        String escTitle = title.replace("\"","\"\"");
        String escDesc = (description==null? "" : description.replace("\"","\"\""));
        return String.format("%d,\"%s\",\"%s\",%s,%s",
            id, escTitle, escDesc, due == null ? "" : due.toString(), completed ? "1" : "0");
    }

    public static Task fromCsvParts(String[] parts) {
        // parts: id,title,description,due,completed
        Task t = new Task();
        try { t.id = Integer.parseInt(parts[0]); } catch(Exception e){ t.id = -1; }
        t.title = parts[1];
        t.description = parts[2];
        t.due = parts[3] == null || parts[3].isEmpty() ? null : LocalDate.parse(parts[3]);
        t.completed = "1".equals(parts[4]) || "true".equalsIgnoreCase(parts[4]);
        return t;
    }
}


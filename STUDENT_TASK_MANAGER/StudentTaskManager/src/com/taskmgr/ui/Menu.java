package com.taskmgr.ui;

import com.taskmgr.dao.TaskDAO;
import com.taskmgr.model.Task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;


public class Menu {
    private final TaskDAO dao = new TaskDAO();
    private final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    public void start() {
        boolean exit = false;
        while(!exit) {
            printMain();
            String ch = scanner.nextLine().trim();
            switch(ch) {
                case "1": manageMenu(); break;
                case "2": listAll(); break;
                case "3": reports(); break;
                case "4": importExportMenu(); break;
                case "5": backup(); break;
                case "0": exit = true; System.out.println("Exiting..."); break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private void printMain() {
        System.out.println();
        System.out.println("==== Student Task Manager ====");
        System.out.println("1. Manage Tasks");
        System.out.println("2. List All Tasks");
        System.out.println("3. Reports");
        System.out.println("4. Import/Export Data");
        System.out.println("5. Backup Data");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }

    private void manageMenu() {
        boolean back = false;
        while(!back) {
            System.out.println("\n-- Manage Tasks --");
            System.out.println("1. Add Task");
            System.out.println("2. Edit Task");
            System.out.println("3. Delete Task");
            System.out.println("4. Mark/Unmark Complete");
            System.out.println("0. Back");
            System.out.print("Choose: ");
            String c = scanner.nextLine().trim();
            switch(c) {
                case "1": addTask(); break;
                case "2": editTask(); break;
                case "3": deleteTask(); break;
                case "4": toggleComplete(); break;
                case "0": back = true; break;
                default: System.out.println("Invalid choice."); break;
            }
        }
    }

    private void addTask() {
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        if(title.isEmpty()) { System.out.println("Title required."); return; }
        System.out.print("Description (optional): ");
        String desc = scanner.nextLine().trim();
        System.out.print("Due date (yyyy-MM-dd) or enter to skip: ");
        String dd = scanner.nextLine().trim();
        LocalDate due = null;
        if(!dd.isEmpty()) {
            try { due = LocalDate.parse(dd, DATE_FMT); } catch(Exception e){ System.out.println("Invalid date. Skipping."); }
        }
        Task t = new Task();
        t.setTitle(title); t.setDescription(desc); t.setDue(due); t.setCompleted(false);
        int id = dao.add(t);
        System.out.println(id > 0 ? "Task added with id " + id : "Failed to add.");
    }

    private void listAll() {
        List<Task> list = dao.getAll();
        if(list.isEmpty()) { System.out.println("No tasks."); return; }
        list.stream().sorted((a,b)-> {
            if(a.getDue()==null && b.getDue()==null) return Integer.compare(a.getId(), b.getId());
            if(a.getDue()==null) return 1;
            if(b.getDue()==null) return -1;
            return a.getDue().compareTo(b.getDue());
        }).forEach(System.out::println);
    }

    private void editTask() {
        System.out.print("Enter id: ");
        int id = readInt();
        dao.getById(id).ifPresentOrElse(t -> {
            System.out.print("New title (enter to keep " + t.getTitle() + "): ");
            String nt = scanner.nextLine().trim(); if(!nt.isEmpty()) t.setTitle(nt);
            System.out.print("New description (enter to keep): ");
            String nd = scanner.nextLine().trim(); if(!nd.isEmpty()) t.setDescription(nd);
            System.out.print("New due date (yyyy-MM-dd) (enter to keep " + (t.getDue()==null?"none":t.getDue()) + "): ");
            String dd = scanner.nextLine().trim();
            if(!dd.isEmpty()) { try { t.setDue(LocalDate.parse(dd, DATE_FMT)); } catch(Exception e){ System.out.println("Invalid date; keeping old."); } }
            boolean ok = dao.update(t);
            System.out.println(ok ? "Updated." : "Update failed.");
        }, () -> System.out.println("Task not found."));
    }

    private void deleteTask() {
        System.out.print("Enter id: ");
        int id = readInt();
        System.out.print("Are you sure? (y/N): ");
        String c = scanner.nextLine().trim();
        if(!c.equalsIgnoreCase("y")) { System.out.println("Cancelled."); return; }
        boolean ok = dao.delete(id);
        System.out.println(ok ? "Deleted." : "Delete failed.");
    }

    private void toggleComplete() {
        System.out.print("Enter id: ");
        int id = readInt();
        dao.getById(id).ifPresentOrElse(t -> {
            t.setCompleted(!t.isCompleted());
            boolean ok = dao.update(t);
            System.out.println(ok ? "Status toggled to " + (t.isCompleted()?"COMPLETED":"PENDING") : "Failed.");
        }, () -> System.out.println("Not found."));
    }

    private void reports() {
        System.out.println("\n-- Reports --");
        System.out.println("Total tasks: " + dao.countAll());
        System.out.println("Completed  : " + dao.countCompleted());
        System.out.println("Pending    : " + dao.countPending());
    }

    private void importExportMenu() {
        System.out.println("\n-- Import/Export --");
        System.out.println("1. Export (export.csv)");
        System.out.println("2. Import (append)");
        System.out.print("Choose: ");
        String c = scanner.nextLine().trim();
        if("1".equals(c)) export();
        else if("2".equals(c)) importCsv();
        else System.out.println("Back/invalid.");
    }

    private void export() {
        List<Task> list = dao.getAll();
        Path out = Paths.get("export.csv");
        try (BufferedWriter bw = Files.newBufferedWriter(out)) {
            bw.write("id,title,description,due,completed"); bw.newLine();
            for(Task t : list) bw.write(t.toCsvLine() + System.lineSeparator());
            System.out.println("Exported to " + out.toString());
        } catch(IOException e) { System.out.println("Export failed: " + e.getMessage()); }
    }

    private void importCsv() {
        System.out.print("Enter filename: ");
        String f = scanner.nextLine().trim();
        if(f.isEmpty()) { System.out.println("Cancelled."); return; }
        Path p = Paths.get(f);
        if(!Files.exists(p)) { System.out.println("File not found."); return; }
        try (BufferedReader br = Files.newBufferedReader(p)) {
            String header = br.readLine();
            List<Task> cur = dao.getAll();
            int maxId = cur.stream().mapToInt(Task::getId).max().orElse(0);
            String line;
            while((line = br.readLine()) != null) {
                String[] parts = com.taskmgr.util.CSVUtils.parseLine(line);
                if(parts.length < 5) continue;
                Task t = Task.fromCsvParts(parts);
                t.setId(++maxId);
                cur.add(t);
            }
            // rewrite
            // use DAO internal method by adding and letting DAO persist all
            // simple way: delete file then re-add
            // but DAO only writes the full list; so we call internal write by re-adding via DAO (bypass: writeAll)
            // Here we write via DAO by updating file with combined list:
            // (the DAO API does not expose writeAll, so just call add for each new item)
            // For simplicity we will write all combined:
            // Reflection avoided; instead create new DAO instance method is simpler, but to keep this short:
            // We'll create a new TaskDAO and use private write via getAll+writeAll; but writeAll is private.
            // Workaround: create temporary file with combined records
            Path tmp = Paths.get("data","tmp_import.csv");
            try (BufferedWriter bw = Files.newBufferedWriter(tmp)) {
                bw.write("id,title,description,due,completed"); bw.newLine();
                for(Task t : cur) bw.write(t.toCsvLine() + System.lineSeparator());
            }
            Files.move(tmp, Paths.get("data","tasks.csv"), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Import complete.");
        } catch(IOException e) { System.out.println("Import failed: " + e.getMessage()); }
    }

    private void backup() {
        String stamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path dest = Paths.get("Backups","Backup_" + stamp);
        try {
            if(!Files.exists(Paths.get("Backups"))) Files.createDirectories(Paths.get("Backups"));
            // copy data dir recursively
            Path dataDir = Paths.get("data");
            if(!Files.exists(dataDir)) Files.createDirectories(dataDir);
            copyDirectory(dataDir, dest);
            long size = folderSize(dest);
            System.out.println("Backup created at: " + dest.toString());
            System.out.println("Backup size: " + size + "bytes");
        } catch(IOException e) {
            System.out.println("Backup failed: " + e.getMessage());
        }
    }

    // --- helpers ---
    private int readInt() {
        try { return Integer.parseInt(scanner.nextLine().trim()); } catch(Exception e) { return -1; }
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(s -> {
            try {
                Path rel = source.relativize(s);
                Path dest = target.resolve(rel);
                if(Files.isDirectory(s)) {
                    if(!Files.exists(dest)) Files.createDirectories(dest);
                } else {
                    Files.copy(s, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch(IOException ex) { throw new RuntimeException(ex); }
        });
    }

    private long folderSize(Path p) throws IOException {
        final long[] size = {0};
        if(!Files.exists(p)) return 0;
        Files.walk(p).filter(Files::isRegularFile).forEach(f -> {
            try { size[0] += Files.size(f); } catch(IOException e) {}
        });
        return size[0];
    }
}


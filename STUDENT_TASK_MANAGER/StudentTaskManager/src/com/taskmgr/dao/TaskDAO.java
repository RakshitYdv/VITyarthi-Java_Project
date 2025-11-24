package com.taskmgr.dao;

import com.taskmgr.model.Task;
import com.taskmgr.util.CSVUtils;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TaskDAO - CSV-backed storage. No external libs.
 * Data file: data/tasks.csv
 */
public class TaskDAO {
    private static final Path DATA_DIR = Paths.get("data");
    private static final Path TASK_FILE = DATA_DIR.resolve("tasks.csv");

    public TaskDAO() {
        try {
            if(!Files.exists(DATA_DIR)) Files.createDirectories(DATA_DIR);
            if(!Files.exists(TASK_FILE)) {
                try (BufferedWriter bw = Files.newBufferedWriter(TASK_FILE)) {
                    bw.write("id,title,description,due,completed");
                    bw.newLine();
                }
            }
        } catch(IOException e) { throw new RuntimeException("Failed to init data dir: " + e.getMessage(), e); }
    }

    public synchronized List<Task> getAll() {
        List<Task> list = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(TASK_FILE)) {
            String header = br.readLine(); // skip
            String line;
            while((line = br.readLine()) != null) {
                String[] parts = CSVUtils.parseLine(line);
                if(parts.length < 5) continue;
                Task t = Task.fromCsvParts(parts);
                list.add(t);
            }
        } catch(IOException e) { System.err.println("Read error: " + e.getMessage()); }
        return list;
    }

    public synchronized Optional<Task> getById(int id) {
        return getAll().stream().filter(t -> t.getId() == id).findFirst();
    }

    public synchronized int add(Task t) {
        List<Task> list = getAll();
        int next = list.stream().mapToInt(Task::getId).max().orElse(0) + 1;
        t.setId(next);
        list.add(t);
        return writeAll(list) ? next : -1;
    }

    public synchronized boolean update(Task t) {
        List<Task> list = getAll();
        boolean found = false;
        for(int i=0;i<list.size();i++) {
            if(list.get(i).getId() == t.getId()) { list.set(i, t); found = true; break; }
        }
        if(!found) return false;
        return writeAll(list);
    }

    public synchronized boolean delete(int id) {
        List<Task> list = getAll();
        boolean removed = list.removeIf(t -> t.getId() == id);
        if(!removed) return false;
        return writeAll(list);
    }

    private boolean writeAll(List<Task> list) {
        try (BufferedWriter bw = Files.newBufferedWriter(TASK_FILE)) {
            bw.write("id,title,description,due,completed");
            bw.newLine();
            for(Task t : list) bw.write(t.toCsvLine() + System.lineSeparator());
            return true;
        } catch(IOException e) {
            System.err.println("Write error: " + e.getMessage());
            return false;
        }
    }

    // helpers for reports
    public int countAll() { return getAll().size(); }
    public int countCompleted() { return (int)getAll().stream().filter(Task::isCompleted).count(); }
    public int countPending() { return countAll() - countCompleted(); }

    public List<Task> getUpcoming(int days) {
        LocalDate now = LocalDate.now();
        return getAll().stream()
            .filter(t -> t.getDue() != null)
            .filter(t -> !t.getDue().isBefore(now) && !t.getDue().isAfter(now.plusDays(days)))
            .sorted(Comparator.comparing((Task x) -> x.getDue()))
            .collect(Collectors.toList());
    }
}

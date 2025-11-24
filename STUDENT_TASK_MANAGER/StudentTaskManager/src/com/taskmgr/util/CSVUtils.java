package com.taskmgr.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal CSV parser/writer that handles quoted fields and escaped quotes.
 * Not fully RFC-complete but sufficient for our simple CSV format.
 */
public class CSVUtils {
    public static String[] parseLine(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i+1 < line.length() && line.charAt(i+1) == '"') {
                    // escaped quote
                    cur.append('"'); i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                parts.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        parts.add(cur.toString());
        return parts.toArray(new String[0]);
    }
}

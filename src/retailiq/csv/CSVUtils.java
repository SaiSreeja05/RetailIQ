package retailiq.csv;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CSVUtils {

    /**
     * Parses a single CSV line into a list of field values, handling RFC 4180 quotes and commas.
     */
    public static List<String> parseLine(String line) {
        List<String> result = new ArrayList<>();
        if (line == null) {
            return result;
        }

        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    // Escaped quote: "" -> "
                    sb.append('\"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                result.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(ch);
            }
        }

        result.add(sb.toString().trim());
        return result;
    }

    /**
     * Formats an array of field values into an RFC 4180 compliant CSV line.
     */
    public static String formatLine(Object... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            String field = fields[i] != null ? fields[i].toString() : "";
            boolean containsSpecial = field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r");
            if (containsSpecial) {
                sb.append('\"');
                sb.append(field.replace("\"", "\"\""));
                sb.append('\"');
            } else {
                sb.append(field);
            }
        }
        return sb.toString();
    }

    /**
     * Reads all lines from a CSV file. Returns empty list if file doesn't exist.
     */
    public static List<String> readLines(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }

    /**
     * Writes lines to a CSV file safely using UTF-8.
     */
    public static void writeLines(String filePath, List<String> lines) throws IOException {
        Path path = Paths.get(filePath);
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    public static int parseInt(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static double parseDouble(String value, double defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}

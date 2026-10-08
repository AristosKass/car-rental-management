package api.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * <h2>CSV Utility</h2>
 * Μικρός CSV reader/writer χωρίς εξωτερικές βιβλιοθήκες.
 * <p>
 * Υποστηρίζει:
 * <ul>
 *   <li>διαχωρισμό με κόμμα,</li>
 *   <li>quoted πεδία με ",</li>
 *   <li>escape διπλού quote ως "" μέσα σε quoted πεδίο.</li>
 * </ul>
 * <p>
 * Για τις ανάγκες της εργασίας είναι αρκετό. Δεν καλύπτει όλες τις edge cases ενός πλήρους CSV parser.
 */
public final class CsvUtil {

    private CsvUtil() {
        // utility class
    }

    /**
     * Διαβάζει CSV από αρχείο και επιστρέφει τις γραμμές ως λίστα πεδίων.
     * Η header γραμμή περιλαμβάνεται στο index 0.
     *
     * @param file csv file (UTF-8)
     * @return rows
     * @throws IOException αν αποτύχει η ανάγνωση
     */
    public static List<List<String>> readAll(File file) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        if (!file.exists()) return rows;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                // Στα παρεχόμενα αρχεία μπορεί να υπάρχουν γραμμές "..." (placeholder). Τις αγνοούμε.
                if (line.startsWith("...")) continue;
                rows.add(parseCsvLine(line));
            }
        }
        return rows;
    }

    /**
     * Διαβάζει CSV από {@link Reader}. Χρησιμοποιείται όταν έχουμε ήδη ανοίξει stream (π.χ. σε repositories).
     * Η header γραμμή περιλαμβάνεται στο index 0.
     *
     * @param reader reader
     * @return rows ως {@code String[]} (ένα array ανά γραμμή)
     * @throws IOException αν αποτύχει η ανάγνωση
     */
    public static List<String[]> readAll(Reader reader) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.startsWith("...")) continue;
                List<String> parsed = parseCsvLine(line);
                rows.add(parsed.toArray(new String[0]));
            }
        }
        return rows;
    }

    /**
     * Γράφει rows σε CSV αρχείο (overwrite).
     *
     * @param file target file
     * @param rows rows
     * @throws IOException αν αποτύχει η εγγραφή
     */
    public static void writeAll(File file, List<List<String>> rows) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {
            for (List<String> row : rows) {
                bw.write(toCsvLine(row));
                bw.newLine();
            }
        }
    }

    /**
     * Μετατρέπει μία γραμμή CSV σε λίστα πεδίων.
     */
    private static List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++; // skip escaped quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                out.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString().trim());
        return out;
    }

    /**
     * Μετατρέπει row σε CSV line.
     */
    private static String toCsvLine(List<String> row) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(escape(row.get(i)));
        }
        return sb.toString();
    }

    /**
     * Μετατρέπει row (array) σε CSV line.
     *
     * @param row πεδία
     * @return μία CSV γραμμή χωρίς newline
     */
    public static String toLine(String[] row) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escape(row[i]));
        }
        return sb.toString();
    }

    /**
     * Escape κανόνες:
     * <ul>
     *   <li>αν το πεδίο περιέχει κόμμα/quote/newline, τότε μπαίνει σε quotes</li>
     *   <li>εσωτερικά quotes γίνονται ""</li>
     * </ul>
     */
    private static String escape(String v) {
        if (v == null) return "";
        boolean mustQuote = v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r");
        String s = v.replace("\"", "\"\"");
        return mustQuote ? ("\"" + s + "\"") : s;
    }
}

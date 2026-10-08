package api.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * <h2>SeedInitializer</h2>
 * Αρχικοποιεί τον φάκελο {@code data/} στην 1η εκτέλεση.
 * <p>
 * Αν τα αρχεία δεν υπάρχουν, αντιγράφει «seed» δεδομένα από {@code src/main/resources/seed/...}
 * και δημιουργεί τα απαραίτητα κενά αρχεία (π.χ. rentals.csv).
 * <p>
 * Σημαντικό: δεν κάνει overwrite σε υφιστάμενα δεδομένα, ώστε να μην χαθούν αλλαγές χρήστη.
 */
public final class SeedInitializer {

    private SeedInitializer() {
        // utility class
    }

    /**
     * Εξασφαλίζει ότι υπάρχουν όλα τα απαραίτητα αρχεία δεδομένων.
     *
     * @throws IOException αν αποτύχει αντιγραφή/εγγραφή αρχείου
     */
    public static void ensureInitialized() throws IOException {
        if (!AppFiles.dataDir().exists()) {
            AppFiles.dataDir().mkdirs();
        }

        // Users
        if (!AppFiles.employeesFile().exists()) {
            copyResourceToFile("/seed/users.csv", AppFiles.employeesFile());
        }

        // Vehicles
        if (!AppFiles.carsFile().exists()) {
            copyResourceToFile("/seed/vehicles_with_plates.csv", AppFiles.carsFile());
        }

        // Customers - δημιουργούμε 2 default πελάτες (όπως ζητά η εκφώνηση: τουλάχιστον 2 χωρίς ενοικιάσεις)
        if (!AppFiles.customersFile().exists()) {
            String header = "ΑΦΜ,ονοματεπώνυμο,τηλέφωνο,email\n";
            String c1 = "123456789,Γιώργος Παπαδόπουλος,6900000001,g.papadopoulos@test.com\n";
            String c2 = "987654321,Μαρία Ιωάννου,6900000002,m.ioannou@test.com\n";
            writeText(AppFiles.customersFile(), header + c1 + c2);
        }

        // Rentals - ξεκινά άδειο
        if (!AppFiles.rentalsFile().exists()) {
            String header = "rentalId,carId,customerAfm,startDate,endDate,returned,returnDate,employeeUsername\n";
            writeText(AppFiles.rentalsFile(), header);
        }
    }

    /**
     * Αντιγράφει resource από το classpath στο αντίστοιχο αρχείο στον δίσκο.
     */
    private static void copyResourceToFile(String resourcePath, File outFile) throws IOException {
        try (InputStream in = SeedInitializer.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new FileNotFoundException("Resource not found: " + resourcePath);
            }
            File parent = outFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            try (OutputStream out = new FileOutputStream(outFile)) {
                byte[] buffer = new byte[8192];
                int r;
                while ((r = in.read(buffer)) != -1) {
                    out.write(buffer, 0, r);
                }
            }
        }
    }

    /**
     * Γράφει text σε αρχείο με UTF-8 encoding (overwrite).
     */
    private static void writeText(File file, String text) throws IOException {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8)) {
            w.write(text);
        }
    }
}

package api.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * <h2>Κεντρικοποίηση Paths Αρχείων</h2>
 * Όλες οι τοποθεσίες αρχείων δεδομένων (CSV) ορίζονται σε ένα σημείο,
 * ώστε η εφαρμογή να τρέχει σε οποιονδήποτε υπολογιστή χωρίς hard-coded absolute paths.
 * <p>
 * Όλα τα αρχεία βρίσκονται στον σχετικό φάκελο {@code data/} δίπλα στο project.
 */
public final class AppFiles {

    private AppFiles() {
        // utility class
    }

    private static final Path DATA_DIR = Paths.get("data");

    /**
     * @return ο φάκελος δεδομένων ({@code data/})
     */
    public static File dataDir() {
        return DATA_DIR.toFile();
    }

    /**
     * @return αρχείο χρηστών/υπαλλήλων ({@code data/users.csv})
     */
    public static File employeesFile() {
        return DATA_DIR.resolve("users.csv").toFile();
    }

    /**
     * @return αρχείο οχημάτων ({@code data/vehicles.csv})
     */
    public static File carsFile() {
        return DATA_DIR.resolve("vehicles.csv").toFile();
    }

    /**
     * @return αρχείο πελατών ({@code data/customers.csv})
     */
    public static File customersFile() {
        return DATA_DIR.resolve("customers.csv").toFile();
    }

    /**
     * @return αρχείο ενοικιάσεων ({@code data/rentals.csv})
     */
    public static File rentalsFile() {
        return DATA_DIR.resolve("rentals.csv").toFile();
    }
}

package api.repo;

import api.model.Car;
import api.model.CarStatus;
import api.util.AppFiles;
import api.util.CsvUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * <h2>CarRepository</h2>
 * Repository για αποθήκευση/ανάκτηση {@link Car}.
 * <p>
 * <b>Persistence:</b> αποθηκεύει σε CSV (UTF-8) στο αρχείο {@code data/vehicles.csv}.
 * Η πρώτη εκτέλεση αρχικοποιεί το αρχείο μέσω {@link api.util.SeedInitializer}.
 * <p>
 * <b>CSV format (header):</b>
 * <pre>
 * id,πινακίδα,μάρκα,τύπος,μοντέλο,έτος,χρώμα,κατάσταση
 * </pre>
 * όπου η «κατάσταση» είναι το ελληνικό label του {@link CarStatus}.
 * <p>
 * Το repository κρατάει τα δεδομένα σε μνήμη (list) και στις αλλαγές γράφει ξανά όλο το CSV.
 * Για το μέγεθος της εργασίας αυτό είναι επαρκές και απλό.
 */
public class CarRepository {

    /**
     * Δημιουργεί repository και φορτώνει από το CSV.
     *
      *  RuntimeException αν αποτύχει η ανάγνωση αρχείου
     */
    public CarRepository() {
        try {
            load();
        } catch (IOException e) {
            throw new RuntimeException("Σφάλμα ανάγνωσης αρχείου οχημάτων: " + e.getMessage(), e);
        }
    }

    /** In-memory λίστα οχημάτων. */
    private final List<Car> cars = new ArrayList<>();

    /**
     * Φορτώνει όλα τα οχήματα από CSV στη μνήμη.
     *
     * @throws IOException αν αποτύχει η ανάγνωση αρχείου
     */
    public void load() throws IOException {
        cars.clear();
        File file = AppFiles.carsFile();
        if (!file.exists()) return;

        try (Reader r = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            List<String[]> rows = CsvUtil.readAll(r);
            // Expect header row at index 0
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length < 8) continue;

                int id = Integer.parseInt(row[0].trim());
                String plate = row[1].trim();
                String brand = row[2].trim();
                String type = row[3].trim();
                String model = row[4].trim();
                int year = Integer.parseInt(row[5].trim());
                String color = row[6].trim();
                CarStatus status = CarStatus.fromCsv(row[7]);

                cars.add(new Car(id, plate, brand, type, model, year, color, status));
            }
        }
    }

    /**
     * Αποθηκεύει την τρέχουσα in-memory λίστα στο CSV.
     *
     * @throws IOException αν αποτύχει η εγγραφή αρχείου
     */
    public void save() throws IOException {
        File file = AppFiles.carsFile();
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8)) {
            // Header όπως δίνεται στο seed αρχείο
            w.write("id,πινακίδα,μάρκα,τύπος,μοντέλο,έτος,χρώμα,κατάσταση\n");
            for (Car c : cars) {
                String[] row = new String[]{
                        String.valueOf(c.getId()),
                        c.getPlate(),
                        c.getBrand(),
                        c.getType(),
                        c.getModel(),
                        String.valueOf(c.getYear()),
                        c.getColor(),
                        c.getStatus().getGreekLabel()
                };
                w.write(CsvUtil.toLine(row));
                w.write("\n");
            }
        }
    }

    /**
     * @return αντίγραφο λίστας όλων των οχημάτων
     */
    public List<Car> findAll() {
        return new ArrayList<>(cars);
    }

    /**
     * @param id id οχήματος
     * @return το όχημα ή {@code null} αν δεν βρέθηκε
     */
    public Car findById(int id) {
        for (Car c : cars) {
            if (c.getId() == id) return c;
        }
        return null;
    }

    /**
     * @param plate πινακίδα
     * @return το όχημα ή {@code null} αν δεν βρέθηκε
     */
    public Car findByPlate(String plate) {
        if (plate == null) return null;
        for (Car c : cars) {
            if (plate.equalsIgnoreCase(c.getPlate())) return c;
        }
        return null;
    }

    /**
     * Προσθέτει νέο όχημα (στη μνήμη).
     *
     * @param car όχημα
     */
    public void add(Car car) {
        cars.add(car);
    }

    /**
     * Αντικαθιστά υπάρχον όχημα (ίδιο id) με την νέα τιμή.
     *
     * @param car ενημερωμένο όχημα
     */
    public void update(Car car) {
        for (int i = 0; i < cars.size(); i++) {
            if (cars.get(i).getId() == car.getId()) {
                cars.set(i, car);
                return;
            }
        }
        // If not found, append (defensive)
        cars.add(car);
    }
}

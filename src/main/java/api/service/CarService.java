package api.service;

import api.exception.ValidationException;
import api.model.Car;
import api.model.CarStatus;
import api.repo.CarRepository;
import api.util.StringUtil;
import api.util.Validators;

import java.util.ArrayList;
import java.util.List;

/**
 * <h2>CarService</h2>
 * Επιχειρησιακές λειτουργίες για τα οχήματα.
 * <p>
 * Κάνει validation (π.χ. μοναδικότητα πινακίδας) και εκθέτει μεθόδους αναζήτησης.
 */
public class CarService {

    private final CarRepository carRepository;

    /**
     * @param carRepository repo οχημάτων
     */
    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    /**
     * @return όλα τα οχήματα (αντίγραφο λίστας)
     */
    public List<Car> findAll() {
        return carRepository.findAll();
    }

    /**
     * Προσθέτει νέο όχημα.
     * <p>
     * Κανόνες:
     * <ul>
     *   <li>id > 0 και μοναδικό</li>
     *   <li>plate μη-κενό και μοναδικό</li>
     *   <li>year «λογικό» ({@link Validators#requireYearReasonable(int)})</li>
     * </ul>
     *
     * @param car όχημα
     * @throws ValidationException αν παραβιαστούν κανόνες
     */
    public void add(Car car) {
        validateCar(car);
        if (carRepository.findById(car.getId()) != null) {
            throw new ValidationException("Υπάρχει ήδη όχημα με αυτό το id.");
        }
        if (carRepository.findByPlate(car.getPlate()) != null) {
            throw new ValidationException("Υπάρχει ήδη όχημα με αυτή την πινακίδα.");
        }
        carRepository.add(car);
        trySave();
    }

    /**
     * Ενημερώνει υπάρχον όχημα (με βάση το id).
     *
     * @param car ενημερωμένο όχημα
     * @throws ValidationException αν δεν υπάρχει ή αν παραβιαστούν κανόνες
     */
    public void update(Car car) {
        validateCar(car);
        Car existing = carRepository.findById(car.getId());
        if (existing == null) {
            throw new ValidationException("Δεν βρέθηκε όχημα με id=" + car.getId());
        }
        // Unique plate (επιτρέπεται αν είναι η ίδια πινακίδα στο ίδιο record)
        Car byPlate = carRepository.findByPlate(car.getPlate());
        if (byPlate != null && byPlate.getId() != car.getId()) {
            throw new ValidationException("Η πινακίδα χρησιμοποιείται από άλλο όχημα.");
        }
        carRepository.update(car);
        trySave();
    }

    /**
     * @param id id οχήματος
     * @return όχημα ή {@code null}
     */
    public Car findById(int id) {
        return carRepository.findById(id);
    }

    /**
     * @param plate πινακίδα
     * @return όχημα ή {@code null}
     */
    public Car findByPlate(String plate) {
        return carRepository.findByPlate(plate);
    }

    /**
     * Αναζήτηση με πολλαπλά κριτήρια (οποιοσδήποτε συνδυασμός).
     * <p>
     * Κενά κριτήρια αγνοούνται.
     *
     * @param brand        κριτήριο μάρκας
     * @param plate        κριτήριο πινακίδας
     * @param model        κριτήριο μοντέλου
     * @param color        κριτήριο χρώματος
     * @param statusOrNull αν δοθεί, φιλτράρει κατά κατάσταση
     * @return λίστα αποτελεσμάτων
     */
    public List<Car> search(String brand, String plate, String model, String color, CarStatus statusOrNull) {
        List<Car> out = new ArrayList<>();
        for (Car c : carRepository.findAll()) {
            if (!matches(StringUtil.nvl(brand), c.getBrand())) continue;
            if (!matches(StringUtil.nvl(plate), c.getPlate())) continue;
            if (!matches(StringUtil.nvl(model), c.getModel())) continue;
            if (!matches(StringUtil.nvl(color), c.getColor())) continue;
            if (statusOrNull != null && c.getStatus() != statusOrNull) continue;
            out.add(c);
        }
        return out;
    }

    /**
     * @return λίστα μόνο με διαθέσιμα οχήματα
     */
    public List<Car> availableCars() {
        List<Car> out = new ArrayList<>();
        for (Car c : carRepository.findAll()) {
            if (c.getStatus() == CarStatus.AVAILABLE) out.add(c);
        }
        return out;
    }

    private void validateCar(Car car) {
        if (car == null) throw new ValidationException("Κενό όχημα.");
        Validators.requirePositive(car.getId(), "Το id πρέπει να είναι θετικό.");
        Validators.requireNotBlank(car.getPlate(), "Η πινακίδα είναι υποχρεωτική.");
        Validators.requireNotBlank(car.getBrand(), "Η μάρκα είναι υποχρεωτική.");
        Validators.requireNotBlank(car.getType(), "Ο τύπος είναι υποχρεωτικός.");
        Validators.requireNotBlank(car.getModel(), "Το μοντέλο είναι υποχρεωτικό.");
        Validators.requireYearReasonable(car.getYear());
        Validators.requireNotBlank(car.getColor(), "Το χρώμα είναι υποχρεωτικό.");
        if (car.getStatus() == null) throw new ValidationException("Η κατάσταση οχήματος είναι υποχρεωτική.");
    }

    private boolean matches(String crit, String value) {
        if (StringUtil.isBlank(crit)) return true;
        return StringUtil.nvl(value).toLowerCase().contains(crit.toLowerCase());
    }

    /**
     * Τα repositories διατηρούν in-memory λίστα και χρειάζονται save() για persistence.
     * Για να μην «σπάσουμε» τον χειρισμό IOException προς GUI, κάνουμε wrap σε RuntimeException εδώ.
     */
    private void trySave() {
        try {
            carRepository.save();
        } catch (Exception e) {
            throw new RuntimeException("Σφάλμα αποθήκευσης οχημάτων: " + e.getMessage(), e);
        }
    }
}

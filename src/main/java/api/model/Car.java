package api.model;

/**
 * <h2>Car (Όχημα)</h2>
 * Οντότητα οχήματος του στόλου.
 * <p>
 * Κύρια αναγνωριστικά:
 * <ul>
 *   <li>{@link #id}: μοναδικός ακέραιος κωδικός</li>
 *   <li>{@link #plate}: μοναδική πινακίδα</li>
 * </ul>
 */
public class Car {

    /** Μοναδικός κωδικός οχήματος. */
    private int id;

    /** Μοναδική πινακίδα. */
    private String plate;

    /** Μάρκα (π.χ. Toyota). */
    private String brand;

    /** Τύπος (π.χ. Sedan). */
    private String type;

    /** Μοντέλο (π.χ. Corolla). */
    private String model;

    /** Έτος κυκλοφορίας. */
    private int year;

    /** Χρώμα. */
    private String color;

    /** Κατάσταση διαθεσιμότητας. */
    private CarStatus status;

    /**
     * @param id     μοναδικός κωδικός
     * @param plate  πινακίδα (μοναδική)
     * @param brand  μάρκα
     * @param type   τύπος
     * @param model  μοντέλο
     * @param year   έτος
     * @param color  χρώμα
     * @param status κατάσταση
     */
    public Car(int id, String plate, String brand, String type, String model, int year, String color, CarStatus status) {
        this.id = id;
        this.plate = plate;
        this.brand = brand;
        this.type = type;
        this.model = model;
        this.year = year;
        this.color = color;
        this.status = status;
    }

    /**
     * @return μοναδικό id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id μοναδικό id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return πινακίδα
     */
    public String getPlate() {
        return plate;
    }

    /**
     * @param plate πινακίδα
     */
    public void setPlate(String plate) {
        this.plate = plate;
    }

    /**
     * @return μάρκα
     */
    public String getBrand() {
        return brand;
    }

    /**
     * @param brand μάρκα
     */
    public void setBrand(String brand) {
        this.brand = brand;
    }

    /**
     * @return τύπος
     */
    public String getType() {
        return type;
    }

    /**
     * @param type τύπος
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return μοντέλο
     */
    public String getModel() {
        return model;
    }

    /**
     * @param model μοντέλο
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * @return έτος
     */
    public int getYear() {
        return year;
    }

    /**
     * @param year έτος
     */
    public void setYear(int year) {
        this.year = year;
    }

    /**
     * @return χρώμα
     */
    public String getColor() {
        return color;
    }

    /**
     * @param color χρώμα
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * @return κατάσταση
     */
    public CarStatus getStatus() {
        return status;
    }

    /**
     * @param status κατάσταση
     */
    public void setStatus(CarStatus status) {
        this.status = status;
    }
}

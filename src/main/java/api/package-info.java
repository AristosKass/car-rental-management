/**
 * <h2>API (Business Layer)</h2>
 * Περιέχει το «επιχειρησιακό» κομμάτι της εφαρμογής (μοντέλα, services, repositories, utilities)
 * χωρίς καμία εξάρτηση από Swing/GUI.
 * <p>
 * Η σχεδίαση ακολουθεί ένα απλό layering:
 * <ul>
 *   <li>{@code api.model}: οντότητες (Car, Customer, Employee, Rental).</li>
 *   <li>{@code api.repo}: αποθήκευση/ανάκτηση δεδομένων από αρχεία CSV.</li>
 *   <li>{@code api.service}: κανόνες και λειτουργίες (validation, δημιουργία ενοικίασης κ.λπ.).</li>
 *   <li>{@code api.util}: βοηθητικές κλάσεις (CSV parsing, validation helpers, paths, αρχικοποίηση δεδομένων).</li>
 * </ul>
 * <p>
 * Ο {@link api.AppContext} λειτουργεί ως απλό «container» εξαρτήσεων:
 * κατασκευάζει repositories/services και κάνει την πρώτη αρχικοποίηση του φακέλου δεδομένων.
 */
package api;

/**
 * <h2>Repositories (Persistence)</h2>
 * Αποτελούν το επίπεδο αποθήκευσης της εφαρμογής.
 * <p>
 * Κάθε repository διατηρεί μια in-memory λίστα (π.χ. {@code List<Car>}) και συγχρονίζει
 * τις αλλαγές σε αντίστοιχο αρχείο CSV μέσα στον φάκελο {@code data/}.
 * <p>
 * Σημαντικό: τα repositories δεν γνωρίζουν τίποτα για GUI. Επιστρέφουν/δέχονται
 * μοντέλα από το {@code api.model}.
 */
package api.repo;

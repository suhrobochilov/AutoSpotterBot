package car.autoSpotterBot.exception;

public class AdNotFoundException extends RuntimeException {

    public AdNotFoundException(Long id) {
        super("Ad with ID " + id + " not found.");
    }

    // Optional: Weitere Konstruktoren oder Methoden, je nach Bedarf
}

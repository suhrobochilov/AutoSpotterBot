package car.autoSpotterBot.exception;

public class StadtNotFoundException extends RuntimeException {

    public StadtNotFoundException(Long id) {
        super("Stadt with ID " + id + " not found.");
    }

    // Optional: Weitere Konstruktoren oder Methoden, je nach Bedarf
}


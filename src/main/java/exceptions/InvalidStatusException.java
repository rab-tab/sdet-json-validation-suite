package exceptions;

public class InvalidStatusException extends OrderValidationException {
    public InvalidStatusException(String orderId, String status) {
        super("Invalid status for order " + orderId + ": " + status);
    }
}

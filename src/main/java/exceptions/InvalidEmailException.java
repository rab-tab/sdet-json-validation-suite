package exceptions;

public class InvalidEmailException extends OrderValidationException {
    public InvalidEmailException(String orderId, String email) {
        super( orderId + ": " + email);
    }

    public InvalidEmailException(String orderId) {
        super(orderId);
    }
}

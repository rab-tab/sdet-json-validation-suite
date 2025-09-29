package exceptions;

public class MissingLinesException extends OrderValidationException {
    public MissingLinesException(String orderId) {
        super("No line items for order: " + orderId);
    }
}
package exceptions;

public class NegativePriceException extends OrderValidationException {
    public NegativePriceException(String orderId, String sku, double price) {
        super("Negative price for order " + orderId + ", SKU " + sku + ": " + price);
    }
}

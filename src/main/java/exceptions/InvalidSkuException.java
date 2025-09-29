package exceptions;

public class InvalidSkuException extends OrderValidationException {
    public InvalidSkuException(String orderId, String sku) {
        super("Invalid SKU for order " + orderId + ": " + sku);
    }
}

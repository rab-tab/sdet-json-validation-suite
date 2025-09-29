import base.BaseTest;
import com.jayway.jsonpath.JsonPath;
import dataProvider.CachedOrderProvider.CachedOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;

import static config.TestConfig.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FormatValidationTest extends BaseTest {
    /**
     * Order identity
     **/

    @ParameterizedTest(name = "Order ID: {0}")
    @MethodSource("dataProvider.CachedOrderProvider#ordersProvider")
    @DisplayName("Validate each order has a non-empty ID and a valid status")
    void validateOrderIdentity(CachedOrder order) {
        String orderId = JsonPath.read(order.json, "$.id");
        String status = JsonPath.read(order.json, "$.status");

        assertTrue(orderId != null && !orderId.isEmpty(), "Order ID missing for order: " + orderId);
        assertTrue(VALID_STATUS.contains(status), "Invalid status for order: " + orderId + " -> " + status);
    }

    /**
     * Validate Customer email
     **/

    @ParameterizedTest(name = "Order ID: {0}")
    @MethodSource("dataProvider.CachedOrderProvider#ordersProvider")
    @DisplayName("Validate email exists for customer and is valid")
    @Tag("expected-failure")
    void emailValidationTest(CachedOrder order) {
        String orderId = JsonPath.read(order.json, "$.id");
        Object emailObj = JsonPath.using(CONF).parse(order.json).read("$.customer.email");
        if (emailObj == null)
            assertTrue(false, "Missing email in order " + orderId);

        String email = emailObj.toString();
        assertTrue(email.matches(EMAIL_REGEX),
                "Invalid email in order " + orderId + ": " + email);

    }


    /**
     * Validate Lines integrity
     **/

    @ParameterizedTest(name = "Order ID: {0}")
    @MethodSource("dataProvider.CachedOrderProvider#ordersProvider")
    @DisplayName("Validate line array is non empty if status is PAID | PENDING")
    @Tag("expected-failure")
    void validateLines(CachedOrder order) {
        String orderId = JsonPath.read(order.json, "$.id");
        String status = JsonPath.read(order.json, "$.status");

        if (status.equals("PAID") || status.equals("PENDING")) {
            List<Object> lines = JsonPath.read(order.json, "$.lines[*]");
            assertTrue(lines != null && !lines.isEmpty(), "Lines cannot be empty for order: " + orderId);

            lines.forEach(lineObj -> {
                Map<String, Object> lineMap = (Map<String, Object>) lineObj;
                String sku = (String) lineMap.get("sku");
                Number qty = (Number) lineMap.get("qty");
                Number price = (Number) lineMap.get("price");

                assertTrue(sku != null && !sku.isEmpty(), "Invalid SKU in order: " + orderId);
                assertTrue(qty != null && qty.intValue() > 0, "Non-positive qty in order: " + orderId + ", SKU: " + sku);
                assertTrue(price != null && price.doubleValue() >= 0, "Negative price in order: " + orderId + ", SKU: " + sku);
            });
        }
    }

    /**
     * Validate Payment / Refund consistency
     **/

    @ParameterizedTest(name = "Order ID: {0}")
    @MethodSource("dataProvider.CachedOrderProvider#ordersProvider")
    @DisplayName("Validate Payment / Refund consistency")
    void validatePaymentRefund(CachedOrder order) {
        String orderId = JsonPath.read(order.json, "$.id");
        String status = JsonPath.read(order.json, "$.status");
        Boolean captured = JsonPath.read(order.json, "$.payment.captured");

        if ("PAID".equals(status)) {
            assertTrue(Boolean.TRUE.equals(captured), "Payment not captured for PAID order: " + orderId);
        }

        if ("CANCELLED".equals(status)) {
            List<Object> lines = JsonPath.read(order.json, "$.lines[*]");
            if (lines != null && !lines.isEmpty()) {
                double lineTotal = lines.stream()
                        .mapToDouble(lineObj -> {
                            Number qty = JsonPath.read(lineObj, "$.qty");
                            Number price = JsonPath.read(lineObj, "$.price");
                            return (qty != null ? qty.doubleValue() : 0.0) * (price != null ? price.doubleValue() : 0.0);
                        })
                        .sum();
                Number refundAmountObj = JsonPath.read(order.json, "$.refund.amount");
                double refundAmount = refundAmountObj != null ? refundAmountObj.doubleValue() : 0.0;
                assertTrue(Double.compare(lineTotal, refundAmount) == 0,
                        "Refund mismatch for CANCELLED order: " + orderId);
            }
        }
    }

    /**
     * Validate Shipping fees
     **/

    @ParameterizedTest(name = "Order ID: {0}")
    @MethodSource("dataProvider.CachedOrderProvider#ordersProvider")
    @DisplayName("Validate Shipping fee for all orders")
    void validateShippingFee(CachedOrder order) {
        String orderId = JsonPath.read(order.json, "$.id");
        Number fee = JsonPath.read(order.json, "$.shipping.fee");
        assertTrue(fee != null && fee.doubleValue() >= 0, "Negative shipping fee in order: " + orderId);
    }
}


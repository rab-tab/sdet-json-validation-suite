import com.fasterxml.jackson.databind.JsonNode;
import dataProvider.CachedOrderProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import util.OrderUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class ReportingTest extends BaseTest {

    @Test
    @DisplayName("Summary of all orders")
    void testReportingSummary() {
        List<JsonNode> orders = CachedOrderProvider.ordersProvider()
                .map(c -> c.node)
                .collect(Collectors.toList());

        int totalOrders = orders.size();
        int totalLineItems = OrderUtil.countTotalLines();

        List<String> problematicOrders = new ArrayList<>();


        for (JsonNode order : orders) {
            String orderId = order.path("id").asText();

            // Empty lines
            if (!order.has("lines") || order.path("lines").isEmpty()) {
                problematicOrders.add(orderId + ": empty lines");
            }

            // Invalid email
            List<String> invalidEmailOrders = OrderUtil.getInvalidEmailOrderIds();
            if (invalidEmailOrders.contains(orderId)) {
                problematicOrders.add(orderId + ": invalid email");
            }

            // Non-positive qty/price
            for (JsonNode item : order.path("lines")) {
                int qty = item.path("qty").asInt(0);
                double price = item.path("price").asDouble(0.0);
                if (qty <= 0) {
                    problematicOrders.add(orderId + ": non-positive qty (SKU " + item.path("sku").asText() + ")");
                }
                if (price < 0) {
                    problematicOrders.add(orderId + ": negative price (SKU " + item.path("sku").asText() + ")");
                }
            }
        }

        int invalidOrders = problematicOrders.size();

        String problematicOrdersStr = String.join("\n", problematicOrders);
        String summary = String.format(
                "Total orders: %d%n" +
                        "Total line items: %d%n" +
                        "Invalid orders: %d%n" +
                        "Problematic orders: -----%n%s%n",
                totalOrders,
                totalLineItems,
                invalidOrders,
                problematicOrdersStr
        );

        System.out.printf("*************** Reporting Summary *****************%n" + summary);

        assertTrue(summary.length() > 0);
    }
}

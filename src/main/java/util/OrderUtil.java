package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;
import config.TestConfig;
import dataProvider.CachedOrderProvider;
import dataProvider.OrderDataProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static config.TestConfig.CONF;

public class OrderUtil {
    /**
     * Returns list of all Order IDs
     */
    public static List<String> loadOrderIds() {
        return OrderDataProvider.loadOrders()
                .map(orderNode -> JsonPath.read(orderNode.toString(), "$.id").toString())
                .collect(Collectors.toList());
    }

    /**
     * Returns total lines count
     */
    public static int countTotalLines() {
        int totalLines = 0;

        for (JsonNode orderNode : OrderDataProvider.loadOrders().toArray(JsonNode[]::new)) {
            List<Object> lines = JsonPath.read(orderNode.toString(), "$.lines[*]");
            if (lines != null) {
                totalLines += lines.size();
            }
        }

        return totalLines;
    }

    /**
     * Computes total quantity per SKU across all cached orders.
     */
    public static Map<String, Integer> getTotalQuantityPerSku() {
        List<CachedOrderProvider.CachedOrder> orders = CachedOrderProvider.ordersProvider()
                .collect(Collectors.toList());
        Map<String, Integer> skuTotals = new HashMap<>();

        for (CachedOrderProvider.CachedOrder order : orders) {
            List<Object> lines = JsonPath.using(CONF).parse(order.json).read("$.lines[*]");
            if (lines != null) {
                for (Object lineObj : lines) {
                    String sku = JsonPath.read(lineObj, "$.sku").toString();
                    Integer qty = Integer.parseInt(JsonPath.read(lineObj, "$.qty").toString());
                    if (qty != null && qty > 0) {
                        skuTotals.put(sku, skuTotals.getOrDefault(sku, 0) + qty);
                    }
                }
            }
        }
        return skuTotals;
    }

    /**
     * Returns the top N SKUs by total quantity.
     */
    public static List<Map.Entry<String, Integer>> getTopSkusByQuantity(int topN) {
        Map<String, Integer> skuTotals = getTotalQuantityPerSku();
        return skuTotals.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * Returns list of order IDs with missing or invalid emails
     */
    public static List<String> getInvalidEmailOrderIds() {
        return CachedOrderProvider.ordersProvider()
                .map(c -> c.node)
                .filter(order -> {
                    String email = order.path("customer").path("email").asText("");
                    return email.isEmpty() || !email.matches(TestConfig.EMAIL_REGEX);
                })
                .map(order -> order.path("id").asText())
                .collect(Collectors.toList());
    }

    /**
     * Returns IDs of PAID orders with payment.captured = false
     */
    public static List<String> getPaidOrdersNotCaptured() {
        return CachedOrderProvider.ordersProvider()
                .map(c -> c.node)
                .filter(order -> "PAID".equalsIgnoreCase(order.path("status").asText()))
                .filter(order -> !order.path("payment").path("captured").asBoolean(true))
                .map(order -> order.path("id").asText())
                .collect(Collectors.toList());
    }

    /**
     * Returns IDs of CANCELLED orders where refund.amount does NOT equal
     * the sum of line totals (qty × price). Shipping fee is ignored.
     */
    public static List<String> getCancelledOrdersWithIncorrectRefund() {
        return CachedOrderProvider.ordersProvider()
                .map(c -> c.node)
                .filter(order -> "CANCELLED".equalsIgnoreCase(order.path("status").asText()))
                .filter(order -> {
                    double expectedRefund = 0.0;
                    for (JsonNode line : order.path("lines")) {
                        int qty = line.path("qty").asInt(0);
                        double price = line.path("price").asDouble(0.0);
                        expectedRefund += qty * price;
                    }
                    double actualRefund = order.path("refund").path("amount").asDouble(0.0);
                    return Double.compare(expectedRefund, actualRefund) != 0;
                })
                .map(order -> order.path("id").asText())
                .collect(Collectors.toList());
    }

    /**
     * Compute GMV (Σ qty × price, no discounts/shipping) for a single order.
     */
    public static double computeGMV(JsonNode order) {
        double total = 0.0;
        for (JsonNode line : order.path("lines")) {
            int qty = line.path("qty").asInt(0);
            double price = line.path("price").asDouble(0.0);
            total += qty * price;
        }
        return total;
    }

    /**
     * Compute GMV for all orders
     */
    public static Map<String, Double> computeGMVPerOrder() {
        return CachedOrderProvider.ordersProvider()
                .collect(Collectors.toMap(
                        c -> c.node.path("id").asText(),
                        c -> computeGMV(c.node)
                ));
    }

    /**
     * Returns IDs of orders with invalid GMV
     **/
    public static List<String> getInvalidGmvOrders() {
        return computeGMVPerOrder().entrySet().stream()
                .filter(e -> e.getValue() < 0.0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}

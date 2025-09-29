import base.BaseTest;
import config.TestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import util.OrderUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ExtractionAggregationTest extends BaseTest {


    /**
     * List of all order IDs
     **/

    @Test
    @DisplayName("Validate Order ID's against expected values")
    void testAllOrderIdsMatchExpectedSequence() {
        List<String> actualOrderIds = OrderUtil.loadOrderIds();
        assertEquals(TestConfig.EXPECTED_ORDER_IDS, actualOrderIds,
                "Order IDs do not match expected sequence");
    }


    /**
     * Count of total line items across all orders
     **/

    @Test
    @Tag("expected-failure")
    @DisplayName("Count of total line items across all orders")
    void testTotalLineItems() {
        int totalLines = OrderUtil.countTotalLines();
        assertEquals(totalLines, TestConfig.EXPECTED_LINE_COUNT);
    }

    /**
     * Top 2 SKUs by total quantity
     **/

    @Test
    void testTopSkusByQuantity() {

        List<Map.Entry<String, Integer>> topSkus = OrderUtil.getTopSkusByQuantity(2);

        // Verify top 2 SKUs and quantities
        assertEquals("PEN-RED", topSkus.get(0).getKey());
        assertEquals(5, topSkus.get(0).getValue().intValue());

        assertEquals("USB-32GB", topSkus.get(1).getKey());
        assertEquals(2, topSkus.get(1).getValue().intValue());
    }


    /**
     * GMV per order (Σ qty×price, before discounts/shipping)
     **/


    @Test
    void testGMVPerOrderExactValues() {
        Map<String, Double> gmvByOrder = OrderUtil.computeGMVPerOrder();

        assertEquals(70.0, gmvByOrder.get("A-1001"));
        assertEquals(0.0, gmvByOrder.get("A-1002"));
        assertEquals(-15.0, gmvByOrder.get("A-1003"));
        assertEquals(16.0, gmvByOrder.get("A-1004"));
        assertEquals(55.0, gmvByOrder.get("A-1005"));

        List<String> invalidOrders = OrderUtil.getInvalidGmvOrders();
        assertEquals(Collections.singletonList("A-1003"), invalidOrders,
                "Expected A-1003 to be flagged for negative GMV");
    }

    /**
     * Orders missing or invalid emails
     **/

    @Test
    @DisplayName("Orders missing or invalid emails ")
    void testOrdersMissingOrInvalidEmails() {
        List<String> invalidEmailOrders = OrderUtil.getInvalidEmailOrderIds();
        assertEquals(Arrays.asList("A-1002", "A-1003"), invalidEmailOrders);
    }


    /**
     * Paid orders with payment.captured = false
     **/

    @Test
    @DisplayName("Paid orders with payment.captured = false")
    void testPaidOrdersWithPaymentNotCaptured() {
        List<String> problematicOrders = OrderUtil.getPaidOrdersNotCaptured();

        assertTrue(problematicOrders.isEmpty(),
                "Unexpected paid orders with payment.captured=false: " + problematicOrders);
    }


    /**
     * Cancelled orders with correct refund amount
     **/

    @Test
    @Tag("expected-failure")
    @DisplayName("Cancelled orders with correct refund amount")
    void testCancelledOrdersWithCorrectRefund() {
        List<String> problematicOrders = OrderUtil.getCancelledOrdersWithIncorrectRefund();

        assertEquals(
                Arrays.asList("A-1004"),
                problematicOrders,
                "Refund amount mismatch for cancelled orders"
        );
    }
}

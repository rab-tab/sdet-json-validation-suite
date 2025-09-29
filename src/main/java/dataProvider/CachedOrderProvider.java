package dataProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CachedOrderProvider {
    public static class CachedOrder {
        public final JsonNode node;
        public final String json;

        public CachedOrder(JsonNode node) {
            this.node = node;
            this.json = node.toString();
        }

        @Override
        public String toString() {
            return JsonPath.read(json, "$.id");
        }
    }

    private static final List<CachedOrder> cachedOrders = OrderDataProvider.loadOrders()
            .map(CachedOrder::new)
            .collect(Collectors.toList());

    public static Stream<CachedOrder> ordersProvider() {
        return cachedOrders.stream();
    }
}

package dataProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class OrderDataProvider {
    private static JsonNode cachedRoot;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static Map<String, Object> cache;

    private static void loadJson() {
        if (cachedRoot == null) {
            try (InputStream input = OrderDataProvider.class
                    .getClassLoader()
                    .getResourceAsStream("orders.json")) {
                cachedRoot = MAPPER.readTree(input);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load orders.json", e);
            }
        }
    }

    public static Stream<JsonNode> loadOrders() {
        loadJson();
        return StreamSupport.stream(cachedRoot.get("orders").spliterator(), false);
    }
}

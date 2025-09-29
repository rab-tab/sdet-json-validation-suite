package config;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;

import java.util.Arrays;
import java.util.List;

public class TestConfig {
    public static final String ORDER_JSON_FILE = "/orders.json";
    public static final String ORDER_SCHEMA_FILE = "/schemas/order-schema.json";

    public static final String EMAIL_REGEX = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
    public static final List<String> VALID_STATUS = Arrays.asList("PAID", "PENDING", "CANCELLED");
    public static final List<String> EXPECTED_ORDER_IDS = Arrays.asList("A-1001", "A-1002", "A-1003", "A-1004", "A-1005");
    public static final Configuration CONF = Configuration.defaultConfiguration().addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);
    public static final int EXPECTED_LINE_COUNT = 8;

}

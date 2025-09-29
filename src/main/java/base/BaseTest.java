package base;

import config.TestConfig;
import util.SchemaValidator;

public abstract class BaseTest {
    static {
        SchemaValidator.validateJson(
                TestConfig.ORDER_JSON_FILE,
                TestConfig.ORDER_SCHEMA_FILE
        );
    }
}
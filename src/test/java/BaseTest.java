import config.TestConfig;

import org.junit.jupiter.api.extension.ExtendWith;
import util.SchemaValidator;

public abstract class BaseTest {
    static {
        SchemaValidator.validateJson(
                TestConfig.ORDER_JSON_FILE,
                TestConfig.ORDER_SCHEMA_FILE
        );
    }
}
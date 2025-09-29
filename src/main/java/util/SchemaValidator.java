package util;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;

public class SchemaValidator {
    private SchemaValidator() {
    }

    public static void validateJson(String jsonFile, String schemaFile) {
        try (InputStream schemaStream = SchemaValidator.class.getResourceAsStream(schemaFile);
             InputStream jsonStream = SchemaValidator.class.getResourceAsStream(jsonFile)) {

            if (schemaStream == null) {
                throw new RuntimeException("Schema file not found: " + schemaFile);
            }
            if (jsonStream == null) {
                throw new RuntimeException("JSON file not found: " + jsonFile);
            }

            JSONObject rawSchema = new JSONObject(new JSONTokener(schemaStream));

            Schema schema = SchemaLoader.load(rawSchema);

            JSONObject jsonSubject = new JSONObject(new JSONTokener(jsonStream));
            schema.validate(jsonSubject);

        } catch (Exception e) {
            throw new RuntimeException("JSON schema validation failed for " + jsonFile, e);
        }
    }
}

package info.magnolia.parser;

import java.util.List;
import java.util.Map;

import info.magnolia.parser.JsonParser.ValueFactory;

public class JsonDTOs {
    interface JsonValue {}
    public record JsonString(String value) implements JsonValue {}
    public record JsonNumber(int value) implements JsonValue {}
    public record JsonBool(boolean value) implements JsonValue {}
    public record JsonNull() implements JsonValue {}
    public record JsonObject(Map<String, JsonValue> value) implements JsonValue {}
    public record JsonArray(List<JsonValue> value) implements JsonValue {}

    public static class JsonValueFactory implements ValueFactory<JsonValue> {
        public JsonValue newJsonNull() { return new JsonNull(); }
        public JsonValue newJsonBool(boolean value) { return new JsonBool(value); }
        public JsonValue newJsonNumber(int value) { return new JsonNumber(value); }
        public JsonValue newJsonString(String value) { return new JsonString(value); }
        public JsonValue newJsonArray(List<JsonValue> values) { return new JsonArray(values); }
        public JsonValue newJsonObject(Map<String, JsonValue> values) { return new JsonObject(values); }
    }

    public static final JsonValueFactory JSON_VALUE_FACTORY = new JsonValueFactory();

}

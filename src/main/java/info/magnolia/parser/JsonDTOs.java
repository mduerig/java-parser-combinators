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

    public static final ValueFactory<JsonValue> JSON_VALUE_FACTORY = new ValueFactory<>(
        JsonNull::new, JsonBool::new, JsonNumber::new, JsonString::new, JsonArray::new, JsonObject::new);
}

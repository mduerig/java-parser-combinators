package info.magnolia.parser;

import static info.magnolia.parser.Parsers.delimited;
import static info.magnolia.parser.Parsers.integer;
import static info.magnolia.parser.Parsers.literal;
import static info.magnolia.parser.Parsers.string;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;

public class JsonParser {
    interface JsonValue {}

    public static record JsonString(String value) implements JsonValue {}

    public static record JsonNumber(int value) implements JsonValue {}

    public static record JsonBool(boolean value) implements JsonValue {}

    public static record JsonNull() implements JsonValue {}

    public static record JsonObject(Map<String, JsonValue> value) implements JsonValue {}

    public static record JsonArray(List<JsonValue> value) implements JsonValue {}

    public static Parser<JsonValue> jsonNull() {
        return
            literal("null")
                .result(new JsonNull());
    }

    public static Parser<JsonValue> jsonBool() {
        return
            literal("true")
                    .result(true)
                .orElse(() ->
            literal("false")
                    .result(false))
                .map(JsonBool::new);
    }

    public static Parser<JsonValue> jsonNumber() {
        return
            integer()
                .map(JsonNumber::new);
    }

    public static Parser<JsonValue> jsonString() {
        return
            string()
                .map(JsonString::new);
    }

    public static Parser<JsonValue> jsonArray() {
        return
            literal("[")
                .then(
            delimited(jsonValue(), literal(",")))
                .andThen(values ->
            literal("]")
                .result(values.collect(toList())))
                    .map(JsonArray::new);
    }

    public static Parser<JsonValue> jsonObject() {
        record KeyValue(String key, JsonValue value) {}

        var binding =
            string()
                .andThen(key ->
            literal("=")
                .then(
            jsonValue())
                .map(value ->
            new KeyValue(key, value)));

        return
            literal("{")
                .then(
            delimited(binding, literal(",")))
                .andThen(bindings ->
            literal("}")
                .result(bindings.collect(toMap(KeyValue::key, KeyValue::value))))
                    .map(JsonObject::new);
    }

    public static Parser<JsonValue> jsonValue() {
        return
            jsonNull()
                .orElse(() ->
            jsonString()
                .orElse(() ->
            jsonNull()
                .orElse(() ->
            jsonNumber()
                .orElse(() ->
            jsonString()
                .orElse(() ->
            jsonArray()
                .orElse(() ->
            jsonObject()))))));
    }

}

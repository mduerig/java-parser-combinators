package info.magnolia.parser;

import static info.magnolia.parser.Parser.result;
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

    public record JsonString(String value) implements JsonValue {}

    public record JsonNumber(int value) implements JsonValue {}

    public record JsonBool(boolean value) implements JsonValue {}

    public record JsonNull() implements JsonValue {}

    public record JsonObject(Map<String, JsonValue> value) implements JsonValue {}

    public record JsonArray(List<JsonValue> value) implements JsonValue {}

    interface JsonValueFactory {
        default JsonValue newJsonNull() { return new JsonNull(); }
        default JsonValue newJsonBool(boolean value) { return new JsonBool(value); }
        default JsonValue newJsonNumber(int value) { return new JsonNumber(value); }
        default JsonValue newJsonString(String value) { return new JsonString(value); }
        default JsonValue newJsonArray(List<JsonValue> values) { return new JsonArray(values); }
        default JsonValue newJsonObject(Map<String, JsonValue> values) { return new JsonObject(values); }
    }

    public static final JsonValueFactory JSON_VALUE_FACTORY = new JsonValueFactory() {};

    public static Parser<JsonValue> jsonNull(JsonValueFactory factory) {
        return
            literal("null")
                .ignore(
            result(
                factory.newJsonNull()));
    }

    public static Parser<JsonValue> jsonBool(JsonValueFactory factory) {
        var parseTrue =
            literal("true")
                .ignore(
            result(
                true));

        var parseFalse =
            literal("false")
                .ignore(
            result(
                false));

        return
            parseTrue
                .orElse(() ->
            parseFalse)
                .map(factory::newJsonBool);
    }

    public static Parser<JsonValue> jsonNumber(JsonValueFactory factory) {
        return
            integer()
                .map(factory::newJsonNumber);
    }

    public static Parser<JsonValue> jsonString(JsonValueFactory factory) {
        return
            string()
                .map(factory::newJsonString);
    }

    public static Parser<JsonValue> jsonArray(JsonValueFactory factory) {
        return
            literal("[")
                .ignore(
            delimited(jsonValue(factory), literal(",")))
                .read(values ->
            literal("]")
                .ignore(
            result(
                values.collect(toList())))
                    .map(factory::newJsonArray));
    }

    public static Parser<JsonValue> jsonObject(JsonValueFactory factory) {
        record Member(String key, JsonValue value) {}

        var member =
            string()
                .read(key ->
            literal(":")
                .ignore(
            jsonValue(factory))
                .read(value ->
            result(
                new Member(key, value))));

        return
            literal("{")
                .ignore(
            delimited(member, literal(",")))
                .read(members ->
            literal("}")
                .ignore(
            result(
                members.collect(toMap(Member::key, Member::value))))
                    .map(factory::newJsonObject));
    }

    public static Parser<JsonValue> jsonValue(JsonValueFactory factory) {
        return
            jsonNull(factory)
                .orElse(() ->
            jsonString(factory)
                .orElse(() ->
            jsonNumber(factory)
                .orElse(() ->
            jsonBool(factory)
                .orElse(() ->
            jsonArray(factory)
                .orElse(() ->
            jsonObject(factory))))));
    }

}

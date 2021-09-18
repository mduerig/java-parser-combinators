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

    public static record JsonString(String value) implements JsonValue {}

    public static record JsonNumber(int value) implements JsonValue {}

    public static record JsonBool(boolean value) implements JsonValue {}

    public static record JsonNull() implements JsonValue {}

    public static record JsonObject(Map<String, JsonValue> value) implements JsonValue {}

    public static record JsonArray(List<JsonValue> value) implements JsonValue {}

    public static Parser<JsonValue> jsonNull() {
        return
            literal("null")
                .andThen(
            result(
                new JsonNull()));
    }

    public static Parser<JsonValue> jsonBool() {
        var parseTrue =
            literal("true")
                .andThen(
            result(
                true));

        var parseFalse =
            literal("false")
                .andThen(
            result(
                false));

        return
            parseTrue
                .orElse(() ->
            parseFalse)
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
                .andThen(
            delimited(jsonValue(), literal(",")))
                .read(values ->
            literal("]")
                .andThen(
            result(
                values.collect(toList())))
                    .map(JsonArray::new));
    }

    public static Parser<JsonValue> jsonObject() {
        record Member(String key, JsonValue value) {}

        var member =
            string()
                .read(key ->
            literal(":")
                .andThen(
            jsonValue())
                .read(value ->
            result(
                new Member(key, value))));

        return
            literal("{")
                .andThen(
            delimited(member, literal(",")))
                .read(members ->
            literal("}")
                .andThen(
            result(
                members.collect(toMap(Member::key, Member::value))))
                    .map(JsonObject::new));
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

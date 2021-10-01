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
                .ignore(
            result(
                new JsonNull()));
    }

    public static Parser<JsonValue> jsonBool() {
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
                .ignore(
            delimited(jsonValue(), literal(",")))
                .read(values ->
            literal("]")
                .ignore(
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
                .ignore(
            jsonValue())
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
                    .map(JsonObject::new));
    }

    public static Parser<JsonValue> jsonValue() {
        return
            jsonNull()
                .orElse(() ->
            jsonString()
                .orElse(() ->
            jsonNumber()
                .orElse(() ->
            jsonBool()
                .orElse(() ->
            jsonArray()
                .orElse(() ->
            jsonObject())))));
    }

}

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
import java.util.function.Function;
import java.util.function.Supplier;

public class JsonParser {

    interface ValueFactory<JSON> {
        JSON newJsonNull();
        JSON newJsonBool(boolean value);
        JSON newJsonNumber(int value);
        JSON newJsonString(String value);
        JSON newJsonArray(List<JSON> values);
        JSON newJsonObject(Map<String, JSON> values);
    }

    public static <JSON> ValueFactory<JSON> newValueFactory(
        Supplier<JSON> jsonNullFactory,
        Function<Boolean, JSON> jsonBoolFactory,
        Function<Integer, JSON> jsonNumberFactory,
        Function<String, JSON> jsonStringFactory,
        Function<List<JSON>, JSON> jsonArrayFactory,
        Function<Map<String, JSON>, JSON> jsonObjectFactory
    ) {
        return new ValueFactory<>() {
            @Override
            public JSON newJsonNull() { return jsonNullFactory.get(); }

            @Override
            public JSON newJsonBool(boolean value) { return jsonBoolFactory.apply(value); }

            @Override
            public JSON newJsonNumber(int value) { return jsonNumberFactory.apply(value); }

            @Override
            public JSON newJsonString(String value) { return jsonStringFactory.apply(value); }

            @Override
            public JSON newJsonArray(List<JSON> values) { return jsonArrayFactory.apply(values); }

            @Override
            public JSON newJsonObject(Map<String, JSON> values) { return jsonObjectFactory.apply(values); }
        };
    }

    public static <JSON> Parser<JSON> jsonNull(ValueFactory<JSON> factory) {
        return
            literal("null")
                .ignore(
            result(
                factory.newJsonNull()));
    }

    public static <JSON> Parser<JSON> jsonBool(ValueFactory<JSON> factory) {
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

    public static <JSON> Parser<JSON> jsonNumber(ValueFactory<JSON> factory) {
        return
            integer()
                .map(factory::newJsonNumber);
    }

    public static <JSON> Parser<JSON> jsonString(ValueFactory<JSON> factory) {
        return
            string()
                .map(factory::newJsonString);
    }

    public static <JSON> Parser<JSON> jsonArray(ValueFactory<JSON> factory) {
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

    public static <JSON> Parser<JSON> jsonObject(ValueFactory<JSON> factory) {
        record Member<JSON>(String key, JSON value) {}

        var member =
            string()
                .read(key ->
            literal(":")
                .ignore(
            jsonValue(factory))
                .read(value ->
            result(
                new Member<>(key, value))));

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

    public static <JSON> Parser<JSON> jsonValue(ValueFactory<JSON> factory) {
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

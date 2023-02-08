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

    record ValueFactory<JSON>(
        Supplier<JSON> newNull,
        Function<Boolean, JSON> newBool,
        Function<Integer, JSON> newNumber,
        Function<String, JSON> newString,
        Function<List<JSON>, JSON> newArray,
        Function<Map<String, JSON>, JSON> newObject) {}

    public static <JSON> Parser<JSON> jsonNull(ValueFactory<JSON> factory) {
        return
            literal("null")
                .ignore(
            result(
                factory.newNull.get()));
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
                .map(factory.newBool);
    }

    public static <JSON> Parser<JSON> jsonNumber(ValueFactory<JSON> factory) {
        return
            integer()
                .map(factory.newNumber);
    }

    public static <JSON> Parser<JSON> jsonString(ValueFactory<JSON> factory) {
        return
            string()
                .map(factory.newString);
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
                    .map(factory.newArray));
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
                    .map(factory.newObject));
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

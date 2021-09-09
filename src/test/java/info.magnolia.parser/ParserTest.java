package info.magnolia.parser;

import static info.magnolia.parser.JsonParser.jsonArray;
import static info.magnolia.parser.JsonParser.jsonBool;
import static info.magnolia.parser.JsonParser.jsonNull;
import static info.magnolia.parser.JsonParser.jsonNumber;
import static info.magnolia.parser.JsonParser.jsonObject;
import static info.magnolia.parser.JsonParser.jsonString;
import static info.magnolia.parser.Parser.success;
import static info.magnolia.parser.Parsers.anyChar;
import static info.magnolia.parser.Parsers.character;
import static info.magnolia.parser.Parsers.chars;
import static info.magnolia.parser.Parsers.delimited;
import static info.magnolia.parser.Parsers.digit;
import static info.magnolia.parser.Parsers.integer;
import static info.magnolia.parser.Parsers.literal;
import static info.magnolia.parser.Parsers.string;
import static info.magnolia.parser.Parsers.sumOfDigits;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import info.magnolia.parser.JsonParser.JsonArray;
import info.magnolia.parser.JsonParser.JsonBool;
import info.magnolia.parser.JsonParser.JsonNull;
import info.magnolia.parser.JsonParser.JsonNumber;
import info.magnolia.parser.JsonParser.JsonObject;
import info.magnolia.parser.JsonParser.JsonString;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class ParserTest {

    @Test
    public void charParser() {
        var result = anyChar().parse("ax");
        assertEquals(success('a', "x"), result);
    }

    @Test
    public void digitParser() {
        var result = digit().parse("5x");
        assertEquals(success('5', "x"), result);
    }

    @Test
    public void sumParser() {
        var result = sumOfDigits().parse("5+7x");
        assertEquals(success(12, "x"), result);
    }

    @Test
    public void andThen() {
        var parser =
            character('a')
                .andThen(c1 ->
            character('b')
                .map(c2 -> c1.toString() + c2.toString()));

        var result = parser.parse("abx");
        assertEquals(success("ab", "x"), result);
    }

    @Test
    public void orElse() {
        var parser =
            character('a')
                .orElse(() ->
            character('b'));

        var result1 = parser.parse("ax");
        assertEquals(success('a', "x"), result1);
        var result2 = parser.parse("bx");
        assertEquals(success('b', "x"), result2);
    }

    @Test
    public void many() {
        var parser = character('a').many();

        var result1 = parser.parse("x")
                .map(ParserTest::toList);
        assertEquals(success(List.of(), "x"), result1);

        var result2 = parser.parse("aaax")
                .map(ParserTest::toList);
        assertEquals(success(List.of('a', 'a', 'a'), "x"), result2);
    }

    @Test
    public void some() {
        var parser = character('a').some();

        var result1 = parser.parse("x");
        assertFalse(result1.isSuccess());

        var result2 = parser.parse("aaax")
                .map(ParserTest::toList);
        assertEquals(success(List.of('a', 'a', 'a'), "x"), result2);
    }

    @Test
    public void literalParser() {
        var parser = literal("abcd");

        var result = parser.parse("abcdx");
        assertEquals(success("abcd", "x"), result);
    }

    @Test
    public void integerParser() {
        var parser = integer();

        var result = parser.parse("1234x");
        assertEquals(success(1234, "x"), result);
    }

    @Test
    public void charsParser() {
        var parser = chars(Character::isAlphabetic);

        var result = parser.parse("abc0");
        assertEquals(success("abc", "0"), result);
    }

    @Test
    public void stringParser() {
        var parser = string();

        var result = parser.parse("\"abc\"x");
        assertEquals(success("abc", "x"), result);
    }

    @Test
    public void delimitedParser() {
        var parser = delimited(literal("a"), literal(","));

        var result1 = parser.parse("x")
                .map(ParserTest::toList);
        assertEquals(success(emptyList(), "x"), result1);

        var result2 = parser.parse("ax")
                .map(ParserTest::toList);
        assertEquals(success(List.of("a"), "x"), result2);

        var result3 = parser.parse("a,ax")
                .map(ParserTest::toList);
        assertEquals(success(List.of("a", "a"), "x"), result3);
    }

    @Test
    public void jsonNullParser() {
        assertEquals(success(new JsonNull(), "x"), jsonNull().parse("nullx"));
    }

    @Test
    public void jsonBoolParser() {
        assertEquals(success(new JsonBool(true), "x"), jsonBool().parse("truex"));
        assertEquals(success(new JsonBool(false), "x"), jsonBool().parse("falsex"));
    }

    @Test
    public void jsonNumberParser() {
        assertEquals(success(new JsonNumber(1234), "x"), jsonNumber().parse("1234x"));
    }

    @Test
    public void jsonStringParser() {
        assertEquals(success(new JsonString("foobar"), "x"), jsonString().parse("\"foobar\"x"));
    }

    @Test
    public void jsonArrayParser() {
        assertEquals(success(new JsonArray(List.of()), "x"), jsonArray().parse("[]x"));
        assertEquals(success(new JsonArray(List.of(new JsonNumber(1), new JsonNumber(2), new JsonNumber(3))), "x"), jsonArray().parse("[1,2,3]x"));
    }

    @Test
    public void jsonObjectParser() {
        assertEquals(success(new JsonObject(Map.of()), "x"), jsonObject().parse("{}x"));
        assertEquals(
            success(
                new JsonObject(Map.of(
                    "int", new JsonNumber(5),
                    "string", new JsonString("foo"),
                    "emptyArray", new JsonArray(emptyList()),
                    "object", new JsonObject(Map.of(
                            "array", new JsonArray(List.of(
                                new JsonNumber(1),
                                new JsonNumber(2),
                                new JsonNumber(3))))))),
                "x"),
            jsonObject().parse("{\"int\"=5,\"string\"=\"foo\",\"emptyArray\"=[],\"object\"={\"array\"=[1,2,3]}}x"));
    }

    private static <T> List<T> toList(Stream<T> stream) {
        return stream.collect(Collectors.toList());
    }

}

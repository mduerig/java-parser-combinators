package info.magnolia.parser;

import static info.magnolia.parser.Parser.success;
import static info.magnolia.parser.Parsers.anyChar;
import static info.magnolia.parser.Parsers.character;
import static info.magnolia.parser.Parsers.chars;
import static info.magnolia.parser.Parsers.digit;
import static info.magnolia.parser.Parsers.integer;
import static info.magnolia.parser.Parsers.literal;
import static info.magnolia.parser.Parsers.separated;
import static info.magnolia.parser.Parsers.string;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

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
                .orElse(
            character('b'));

        var result1 = parser.parse("ax");
        assertEquals(success('a', "x"), result1);
        var result2 = parser.parse("bx");
        assertEquals(success('b', "x"), result2);
    }

    @Test
    public void many() {
        var parser = character('a').many();

        var result1 = parser.parse("x");
        assertEquals(success(List.of(), "x"), result1);

        var result2 = parser.parse("aaax");
        assertEquals(success(List.of('a', 'a', 'a'), "x"), result2);
    }

    @Test
    public void some() {
        var parser = character('a').some();

        var result1 = parser.parse("x");
        assertFalse(result1.isSuccess());

        var result2 = parser.parse("aaax");
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
    public void separatedParser() {
        var parser = separated(literal("a"), literal(","));

        var result1 = parser.parse("x");
        assertEquals(success(emptyList(), "x"), result1);

        var result2 = parser.parse("ax");
        assertEquals(success(List.of("a"), "x"), result2);

        var result3 = parser.parse("a,ax");
        assertEquals(success(List.of("a", "a"), "x"), result3);
    }


}

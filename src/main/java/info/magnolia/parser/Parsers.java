package info.magnolia.parser;

import static info.magnolia.parser.Parser.failure;
import static info.magnolia.parser.Parser.result;
import static info.magnolia.parser.Parser.success;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.empty;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class Parsers {

    public static Parser<Character> character() {
        return input -> input.isEmpty()
           ? failure("Expected Character, found EOF", "")
           : success(input.charAt(0), input.substring(1));
    }

    public static Parser<Character> character(Predicate<Character> predicate) {
        return character()
            .filter(predicate);
    }

    public static Parser<Character> character(Character character) {
        return character(c -> c == character);
    }

    public static Parser<Integer> digit() {
        return character(Character::isDigit)
            .map(c -> c - '0');
    }

    public record Coordinate(Character c, Integer i) {}

    public static Parser<Coordinate> coordinate() {
        return
            character()
                .read(c ->
            digit()
                .read(d ->
            result(
                 new Coordinate(c, d))));
    }

    public static Parser<String> literal(String literal) {
        var letterParsers = literal.chars()
            .mapToObj(c ->
                character((char) c)
                    .map(Object::toString));

        return letterParsers.reduce(
            result(""),
            (parser1, parser2) ->
                parser1
                    .read(letter1 ->
                parser2.
                    read(letter2 ->
                result(
                    letter1 + letter2))));
    }

    public static Parser<Integer> integer() {
        return digit()
            .some()
            .map(digits ->
                digits.reduce(0, (n, d) -> n * 10 + d));
    }

    public static Parser<String> chars(Predicate<Character> predicate) {
        return character(predicate)
            .map(Objects::toString)
            .many()
            .map(strings -> strings.collect(joining()));
    }

    public static Parser<String> string() {
        return
            character('"')
                .andThen(
            chars(c -> c != '"')
                .read(s ->
            character('"')
                .andThen(
            result(
                s))));
    }

    public static <R, S> Parser<Stream<R>> delimited(Parser<R> parser, Parser<S> deliminator) {
        var nonEmpty =
            parser
                .read(result ->
            deliminator.andThen(parser).many()
                .read(results ->
            result(
                concat(Stream.of(result), results))));
        return
            nonEmpty
                .orElse(() ->
            result(
                empty()));
    }

}

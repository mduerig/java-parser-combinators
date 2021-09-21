package info.magnolia.parser;

import static info.magnolia.parser.Parser.failure;
import static info.magnolia.parser.Parser.none;
import static info.magnolia.parser.Parser.result;
import static info.magnolia.parser.Parser.success;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Stream.concat;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
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

    // h,4
    public static Parser<Coordinate> coordinate() {
        return
            character()
                .read(c ->
            character(',')
                .ignore(
            digit()
                .read(d ->
            result(
                 new Coordinate(c, d)))));
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
        // Left associative reduction aka foldLeft. See https://bugs.openjdk.java.net/browse/JDK-8133680
        Collector<Integer, ?, Integer> toInteger = collectingAndThen(
            reducing(
                Function.<Integer>identity(),
                d -> n -> n * 10 + d,
                Function::andThen),
            endo -> endo.apply(0));

        return digit()
            .some()
            .map(digits ->
                digits.collect(toInteger));
                // digits.reduce(0, (n, d) -> n * 10 + d));  not associative -> fails on parallel streams
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
                .ignore(
            chars(c -> c != '"')
                .read(s ->
            character('"')
                .ignore(
            result(
                s))));
    }

    public static <R, S> Parser<Stream<R>> delimited(Parser<R> parser, Parser<S> deliminator) {
        var more =
            deliminator.ignore(parser)
                .many();

        var some =
            parser
                .read(result ->
            more
                .read(results ->
            result(
                concat(Stream.of(result), results))));
        return
            some
                .orElse(() ->
            none());
    }

}

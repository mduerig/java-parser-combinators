package info.magnolia.parser;

import static info.magnolia.parser.Parser.failure;
import static info.magnolia.parser.Parser.success;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class Parsers {

    public static <R> Parser<R> constant(R result) {
        return s -> success(result, s);
    }

    public static Parser<Character> anyChar() {
        return s -> s.isEmpty()
           ? failure("Expected Character, found EOF", "")
           : success(s.charAt(0), s.substring(1));
    }

    public static Parser<Character> character(Predicate<Character> predicate) {
        return anyChar().filter(predicate);
    }

    public static Parser<Character> character(Character character) {
        return character(c -> c == character);
    }

    public static Parser<Character> digit() {
        return character(Character::isDigit);
    }

    public static Parser<String> literal(String literal) {
        return literal.chars()
                .mapToObj(c -> character((char) c)
                    .map(Object::toString))
                .reduce(
                    constant(""),
                    (p1, p2) -> p1.andThen(s1 -> p2.map(s2 -> s1 + s2)));
    }

    public static Parser<Integer> integer() {
        return digit()
            .map(Objects::toString)
            .some()
            .map(strings -> String.join("", strings))
            .map(Integer::parseInt);
    }

    public static Parser<String> chars(Predicate<Character> predicate) {
        return character(predicate)
            .map(Objects::toString)
            .many()
            .map(strings -> String.join("", strings));
    }

    public static Parser<String> string() {
        return character('"')
            .then(chars(c -> c != '"')
            .andThen(s -> character('"')
            .result(s)));
    }

    public static <R, S> Parser<List<R>> separated(Parser<R> parser, Parser<S> separator) {
        Parser<List<R>> ne = parser
            .andThen(r ->
                 separator
                     .then(parser)
                     .many()
                         .map(rs -> cat(r, rs)));
        return ne.orElse(constant(emptyList()));
    }

    // michid improve, dedup
    private static <T> List<T> cat(T a, List<T> as) {
        return concat(Stream.of(a), as.stream())
                .collect(toList());
    }

}

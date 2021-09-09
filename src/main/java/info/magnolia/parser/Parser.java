package info.magnolia.parser;

import static info.magnolia.parser.Parsers.nothing;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.empty;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

interface Parser<T> {

    record Result<T> (T value, String error, String remainder) {
        public boolean isSuccess() {
            return error == null;
        }

        public <R> Result<R> map(Function<T, R> f) {
            return isSuccess()
               ? success(f.apply(value), remainder)
               : failure(error, remainder);
        }
    }

    Result<T> parse(String input);

    static <T> Result<T> success(T result, String remainder) {
        return new Result<>(result, null, remainder);
    }

    static <T> Result<T> failure(String error, String remainder) {
        return new Result<>(null, error, remainder);
    }

    default Parser<T> filter(Predicate<T> predicate) {
        return input -> {
            var result = parse(input);
            return result.isSuccess() && predicate.test(result.value)
               ? result
               : failure("Unexpected input " + input, input);
        };
    }

    default <R> Parser<R> map(Function<T, R> f) {
        return input -> parse(input).map(f);
    }

    default <R> Parser<R> result(R value) {
        return map(__ -> value);
    }

    default <R> Parser<R> andThen(Function<T, Parser<R>> f) {
        return input -> {
            var result = parse(input);
            return result.isSuccess()
                ? f.apply(result.value)
                    .parse(result.remainder)
                : failure(result.error, result.remainder);
        };
    }

    default <R> Parser<R> then(Parser<R> parser) {
        return andThen(__ -> parser);
    }

    default Parser<T> orElse(Supplier<Parser<T>> alternative) {
        return input -> {
            var result = parse(input);
            return result.isSuccess()
                ? result
                : alternative.get().parse(input);
        };
    }

    default Parser<Stream<T>> some() {
        return
            this
                .andThen(result ->
            many()
                .map(results ->
            concat(Stream.of(result), results)));
    }

    default Parser<Stream<T>> many() {
        return
            some()
                .orElse(() ->
            nothing(empty()));
    }
}

package info.magnolia.parser;

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

    static <T> Result<T> success(T result, String remainder) {
        return new Result<>(result, null, remainder);
    }

    static <T> Result<T> failure(String error, String remainder) {
        return new Result<>(null, error, remainder);
    }

    Result<T> parse(String input);

    static <R> Parser<R> result(R result) {
        return input -> success(result, input);
    }

    default Parser<T> filter(Predicate<T> predicate) {
        return input -> {
            var result = parse(input);
            return !result.isSuccess() || predicate.test(result.value)
               ? result
               : failure("Unexpected input " + input, input);
        };
    }

    default <R> Parser<R> map(Function<T, R> f) {
        return input -> parse(input).map(f);
    }

    default <R> Parser<R> read(Function<T, Parser<R>> f) {
        return input -> {
            var result = parse(input);
            return result.isSuccess()
                ? f.apply(result.value)
                    .parse(result.remainder)
                : failure(result.error, result.remainder);
        };
    }

    default <R> Parser<R> ignore(Parser<R> parser) {
        return read(__ -> parser);
    }

    default Parser<T> orElse(Supplier<Parser<T>> alternative) {
        return input -> {
            var result = parse(input);
            return result.isSuccess()
                ? result
                : alternative.get().parse(input);
        };
    }

    static <T> Parser<Stream<T>> none() {
        return result(empty());
    }

    default Parser<Stream<T>> once() {
        return this
            .map(Stream::of);
    }

    default Parser<Stream<T>> some() {
        return
            once()
                .read(result ->
            many()
                .read(results ->
            result(
                concat(result, results))));
    }

    default Parser<Stream<T>> many() {
        return
            some()
                .orElse(() ->
            none());
    }
}

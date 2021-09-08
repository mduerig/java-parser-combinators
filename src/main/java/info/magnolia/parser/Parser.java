package info.magnolia.parser;

import static info.magnolia.parser.Parsers.constant;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.empty;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

interface Parser<T> {

    record Result<T> (T result, String error, String remainder) {
        public boolean isSuccess() {
            return error == null;
        }

        public <R> Result<R> map(Function<T, R> f) {
            return isSuccess()
               ? success(f.apply(result), remainder)
               : failure(error, remainder);
        }
    }

    Result<T> parse(String s);

    static <T> Result<T> success(T result, String remainder) {
        return new Result<>(result, null, remainder);
    }

    static <T> Result<T> failure(String error, String remainder) {
        return new Result<>(null, error, remainder);
    }

    default Parser<T> filter(Predicate<T> predicate) {
        return s -> {
            var r = parse(s);
            return r.isSuccess() && predicate.test(r.result)
               ? r
               : failure("Unexpected input " + s, s);
        };
    }

    default <R> Parser<R> map(Function<T, R> f) {
        return s -> parse(s).map(f);
    }

    default <R> Parser<R> result(R r) {
        return map(__ -> r);
    }

    default <R> Parser<R> andThen(Function<T, Parser<R>> f) {
        return s -> {
            var r1 = parse(s);
            return r1.isSuccess()
                ? f.apply(r1.result)
                    .parse(r1.remainder)
                : failure(r1.error, r1.remainder);
        };
    }

    default <R> Parser<R> then(Parser<R> p) {
        return andThen(__ -> p);
    }

    default Parser<T> orElse(Supplier<Parser<T>> other) {
        return s -> {
            var r1 = parse(s);
            return r1.isSuccess()
                ? r1
                : other.get().parse(s);
        };
    }

    default Parser<Stream<T>> some() {
        return
            this
                .andThen(r ->
            this.many()
                .map(rs ->
            concat(Stream.of(r), rs)));
    }

    default Parser<Stream<T>> many() {
        return some()
            .orElse(() ->
               constant(empty()));
    }
}

package info.magnolia.parser;

import static info.magnolia.parser.Parsers.constant;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.empty;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

interface Parser<T> {

    class Result<T> {
        private final T result;
        private final String error;
        private final String rest;

        private Result(T result, String error, String rest) {
            this.result = result;
            this.error = error;
            this.rest = rest;
        }

        public boolean isSuccess() {
            return error == null;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            Result<?> that = (Result<?>) other;
            return Objects.equals(result, that.result) && Objects.equals(rest, that.rest);
        }

        @Override
        public int hashCode() {
            return Objects.hash(result, rest);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Result.class.getSimpleName() + "[", "]")
                    .add("result=" + result)
                    .add("error=" + error)
                    .add("rest='" + rest + "'")
                    .toString();
        }

        public <R> Result<R> map(Function<T, R> f) {
            return isSuccess()
               ? success(f.apply(result), rest)
               : failure(error, rest);
        }
    }

    Result<T> parse(String s);

    static <T> Result<T> success(T result, String rest) {
        return new Result<>(result, null, rest);
    }

    static <T> Result<T> failure(String error, String rest) {
        return new Result<>(null, error, rest);
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
                    .parse(r1.rest)
                : failure(r1.error, r1.rest);
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
        return this
            .andThen(a ->
                this.many()
            .map(as -> concat(Stream.of(a), as)));
    }

    default Parser<Stream<T>> many() {
        return some()
            .orElse(() ->
               constant(empty()));
    }
}

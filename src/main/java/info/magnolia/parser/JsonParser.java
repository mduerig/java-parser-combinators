package info.magnolia.parser;

import static info.magnolia.parser.Parsers.integer;
import static info.magnolia.parser.Parsers.literal;
import static info.magnolia.parser.Parsers.separated;
import static info.magnolia.parser.Parsers.string;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class JsonParser {
    interface JsonValue {}

    public static class JsonString implements JsonValue {
        private final String value;

        public JsonString(String value) {this.value = value;}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JsonString that = (JsonString) o;
            return Objects.equals(value, that.value);
        }
    }

    public static class JsonNumber implements JsonValue {
        private final int value;

        public JsonNumber(int value) {this.value = value;}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JsonNumber that = (JsonNumber) o;
            return value == that.value;
        }
    }

    public static class JsonBool implements JsonValue {
        private final boolean value;

        public JsonBool(boolean value) {this.value = value;}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JsonBool jsonBool = (JsonBool) o;
            return value == jsonBool.value;
        }
    }

    public static class JsonNull implements JsonValue {
        @Override
        public boolean equals(Object obj) {
            return obj.getClass().equals(JsonNull.class);
        }
    }

    public static class JsonObject implements JsonValue {
        private final Map<String, JsonValue> value;

        public JsonObject(
                Map<String, JsonValue> value) {this.value = value;}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JsonObject that = (JsonObject) o;
            return Objects.equals(value, that.value);
        }
    }

    public static class JsonArray implements JsonValue {
        private final List<JsonValue> value;

        public JsonArray(List<JsonValue> value) {this.value = value;}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JsonArray jsonArray = (JsonArray) o;
            return Objects.equals(value, jsonArray.value);
        }
    }

    public static Parser<JsonValue> jsonNull() {
        return
            literal("null")
                .result(new JsonNull());
    }

    public static Parser<JsonValue> jsonBool() {
        return
            literal("true")
                    .result(true)
                .orElse(() ->
            literal("false")
                    .result(false))
                .map(JsonBool::new);
    }

    public static Parser<JsonValue> jsonNumber() {
        return
            integer()
                .map(JsonNumber::new);
    }

    public static Parser<JsonValue> jsonString() {
        return
            string()
                .map(JsonString::new);
    }

    public static Parser<JsonValue> jsonArray() {
        return
            literal("[")
                .then(
            separated(jsonValue(), literal(",")))
                .andThen(values ->
             literal("]")
                .result(values.collect(Collectors.toList())))
                    .map(JsonArray::new);
    }

    public static Parser<JsonValue> jsonObject() {
        class KeyValue {
            public String getKey() {
                return key;
            }

            public JsonValue getValue() {
                return value;
            }

            final String key; final JsonValue value;
            KeyValue(String key, JsonValue value) {
                this.key = key;
                this.value = value;
            }
        }

        var binding =
            string()
                .andThen(key ->
            literal("=")
                .then(
            jsonValue())
                .map(value ->
            new KeyValue(key, value)));


        return
            literal("{")
                .then(
            separated(binding, literal(",")))
                .andThen(bindings ->
             literal("}")
                .result(bindings.collect(toMap(KeyValue::getKey, KeyValue::getValue))))
                    .map(JsonObject::new);
    }

    public static Parser<JsonValue> jsonValue() {
        return
            jsonNull()
                .orElse(() ->
            jsonString()
                .orElse(() ->
            jsonNull()
                .orElse(() ->
            jsonNumber()
                .orElse(() ->
            jsonString()
                .orElse(() ->
            jsonArray()
                .orElse(() ->
            jsonObject()))))));
    }

}

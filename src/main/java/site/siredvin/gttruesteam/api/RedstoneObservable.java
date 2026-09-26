package site.siredvin.gttruesteam.api;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** A read-only, stable view of a controller's values for redstone automation. */
public interface RedstoneObservable {

    List<Descriptor> redstoneValues();

    Optional<Value> readRedstoneValue(String id);

    enum Type {
        INTEGER, FLOAT, STRING, BOOLEAN;

        public boolean accepts(Object value) {
            return switch (this) {
                case INTEGER -> value instanceof Long;
                case FLOAT -> value instanceof Double number && Double.isFinite(number);
                case STRING -> value instanceof String;
                case BOOLEAN -> value instanceof Boolean;
            };
        }
    }

    record Descriptor(String id, String labelKey, Type type) {
        public Descriptor {
            Objects.requireNonNull(type);
            Objects.requireNonNull(labelKey);
            if (id == null || id.isBlank() || id.length() > 128) {
                throw new IllegalArgumentException("Invalid observable identifier");
            }
        }
    }

    record Value(Type type, Object value) {
        public Value {
            if (type == null || !type.accepts(value)) {
                throw new IllegalArgumentException("Value does not match its declared type");
            }
        }

        public static Value integer(long value) {
            return new Value(Type.INTEGER, value);
        }

        public static Value floating(double value) {
            return new Value(Type.FLOAT, value);
        }

        public static Value string(String value) {
            return new Value(Type.STRING, value);
        }

        public static Value bool(boolean value) {
            return new Value(Type.BOOLEAN, value);
        }
    }
}

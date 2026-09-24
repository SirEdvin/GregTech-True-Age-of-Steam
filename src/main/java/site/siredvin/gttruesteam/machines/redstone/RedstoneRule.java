package site.siredvin.gttruesteam.machines.redstone;

import site.siredvin.gttruesteam.api.RedstoneObservable;
import site.siredvin.gttruesteam.api.RedstoneObservable.Type;
import site.siredvin.gttruesteam.api.RedstoneObservable.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public record RedstoneRule(String valueId, Type type, Operator operator, String operand, int strength) {

    public enum Operator {
        LESS, GREATER, EQUAL, LESS_EQUAL, GREATER_EQUAL, NOT_EQUAL, IS_TRUE, IS_FALSE;

        public boolean supports(Type type) {
            if (type == null) return false;
            return switch (type) {
                case INTEGER, FLOAT -> ordinal() <= NOT_EQUAL.ordinal();
                case STRING -> this == EQUAL || this == NOT_EQUAL;
                case BOOLEAN -> this == IS_TRUE || this == IS_FALSE;
            };
        }
    }

    public static RedstoneRule invalid() {
        return new RedstoneRule("", null, null, "", 0);
    }

    public boolean isValid() {
        if (valueId == null || valueId.isBlank() || valueId.length() > 128 || operator == null ||
                !operator.supports(type) || operand == null || operand.length() > 256 || strength < 0 || strength > 15) {
            return false;
        }
        try {
            return switch (type) {
                case INTEGER -> { Long.parseLong(operand); yield true; }
                case FLOAT -> Double.isFinite(Double.parseDouble(operand));
                case STRING -> true;
                case BOOLEAN -> operand.isEmpty();
            };
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public boolean matches(Value value) {
        if (!isValid() || value == null || value.type() != type) return false;
        return switch (type) {
            case INTEGER -> compare(Long.compare((Long) value.value(), Long.parseLong(operand)));
            case FLOAT -> {
                double actual = (Double) value.value();
                double expected = Double.parseDouble(operand);
                yield compare(actual == expected ? 0 : actual < expected ? -1 : 1);
            }
            case STRING -> operator == Operator.EQUAL ? value.value().equals(operand) : !value.value().equals(operand);
            case BOOLEAN -> (Boolean) value.value() == (operator == Operator.IS_TRUE);
        };
    }

    private boolean compare(int comparison) {
        return switch (operator) {
            case LESS -> comparison < 0;
            case GREATER -> comparison > 0;
            case EQUAL -> comparison == 0;
            case LESS_EQUAL -> comparison <= 0;
            case GREATER_EQUAL -> comparison >= 0;
            case NOT_EQUAL -> comparison != 0;
            default -> false;
        };
    }

    public static int evaluate(List<RedstoneRule> rules, int capacity, RedstoneObservable provider) {
        if (provider == null) return 0;
        var descriptors = new HashMap<String, Type>();
        provider.redstoneValues().forEach(value -> descriptors.put(value.id(), value.type()));
        var snapshot = new HashMap<String, Optional<Value>>();
        int output = 0;
        for (int i = 0; i < Math.min(capacity, rules.size()); i++) {
            var rule = rules.get(i);
            if (!rule.isValid() || descriptors.get(rule.valueId()) != rule.type()) continue;
            var value = snapshot.computeIfAbsent(rule.valueId(), provider::readRedstoneValue);
            if (value.isPresent() && rule.matches(value.get())) output |= rule.strength();
        }
        return output;
    }
}

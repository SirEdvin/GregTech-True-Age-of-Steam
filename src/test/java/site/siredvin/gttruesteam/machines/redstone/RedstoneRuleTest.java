package site.siredvin.gttruesteam.machines.redstone;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import site.siredvin.gttruesteam.api.RedstoneObservable;
import site.siredvin.gttruesteam.api.RedstoneObservable.Descriptor;
import site.siredvin.gttruesteam.api.RedstoneObservable.Type;
import site.siredvin.gttruesteam.api.RedstoneObservable.Value;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static site.siredvin.gttruesteam.machines.redstone.RedstoneRule.Operator.*;

class RedstoneRuleTest {

    private static RedstoneRule rule(Type type, RedstoneRule.Operator op, String operand, int strength) {
        return new RedstoneRule("value", type, op, operand, strength);
    }

    private static RedstoneObservable provider(Type type, Optional<Value> value, AtomicInteger reads) {
        return new RedstoneObservable() {
            @Override
            public List<Descriptor> redstoneValues() {
                return List.of(new Descriptor("value", "test.value", type));
            }

            @Override
            public Optional<Value> readRedstoneValue(String id) {
                reads.incrementAndGet();
                return value;
            }
        };
    }

    @ParameterizedTest
    @EnumSource(Type.class)
    void typedValuesAndStableDescriptors(Type type) {
        Object actual = switch (type) {
            case INTEGER -> 1L;
            case FLOAT -> 1.5;
            case STRING -> "BASIC";
            case BOOLEAN -> false;
        };
        assertEquals(actual, new Value(type, actual).value());
        assertThrows(IllegalArgumentException.class, () -> new Value(type, new Object()));
        assertEquals("value", new Descriptor("value", "translated.label", type).id());
        assertThrows(IllegalArgumentException.class, () -> new Descriptor("", "label", type));
    }

    @ParameterizedTest
    @EnumSource(value = RedstoneRule.Operator.class, names = { "IS_TRUE", "IS_FALSE" }, mode = EnumSource.Mode.EXCLUDE)
    void numericComparisonTruthTable(RedstoneRule.Operator op) {
        for (int comparison = -1; comparison <= 1; comparison++) {
            boolean expected = switch (op) {
                case LESS -> comparison < 0;
                case GREATER -> comparison > 0;
                case EQUAL -> comparison == 0;
                case LESS_EQUAL -> comparison <= 0;
                case GREATER_EQUAL -> comparison >= 0;
                case NOT_EQUAL -> comparison != 0;
                default -> throw new AssertionError();
            };
            assertEquals(expected, rule(Type.INTEGER, op, "10", 15).matches(Value.integer(10 + comparison)));
            assertEquals(expected, rule(Type.FLOAT, op, "10.5", 15).matches(Value.floating(10.5 + comparison)));
        }
    }

    @Test
    void integerComparisonDoesNotLosePrecision() {
        assertFalse(rule(Type.INTEGER, EQUAL, "9223372036854775806", 1).matches(Value.integer(Long.MAX_VALUE)));
        assertTrue(rule(Type.INTEGER, GREATER, "9223372036854775806", 1).matches(Value.integer(Long.MAX_VALUE)));
    }

    @Test
    void floatEqualityIsExactButSignedZeroIsEqual() {
        assertTrue(rule(Type.FLOAT, EQUAL, "-0.0", 1).matches(Value.floating(0.0)));
        assertFalse(rule(Type.FLOAT, EQUAL, "0.3", 1).matches(Value.floating(0.1 + 0.2)));
        assertThrows(IllegalArgumentException.class, () -> Value.floating(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> Value.floating(Double.POSITIVE_INFINITY));
    }

    @Test
    void stringAndBooleanChecks() {
        assertTrue(rule(Type.STRING, EQUAL, "BASIC", 1).matches(Value.string("BASIC")));
        assertFalse(rule(Type.STRING, EQUAL, "basic", 1).matches(Value.string("BASIC")));
        assertTrue(rule(Type.STRING, NOT_EQUAL, "basic", 1).matches(Value.string("BASIC")));
        assertFalse(rule(Type.STRING, LESS, "BASIC", 1).isValid());
        assertTrue(rule(Type.BOOLEAN, IS_TRUE, "", 1).matches(Value.bool(true)));
        assertFalse(rule(Type.BOOLEAN, IS_TRUE, "", 1).matches(Value.bool(false)));
        assertTrue(rule(Type.BOOLEAN, IS_FALSE, "", 1).matches(Value.bool(false)));
        assertFalse(rule(Type.BOOLEAN, EQUAL, "false", 1).isValid());
        assertFalse(rule(Type.BOOLEAN, IS_FALSE, "false", 1).isValid());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "1.5", "9223372036854775808", "NaN", "Infinity", " 1" })
    void invalidIntegerOperands(String operand) {
        assertFalse(rule(Type.INTEGER, EQUAL, operand, 1).isValid());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "NaN", "Infinity", "-Infinity", "1e999", "abc" })
    void invalidFloatOperands(String operand) {
        assertFalse(rule(Type.FLOAT, EQUAL, operand, 1).isValid());
    }

    @Test
    void invalidRuleBoundsAndTypeChanges() {
        assertFalse(rule(Type.INTEGER, EQUAL, "0", -1).isValid());
        assertFalse(rule(Type.INTEGER, EQUAL, "0", 16).isValid());
        assertFalse(rule(Type.STRING, EQUAL, "x".repeat(257), 1).isValid());
        assertFalse(new RedstoneRule("x".repeat(129), Type.STRING, EQUAL, "", 1).isValid());
        assertFalse(rule(Type.INTEGER, NOT_EQUAL, "0", 1).matches(Value.string("1")));
        assertFalse(RedstoneRule.invalid().isValid());
    }

    @Test
    void firstMatchIncludesZeroAndReordering() {
        var provider = provider(Type.INTEGER, Optional.of(Value.integer(5)), new AtomicInteger());
        var low = rule(Type.INTEGER, GREATER, "0", 4);
        var high = rule(Type.INTEGER, EQUAL, "5", 15);
        var zero = rule(Type.INTEGER, EQUAL, "5", 0);
        assertEquals(4, RedstoneRule.evaluate(List.of(low, high), 6, provider));
        assertEquals(15, RedstoneRule.evaluate(List.of(high, low), 6, provider));
        assertEquals(0, RedstoneRule.evaluate(List.of(zero, high), 6, provider));
        assertEquals(0, RedstoneRule.evaluate(List.of(), 6, provider));
        assertEquals(0, RedstoneRule.evaluate(List.of(high), 6, null));
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3, 4, 5, 6 })
    void capacityAndSnapshot(int capacity) {
        var reads = new AtomicInteger();
        var provider = provider(Type.INTEGER, Optional.of(Value.integer(5)), reads);
        var rules = new java.util.ArrayList<RedstoneRule>();
        for (int i = 0; i < capacity; i++) rules.add(rule(Type.INTEGER, EQUAL, "9", 1));
        rules.add(rule(Type.INTEGER, EQUAL, "5", 15));
        assertEquals(0, RedstoneRule.evaluate(rules, capacity, provider));
        assertEquals(1, reads.get());
        assertEquals(capacity, RedstoneRuleCodec.decode(RedstoneRuleCodec.encode(rules), capacity).size());
    }

    @Test
    void unavailableAndChangedTypesNeverMatchInequality() {
        var unavailable = provider(Type.INTEGER, Optional.empty(), new AtomicInteger());
        assertEquals(0, RedstoneRule.evaluate(List.of(rule(Type.INTEGER, NOT_EQUAL, "0", 15)), 6, unavailable));
        var changed = provider(Type.STRING, Optional.of(Value.string("5")), new AtomicInteger());
        assertEquals(0, RedstoneRule.evaluate(List.of(rule(Type.INTEGER, NOT_EQUAL, "0", 15)), 6, changed));
        var unavailableBoolean = provider(Type.BOOLEAN, Optional.empty(), new AtomicInteger());
        assertEquals(0, RedstoneRule.evaluate(List.of(rule(Type.BOOLEAN, IS_FALSE, "", 15)), 6, unavailableBoolean));
    }

    @Test
    void codecPreservesAllTypesOrderAndUnknownIdentifiers() {
        var rules = List.of(rule(Type.INTEGER, LESS, "-5", 1), rule(Type.FLOAT, GREATER_EQUAL, "1.25", 4),
                rule(Type.STRING, NOT_EQUAL, "BASIC", 0), rule(Type.BOOLEAN, IS_FALSE, "", 15),
                new RedstoneRule("removed_value", Type.INTEGER, EQUAL, "1", 2));
        assertEquals(rules, RedstoneRuleCodec.decode(RedstoneRuleCodec.encode(rules), 6));
    }

    @Test
    void corruptSavedEntriesCannotEmit() {
        var list = new ListTag();
        list.add(new CompoundTag());
        var valid = RedstoneRuleCodec.encode(List.of(rule(Type.INTEGER, EQUAL, "1", 15))).getCompound(0);
        var badStrength = valid.copy();
        badStrength.putInt("strength", 300);
        list.add(badStrength);
        var missingStrength = valid.copy();
        missingStrength.remove("strength");
        list.add(missingStrength);
        var badType = valid.copy();
        badType.putString("type", "REMOVED");
        list.add(badType);
        var tooLong = valid.copy();
        tooLong.putString("operand", "x".repeat(257));
        list.add(tooLong);
        assertTrue(RedstoneRuleCodec.decode(list, 6).stream().noneMatch(RedstoneRule::isValid));
    }
}

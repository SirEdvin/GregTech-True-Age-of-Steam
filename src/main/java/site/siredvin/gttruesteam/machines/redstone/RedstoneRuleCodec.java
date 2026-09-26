package site.siredvin.gttruesteam.machines.redstone;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import site.siredvin.gttruesteam.api.RedstoneObservable.Type;

import java.util.ArrayList;
import java.util.List;

public final class RedstoneRuleCodec {

    private RedstoneRuleCodec() {}

    public static ListTag encode(List<RedstoneRule> rules) {
        var list = new ListTag();
        for (var rule : rules) {
            var tag = new CompoundTag();
            if (rule.isValid()) {
                tag.putString("value", rule.valueId());
                tag.putString("type", rule.type().name());
                tag.putString("operator", rule.operator().name());
                tag.putString("operand", rule.operand());
                tag.putInt("strength", rule.strength());
            }
            list.add(tag);
        }
        return list;
    }

    public static List<RedstoneRule> decode(ListTag list, int capacity) {
        var rules = new ArrayList<RedstoneRule>();
        for (int i = 0; i < Math.min(capacity, list.size()); i++) {
            var tag = list.getCompound(i);
            try {
                if (!tag.contains("value", Tag.TAG_STRING) || !tag.contains("type", Tag.TAG_STRING) ||
                        !tag.contains("operator", Tag.TAG_STRING) || !tag.contains("operand", Tag.TAG_STRING) ||
                        !tag.contains("strength", Tag.TAG_INT)) {
                    rules.add(RedstoneRule.invalid());
                    continue;
                }
                var rule = new RedstoneRule(tag.getString("value"), Type.valueOf(tag.getString("type")),
                        RedstoneRule.Operator.valueOf(tag.getString("operator")), tag.getString("operand"),
                        tag.getInt("strength"));
                rules.add(rule.isValid() ? rule : RedstoneRule.invalid());
            } catch (IllegalArgumentException ignored) {
                rules.add(RedstoneRule.invalid());
            }
        }
        return List.copyOf(rules);
    }
}

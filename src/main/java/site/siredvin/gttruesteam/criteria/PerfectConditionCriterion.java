package site.siredvin.gttruesteam.criteria;

import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.google.gson.JsonObject;
import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamStats;

public class PerfectConditionCriterion extends SimpleCriterionTrigger<StatCountCriterionInstance> {

    public static final ResourceLocation ID = GTTrueSteam.id("perfect_condition_recipe");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public void trigger(ServerPlayer player) {
        trigger(player, instance -> instance.test(player));
    }

    public StatCountCriterionInstance atLeast(int count) {
        return new StatCountCriterionInstance(ID, TrueSteamStats.PERFECT_CONDITION_RECIPE_CRAFTED, count);
    }

    @Override
    public StatCountCriterionInstance createInstance(JsonObject json, ContextAwarePredicate predicate,
                                                     DeserializationContext context) {
        return new StatCountCriterionInstance(ID, TrueSteamStats.PERFECT_CONDITION_RECIPE_CRAFTED,
                json.get("count").getAsInt());
    }
}

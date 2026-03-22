package site.siredvin.gttruesteam.criteria;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamStats;

public class InfernalMaintainceCriterion extends SimpleCriterionTrigger<StatCountCriterionInstance> {

    public static final ResourceLocation ID = GTTrueSteam.id("infernal_maintance_performed");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public void trigger(ServerPlayer player) {
        trigger(player, instance -> instance.test(player));
    }

    public StatCountCriterionInstance atLeast(int count) {
        return new StatCountCriterionInstance(ID, TrueSteamStats.INFERNAL_MAINTAIN_RECIPE_PERFORMED, count);
    }

    @Override
    public StatCountCriterionInstance createInstance(JsonObject json, ContextAwarePredicate predicate,
            DeserializationContext context) {
        return new StatCountCriterionInstance(ID, TrueSteamStats.INFERNAL_MAINTAIN_RECIPE_PERFORMED,
                json.get("count").getAsInt());
    }
}

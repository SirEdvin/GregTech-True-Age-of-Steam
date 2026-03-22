package site.siredvin.gttruesteam.criteria;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;

import com.google.gson.JsonObject;

import java.util.function.Supplier;

public class StatCountCriterionInstance extends AbstractCriterionTriggerInstance {

    private final Supplier<Stat<ResourceLocation>> stat;
    private final int count;

    public StatCountCriterionInstance(ResourceLocation id, Supplier<Stat<ResourceLocation>> stat, int count) {
        super(id, ContextAwarePredicate.ANY);
        this.stat = stat;
        this.count = count;
    }

    public boolean test(ServerPlayer player) {
        return player.getStats().getValue(stat.get()) >= count;
    }

    @Override
    public JsonObject serializeToJson(SerializationContext context) {
        JsonObject obj = super.serializeToJson(context);
        obj.addProperty("count", count);
        return obj;
    }
}

package site.siredvin.gttruesteam.recipe.condition;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.TrueSteamLang;
import site.siredvin.gttruesteam.TrueSteamRecipeConditions;
import site.siredvin.gttruesteam.machines.mob_spawner.MobSpawnerMachine;

import java.util.Optional;

@NoArgsConstructor
public class SpawnerMobCondition extends RecipeCondition<SpawnerMobCondition> {

    // spotless:off
    public static final Codec<SpawnerMobCondition> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("mobType").forGetter(SpawnerMobCondition::getMobType)
            ).apply(instance, SpawnerMobCondition::new));
    // spotless:on

    @Getter
    private @NotNull String mobType = "";

    public SpawnerMobCondition(@NotNull String mobType) {
        super();
        this.mobType = mobType;
    }

    public SpawnerMobCondition(@NotNull ResourceLocation mobType) {
        super();
        this.mobType = mobType.toString();
    }

    @Override
    public RecipeConditionType<SpawnerMobCondition> getType() {
        return TrueSteamRecipeConditions.SPAWNER_MOB;
    }

    @Override
    public Component getTooltips() {
        return TrueSteamLang.SPAWNER_MOB_CONDITION;
    }

    @Override
    protected boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        var machine = recipeLogic.machine;
        if (machine instanceof MobSpawnerMachine mobSpawnerMachine) {
            Optional<ResourceLocation> spawnerMob = mobSpawnerMachine.getSpawnerMobType();
            return spawnerMob.isPresent() && spawnerMob.get().toString().equals(mobType);
        }
        return false;
    }

    @Override
    public SpawnerMobCondition createTemplate() {
        return new SpawnerMobCondition();
    }
}

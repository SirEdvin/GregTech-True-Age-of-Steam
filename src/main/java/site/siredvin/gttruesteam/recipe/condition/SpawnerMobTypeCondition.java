package site.siredvin.gttruesteam.recipe.condition;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;

import net.minecraft.network.chat.Component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.TrueSteamLang;
import site.siredvin.gttruesteam.TrueSteamRecipeConditions;
import site.siredvin.gttruesteam.api.ISpawnerExtractionCondition;
import site.siredvin.gttruesteam.machines.spawner_extraction.MobType;

@NoArgsConstructor
public class SpawnerMobTypeCondition extends RecipeCondition<SpawnerMobTypeCondition> {

    // spotless:off
    public static final Codec<SpawnerMobTypeCondition> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.xmap(MobType::valueOf, MobType::name).fieldOf("mobType")
                            .forGetter(SpawnerMobTypeCondition::getMobType)
            ).apply(instance, SpawnerMobTypeCondition::new));
    // spotless:on

    public SpawnerMobTypeCondition(@NotNull MobType mobType) {
        super();
        this.mobType = mobType;
    }

    @Getter
    private @NotNull MobType mobType = MobType.NETHER;

    @Override
    public RecipeConditionType<SpawnerMobTypeCondition> getType() {
        return TrueSteamRecipeConditions.SPAWNER_MOB_TYPE;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable(TrueSteamLang.SPAWNER_MOB_TYPE_CONDITION_KEY, this.mobType.name());
    }

    @Override
    protected boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        var machine = recipeLogic.machine;
        if (machine instanceof ISpawnerExtractionCondition spawnerMachine) {
            return spawnerMachine.supportMobType(this.mobType);
        }
        return false;
    }

    @Override
    public SpawnerMobTypeCondition createTemplate() {
        return new SpawnerMobTypeCondition();
    }
}

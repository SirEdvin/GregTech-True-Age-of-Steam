package site.siredvin.gttruesteam.recipe.condition;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;

import net.minecraft.network.chat.Component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.TrueSteamLang;
import site.siredvin.gttruesteam.TrueSteamRecipeConditions;
import site.siredvin.gttruesteam.machines.industrial_heater.InfernalBoilerMachine;

public class BeatingHuskCondition extends RecipeCondition<BeatingHuskCondition> {

    // spotless:off
    public static final Codec<BeatingHuskCondition> CODEC =
            RecordCodecBuilder.create(instance -> instance.stable(new BeatingHuskCondition()));
    // spotless:on

    public BeatingHuskCondition() {
        super();
    }

    @Override
    public RecipeConditionType<BeatingHuskCondition> getType() {
        return TrueSteamRecipeConditions.BEATING_HUSK_TYPE;
    }

    @Override
    public Component getTooltips() {
        return TrueSteamLang.BEATING_HUSK_CONDITION;
    }

    @Override
    protected boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        var machine = recipeLogic.machine;
        if (machine instanceof InfernalBoilerMachine infernalBoilerMachine) {
            return infernalBoilerMachine.isBeatingHuskPresent();
        }
        return false;
    }

    @Override
    public BeatingHuskCondition createTemplate() {
        return new BeatingHuskCondition();
    }
}

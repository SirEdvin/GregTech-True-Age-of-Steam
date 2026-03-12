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
import site.siredvin.gttruesteam.machines.cim.ConceptInfusionMatrixMachine;

@NoArgsConstructor
public class InnerRecipeTypeCondition extends RecipeCondition<InnerRecipeTypeCondition> {

    // spotless:off
    public static final Codec<InnerRecipeTypeCondition> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("requiredRecipeType").forGetter(InnerRecipeTypeCondition::getRequiredRecipeType)
            ).apply(instance, InnerRecipeTypeCondition::new));
    // spotless:on

    public InnerRecipeTypeCondition(@NotNull String requiredRecipeType) {
        super();
        this.requiredRecipeType = requiredRecipeType;
    }

    @Getter
    private @NotNull String requiredRecipeType = "";

    @Override
    public RecipeConditionType<InnerRecipeTypeCondition> getType() {
        return TrueSteamRecipeConditions.INNER_RECIPE_TYPE;
    }

    @Override
    public Component getTooltips() {
        return TrueSteamLang.INNER_RECIPE_TYPE_CONDITION.copy().append(Component.translatable(requiredRecipeType));
    }

    @Override
    protected boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        var machine = recipeLogic.machine;
        if (machine instanceof ConceptInfusionMatrixMachine conceptInfusionMatrixMachine) {
            var recipeType = conceptInfusionMatrixMachine.machineRecipe();
            return conceptInfusionMatrixMachine.isMachinesRunning() && recipeType.equals(requiredRecipeType);
        }
        return false;
    }

    @Override
    public InnerRecipeTypeCondition createTemplate() {
        return new InnerRecipeTypeCondition();
    }
}

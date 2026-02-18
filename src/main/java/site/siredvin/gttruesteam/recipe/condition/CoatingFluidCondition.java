package site.siredvin.gttruesteam.recipe.condition;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.TrueSteamLang;
import site.siredvin.gttruesteam.TrueSteamRecipeConditions;
import site.siredvin.gttruesteam.machines.coating_shrine.CoatingShrineMachine;

@NoArgsConstructor
public class CoatingFluidCondition extends RecipeCondition {

    // spotless:off
    public static final Codec<CoatingFluidCondition> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("requiredFluid").forGetter(CoatingFluidCondition::getRequiredFluid)
            ).apply(instance, CoatingFluidCondition::new));
    // spotless:on

    public CoatingFluidCondition(@NotNull String requiredFluid) {
        super();
        this.requiredFluid = requiredFluid;
    }

    public CoatingFluidCondition(@NotNull Fluid fluid) {
        super();
        var fluidRegistry = GTRegistries.builtinRegistry().registry(Registries.FLUID).get();
        this.requiredFluid = fluidRegistry.getKey(fluid).toString();
    }

    @Getter
    private @NotNull String requiredFluid = "";

    @Override
    public RecipeConditionType<?> getType() {
        return TrueSteamRecipeConditions.COATING_FLUID;
    }

    @Override
    public Component getTooltips() {
        return TrueSteamLang.COATING_FLUID_CONDITION;
    }

    public Fluid getRequiredFluidRecord() {
        var fluidRegistry = GTRegistries.builtinRegistry().registry(Registries.FLUID).get();
        return fluidRegistry.get(ResourceLocation.parse(requiredFluid));
    }

    @Override
    protected boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        var machine = recipeLogic.machine;
        if (machine instanceof CoatingShrineMachine coatingShrineMachine) {
            var blockState = coatingShrineMachine.getFluid();
            var fluidState = blockState.getFluidState();
            return !fluidState.isEmpty() && fluidState.isSource() && fluidState.is(getRequiredFluidRecord());
        }
        return false;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new CoatingFluidCondition();
    }
}

package site.siredvin.gttruesteam.machines.industrial_gas_pressurizer;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities;

import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;
import site.siredvin.gttruesteam.config.GasPressurizerConfig;

import java.util.stream.Collectors;

public class IndustrialGasPressurizerRecipeModifier implements RecipeModifier {

    public static final String PerfectConditionReached = "PerfectConditionReached";

    private static final ModifierFunction OUTPUT_BOOST = recipe -> {
        var copy = recipe.copy();
        var outputFluids = copy.outputs.get(GTRecipeCapabilities.FLUID);
        if (outputFluids != null) {
            var assumedPercentage = recipe.data.contains(TrueSteamRecipeTypes.ASSUMED_PERCENTAGE) ?
                    recipe.data.getInt(TrueSteamRecipeTypes.ASSUMED_PERCENTAGE) : 100;
            var factor = (100.0 / assumedPercentage) * (1 + GasPressurizerConfig.percentageIncrease.get());
            var newFluids = outputFluids.stream().map(x -> {
                var insides = x.content;
                if (insides instanceof FluidIngredient fluidIngredient) {
                    var stackCopy = fluidIngredient.copy();
                    stackCopy.setAmount((int) (stackCopy.getAmount() * factor));
                    return new Content(stackCopy, x.chance, x.maxChance, x.tierChanceBoost);
                }
                return x;
            }).collect(Collectors.toList());
            copy.outputs.put(GTRecipeCapabilities.FLUID, newFluids);
            copy.data.putBoolean(PerfectConditionReached, true);
        }
        return copy;
    };

    @Override
    public @NotNull ModifierFunction getModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof IndustrialGasPressurizerMachine pressurizerMachine))
            return ModifierFunction.IDENTITY;

        var perfectConditionState = pressurizerMachine.getState();

        if (perfectConditionState != PerfectConditionState.REACHED)
            return ModifierFunction.IDENTITY;

        return OUTPUT_BOOST;
    }
}

package site.siredvin.gttruesteam.machines.industrial_gas_pressurizer;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities;

import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;
import site.siredvin.gttruesteam.config.GasPressurizerConfig;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
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

        // Condition 1: more than 2 seconds since last crafting
        Instant lastCraftTime = Instant.ofEpochMilli(pressurizerMachine.getRecipeLogic().getLastCraftingTime());
        if (!Instant.now().isAfter(lastCraftTime.plusSeconds(2))) {
            return ModifierFunction.IDENTITY;
        }

        // Condition 2: all fluid input tanks are between 35% and 85% capacity
        var inputHandlers = pressurizerMachine.getCapabilitiesFlat()
                .getOrDefault(IO.IN, Collections.emptyMap())
                .getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList());

        if (inputHandlers.isEmpty()) {
            return ModifierFunction.IDENTITY;
        }

        boolean allInRange = inputHandlers.stream()
                .filter(handler -> handler instanceof NotifiableFluidTank)
                .map(handler -> (NotifiableFluidTank) handler)
                .flatMap(tank -> Arrays.stream(tank.getStorages()))
                .allMatch(storage -> {
                    int capacity = storage.getCapacity();
                    if (capacity == 0) return false;
                    double ratio = (double) storage.getFluidAmount() / capacity;
                    return ratio >= 0.35 && ratio <= 0.85;
                });

        if (!allInRange) {
            return ModifierFunction.IDENTITY;
        }

        return OUTPUT_BOOST;
    }
}

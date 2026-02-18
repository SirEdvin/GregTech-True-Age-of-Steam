package site.siredvin.gttruesteam.machines.industrial_heater;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities;

import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;

import java.util.*;
import java.util.stream.Collectors;

public class InfernalBoilerRecipeModifier implements RecipeModifier {

    private static LoadingCache<Integer, ModifierFunction> OVERHEATING_MODIFIER_CACHE = CacheBuilder.newBuilder().build(CacheLoader.from(InfernalBoilerRecipeModifier::buildOverheatingFunction));

    public static ModifierFunction SUPREME_CHARGING = ModifierFunction.builder().durationMultiplier(0.25).build();

    public static OverclockingLogic SUBCLOCKING = OverclockingLogic.create(OverclockingLogic.PERFECT_DURATION_FACTOR, OverclockingLogic.PERFECT_HALF_VOLTAGE_FACTOR, false);

    private static ModifierFunction buildOverheatingFunction(int level) {
        return (recipe) -> {
            var copy = recipe.copy();
            var outputFluids = copy.outputs.get(GTRecipeCapabilities.FLUID);
            var newFluids = outputFluids.stream().map(x -> {
                var insides = x.content;
                if (insides instanceof FluidIngredient fluidIngredient) {
                    var stackCopy = fluidIngredient.copy();
                    stackCopy.setAmount(stackCopy.getAmount() * (4 + 2 * level));
                    return new Content(stackCopy, x.chance, x.maxChance, x.tierChanceBoost);
                }
                return x;
            }).collect(Collectors.toList());
            copy.outputs.put(GTRecipeCapabilities.FLUID, newFluids);
            copy.duration *= (int) (copy.duration * (2 + 0.5 * level));
            return copy;
        };
    }

    @Override
    public @NotNull ModifierFunction getModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof InfernalBoilerMachine infernalBoilerMachine)) return ModifierFunction.NULL;
        var level = infernalBoilerMachine.getCoilType().getLevel();
        var logic = infernalBoilerMachine.getRecipeLogic();
        if (recipe.data.contains(TrueSteamRecipeTypes.INFERNAL_CYCLES_DATA_KEY)) {
            if (logic.getHeatLevel() == HeatLevel.SUPREME)
                return SUPREME_CHARGING;
            return ModifierFunction.IDENTITY;
        }
         if (logic.getInfernalCharges() <= 0) {
            return ModifierFunction.NULL;
         }
        var heatLevel = logic.getHeatLevel();
        var maxParallels = heatLevel.getMaxParallels() * level;
        var realParallels = ParallelLogic.getParallelAmount(infernalBoilerMachine, recipe, maxParallels);
        if (realParallels == 0) return ModifierFunction.NULL;
        var ocModifier = SUBCLOCKING.getModifier(infernalBoilerMachine, recipe,
                infernalBoilerMachine.getOverclockVoltage());
        var parallelModifier = ModifierFunction.builder().modifyAllContents(ContentModifier.multiplier(realParallels))
                .parallels(realParallels).build();
        var result = ocModifier.andThen(parallelModifier);
        if (heatLevel == HeatLevel.SUPREME && recipe.data.getBoolean(TrueSteamRecipeTypes.OVERHEATED_KEY)) {
            result = result.andThen(InfernalBoilerRecipeModifier.OVERHEATING_MODIFIER_CACHE.getUnchecked(level));
        }
        return result;
    }
}

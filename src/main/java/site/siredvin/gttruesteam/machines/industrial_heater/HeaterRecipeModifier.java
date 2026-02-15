package site.siredvin.gttruesteam.machines.industrial_heater;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities;

import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.TrueSteamMaterials;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;

import java.util.*;

public class HeaterRecipeModifier implements RecipeModifier {

    @Override
    public @NotNull ModifierFunction getModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof IndustrialHeaterMachine industrialHeaterMachine)) return ModifierFunction.NULL;
        if (recipe.data.contains(TrueSteamRecipeTypes.PURE_CYCLES_DATA_KEY))
            return ModifierFunction.IDENTITY;
        var level = industrialHeaterMachine.getCoilType().getLevel();
        var logic = industrialHeaterMachine.getHeaterRecipeLogic();
        // if (logic.getPureChargers() <= 0) {
        // return ModifierFunction.builder().durationMultiplier(2).build();
        // }
        var heatLevel = logic.getHeatLevel();
        var maxParallels = heatLevel.getMaxParallels() * level;
        var realParallels = ParallelLogic.getParallelAmount(industrialHeaterMachine, recipe, maxParallels);
        if (realParallels == 0) return ModifierFunction.NULL;
        var ocModifier = OverclockingLogic.NON_PERFECT_OVERCLOCK.getModifier(industrialHeaterMachine, recipe,
                industrialHeaterMachine.getOverclockVoltage());
        var parallelModifier = ModifierFunction.builder().modifyAllContents(ContentModifier.multiplier(realParallels))
                .parallels(realParallels).build();
        var result = ocModifier.andThen(parallelModifier);
        if (heatLevel == HeatLevel.SUPREME) {
            if (recipe.outputs.containsKey(GTRecipeCapabilities.FLUID)) {
                var fluidOutputs = recipe.outputs.get(GTRecipeCapabilities.FLUID);
                if (fluidOutputs.size() == 1) {
                    var firstOutputContent = fluidOutputs.get(0);
                    var firstOutput = firstOutputContent.content;
                    if (firstOutput instanceof FluidIngredient fluidIngredient) {
                        if (fluidIngredient.getStacks().length == 1) {
                            var fluidStack = fluidIngredient.getStacks()[0];
                            int fluidMultiplier;
                            int durationMultiplicator;
                            if (fluidStack.getFluid().isSame(GTMaterials.Steam.getFluid())) {
                                fluidMultiplier = 4;
                                durationMultiplicator = 2;
                            } else if (fluidStack.getFluid().isSame(TrueSteamMaterials.WarmSteam.getFluid())) {
                                fluidMultiplier = 3;
                                durationMultiplicator = 1;
                            } else if (fluidStack.getFluid().isSame(TrueSteamMaterials.HotSteam.getFluid())) {
                                fluidMultiplier = 2;
                                durationMultiplicator = 1;
                            } else {
                                fluidMultiplier = 0;
                                durationMultiplicator = 1;
                            }

                            if (fluidMultiplier != 0) {
                                result = result.andThen(recipe1 -> {
                                    var copy = recipe1.copy();
                                    var fluidContent = copy.outputs.get(GTRecipeCapabilities.FLUID).get(0);
                                    var fluidIngredient1 = (FluidIngredient) fluidContent.content;
                                    copy.outputs.put(GTRecipeCapabilities.FLUID, List.of(new Content(
                                            FluidIngredient.of(TrueSteamMaterials.SuperhotSteam.getFluid(),
                                                    fluidIngredient1.getAmount() * fluidMultiplier),
                                            fluidContent.chance, fluidContent.maxChance,
                                            fluidContent.tierChanceBoost)));
                                    copy.duration *= durationMultiplicator;
                                    return copy;
                                });
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}

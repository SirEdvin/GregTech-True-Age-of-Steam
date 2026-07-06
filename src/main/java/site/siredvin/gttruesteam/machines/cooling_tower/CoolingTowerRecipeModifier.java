package site.siredvin.gttruesteam.machines.cooling_tower;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;
import site.siredvin.gttruesteam.machines.shared.cooling.PassiveCoolingMachine;
import site.siredvin.gttruesteam.recipe.condition.CoolingCapacityCondition;

public class CoolingTowerRecipeModifier implements RecipeModifier {

    @Override
    public @NotNull ModifierFunction getModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof PassiveCoolingMachine passiveCoolingMachine)) return ModifierFunction.NULL;
        if (recipe.getType() != TrueSteamRecipeTypes.FLUID_COOLING) return ModifierFunction.NULL;
        var recipeLogic = passiveCoolingMachine.getRecipeLogic();
        var requiredPerRecipe = recipe.data.getInt(TrueSteamRecipeTypes.COOLING_CONSUMED);
        var baseCapacity = recipe.conditions.stream().filter(x -> x instanceof CoolingCapacityCondition).findFirst()
                .map(x -> ((CoolingCapacityCondition) x).getRequiredCapacity()).orElse(0);
        // Here we are going to calculate max parallels for this recipe just now
        var maximumParallels = (recipeLogic.getAggregatedCoolingCapacity() - baseCapacity) / requiredPerRecipe;
        var realParallels = ParallelLogic.getParallelAmountWithoutEU(passiveCoolingMachine, recipe, maximumParallels);
        if (realParallels == 0) return ModifierFunction.NULL;
        return ModifierFunction.builder().modifyAllContents(ContentModifier.multiplier(realParallels))
                .parallels(realParallels).durationMultiplier(0.1).build();
    }
}

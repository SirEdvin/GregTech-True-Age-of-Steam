package site.siredvin.gttruesteam.machines.industrial_coating_line;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;
import site.siredvin.gttruesteam.api.ICoatingMachine;

public class IndustrialCoatingLineRecipeModifier implements RecipeModifier {

    @Override
    public @NotNull ModifierFunction getModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof ICoatingMachine coatingMachine)) return ModifierFunction.NULL;
        if (!(machine instanceof IndustrialCoatingLineMachine lineMachine)) return ModifierFunction.NULL;
        if (recipe.getType() != TrueSteamRecipeTypes.INDUSTRIAL_COATING_LINE) return ModifierFunction.NULL;

        var fluidContents = recipe.getInputContents(FluidRecipeCapability.CAP);
        if (fluidContents.isEmpty()) return ModifierFunction.NULL;

        var ingredient = FluidRecipeCapability.CAP.of(fluidContents.get(0).content);
        var stacks = ingredient.getStacks();
        if (stacks.length == 0) return ModifierFunction.NULL;

        var fluid = stacks[0].getFluid();
        var maximumParallels = coatingMachine.countFluidCells(fluid);
        if (maximumParallels == 0) return ModifierFunction.NULL;

        var realParallels = ParallelLogic.getParallelAmountWithoutEU(lineMachine, recipe, maximumParallels);
        if (realParallels == 0) return ModifierFunction.NULL;

        var parallelModifier = ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(realParallels))
                .parallels(realParallels)
                .build();
        return OverclockingLogic.NON_PERFECT_OVERCLOCK
                .getModifier(lineMachine, recipe, lineMachine.getOverclockVoltage())
                .andThen(parallelModifier);
    }
}

package site.siredvin.gttruesteam.machines.volcanic_boiler;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

import org.jetbrains.annotations.NotNull;

/**
 * Recipe modifier for the Volcanic Pressure Boiler.
 *
 * Parallel capacity is determined by the installed heating coil temperature:
 * maxParallels = coilTemperature / 900 (minimum 1).
 * Overclock is applied at the standard non-perfect rate.
 */
public class VolcanicPressureBoilerRecipeModifier implements RecipeModifier {

    @Override
    public @NotNull ModifierFunction getModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof VolcanicPressureBoilerMachine vpbm)) return ModifierFunction.NULL;

        int coilTemp = vpbm.getCoilType().getCoilTemperature();
        int maxParallels = Math.max(1, coilTemp / 900);

        var realParallels = ParallelLogic.getParallelAmountWithoutEU(vpbm, recipe, maxParallels);
        if (realParallels == 0) return ModifierFunction.NULL;

        var ocModifier = OverclockingLogic.NON_PERFECT_OVERCLOCK.getModifier(
                vpbm, recipe, vpbm.getOverclockVoltage());
        var parallelModifier = ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(realParallels))
                .parallels(realParallels)
                .build();

        return ocModifier.andThen(parallelModifier);
    }
}

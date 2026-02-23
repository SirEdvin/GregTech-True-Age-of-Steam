package site.siredvin.gttruesteam.machines.regulated_cryo_chamber;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

import org.jetbrains.annotations.NotNull;

public class RegulatedCryoChamberRecipeModifier implements RecipeModifier {

    @Override
    public @NotNull ModifierFunction getModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof RegulatedCryoChamberMachine regulatedCryoChamberMachine)) return ModifierFunction.NULL;
        var coils = regulatedCryoChamberMachine.getCoilType();
        var maxParallels = 1 + coils.getLevel();
        var realParallels = ParallelLogic.getParallelAmount(regulatedCryoChamberMachine, recipe, maxParallels);
        if (realParallels == 0) return ModifierFunction.NULL;
        // Calculate OC level here.
        // Basically, MV is 2 and if coil level is bigger we got perfect OC, else - not
        var ocModifier = recipe.ocLevel < coils.getLevel() ? OverclockingLogic.PERFECT_OVERCLOCK :
                OverclockingLogic.NON_PERFECT_OVERCLOCK;
        var parallelModifier = ModifierFunction.builder().modifyAllContents(ContentModifier.multiplier(realParallels))
                .parallels(realParallels).build();
        var recipeLogic = regulatedCryoChamberMachine.getRecipeLogic();
        return ocModifier.getModifier(regulatedCryoChamberMachine, recipe,
                regulatedCryoChamberMachine.getOverclockVoltage()).andThen(parallelModifier).andThen(
                        ModifierFunction.builder().durationMultiplier(1 - recipeLogic.getCooldownReduction(coils))
                                .build());
    }
}

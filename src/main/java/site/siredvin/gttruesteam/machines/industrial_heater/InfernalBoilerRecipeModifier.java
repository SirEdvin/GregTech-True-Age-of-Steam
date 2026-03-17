package site.siredvin.gttruesteam.machines.industrial_heater;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;
import site.siredvin.gttruesteam.config.InfernalBoilerConfig;

import java.util.*;

public class InfernalBoilerRecipeModifier implements RecipeModifier {

    public static ModifierFunction SUPREME_CHARGING = ModifierFunction.builder().durationMultiplier(0.25).build();

    private static double getScalingFactor(int level) {
        return InfernalBoilerConfig.basicSupremeCoef.get() +
                InfernalBoilerConfig.coilSupremeCoef.get() * level;
    }

    @Override
    public @NotNull ModifierFunction getModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof InfernalBoilerMachine infernalBoilerMachine)) return ModifierFunction.NULL;
        int level = (int) ((infernalBoilerMachine.getCoilType().getCoilTemperature() / 900.0) - 1);
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
        var maxParallels = heatLevel.getMaxParallels();
        if (heatLevel == HeatLevel.SUPREME && recipe.data.getBoolean(TrueSteamRecipeTypes.OVERHEATED_KEY)) {
            maxParallels = (int) (maxParallels * getScalingFactor(level));
        }
        var realParallels = ParallelLogic.getParallelAmountWithoutEU(infernalBoilerMachine, recipe, maxParallels);
        if (realParallels == 0) return ModifierFunction.NULL;
        if (heatLevel == HeatLevel.SUPREME && realParallels != maxParallels) {
            return ModifierFunction.NULL;
        }
        var ocModifier = OverclockingLogic.NON_PERFECT_OVERCLOCK.getModifier(infernalBoilerMachine, recipe,
                infernalBoilerMachine.getOverclockVoltage());
        var parallelModifier = ModifierFunction.builder().modifyAllContents(ContentModifier.multiplier(realParallels))
                .parallels(realParallels).build();
        return ocModifier.andThen(parallelModifier);
    }
}

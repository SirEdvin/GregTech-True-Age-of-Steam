package site.siredvin.gttruesteam.machines.boilers;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.machine.steam.SteamSolarBoiler;
import com.gregtechceu.gtceu.common.machine.steam.SteamSolidBoilerMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import org.jetbrains.annotations.NotNull;

public class ExpandedSteamSolarBoilerMachine extends SteamSolarBoiler {
    private final boolean isHigherPressure;

    public ExpandedSteamSolarBoilerMachine(IMachineBlockEntity holder, boolean isHigherPressure, Object... args) {
        super(holder, true, args);
        this.isHigherPressure = isHigherPressure;
    }

    @Override
    protected long getBaseSteamOutput() {
        return isHigherPressure ? ConfigHolder.INSTANCE.machines.smallBoilers.hpSolarBoilerBaseOutput * 2L :
                (long) (ConfigHolder.INSTANCE.machines.smallBoilers.hpSolarBoilerBaseOutput * 1.5);
    }

    // Boiler fixes
    protected int getCooldownInterval() {
        return isHigherPressure ? 30 : 35;
    }
    public int getMaxTemperature() {
        return isHigherPressure ? 2000 : 1500;
    }

    public static @NotNull ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof ExpandedSteamSolarBoilerMachine boilerMachine)) {
            return RecipeModifier.nullWrongType(SteamBoilerMachine.class, machine);
        }
        if (!boilerMachine.isHigherPressure) return ModifierFunction.builder()
                .durationMultiplier(0.125)
                .build();

        return ModifierFunction.builder()
                .durationMultiplier(0.25)
                .build();
    }
}

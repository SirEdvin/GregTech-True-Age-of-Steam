package site.siredvin.gttruesteam.machines.boilers;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.machine.steam.SteamLiquidBoilerMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraftforge.fluids.FluidType;

import org.jetbrains.annotations.NotNull;

public class ExpandedSteamLiquidBoilerMachine extends SteamLiquidBoilerMachine {

    private final boolean isHigherPressure;

    public ExpandedSteamLiquidBoilerMachine(IMachineBlockEntity holder, boolean isHigherPressure, Object... args) {
        super(holder, true, args);
        this.isHigherPressure = isHigherPressure;
    }

    @Override
    protected @NotNull NotifiableFluidTank createFuelTank(Object @NotNull... args) {
        return new NotifiableFluidTank(this, 1, 32 * FluidType.BUCKET_VOLUME, IO.IN);
    }

    @Override
    protected long getBaseSteamOutput() {
        return isHighPressure ? ConfigHolder.INSTANCE.machines.smallBoilers.hpLiquidBoilerBaseOutput * 2L :
                (long) (ConfigHolder.INSTANCE.machines.smallBoilers.hpLiquidBoilerBaseOutput * 1.5);
    }

    // Boiler fixes
    protected int getCooldownInterval() {
        return isHigherPressure ? 30 : 35;
    }

    public int getMaxTemperature() {
        return isHigherPressure ? 2000 : 1500;
    }

    public static @NotNull ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof ExpandedSteamLiquidBoilerMachine boilerMachine)) {
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

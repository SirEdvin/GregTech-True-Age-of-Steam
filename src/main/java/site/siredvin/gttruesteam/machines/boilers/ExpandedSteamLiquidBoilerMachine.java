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

    private final double scaling;

    public ExpandedSteamLiquidBoilerMachine(IMachineBlockEntity holder, double scaling, Object... args) {
        super(holder, true, args);
        this.scaling = scaling;
    }

    @Override
    protected @NotNull NotifiableFluidTank createFuelTank(Object @NotNull... args) {
        return new NotifiableFluidTank(this, 1, (int) (8 * this.scaling * FluidType.BUCKET_VOLUME), IO.IN);
    }

    @Override
    protected @NotNull NotifiableFluidTank createSteamTank(Object... args) {
        return new NotifiableFluidTank(this, 1, (int) (8 * this.scaling * FluidType.BUCKET_VOLUME), IO.OUT);
    }

    @Override
    protected @NotNull NotifiableFluidTank createWaterTank(@SuppressWarnings("unused") Object... args) {
        return new NotifiableFluidTank(this, 1, (int) (8 * this.scaling * FluidType.BUCKET_VOLUME), IO.IN);
    }

    @Override
    protected long getBaseSteamOutput() {
        return (long) (ConfigHolder.INSTANCE.machines.smallBoilers.hpLiquidBoilerBaseOutput * this.scaling);
    }

    // Boiler fixes
    protected int getCooldownInterval() {
        return (int) (45 - 5 * this.scaling);
    }

    public int getMaxTemperature() {
        return (int) (1000 * this.scaling);
    }

    public static @NotNull ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof ExpandedSteamLiquidBoilerMachine boilerMachine)) {
            return RecipeModifier.nullWrongType(SteamBoilerMachine.class, machine);
        }

        return ModifierFunction.builder()
                .durationMultiplier(0.5 / boilerMachine.scaling)
                .build();
    }
}

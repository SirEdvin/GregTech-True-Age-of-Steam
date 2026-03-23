package site.siredvin.gttruesteam.machines.boilers;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.machine.steam.SteamSolidBoilerMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;

import org.jetbrains.annotations.NotNull;

public class ExpandedSteamSolidBoilerMachine extends SteamSolidBoilerMachine {

    private final double scaling;

    public ExpandedSteamSolidBoilerMachine(IMachineBlockEntity holder, double scaling, Object... args) {
        super(holder, true, args);
        this.scaling = scaling;
        this.waterTank.getStorages()[0].setCapacity((int) (this.waterTank.getStorages()[0].getCapacity() * scaling));
        this.steamTank.getStorages()[0].setCapacity((int) (this.steamTank.getStorages()[0].getCapacity() * scaling));
    }

    @Override
    protected long getBaseSteamOutput() {
        return (long) (ConfigHolder.INSTANCE.machines.smallBoilers.hpSolidBoilerBaseOutput * this.scaling);
    }

    // Boiler fixes
    protected int getCooldownInterval() {
        return (int) (45 - 5 * this.scaling);
    }

    public static @NotNull ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof ExpandedSteamSolidBoilerMachine)) {
            return RecipeModifier.nullWrongType(SteamBoilerMachine.class, machine);
        }

        return ModifierFunction.builder()
                .durationMultiplier(0.5)
                .durationMultiplier(0.5)
                .build();
    }
}

package site.siredvin.gttruesteam.machines.industrial_gas_pressurizer;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;

import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.config.GasPressurizerConfig;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

public class IndustrialGasPressurizerMachine extends WorkableElectricMultiblockMachine {

    public IndustrialGasPressurizerMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object @NotNull... args) {
        return new IndustrialGasPressurizerRecipeLogic(this);
    }

    @Override
    public @NotNull IndustrialGasPressurizerRecipeLogic getRecipeLogic() {
        return (IndustrialGasPressurizerRecipeLogic) super.getRecipeLogic();
    }

    public PerfectConditionState getState() {
        // Condition 1: more than 2 seconds since last crafting
        Instant lastCraftTime = Instant.ofEpochMilli(getRecipeLogic().getLastCraftingTime());
        if (!Instant.now().isAfter(lastCraftTime.plusSeconds(2))) {
            return PerfectConditionState.ON_COOLDOWN;
        }

        // Condition 2: all fluid input tanks are between 35% and 85% capacity
        var inputHandlers = getCapabilitiesFlat()
                .getOrDefault(IO.IN, Collections.emptyMap())
                .getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList());

        if (inputHandlers.isEmpty()) {
            return PerfectConditionState.UNREACHABLE;
        }

        var storages = inputHandlers.stream()
                .filter(handler -> handler instanceof NotifiableFluidTank)
                .map(handler -> (NotifiableFluidTank) handler)
                .flatMap(tank -> Arrays.stream(tank.getStorages()));

        for (Iterator<CustomFluidTank> it = storages.iterator(); it.hasNext();) {
            var storage = it.next();
            int capacity = storage.getCapacity();
            if (capacity == 0) return PerfectConditionState.UNREACHABLE;
            double ratio = (double) storage.getFluidAmount() / capacity;
            if (ratio < GasPressurizerConfig.lowerThreshold.get()) return PerfectConditionState.TOO_LOW_FLUID_LEVEL;
            if (ratio > GasPressurizerConfig.upperThreshold.get()) return PerfectConditionState.TOO_HIGH_FLUID_LEVEL;
        }

        return PerfectConditionState.REACHED;
    }
}

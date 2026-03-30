package site.siredvin.gttruesteam.machines.shared.cooling;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import lombok.Getter;
import lombok.Setter;
import site.siredvin.gttruesteam.api.ICoolingCoilType;

public class PassiveCoolingRecipeLogic extends RecipeLogic {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            PassiveCoolingRecipeLogic.class,
            RecipeLogic.MANAGED_FIELD_HOLDER);

    @Getter
    @Setter
    @DescSynced
    @Persisted
    private int aggregatedCoolingCapacity = 0;

    private final int capacityFactor;
    private final int rateFactor;

    public PassiveCoolingRecipeLogic(IRecipeLogicMachine machine) {
        this(machine, 1, 1);
    }

    public PassiveCoolingRecipeLogic(IRecipeLogicMachine machine, int capacityFactor, int rateFactor) {
        super(machine);
        this.capacityFactor = capacityFactor;
        this.rateFactor = rateFactor;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public void tickAggregatedCoolingCapacity(ICoolingCoilType coilType) {
        var realCapacity = coilType.getCoolingCapacity() * capacityFactor;
        if (this.aggregatedCoolingCapacity < realCapacity) {
            this.aggregatedCoolingCapacity = Math.min(
                    realCapacity,
                    this.aggregatedCoolingCapacity + this.rateFactor * coilType.getCoolingRate());
        }
    }
}

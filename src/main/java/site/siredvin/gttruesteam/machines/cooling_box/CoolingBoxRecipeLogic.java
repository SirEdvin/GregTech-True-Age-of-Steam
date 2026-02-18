package site.siredvin.gttruesteam.machines.cooling_box;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import lombok.Getter;
import lombok.Setter;
import site.siredvin.gttruesteam.api.ICoolingCoilType;

public class CoolingBoxRecipeLogic extends RecipeLogic {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CoolingBoxRecipeLogic.class,
            RecipeLogic.MANAGED_FIELD_HOLDER);

    @Getter
    @Setter
    @DescSynced
    @Persisted
    private int aggregatedCoolingCapacity = 0;

    public CoolingBoxRecipeLogic(IRecipeLogicMachine machine) {
        super(machine);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public void tickAggregatedCoolingCapacity(ICoolingCoilType coilType) {
        if (this.aggregatedCoolingCapacity < coilType.getCoolingCapacity()) {
            this.aggregatedCoolingCapacity = Math.min(
                    coilType.getCoolingCapacity(),
                    this.aggregatedCoolingCapacity + coilType.getCoolingRate());
        }
    }
}

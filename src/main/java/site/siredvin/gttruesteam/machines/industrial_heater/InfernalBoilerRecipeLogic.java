package site.siredvin.gttruesteam.machines.industrial_heater;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import lombok.Getter;
import lombok.Setter;

public class InfernalBoilerRecipeLogic extends RecipeLogic {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(InfernalBoilerRecipeLogic.class,
            RecipeLogic.MANAGED_FIELD_HOLDER);

    @Getter
    @Setter
    @DescSynced
    @Persisted
    private int infernalCharges = 512;

    @Getter
    @Setter
    @DescSynced
    @Persisted
    private int cycleCounter = 0;

    @DescSynced
    @Persisted
    private int cycleCounterReductionFactor = 0;

    public InfernalBoilerRecipeLogic(IRecipeLogicMachine machine) {
        super(machine);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public void trackCycle(int coilLevel) {
        this.cycleCounterReductionFactor = 0;
        if (this.infernalCharges > 0) {
            this.infernalCharges--;
        }
        if (this.cycleCounter < HeatLevel.MAX.getRequiredHeat()) {
            this.cycleCounter += coilLevel;
        }
    }

    public void decreaseCycleCounter() {
        this.cycleCounter = Math.max(0, (int) (this.cycleCounter - Math.pow(2, this.cycleCounterReductionFactor)));
        this.cycleCounterReductionFactor++;
    }

    public HeatLevel getHeatLevel() {
        for (var heatLevel : HeatLevel.HEAT_LEVELS) {
            if (this.cycleCounter > heatLevel.getRequiredHeat())
                return heatLevel;
        }
        return HeatLevel.NONE;
    }
}

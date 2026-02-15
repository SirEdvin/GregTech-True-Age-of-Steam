package site.siredvin.gttruesteam.machines.industrial_heater;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import lombok.Getter;
import lombok.Setter;

public class HeaterRecipeLogic extends RecipeLogic {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(HeaterRecipeLogic.class,
            RecipeLogic.MANAGED_FIELD_HOLDER);

    @Getter
    @Setter
    @DescSynced
    @Persisted
    private int pureChargers = 256;

    @Getter
    @Setter
    @DescSynced
    @Persisted
    private int cycleCounter = 0;

    @DescSynced
    @Persisted
    private int cycleCounterReductionFactor = 0;

    public HeaterRecipeLogic(IRecipeLogicMachine machine) {
        super(machine);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public void trackCycle(int coilLevel) {
        this.cycleCounterReductionFactor = 0;
        if (this.pureChargers > 0) {
            this.pureChargers--;
        }
        if (this.cycleCounter < HeatLevel.MAX.getRequiredLevel()) {
            this.cycleCounter += coilLevel;
        }
    }

    public void decreaseCycleCounter() {
        this.cycleCounter = Math.max(0, (int) (this.cycleCounter - Math.pow(2, this.cycleCounterReductionFactor)));
        this.cycleCounterReductionFactor++;
    }

    public HeatLevel getHeatLevel() {
        for (var heatLevel : HeatLevel.HEAT_LEVELS) {
            if (this.cycleCounter > heatLevel.getRequiredLevel())
                return heatLevel;
        }
        return HeatLevel.NONE;
    }
}

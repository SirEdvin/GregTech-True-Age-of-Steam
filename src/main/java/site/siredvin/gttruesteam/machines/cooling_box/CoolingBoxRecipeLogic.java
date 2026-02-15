package site.siredvin.gttruesteam.machines.cooling_box;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import site.siredvin.gttruesteam.machines.industrial_heater.HeatLevel;

public class CoolingBoxRecipeLogic extends RecipeLogic {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CoolingBoxRecipeLogic.class,
            RecipeLogic.MANAGED_FIELD_HOLDER);

    public CoolingBoxRecipeLogic(IRecipeLogicMachine machine) {
        super(machine);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}

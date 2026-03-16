package site.siredvin.gttruesteam.machines.industrial_gas_pressurizer;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

public class IndustrialGasPressurizerRecipeLogic extends RecipeLogic {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            IndustrialGasPressurizerRecipeLogic.class,
            RecipeLogic.MANAGED_FIELD_HOLDER);

    @Getter
    @Setter
    @DescSynced
    @Persisted
    private Long lastCraftingTime = Instant.EPOCH.getEpochSecond();

    public IndustrialGasPressurizerRecipeLogic(IRecipeLogicMachine machine) {
        super(machine);
    }

    @Override
    public void onRecipeFinish() {
        this.lastCraftingTime = Instant.now().toEpochMilli();
        super.onRecipeFinish();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}

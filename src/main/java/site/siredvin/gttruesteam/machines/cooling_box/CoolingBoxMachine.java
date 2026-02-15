package site.siredvin.gttruesteam.machines.cooling_box;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import org.jetbrains.annotations.NotNull;

public class CoolingBoxMachine extends WorkableMultiblockMachine {

    public CoolingBoxMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object @NotNull ... args) {
        return new CoolingBoxRecipeLogic(this);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
    }
}

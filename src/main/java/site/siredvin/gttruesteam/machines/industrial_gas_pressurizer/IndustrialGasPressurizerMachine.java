package site.siredvin.gttruesteam.machines.industrial_gas_pressurizer;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import org.jetbrains.annotations.NotNull;

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
}

package site.siredvin.gttruesteam.machines.industrial_heater;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;

public class InfernalBoilerMachine extends CoilWorkableElectricMultiblockMachine {

    private int resetCounter = 0;

    public InfernalBoilerMachine(IMachineBlockEntity holder) {
        super(holder);
        this.subscribeServerTick(() -> {
            if (!this.isActive() || this.getRecipeLogic().getInfernalCharges() <= 0) {
                this.resetCounter++;
                if (this.resetCounter >= 20 * this.getCoilType().getLevel()) {
                    this.getRecipeLogic().decreaseCycleCounter();
                    this.resetCounter = 0;
                }
            }
        });
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object @NotNull... args) {
        return new InfernalBoilerRecipeLogic(this);
    }

    @Override
    public @NotNull InfernalBoilerRecipeLogic getRecipeLogic() {
        return (InfernalBoilerRecipeLogic) super.getRecipeLogic();
    }

    @Override
    public void afterWorking() {
        var logic = this.getRecipeLogic();
        logic.trackCycle(this.getCoilType().getLevel());
        var lastRecipe = logic.getLastRecipe();
        if (lastRecipe != null && lastRecipe.data.contains(TrueSteamRecipeTypes.INFERNAL_CYCLES_DATA_KEY)) {
            logic.setInfernalCharges(lastRecipe.data.getInt(TrueSteamRecipeTypes.INFERNAL_CYCLES_DATA_KEY));
        }
        super.afterWorking();
    }
}

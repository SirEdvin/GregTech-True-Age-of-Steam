package site.siredvin.gttruesteam.machines.industrial_heater;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;

public class IndustrialHeaterMachine extends CoilWorkableElectricMultiblockMachine {

    private int resetCounter = 0;

    public IndustrialHeaterMachine(IMachineBlockEntity holder) {
        super(holder);
        this.subscribeServerTick(() -> {
            if (!this.isActive() || this.getHeaterRecipeLogic().getPureChargers() <= 0) {
                this.resetCounter++;
                if (this.resetCounter >= 10 * this.getCoilType().getLevel()) {
                    this.getHeaterRecipeLogic().decreaseCycleCounter();
                    this.resetCounter = 0;
                }
            }
        });
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object @NotNull... args) {
        return new HeaterRecipeLogic(this);
    }

    public HeaterRecipeLogic getHeaterRecipeLogic() {
        return (HeaterRecipeLogic) this.recipeLogic;
    }

    @Override
    public void afterWorking() {
        var logic = this.getHeaterRecipeLogic();
        logic.trackCycle(this.getCoilType().getLevel());
        var lastRecipe = logic.getLastRecipe();
        if (lastRecipe != null && lastRecipe.data.contains(TrueSteamRecipeTypes.PURE_CYCLES_DATA_KEY)) {
            logic.setPureChargers(Math.min(lastRecipe.data.getInt(TrueSteamRecipeTypes.PURE_CYCLES_DATA_KEY), 256));
        }
        super.afterWorking();
    }
}

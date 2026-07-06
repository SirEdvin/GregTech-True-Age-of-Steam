package site.siredvin.gttruesteam.machines.regulated_cryo_chamber;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.machines.shared.cooling.CoolingCoilElectricMachine;

public class RegulatedCryoChamberMachine extends CoolingCoilElectricMachine {

    public RegulatedCryoChamberMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object @NotNull... args) {
        return new RegulatedCryoChamberRecipeLogic(this);
    }

    @Override
    public @NotNull RegulatedCryoChamberRecipeLogic getRecipeLogic() {
        return (RegulatedCryoChamberRecipeLogic) this.recipeLogic;
    }
}

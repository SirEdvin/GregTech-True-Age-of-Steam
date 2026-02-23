package site.siredvin.gttruesteam.machines.regulated_cryo_chamber;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.TrueSteamPredicates;
import site.siredvin.gttruesteam.api.ICoolingCoilType;
import site.siredvin.gttruesteam.common.TSCoilType;

public class RegulatedCryoChamberMachine extends WorkableElectricMultiblockMachine {

    @Getter
    private ICoolingCoilType coilType = TSCoilType.FROSTBITE_MAGNALIUM;

    public RegulatedCryoChamberMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        var type = getMultiblockState().getMatchContext().get(TrueSteamPredicates.COOLING_COIL_TYPE_MARK);
        if (type instanceof ICoolingCoilType coil) {
            this.coilType = coil;
        }
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

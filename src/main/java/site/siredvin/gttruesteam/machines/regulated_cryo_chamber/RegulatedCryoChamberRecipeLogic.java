package site.siredvin.gttruesteam.machines.regulated_cryo_chamber;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import site.siredvin.gttruesteam.api.ICoolingCoilType;

public class RegulatedCryoChamberRecipeLogic extends RecipeLogic {

    public RegulatedCryoChamberRecipeLogic(IRecipeLogicMachine machine) {
        super(machine);
    }

    public float getCooldownReduction(ICoolingCoilType coil) {
        var reductionFactor = 1 - coil.getActiveCoolingReduction();
        var slidingReductionInfo = 1 - Math.max(0, (100.0f - getConsecutiveRecipes()) / 100.0f);
        return reductionFactor * slidingReductionInfo;
    }
}

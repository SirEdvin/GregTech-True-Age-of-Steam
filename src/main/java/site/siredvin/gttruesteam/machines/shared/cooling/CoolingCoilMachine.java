package site.siredvin.gttruesteam.machines.shared.cooling;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;

import lombok.Getter;
import site.siredvin.gttruesteam.TrueSteamPredicates;
import site.siredvin.gttruesteam.api.ICoolingCoilType;
import site.siredvin.gttruesteam.common.TSCoilType;

public class CoolingCoilMachine extends WorkableMultiblockMachine {

    @Getter
    private ICoolingCoilType coilType = TSCoilType.FROSTBITE_MAGNALIUM;

    public CoolingCoilMachine(IMachineBlockEntity holder, Object... args) {
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
}

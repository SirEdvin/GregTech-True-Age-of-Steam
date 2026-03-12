package site.siredvin.gttruesteam.machines.cim;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

import net.minecraft.core.BlockPos;

import site.siredvin.gttruesteam.TrueSteamPredicates;

import java.util.List;

public class ConceptInfusionMatrixMachine extends WorkableElectricMultiblockMachine {

    private int resetCounter = 0;

    public ConceptInfusionMatrixMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        subscribeServerTick(() -> {
            if (isActive()) {
                if (isMachinesRunning()) {
                    resetCounter = 0;
                } else {
                    resetCounter += 1;
                    if (resetCounter > 4) {
                        this.recipeLogic.interruptRecipe();
                        resetCounter = 0;
                    }
                }
            }
        });
    }

    public boolean isMachinesRunning() {
        if (!isFormed)
            return false;
        List<BlockPos> machines = getMultiblockState().getMatchContext().get(TrueSteamPredicates.MACHINE_POS_LIST_MARK);
        if (machines == null)
            return false;
        var level = getLevel();
        if (level == null)
            return false;
        for (var pos : machines) {
            var entity = level.getBlockEntity(pos);
            if (entity instanceof MetaMachineBlockEntity) {
                var meta = ((MetaMachineBlockEntity) entity).metaMachine;
                if (meta instanceof IWorkable workable) {
                    if (!workable.isActive()) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public String machineRecipe() {
        if (!isFormed)
            return "Unknown";
        List<BlockPos> machines = getMultiblockState().getMatchContext().get(TrueSteamPredicates.MACHINE_POS_LIST_MARK);
        if (machines == null)
            return "Unknown";
        var level = getLevel();
        if (level == null)
            return "Unknown";
        var entity = level.getBlockEntity(machines.get(0));
        if (entity instanceof MetaMachineBlockEntity) {
            var meta = ((MetaMachineBlockEntity) entity).metaMachine;
            if (meta instanceof IRecipeLogicMachine recipeLogicMachine) {
                return recipeLogicMachine.getRecipeType().registryName.toLanguageKey();
            }
        }
        return "Unknown";
    }
}

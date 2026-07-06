package site.siredvin.gttruesteam.machines.industrial_coating_line;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.Fluid;

import site.siredvin.gttruesteam.TrueSteamPredicates;
import site.siredvin.gttruesteam.api.ICoatingMachine;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class IndustrialCoatingLineMachine extends WorkableElectricMultiblockMachine implements ICoatingMachine {

    private List<BlockPos> fluidCellPositions = Collections.emptyList();

    public IndustrialCoatingLineMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        var positions = getMultiblockState().getMatchContext()
                .get(TrueSteamPredicates.INDUSTRIAL_COATING_FLUID_POS_MARK);
        fluidCellPositions = positions instanceof List<?> list ?
                list.stream().filter(BlockPos.class::isInstance).map(BlockPos.class::cast).toList() :
                Collections.emptyList();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        fluidCellPositions = Collections.emptyList();
    }

    @Override
    public boolean hasFluid(Fluid fluid) {
        return countFluidCells(fluid) > 0;
    }

    @Override
    public int countFluidCells(Fluid fluid) {
        var level = getLevel();
        if (level == null || fluidCellPositions.isEmpty()) return 0;
        int count = 0;
        for (var pos : fluidCellPositions) {
            var fluidState = Objects.requireNonNull(level).getBlockState(pos).getFluidState();
            if (!fluidState.isEmpty() && fluidState.isSource() && fluidState.is(fluid)) {
                count++;
            }
        }
        return count;
    }
}

package site.siredvin.gttruesteam.common;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import site.siredvin.gttruesteam.machines.parts.HeatHatchMachine;

/** Connections live entirely in block state; this block has no block entity or ticker. */
public class InsulatedHeatPipeBlock extends PipeBlock {
    public InsulatedHeatPipeBlock(Properties properties) {
        super(0.25f, properties);
        BlockState state = stateDefinition.any();
        for (var property : PROPERTY_BY_DIRECTION.values()) state = state.setValue(property, false);
        registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    private boolean connects(LevelAccessor level, BlockPos neighbor, Direction direction) {
        if (!level.hasChunkAt(neighbor)) return false;
        if (level.getBlockState(neighbor).getBlock() instanceof InsulatedHeatPipeBlock) return true;
        return MetaMachine.getMachine(level, neighbor) instanceof HeatHatchMachine hatch &&
                hatch.getFrontFacing() == direction.getOpposite();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        for (Direction direction : Direction.values()) {
            state = state.setValue(PROPERTY_BY_DIRECTION.get(direction),
                    connects(context.getLevel(), context.getClickedPos().relative(direction), direction));
        }
        return state;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(PROPERTY_BY_DIRECTION.get(direction), connects(level, neighborPos, direction));
    }
}

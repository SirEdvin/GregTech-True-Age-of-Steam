package site.siredvin.gttruesteam.common;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
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
public class InsulatedHeatPipeBlock extends PipeBlock implements com.gregtechceu.gtceu.api.item.tool.IToolGridHighlight {
    public InsulatedHeatPipeBlock(Properties properties) {
        super(0.25f, properties);
        BlockState state = stateDefinition.any();
        for (var property : PROPERTY_BY_DIRECTION.values()) state = state.setValue(property, false);
        registerDefaultState(state);
    }

    public double transferCoefficient() { return 16; }

    public static int tintColor(int index) {
        return switch (index) {
            case 0 -> 0xff000000 | site.siredvin.gttruesteam.TrueSteamConcepts.HeatingConcept.getMaterial().getMaterialRGB();
            case 1 -> 0xff000000 | site.siredvin.gttruesteam.TrueSteamConcepts.InsertionConcept.getMaterial().getMaterialRGB();
            default -> -1;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level,
                                                              BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        if (context instanceof net.minecraft.world.phys.shapes.EntityCollisionContext entityContext &&
                entityContext.getEntity() instanceof Player player) {
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack held = player.getItemInHand(hand);
                if (ToolHelper.getToolTypes(held).contains(GTToolType.WRENCH) ||
                        held.getItem() instanceof net.minecraft.world.item.BlockItem item &&
                                item.getBlock() instanceof InsulatedHeatPipeBlock) {
                    return net.minecraft.world.phys.shapes.Shapes.block();
                }
            }
        }
        return super.getShape(state, level, pos, context);
    }

    @Override
    public net.minecraft.world.phys.shapes.VoxelShape getCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter level,
                                                                       BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        return super.getShape(state, level, pos, context);
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
        Direction attached = context.getClickedFace().getOpposite();
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = context.getClickedPos().relative(direction);
            if (!context.getLevel().hasChunkAt(neighbor)) continue;
            BlockState other = context.getLevel().getBlockState(neighbor);
            state = state.setValue(PROPERTY_BY_DIRECTION.get(direction),
                    direction == attached && connects(context.getLevel(), neighbor, direction) ||
                            other.getBlock() instanceof InsulatedHeatPipeBlock && isConnected(other, direction.getOpposite()));
        }
        return state;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide) for (Direction direction : Direction.values()) {
            if (isConnected(state, direction)) setConnection(level, pos, direction, true);
        }
    }

    public static boolean isConnected(BlockState state, Direction direction) {
        return state.getValue(PROPERTY_BY_DIRECTION.get(direction));
    }

    public static int connectionMask(BlockState state) {
        int mask = 0;
        for (Direction direction : Direction.values()) if (isConnected(state, direction)) mask |= 1 << direction.ordinal();
        return mask;
    }

    public static void setConnection(Level level, BlockPos pos, Direction direction, boolean open) {
        if (level.isClientSide || !level.hasChunkAt(pos)) return;
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof InsulatedHeatPipeBlock)) return;
        level.setBlockAndUpdate(pos, state.setValue(PROPERTY_BY_DIRECTION.get(direction), open));
        BlockPos neighbor = pos.relative(direction);
        if (!level.hasChunkAt(neighbor)) return;
        BlockState other = level.getBlockState(neighbor);
        if (other.getBlock() instanceof InsulatedHeatPipeBlock) {
            level.setBlockAndUpdate(neighbor, other.setValue(PROPERTY_BY_DIRECTION.get(direction.getOpposite()), open));
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack tool = player.getItemInHand(hand);
        if (!player.mayBuild() || !ToolHelper.getToolTypes(tool).contains(GTToolType.WRENCH) || !ToolHelper.canUse(tool)) {
            return InteractionResult.PASS;
        }
        Direction side = ICoverable.determineGridSideHit(hit);
        if (side == null) side = hit.getDirection();
        if (!level.isClientSide) {
            setConnection(level, pos, side, !isConnected(state, side));
            if (player instanceof ServerPlayer serverPlayer) {
                ToolHelper.playToolSound(GTToolType.WRENCH, serverPlayer);
                if (!player.isCreative()) ToolHelper.damageItem(tool, serverPlayer, 1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean shouldRenderGrid(Player player, BlockPos pos, BlockState state, ItemStack held, java.util.Set<GTToolType> types) {
        return types.contains(GTToolType.WRENCH);
    }

    @Override
    public com.lowdragmc.lowdraglib.gui.texture.ResourceTexture sideTips(Player player, BlockPos pos, BlockState state,
                                                                        java.util.Set<GTToolType> types, Direction side) {
        if (!types.contains(GTToolType.WRENCH)) return null;
        return isConnected(state, side) ? com.gregtechceu.gtceu.api.gui.GuiTextures.TOOL_PIPE_CONNECT :
                com.gregtechceu.gtceu.api.gui.GuiTextures.TOOL_PIPE_BLOCK;
    }
}

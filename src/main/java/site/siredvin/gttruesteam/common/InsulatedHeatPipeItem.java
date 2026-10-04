package site.siredvin.gttruesteam.common;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

public class InsulatedHeatPipeItem extends BlockItem {
    public InsulatedHeatPipeItem(Block block, Properties properties) { super(block, properties); }

    @Override
    public net.minecraft.world.InteractionResult place(BlockPlaceContext context) {
        return super.place(connectionPlacementContext(context));
    }

    private BlockPlaceContext connectionPlacementContext(BlockPlaceContext context) {
        var side = context.getClickedFace();
        // getClickedPos may already point at the occupied support when replacement is impossible.
        BlockPos base = BlockPos.containing(context.getClickLocation().subtract(
                side.getStepX() * 0.001, side.getStepY() * 0.001, side.getStepZ() * 0.001));
        if (context.getLevel().getBlockState(base).getBlock() instanceof InsulatedHeatPipeBlock) {
            var selected = ICoverable.traceCoverSide(new BlockHitResult(context.getClickLocation(), side, base, false));
            if (selected != null && context.getLevel().isEmptyBlock(base.relative(selected))) {
                return new BlockPlaceContext(context.getLevel(), context.getPlayer(), context.getHand(), context.getItemInHand(),
                        new BlockHitResult(context.getClickLocation(), selected, base, false));
            }
        }
        return context;
    }
}

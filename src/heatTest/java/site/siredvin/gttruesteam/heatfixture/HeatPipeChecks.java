package site.siredvin.gttruesteam.heatfixture;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import site.siredvin.gttruesteam.TrueSteamBlocks;
import site.siredvin.gttruesteam.common.InsulatedHeatPipeBlock;

import java.util.function.BiConsumer;

public final class HeatPipeChecks {
    public static BlockState openPipe() {
        BlockState state = TrueSteamBlocks.InsulatedHeatPipe.getDefaultState();
        for (Direction side : Direction.values()) state = state.setValue(InsulatedHeatPipeBlock.PROPERTY_BY_DIRECTION.get(side), true);
        return state;
    }

    public static void run(ServerLevel world, BiConsumer<Boolean, String> check) {
        var player = FakePlayerFactory.getMinecraft(world);
        BlockPos origin = new BlockPos(520, 130, 520);
        for (int x = -2; x <= 3; x++) for (int z = -2; z <= 2; z++) {
            world.setBlockAndUpdate(origin.offset(x, 0, z), Blocks.AIR.defaultBlockState());
        }
        var pipe = TrueSteamBlocks.InsulatedHeatPipe.get();
        world.setBlockAndUpdate(origin, pipe.defaultBlockState());
        world.setBlockAndUpdate(origin.east().south(), pipe.defaultBlockState());
        ItemStack pipes = new ItemStack(pipe.asItem(), 16);
        player.setItemInHand(InteractionHand.MAIN_HAND, pipes);
        var hit = new BlockHitResult(Vec3.atCenterOf(origin).add(0.5, 0, 0), Direction.EAST, origin, false);
        var context = new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        check.accept(((BlockItem) pipe.asItem()).place(context).consumesAction(), "pipe item places against selected face");
        check.accept(InsulatedHeatPipeBlock.isConnected(world.getBlockState(origin), Direction.EAST) &&
                InsulatedHeatPipeBlock.isConnected(world.getBlockState(origin.east()), Direction.WEST), "placement opens both ends toward support pipe");
        check.accept(!InsulatedHeatPipeBlock.isConnected(world.getBlockState(origin.east()), Direction.SOUTH), "placement does not join adjacent closed branch");
        check.accept(world.getBlockEntity(origin.east()) == null, "manual connection pipe remains block-entity-free");

        ItemStack wrench = GTMaterialItems.TOOL_ITEMS.get(GTMaterials.Steel, GTToolType.WRENCH).get().get();
        Vec3 rayStart = Vec3.atLowerCornerOf(origin).add(0.1, 0.5, -1);
        Vec3 rayEnd = Vec3.atLowerCornerOf(origin).add(0.1, 0.5, 0.5);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        check.accept(world.clip(new net.minecraft.world.level.ClipContext(rayStart, rayEnd,
                net.minecraft.world.level.ClipContext.Block.OUTLINE, net.minecraft.world.level.ClipContext.Fluid.NONE, player))
                .getType() == net.minecraft.world.phys.HitResult.Type.MISS, "empty hand ray outside thin pipe misses");
        for (ItemStack held : new ItemStack[] { wrench, pipes }) {
            player.setItemInHand(InteractionHand.MAIN_HAND, held);
            var outerHit = world.clip(new net.minecraft.world.level.ClipContext(rayStart, rayEnd,
                    net.minecraft.world.level.ClipContext.Block.OUTLINE, net.minecraft.world.level.ClipContext.Fluid.NONE, player));
            check.accept(outerHit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK && outerHit.getBlockPos().equals(origin),
                    "full block targeting outside pipe with " + held.getDescriptionId());
            check.accept(world.clip(new net.minecraft.world.level.ClipContext(rayStart, rayEnd,
                    net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, player))
                    .getType() == net.minecraft.world.phys.HitResult.Type.MISS, "tool targeting does not enlarge physical collision");
            if (held == wrench) {
                boolean wasOpen = InsulatedHeatPipeBlock.isConnected(world.getBlockState(origin), Direction.WEST);
                pipe.use(world.getBlockState(origin), world, origin, player, InteractionHand.MAIN_HAND, outerHit);
                check.accept(InsulatedHeatPipeBlock.isConnected(world.getBlockState(origin), Direction.WEST) != wasOpen,
                        "outer grid ray hit toggles adjacent side with wrench");
                pipe.use(world.getBlockState(origin), world, origin, player, InteractionHand.MAIN_HAND, outerHit);
            }
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.OFF_HAND, wrench);
        check.accept(world.clip(new net.minecraft.world.level.ClipContext(rayStart, rayEnd,
                net.minecraft.world.level.ClipContext.Block.OUTLINE, net.minecraft.world.level.ClipContext.Fluid.NONE, player))
                .getType() == net.minecraft.world.phys.HitResult.Type.BLOCK, "offhand wrench also retains full block target");
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
        int damage = wrench.getDamageValue();
        check.accept(pipe.use(world.getBlockState(origin), world, origin, player, InteractionHand.MAIN_HAND, hit).consumesAction(), "real GregTech wrench accepted");
        check.accept(wrench.getDamageValue() > damage, "survival wrench interaction consumes durability");
        check.accept(!InsulatedHeatPipeBlock.isConnected(world.getBlockState(origin), Direction.EAST) &&
                !InsulatedHeatPipeBlock.isConnected(world.getBlockState(origin.east()), Direction.WEST), "wrench closes both ends");
        world.setBlockAndUpdate(origin.north(), Blocks.STONE.defaultBlockState());
        check.accept(!InsulatedHeatPipeBlock.isConnected(world.getBlockState(origin), Direction.EAST), "neighbor update preserves closed port");
        pipe.use(world.getBlockState(origin), world, origin, player, InteractionHand.MAIN_HAND, hit);
        check.accept(InsulatedHeatPipeBlock.isConnected(world.getBlockState(origin.east()), Direction.WEST), "wrench reopens both ends");
        var saved = net.minecraft.nbt.NbtUtils.writeBlockState(world.getBlockState(origin));
        check.accept(net.minecraft.nbt.NbtUtils.readBlockState(world.holderLookup(net.minecraft.core.registries.Registries.BLOCK), saved).equals(world.getBlockState(origin)), "connection states survive block-state serialization");

        // An open arm remains open without a neighbor, and a new pipe picks it up.
        InsulatedHeatPipeBlock.setConnection(world, origin.east(), Direction.EAST, true);
        world.setBlockAndUpdate(origin.east(2).south(), Blocks.IRON_BLOCK.defaultBlockState());
        player.setItemInHand(InteractionHand.MAIN_HAND, pipes);
        var support = origin.east(2).south();
        var branchHit = new BlockHitResult(Vec3.atCenterOf(support).add(0, 0, -0.5), Direction.NORTH, support, false);
        check.accept(((BlockItem) pipe.asItem()).place(new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND, branchHit))).consumesAction(), "pipe placed beside pre-opened port");
        check.accept(InsulatedHeatPipeBlock.isConnected(world.getBlockState(origin.east(2)), Direction.WEST), "placement joins pre-opened neighbor arm");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        check.accept(!pipe.use(world.getBlockState(origin), world, origin, player, InteractionHand.MAIN_HAND, hit).consumesAction(), "empty hand does not change connections");
        for (Direction direction : Direction.values()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
            boolean prior = InsulatedHeatPipeBlock.isConnected(world.getBlockState(origin), direction);
            var sideHit = new BlockHitResult(Vec3.atCenterOf(origin).add(direction.getStepX() * 0.5,
                    direction.getStepY() * 0.5, direction.getStepZ() * 0.5), direction, origin, false);
            pipe.use(world.getBlockState(origin), world, origin, player, InteractionHand.MAIN_HAND, sideHit);
            check.accept(InsulatedHeatPipeBlock.isConnected(world.getBlockState(origin), direction) != prior, "wrench supports side " + direction);
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, pipes);
        world.removeBlock(origin.north(), false);
        var edgeHit = new BlockHitResult(Vec3.atLowerCornerOf(origin).add(1, 0.5, 0.1), Direction.EAST, origin, false);
        check.accept(((BlockItem) pipe.asItem()).place(new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND, edgeHit))).consumesAction(), "grid-edge pipe placement succeeds");
        check.accept(world.getBlockState(origin.north()).is(pipe), "grid-edge placement selects adjacent face rather than clicked face");
        check.accept(InsulatedHeatPipeBlock.isConnected(world.getBlockState(origin.north()), Direction.SOUTH) &&
                InsulatedHeatPipeBlock.isConnected(world.getBlockState(origin), Direction.NORTH), "grid-edge placement joins both pipe ends");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        for (int x = -2; x <= 3; x++) for (int z = -2; z <= 2; z++) world.removeBlock(origin.offset(x, 0, z), false);
    }
}

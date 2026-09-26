package site.siredvin.gttruesteam.heatfixture;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamMachines;
import site.siredvin.gttruesteam.TrueSteamPartAbilities;
import site.siredvin.gttruesteam.machines.shared.heat.HeatMultiblockMachine;
import com.mojang.brigadier.arguments.DoubleArgumentType;

import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.Commands.argument;

/** Compiled only into the opt-in development source set, never the distributable JAR. */
public final class HeatFixture extends HeatMultiblockMachine {

    private static MultiblockMachineDefinition definition;
    private static MultiblockMachineDefinition remoteDefinition;
    // Separate endpoints beyond the controller ticket's retained generation-chunk neighborhood.
    public static final int REMOTE_HATCH_DISTANCE = 256;

    public HeatFixture(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public double getHeatCapacity() {
        if (hasBlock(Blocks.REDSTONE_BLOCK)) return 0;
        if (hasBlock(Blocks.LAPIS_BLOCK) || hasBlock(Blocks.DIAMOND_BLOCK)) return 100000;
        return hasBlock(Blocks.GOLD_BLOCK) ? 2000 : 1000;
    }

    @Override
    public double getMaxTemperature() {
        if (hasBlock(Blocks.DIAMOND_BLOCK)) return 1300;
        return hasBlock(Blocks.GOLD_BLOCK) ? 320 : 310;
    }

    private boolean hasBlock(Block block) {
        if (!(getLevel() instanceof ServerLevel level)) return false;
        return getMultiblockState().getCache().stream().anyMatch(pos -> {
            var chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
            return chunk != null && chunk.getBlockState(pos).is(block);
        });
    }

    @Mod.EventBusSubscriber(modid = "gttruesteam", bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent
        @SuppressWarnings("rawtypes")
        public static void machines(GTCEuAPI.RegisterEvent event) {
            if (event.getGenericType() != MachineDefinition.class) return;
            definition = GTTrueSteam.REGISTRATE.multiblock("development_heat_fixture", HeatFixture::new)
                    .langValue("Development Heat Fixture")
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                    .appearanceBlock(() -> Blocks.IRON_BLOCK)
                    .pattern(def -> FactoryBlockPattern.start(RelativeDirection.RIGHT, RelativeDirection.UP, RelativeDirection.FRONT).aisle("CHH")
                            .where("C", Predicates.controller(Predicates.blocks(def.get())))
                            .where("H", Predicates.abilities(TrueSteamPartAbilities.HEAT)
                                    .or(Predicates.blocks(Blocks.IRON_BLOCK, Blocks.GOLD_BLOCK, Blocks.DIAMOND_BLOCK, Blocks.REDSTONE_BLOCK, Blocks.LAPIS_BLOCK)))
                            .build())
                    .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                            GTCEu.id("block/multiblock/electric_blast_furnace"))
                    .register();
            remoteDefinition = GTTrueSteam.REGISTRATE.multiblock("development_remote_heat_fixture", HeatFixture::new)
                    .langValue("Development Remote Heat Fixture")
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                    .appearanceBlock(() -> Blocks.IRON_BLOCK)
                    .pattern(def -> FactoryBlockPattern.start(RelativeDirection.RIGHT, RelativeDirection.UP, RelativeDirection.FRONT)
                            .aisle("C" + "I".repeat(REMOTE_HATCH_DISTANCE - 1) + "H")
                            .where("C", Predicates.controller(Predicates.blocks(def.get())))
                            .where("I", Predicates.blocks(Blocks.IRON_BLOCK))
                            .where("H", Predicates.abilities(TrueSteamPartAbilities.HEAT))
                            .build())
                    .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                            GTCEu.id("block/multiblock/electric_blast_furnace"))
                    .register();
        }
    }

    @Mod.EventBusSubscriber(modid = "gttruesteam")
    public static final class Commands {
        @SubscribeEvent
        public static void register(RegisterCommandsEvent event) {
            event.getDispatcher().register(literal("heatfixture").requires(source -> source.hasPermission(2))
                    .then(literal("place").executes(context -> {
                        ServerLevel level = context.getSource().getLevel();
                        BlockPos origin = BlockPos.containing(context.getSource().getPosition()).offset(0, 1, 3);
                        place(level, origin, false, 3);
                        place(level, origin.offset(0, 0, 6), true, 0);
                        for (int z = 1; z < 6; z++) level.setBlockAndUpdate(origin.offset(1, 0, z), GTBlocks.COMPUTER_HEAT_VENT.getDefaultState());
                        var first = MetaMachine.getMachine(level, origin.offset(1, 0, 0));
                        var second = MetaMachine.getMachine(level, origin.offset(1, 0, 6));
                        first.setFrontFacing(Direction.SOUTH);
                        second.setFrontFacing(Direction.NORTH);
                        context.getSource().sendSuccess(() -> Component.literal("Fixture placed at " + origin + "; use heatfixture seed after formation."), false);
                        return 1;
                    }))
                    .then(literal("add").then(argument("pos", BlockPosArgument.blockPos())
                            .then(argument("joules", DoubleArgumentType.doubleArg()).executes(context -> {
                                BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
                                if (MetaMachine.getMachine(context.getSource().getLevel(), pos) instanceof HeatFixture fixture) {
                                    double accepted = fixture.changeHeat(DoubleArgumentType.getDouble(context, "joules"), false);
                                    context.getSource().sendSuccess(() -> Component.literal("Accepted " + accepted + " J at " + pos), false);
                                    return accepted != 0 ? 1 : 0;
                                }
                                return 0;
                            }))))
                    .then(literal("seed").executes(context -> {
                        var player = context.getSource().getPlayerOrException();
                        var hit = player.pick(8, 0, false);
                        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK &&
                                MetaMachine.getMachine(player.level(), blockHit.getBlockPos()) instanceof HeatFixture fixture) {
                            double accepted = fixture.changeHeat(1100, false);
                            context.getSource().sendSuccess(() -> Component.literal("Accepted " + accepted + " J; T=" + fixture.getTemperature() + ", melting=" + fixture.isMelting()), false);
                            return accepted != 0 ? 1 : 0;
                        }
                        return 0;
                    })));
        }

        public static void place(ServerLevel level, BlockPos pos, boolean gold, int hatchIndex) {
            level.setBlockAndUpdate(pos, definition.defaultBlockState());
            MetaMachine.getMachine(level, pos).setFrontFacing(Direction.NORTH);
            level.setBlockAndUpdate(pos.east(), TrueSteamMachines.HEAT_HATCHES.get(hatchIndex).defaultBlockState());
            level.setBlockAndUpdate(pos.east(2), (gold ? Blocks.GOLD_BLOCK : Blocks.IRON_BLOCK).defaultBlockState());
        }

        public static void placeRemote(ServerLevel level, BlockPos pos) {
            level.setBlockAndUpdate(pos.east(REMOTE_HATCH_DISTANCE), Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(pos, remoteDefinition.defaultBlockState());
            MetaMachine.getMachine(level, pos).setFrontFacing(Direction.NORTH);
            for (int x = 1; x < REMOTE_HATCH_DISTANCE; x++) level.setBlockAndUpdate(pos.east(x), Blocks.IRON_BLOCK.defaultBlockState());
            level.setBlockAndUpdate(pos.east(REMOTE_HATCH_DISTANCE), TrueSteamMachines.HEAT_HATCHES.get(0).defaultBlockState());
        }
    }
}

package site.siredvin.gttruesteam.heatfixture;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;

import site.siredvin.gttruesteam.TrueSteamMachines;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;
import site.siredvin.gttruesteam.machines.shared.heat.DebugHeatMachine;

import java.nio.file.Files;
import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = "gttruesteam")
public final class DebugHeatChecks {
    private static ServerLevel world;
    private static long start;
    private static final BlockPos A = new BlockPos(513, 121, 512);
    private static final BlockPos B = A.east(6);
    private static final JsonArray checks = new JsonArray();
    private static double before;
    private static long deadline;

    @SubscribeEvent
    public static void started(ServerStartedEvent event) {
        if (!Boolean.getBoolean("gttruesteam.heatTestDebug")) return;
        world = event.getServer().overworld();
        world.setChunkForced(32, 32, true);
        place(A, true);
        place(B, false);
        for (int x = 515; x <= 517; x++) world.setBlockAndUpdate(new BlockPos(x, 121, 513), GTBlocks.COMPUTER_HEAT_VENT.getDefaultState());
        start = world.getGameTime();
    }

    private static void place(BlockPos pos, boolean producer) {
        for (int x = -1; x <= 1; x++) for (int y = -1; y <= 1; y++) for (int z = 0; z <= 2; z++) {
            world.setBlockAndUpdate(pos.offset(x, y, z), x == 0 && y == 0 && z == 1 ? Blocks.AIR.defaultBlockState() : Blocks.IRON_BLOCK.defaultBlockState());
        }
        world.setBlockAndUpdate(pos, (producer ? TrueSteamMachines.DEBUG_HEAT_PRODUCER : TrueSteamMachines.DEBUG_HEAT_CONSUMER).defaultBlockState());
        MetaMachine.getMachine(world, pos).setFrontFacing(Direction.NORTH);
        BlockPos hatch = pos.offset(producer ? 1 : -1, 0, 1);
        world.setBlockAndUpdate(hatch, TrueSteamMachines.HEAT_HATCHES.get(0).defaultBlockState());
        MetaMachine.getMachine(world, hatch).setFrontFacing(producer ? Direction.EAST : Direction.WEST);
    }

    private static DebugHeatMachine machine(BlockPos pos) { return (DebugHeatMachine) MetaMachine.getMachine(world, pos); }
    private static void check(boolean result, String name) {
        if (!result) throw new AssertionError(name);
        checks.add(name);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void tick(TickEvent.LevelTickEvent event) {
        if (world == null || event.level != world || event.phase != TickEvent.Phase.END) return;
        long elapsed = world.getGameTime() - start;
        try {
            if (elapsed == 10) {
                for (BlockPos pos : new BlockPos[] { A, B }) {
                    check(machine(pos).checkPatternWithLock(), "hollow 3x3x3 pattern matches " + pos);
                    machine(pos).onStructureFormed();
                }
                check(world.getRecipeManager().getAllRecipesFor(TrueSteamRecipeTypes.DEBUG_HEAT_PRODUCING).size() == 1, "one real producer recipe loaded");
                check(world.getRecipeManager().getAllRecipesFor(TrueSteamRecipeTypes.DEBUG_HEAT_CONSUMING).size() == 1, "one real consumer recipe loaded");
            }
            if (elapsed == 100) {
                check(machine(A).getStoredHeat() > 0, "producer generates heat through real recipe ticks");
                check(machine(B).getRecipeLogic().getProgress() > 0, "consumer operates using vent-delivered heat");
                machine(A).getRecipeLogic().setWorkingEnabled(false);
                machine(B).getRecipeLogic().setWorkingEnabled(false);
                machine(A).getRecipeLogic().setStatus(com.gregtechceu.gtceu.api.machine.trait.RecipeLogic.Status.SUSPEND);
                machine(B).getRecipeLogic().setStatus(com.gregtechceu.gtceu.api.machine.trait.RecipeLogic.Status.SUSPEND);
                before = machine(A).getStoredHeat() + machine(B).getStoredHeat();
            }
            if (elapsed == 140) {
                check(Math.abs(machine(A).getStoredHeat() + machine(B).getStoredHeat() - before) < 1e-8, "disabled recipes leave only conservative exchange");
                world.removeBlock(new BlockPos(516, 121, 513), false);
                machine(B).changeHeat(-machine(B).getStoredHeat(), false);
                machine(B).getRecipeLogic().setWorkingEnabled(true);
            }
            if (elapsed == 180) {
                check(machine(B).getStoredHeat() == 0 && machine(B).getRecipeLogic().isWaiting(), "consumer waits without negative energy");
                machine(B).changeHeat(20, false);
            }
            if (elapsed == 200) {
                check(machine(B).getStoredHeat() < 20, "consumer resumes when heat arrives");
                world.setBlockAndUpdate(A.offset(1, 0, 1), Blocks.IRON_BLOCK.defaultBlockState());
                check(machine(A).checkPatternWithLock(), "producer forms without any heat hatch");
                machine(A).onStructureFormed();
                machine(A).changeHeat(1100 - machine(A).getStoredHeat(), false);
                deadline = world.getGameTime() + 40;
            }
            if (deadline != 0 && world.getGameTime() == deadline) {
                check(MetaMachine.getMachine(world, A) == null, "controller-owned countdown expires without any hatch");
                finish(null);
            }
            if (elapsed > 300) throw new AssertionError("debug timeout");
        } catch (Throwable failure) { finish(failure); }
    }

    private static void finish(Throwable failure) {
        JsonObject report = new JsonObject();
        report.addProperty("passed", failure == null);
        report.add("checks", checks);
        if (failure != null) report.addProperty("failure", failure.toString());
        try { Files.writeString(Path.of("heat-debug-results.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch (Exception e) { throw new RuntimeException(e); }
        var server = world.getServer();
        world.setChunkForced(32, 32, false);
        world = null;
        server.halt(false);
    }
}

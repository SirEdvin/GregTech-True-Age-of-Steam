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
import site.siredvin.gttruesteam.machines.redstone.RedstoneHatchMachine;
import site.siredvin.gttruesteam.machines.redstone.RedstoneRule;
import site.siredvin.gttruesteam.api.RedstoneObservable;

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
    private static double beforeClosed;
    private static long deadline;

    @SubscribeEvent
    public static void started(ServerStartedEvent event) {
        if (!Boolean.getBoolean("gttruesteam.heatTestDebug")) return;
        world = event.getServer().overworld();
        world.setChunkForced(32, 32, true);
        place(A, true);
        place(B, false);
        for (int x = 515; x <= 517; x++) world.setBlockAndUpdate(new BlockPos(x, 121, 513), HeatPipeChecks.openPipe());
        start = world.getGameTime();
    }

    private static void place(BlockPos pos, boolean producer) {
        for (int x = -1; x <= 1; x++) for (int y = -1; y <= 1; y++) for (int z = 0; z <= 2; z++) {
            world.setBlockAndUpdate(pos.offset(x, y, z), x == 0 && y == 0 && z == 1 ? Blocks.AIR.defaultBlockState() : Blocks.IRON_BLOCK.defaultBlockState());
        }
        world.setBlockAndUpdate(pos, (producer ? TrueSteamMachines.DEBUG_HEAT_PRODUCER : TrueSteamMachines.DEBUG_HEAT_CONSUMER).defaultBlockState());
        MetaMachine.getMachine(world, pos).setFrontFacing(Direction.NORTH);
        BlockPos hatch = pos.offset(producer ? 1 : -1, 0, 1);
        world.setBlockAndUpdate(hatch, TrueSteamMachines.HEAT_HATCH.defaultBlockState());
        MetaMachine.getMachine(world, hatch).setFrontFacing(producer ? Direction.EAST : Direction.WEST);
        world.setBlockAndUpdate(pos.above(), TrueSteamMachines.REDSTONE_HATCHES[com.gregtechceu.gtceu.api.GTValues.HV].defaultBlockState());
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
                HeatPipeChecks.run(world, DebugHeatChecks::check);
                for (BlockPos pos : new BlockPos[] { A, B }) {
                    check(machine(pos).checkPatternWithLock(), "hollow 3x3x3 pattern matches " + pos);
                    machine(pos).onStructureFormed();
                    var text = new java.util.ArrayList<net.minecraft.network.chat.Component>();
                    machine(pos).addDisplayText(text);
                    check(text.size() == 6, "controller exposes status and all four thermal readouts " + pos);
                    check(machine(pos) instanceof com.gregtechceu.gtceu.api.machine.feature.IUIMachine,
                            "controller participates in native GTCEu right-click UI dispatch " + pos);
                    check(machine(pos).createUI(net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(world)) != null,
                            "controller constructs native GTCEu menu " + pos);
                    BlockPos hatchPos = pos.offset(pos.equals(A) ? 1 : -1, 0, 1);
                    var hatch = (site.siredvin.gttruesteam.machines.parts.HeatHatchMachine) MetaMachine.getMachine(world, hatchPos);
                    check(hatch.replacePartModelWhenFormed(), "formed hatch enables multiblock appearance " + pos);
                    check(hatch.getFormedAppearance(world.getBlockState(hatchPos), hatchPos, Direction.UP).is(Blocks.IRON_BLOCK),
                            "formed hatch inherits iron casing " + pos);
                    check(hatch.createUIWidget() != null, "heat hatch provides thermal UI " + pos);
                    var redstone = (RedstoneHatchMachine) MetaMachine.getMachine(world, pos.above());
                    var provider = redstone.provider();
                    check(provider != null, "debug structure owns redstone hatch " + pos);
                    check(provider.redstoneValues().stream().filter(value -> value.type() == RedstoneObservable.Type.FLOAT).count() >= 3,
                            "redstone editor exposes thermal float readings " + pos);
                    machine(pos).changeHeat(500 - machine(pos).getStoredHeat(), false);
                    check(provider.readRedstoneValue("heat_joules").orElseThrow().value().equals(500.0), "stored joules reading " + pos);
                    check(provider.readRedstoneValue("temperature_kelvin").orElseThrow().value().equals(machine(pos).getTemperature()), "temperature reading " + pos);
                    check(provider.readRedstoneValue("heat_capacity_percent").orElseThrow().value().equals(50.0), "half-filled capacity reading " + pos);
                    check(provider.readRedstoneValue("unknown_heat_value").isEmpty(), "unknown reading unavailable " + pos);
                    for (String id : new String[] { "heat_joules", "temperature_kelvin", "heat_capacity_percent" }) {
                        check(redstone.saveRule(redstone.rules().size(), new RedstoneRule(id, RedstoneObservable.Type.FLOAT,
                                RedstoneRule.Operator.GREATER, "0", 1 << redstone.rules().size())), "thermal threshold rule accepted " + id);
                    }
                    machine(pos).changeHeat(-machine(pos).getStoredHeat(), false);
                }
                check(world.getRecipeManager().getAllRecipesFor(TrueSteamRecipeTypes.DEBUG_HEAT_PRODUCING).size() == 1, "one real producer recipe loaded");
                check(world.getRecipeManager().getAllRecipesFor(TrueSteamRecipeTypes.DEBUG_HEAT_CONSUMING).size() == 1, "one real consumer recipe loaded");
                check(world.getBlockEntity(new BlockPos(516, 121, 513)) == null, "heat pipe has no block entity");
                check(site.siredvin.gttruesteam.TrueSteamConcepts.InsertionConcept.getCatalysts().size() ==
                        site.siredvin.gttruesteam.TrueSteamConcepts.ExtractionConcept.getCatalysts().size() +
                                site.siredvin.gttruesteam.TrueSteamConcepts.PolarizationConcept.getCatalysts().size(),
                        "insertion combines extraction and polarization catalysts");
                check(world.getRecipeManager().byKey(site.siredvin.gttruesteam.GTTrueSteam.id("assembler/insulated_heat_pipe")).isPresent(),
                        "insulated heat pipe assembler recipe registered");
                var recipe = (com.gregtechceu.gtceu.api.recipe.GTRecipe) world.getRecipeManager()
                        .byKey(site.siredvin.gttruesteam.GTTrueSteam.id("assembler/heat_hatch")).orElseThrow();
                var cap = com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability.CAP;
                check(recipe.getInputContents(cap).size() == 6, "hatch recipe has five ingredients and configured circuit");
                var output = cap.of(recipe.getOutputContents(cap).get(0).content).getItems()[0];
                check(output.is(TrueSteamMachines.HEAT_HATCH.asStack().getItem()) && output.getCount() == 2,
                        "assembler recipe outputs two simple heat hatches");
            }
            if (elapsed == 100) {
                check(machine(A).getStoredHeat() > 0, "producer generates heat through real recipe ticks");
                check(machine(B).getRecipeLogic().getProgress() > 0, "consumer operates using pipe-delivered heat");
                check(((RedstoneHatchMachine) MetaMachine.getMachine(world, A.above())).output() == 7,
                        "live hatch emits combined thermal threshold signal");
                var sender = (site.siredvin.gttruesteam.machines.parts.HeatHatchMachine) MetaMachine.getMachine(world, A.offset(1, 0, 1));
                var receiver = (site.siredvin.gttruesteam.machines.parts.HeatHatchMachine) MetaMachine.getMachine(world, B.offset(-1, 0, 1));
                check(sender.getExchangeOut() > 0 && sender.getExchangeIn() == 0, "sender reports actual outgoing joules");
                check(sender.getNetworkCoefficient() == 16 && receiver.getNetworkCoefficient() == 16,
                        "both hatches report pipe-derived network coefficient");
                check(receiver.getExchangeIn() == sender.getExchangeOut() && receiver.getExchangeOut() == 0,
                        "receiver telemetry matches sender without double counting");
                machine(A).getRecipeLogic().setWorkingEnabled(false);
                machine(B).getRecipeLogic().setWorkingEnabled(false);
                machine(A).getRecipeLogic().setStatus(com.gregtechceu.gtceu.api.machine.trait.RecipeLogic.Status.SUSPEND);
                machine(B).getRecipeLogic().setStatus(com.gregtechceu.gtceu.api.machine.trait.RecipeLogic.Status.SUSPEND);
                before = machine(A).getStoredHeat() + machine(B).getStoredHeat();
                beforeClosed = machine(A).getStoredHeat();
                site.siredvin.gttruesteam.common.InsulatedHeatPipeBlock.setConnection(world, new BlockPos(516, 121, 513), Direction.EAST, false);
            }
            if (elapsed == 120) {
                check(machine(A).getStoredHeat() == beforeClosed, "closed pipe port stops live heat exchange");
                var sender = (site.siredvin.gttruesteam.machines.parts.HeatHatchMachine) MetaMachine.getMachine(world, A.offset(1, 0, 1));
                check(sender.getExchangeOut() == 0, "closed connection clears exchange telemetry");
                site.siredvin.gttruesteam.common.InsulatedHeatPipeBlock.setConnection(world, new BlockPos(516, 121, 513), Direction.EAST, true);
            }
            if (elapsed == 140) {
                check(machine(A).getStoredHeat() < beforeClosed, "reopened port resumes live heat exchange");
                check(Math.abs(machine(A).getStoredHeat() + machine(B).getStoredHeat() - before) < 1e-8, "disabled recipes leave only conservative exchange");
                world.setBlockAndUpdate(new BlockPos(516, 121, 513), GTBlocks.COMPUTER_HEAT_VENT.getDefaultState());
                machine(B).changeHeat(-machine(B).getStoredHeat(), false);
                machine(B).getRecipeLogic().setWorkingEnabled(true);
            }
            if (elapsed == 180) {
                check(machine(B).getStoredHeat() == 0 && machine(B).getRecipeLogic().isWaiting(), "consumer waits without negative energy");
                var sender = (site.siredvin.gttruesteam.machines.parts.HeatHatchMachine) MetaMachine.getMachine(world, A.offset(1, 0, 1));
                var receiver = (site.siredvin.gttruesteam.machines.parts.HeatHatchMachine) MetaMachine.getMachine(world, B.offset(-1, 0, 1));
                check(sender.getExchangeOut() == 0 && receiver.getExchangeIn() == 0,
                        "computer heat vent no longer conducts and next exchange clears stale telemetry");
                machine(B).changeHeat(20, false);
            }
            if (elapsed == 200) {
                check(machine(B).getStoredHeat() < 20, "consumer resumes when heat arrives");
                world.setBlockAndUpdate(A.offset(1, 0, 1), Blocks.IRON_BLOCK.defaultBlockState());
                check(machine(A).checkPatternWithLock(), "producer forms without any heat hatch");
                machine(A).onStructureFormed();
                machine(A).changeHeat(1100 - machine(A).getStoredHeat(), false);
                check(Math.abs((Double) machine(A).readRedstoneValue("heat_capacity_percent").orElseThrow().value() - 110.0) < 1e-8,
                        "capacity percentage preserves overheat above 100 percent");
                machine(B).onStructureInvalid();
                check(machine(B).readRedstoneValue("heat_joules").isEmpty() &&
                        machine(B).readRedstoneValue("temperature_kelvin").isEmpty() &&
                        machine(B).readRedstoneValue("heat_capacity_percent").isEmpty(), "invalid structure exposes no thermal readings");
                check(((RedstoneHatchMachine) MetaMachine.getMachine(world, B.above())).output() == 0,
                        "invalid structure clears redstone output");
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

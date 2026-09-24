package site.siredvin.gttruesteam.heatfixture;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamMachines;
import site.siredvin.gttruesteam.machines.parts.HeatHatchMachine;
import site.siredvin.gttruesteam.machines.shared.heat.HeatNetworkManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;

/** Uses the production scheduled coordinator, not a substitute exchange algorithm. */
@Mod.EventBusSubscriber(modid = "gttruesteam")
public final class HeatNetworkChecks {

    private static final BlockPos A = new BlockPos(256, 120, 256);
    private static BlockPos B = A.offset(0, 0, 6);
    private static final BlockPos C = A.offset(0, 0, 12);
    private static final JsonArray checks = new JsonArray();
    private static final TicketType<BlockPos> ENDPOINT_TICKET = TicketType.create("heat_fixture_endpoint", Comparator.comparingLong(BlockPos::asLong));
    private static ServerLevel world;
    private static long placed;
    private static long seeded;
    private static int scenario;
    private static int topologyStage;
    private static boolean finished;

    @SubscribeEvent
    public static void started(ServerStartedEvent event) {
        if (!Boolean.getBoolean("gttruesteam.heatTestNetwork")) return;
        world = event.getServer().overworld();
        world.setChunkForced(16, 16, true);
        world.setChunkForced(16, 17, true);
        world.setChunkForced(16, 18, true);
        try {
            setup();
        } catch (Throwable failure) {
            finish(failure);
        }
    }

    private static void setup() {
        B = A.offset(0, 0, scenario == 26 || scenario == 29 ? 32 : scenario == 27 ? 33 : 6);
        topologyStage = 0;
        for (int z = 0; z <= 40; z++) {
            for (int x = 0; x <= 3; x++) world.setBlockAndUpdate(A.offset(x, 0, z), Blocks.AIR.defaultBlockState());
        }
        int sender = scenario < 16 ? scenario / 4 : 0;
        int receiver = scenario < 16 ? scenario % 4 : 0;
        HeatFixture.Commands.place(world, A, false, sender);
        HeatFixture.Commands.place(world, B, false, receiver);
        if (scenario < 16) {
            world.setBlockAndUpdate(A.east(2), Blocks.LAPIS_BLOCK.defaultBlockState());
            world.setBlockAndUpdate(B.east(2), Blocks.LAPIS_BLOCK.defaultBlockState());
        }
        hatch(A.east()).setFrontFacing(Direction.SOUTH);
        hatch(B.east()).setFrontFacing(Direction.NORTH);
        for (int z = 1; z < B.getZ() - A.getZ(); z++) world.setBlockAndUpdate(A.offset(1, 0, z), GTBlocks.COMPUTER_HEAT_VENT.getDefaultState());
        if (scenario >= 16 && scenario <= 19) {
            world.setBlockAndUpdate(A.east(2), TrueSteamMachines.HEAT_HATCHES.get(3).defaultBlockState());
            world.setBlockAndUpdate(B.east(2), TrueSteamMachines.HEAT_HATCHES.get(1).defaultBlockState());
            hatch(A.east(2)).setFrontFacing(scenario == 18 ? Direction.EAST : Direction.SOUTH);
            hatch(B.east(2)).setFrontFacing(Direction.NORTH);
            for (int z = 1; z < 6; z++) world.setBlockAndUpdate(A.offset(2, 0, z), GTBlocks.COMPUTER_HEAT_VENT.getDefaultState());
        }
        if (scenario == 19) world.removeBlock(A.offset(1, 0, 3), false);
        if (scenario == 20) hatch(A.east()).setFrontFacing(Direction.WEST);
        if (scenario == 21) world.removeBlock(A.offset(1, 0, 3), false);
        if (scenario == 22) {
            world.setBlockAndUpdate(A.east(), TrueSteamMachines.HEAT_HATCHES.get(3).defaultBlockState());
            hatch(A.east()).setFrontFacing(Direction.SOUTH);
            world.setBlockAndUpdate(A.east(2), Blocks.DIAMOND_BLOCK.defaultBlockState());
        }
        if (scenario == 28) {
            world.setBlockAndUpdate(A.east(2), TrueSteamMachines.HEAT_HATCHES.get(3).defaultBlockState());
            hatch(A.east()).setFrontFacing(Direction.EAST);
            hatch(A.east(2)).setFrontFacing(Direction.WEST);
        }
        if (scenario == 23 || scenario == 24) {
            world.setBlockAndUpdate(A.east(), TrueSteamMachines.HEAT_HATCHES.get(3).defaultBlockState());
            hatch(A.east()).setFrontFacing(Direction.SOUTH);
            HeatFixture.Commands.place(world, C, false, 1);
            hatch(C.east()).setFrontFacing(Direction.NORTH);
            for (int z = 1; z < 12; z++) world.setBlockAndUpdate(A.offset(3, 0, z), GTBlocks.COMPUTER_HEAT_VENT.getDefaultState());
            for (int z : new int[] { 1, 5, 11 }) {
                for (int x = 1; x <= 3; x++) world.setBlockAndUpdate(A.offset(x, 0, z), GTBlocks.COMPUTER_HEAT_VENT.getDefaultState());
            }
        }
        placed = world.getGameTime();
        seeded = -1;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void tick(TickEvent.LevelTickEvent event) {
        if (finished || world == null || event.level != world || event.phase != TickEvent.Phase.END) return;
        long now = world.getGameTime();
        try {
            if (seeded < 0 && now - placed >= 8 && now % 20 == 1) {
                for (BlockPos pos : scenario == 23 || scenario == 24 ? new BlockPos[] { A, B, C } : new BlockPos[] { A, B }) {
                    HeatFixture controller = controller(pos);
                    check(controller.checkPatternWithLock(), "scenario " + scenario + " forms " + pos);
                    controller.onStructureFormed();
                }
                BlockPos donor = scenario == 17 ? B : A;
                double initial = scenario < 16 ? 50000 : scenario == 22 ? 100000 : scenario == 23 || scenario == 24 ? 1000 : 500;
                check(controller(donor).changeHeat(initial, false) == initial, "scenario " + scenario + " seeded");
                HeatFixture donorMachine = controller(donor);
                check(CompletableFuture.supplyAsync(() -> donorMachine.changeHeat(1, false)).join() == 0,
                        "off-server-thread mutation rejected");
                if (scenario == 23 || scenario == 24) {
                    controller(B).changeHeat(490, false);
                    controller(C).changeHeat(500, false);
                }
                if (scenario == 24) {
                    for (BlockPos pos : new BlockPos[] { A, B, C }) {
                        HeatNetworkManager.unregisterController(world, pos);
                        HeatNetworkManager.unregisterHatch(world, pos.east());
                    }
                    for (BlockPos pos : new BlockPos[] { C, B, A }) {
                        HeatNetworkManager.registerController(world, pos);
                        HeatNetworkManager.registerHatch(world, pos.east());
                    }
                }
                seeded = now;
            }
            if (seeded >= 0 && now % 20 == 0) {
                if (scenario == 29) {
                    if (topologyStage >= 6) {
                        check(world.getChunkSource().getChunkNow(16, 16) != null && world.getChunkSource().getChunkNow(16, 17) != null,
                                "source and intermediate chunks stay available during endpoint gap");
                        if (topologyStage < 10) {
                            check(world.getChunkSource().getChunkNow(16, 18) == null,
                                    "receiver endpoint unavailable without discovery reloading it");
                            check(Math.abs(controller(A).getStoredHeat() - 480.4) < 1e-8,
                                    "source retains energy while receiver endpoint unavailable");
                            if (topologyStage == 9) {
                                world.getChunkSource().addRegionTicket(ENDPOINT_TICKET, new ChunkPos(B), 0, B);
                                world.getChunk(16, 18);
                            }
                        } else {
                            double expected = 19.6 + (480.4 - 19.6) / 100 * 2;
                            check(Math.abs(controller(B).getStoredHeat() - expected) < 1e-8,
                                    "restored endpoint receives one scheduled package without catch-up");
                            check(Math.abs(controller(A).getStoredHeat() + controller(B).getStoredHeat() - 500) < 1e-8,
                                    "endpoint reload conserves total energy");
                            finish(null);
                        }
                        topologyStage++;
                        return;
                    }
                    double expected = topologyStage == 5 ? 19.6 : 10;
                    check(Math.abs(controller(B).getStoredHeat() - expected) < 1e-8,
                            "intermediate chunk availability stage " + topologyStage + " has no unavailable or catch-up transfers");
                    if (topologyStage == 0) {
                        world.getChunkSource().addRegionTicket(ENDPOINT_TICKET, new ChunkPos(A), 0, A);
                        world.getChunkSource().addRegionTicket(ENDPOINT_TICKET, new ChunkPos(B), 0, B);
                        for (int z = 16; z <= 18; z++) world.setChunkForced(16, z, false);
                    } else if (topologyStage < 5) {
                        check(world.getChunkSource().getChunkNow(16, 17) == null,
                                "intermediate vent chunk is unavailable and discovery does not reload it");
                        check(world.getChunkSource().getChunkNow(16, 16) != null && world.getChunkSource().getChunkNow(16, 18) != null,
                                "both endpoint chunks remain loaded during intermediate unavailability");
                        if (topologyStage == 4) {
                            world.setChunkForced(16, 17, true);
                            world.getChunk(16, 17);
                        }
                    }
                    if (topologyStage == 5) {
                        BlockPos middle = A.offset(0, 0, 16);
                        world.getChunkSource().addRegionTicket(ENDPOINT_TICKET, new ChunkPos(middle), 0, middle);
                        world.setChunkForced(16, 17, false);
                        world.getChunkSource().removeRegionTicket(ENDPOINT_TICKET, new ChunkPos(B), 0, B);
                    }
                    topologyStage++;
                    return;
                }
                if (scenario == 25) {
                    double expected = topologyStage == 3 ? 19.6 : 10;
                    check(Math.abs(controller(B).getStoredHeat() - expected) < 1e-8,
                            "live topology stage " + topologyStage + " uses fresh connections without catch-up");
                    check(Math.abs(controller(A).getStoredHeat() + controller(B).getStoredHeat() - 500) < 1e-8,
                            "live rotation and reconnection conserve energy");
                    if (topologyStage == 0) hatch(A.east()).setFrontFacing(Direction.WEST);
                    if (topologyStage == 1) {
                        hatch(A.east()).setFrontFacing(Direction.SOUTH);
                        world.removeBlock(A.offset(1, 0, 3), false);
                    }
                    if (topologyStage == 2) world.setBlockAndUpdate(A.offset(1, 0, 3), GTBlocks.COMPUTER_HEAT_VENT.getDefaultState());
                    if (topologyStage++ == 3) {
                        scenario++;
                        setup();
                    }
                    return;
                }
                if (scenario == 23 || scenario == 24) {
                    double ab = (1000 - 490) / 2.0;
                    double ac = (1000 - ab - 500) / 2.0;
                    double bc = ((490 + ab) - (500 + ac)) * 0.01 * 2;
                    check(Math.abs(controller(A).getStoredHeat() - (1000 - ab - ac)) < 1e-8,
                            "distinct fan-out peers receive independent pair allowances");
                    check(Math.abs(controller(B).getStoredHeat() - (490 + ab - bc)) < 1e-8 &&
                            Math.abs(controller(C).getStoredHeat() - (500 + ac + bc)) < 1e-8,
                            "earlier pairs reverse the later donor and select its current sending tier");
                    check(Math.abs(controller(A).getStoredHeat() + controller(B).getStoredHeat() + controller(C).getStoredHeat() - 1990) < 1e-8,
                            "sequential fan-out conserves energy without stale simultaneous packages");
                    if (scenario == 24) {
                        check(true, "reversed registration order produces identical sequential exchange");
                    }
                    scenario++;
                    setup();
                    return;
                }
                double amount = scenario < 16 ? new double[] { 2, 8, 32, 128 }[scenario / 4] * 5 :
                        switch (scenario) {
                            case 16, 19 -> 250;
                            case 17 -> 40;
                            case 18 -> 10;
                            case 20, 21, 27, 28 -> 0;
                            case 26 -> 10;
                            case 22 -> 50000;
                            default -> throw new AssertionError("Unknown scenario");
                        };
                HeatFixture receiver = controller(scenario == 17 ? A : B);
                check(Math.abs(receiver.getStoredHeat() - amount) < 1e-8, "scenario " + scenario + " sender-only nonstacked package " + amount);
                check(Math.abs(controller(A).getStoredHeat() + controller(B).getStoredHeat() -
                        (scenario < 16 ? 50000 : scenario == 22 ? 100000 : 500)) < 1e-8, "scenario " + scenario + " conserved joules");
                check(controller(scenario == 17 ? B : A).getTemperature() >= receiver.getTemperature(), "no equilibrium overshoot");
                verifyDisplay(A.east(), controller(A));
                verifyDisplay(B.east(), controller(B));
                if (scenario == 28) {
                    verifyDisplay(A.east(2), controller(A));
                    check(hatch(A.east()).resolveOwner().machine() == hatch(A.east(2)).resolveOwner().machine(),
                            "directly connected same-owner hatches do not create a self-transfer package");
                    world.setBlockAndUpdate(C, GTMultiMachines.ELECTRIC_BLAST_FURNACE.defaultBlockState());
                    IMultiController unsupported = (IMultiController) MetaMachine.getMachine(world, C);
                    HeatHatchMachine owned = hatch(B.east());
                    owned.removedFromController(controller(B));
                    owned.addedToController(unsupported);
                    check(owned.resolveOwner().status() == 0 && owned.resolveOwner().machine() == null,
                            "non-heat multiblock owner is unavailable without invalid casts");
                    owned.removedFromController(unsupported);
                    owned.addedToController(controller(B));
                    for (int tier : new int[] { -1, 0, 1, 2, 7, 8, 9, 10, 11, 12, 13, 14 }) {
                        try {
                            HeatHatchMachine.coefficient(tier);
                            throw new AssertionError("Unsupported tier accepted: " + tier);
                        } catch (IllegalArgumentException expected) {
                            check(true, "unsupported hatch tier rejected: " + tier);
                        }
                    }
                    scenario++;
                    setup();
                    return;
                }
                if (scenario >= 16 && scenario <= 19) {
                    verifyDisplay(A.east(2), controller(A));
                    verifyDisplay(B.east(2), controller(B));
                }
                if (scenario == 22) {
                    check(receiver.isMelting() && receiver.getMeltingTicksRemaining() == 40,
                            "incoming heat crosses safe capacity without receiver-tier throttling or shortened episode");
                    receiver.changeHeat(-receiver.getStoredHeat(), false);
                    receiver.onStructureInvalid();
                    hatch(B.east()).refreshDisplay();
                    check((int) field(hatch(B.east()), "ownerStatus") == 0 && (double) field(hatch(B.east()), "displayHeat") == 0,
                            "invalidation clears stale display values");
                    check(receiver.checkPatternWithLock(), "invalidated owner can reform");
                    receiver.onStructureFormed();
                    hatch(B.east()).refreshDisplay();
                    verifyDisplay(B.east(), receiver);
                    HeatHatchMachine owned = hatch(B.east());
                    owned.addedToController(controller(A));
                    check(owned.resolveOwner().machine() == null, "ambiguous ownership fails closed");
                    owned.removedFromController(receiver);
                    check(owned.resolveOwner().machine() == null, "stale owner association does not attach an unrelated formed controller");
                    owned.removedFromController(controller(A));
                    check(owned.resolveOwner().status() == 0 && !owned.canShared(), "unattached hatch is unavailable and cannot be shared");
                    owned.addedToController(receiver);
                    owned.refreshDisplay();
                    verifyDisplay(B.east(), receiver);
                    world.setBlockAndUpdate(B.east(2), Blocks.REDSTONE_BLOCK.defaultBlockState());
                    check(receiver.checkPatternWithLock(), "invalid thermal fixture still has a matched block pattern");
                    receiver.onStructureFormed();
                    owned.refreshDisplay();
                    check(owned.resolveOwner().status() == 2 && receiver.changeHeat(1, false) == 0,
                            "invalid thermal definition disables mutation and has distinct owner status");
                    var unavailable = display(owned);
                    check(unavailable.size() == 3 && ((TranslatableContents) unavailable.get(2).getContents()).getKey().equals("gttruesteam.heat.invalid"),
                            "invalid UI keeps tier and sending coefficient without nonfinite owner values");
                    try {
                        HeatHatchMachine.coefficient(0);
                        throw new AssertionError("Unsupported tier accepted");
                    } catch (IllegalArgumentException expected) {
                        check(true, "unsupported tier mapping rejected");
                    }
                    scenario++;
                    setup();
                } else {
                    scenario++;
                    setup();
                }
            }
            if (now - placed > (scenario == 29 ? 300 : 100)) throw new AssertionError("Network scenario timeout");
        } catch (Throwable failure) {
            finish(failure);
        }
    }

    private static void verifyDisplay(BlockPos pos, HeatFixture owner) throws ReflectiveOperationException {
        HeatHatchMachine hatch = hatch(pos);
        check((int) field(hatch, "ownerStatus") == 1 && (double) field(hatch, "displayHeat") == owner.getStoredHeat() &&
                (double) field(hatch, "displayCapacity") == owner.getHeatCapacity() &&
                (double) field(hatch, "displayTemperature") == owner.getTemperature() &&
                (double) field(hatch, "displayMaximum") == owner.getMaxTemperature() &&
                (boolean) field(hatch, "displayMelting") == owner.isMelting() &&
                (int) field(hatch, "displayCountdown") == owner.getMeltingTicksRemaining(), "server display snapshot matches owner at " + pos);
        double before = owner.getStoredHeat();
        var lines = display(hatch);
        check(lines.size() == (owner.isMelting() ? 7 : 6) && owner.getStoredHeat() == before,
                "read-only presentation includes thermal values and only an active warning");
        if (owner.isMelting()) {
            double shown = Double.parseDouble((String) ((TranslatableContents) lines.get(2).getContents()).getArgs()[0]);
            check(shown > owner.getMaxTemperature(), "rounded temperature is not clamped to safe maximum");
        }
    }

    private static List<Component> display(HeatHatchMachine hatch) throws ReflectiveOperationException {
        var method = HeatHatchMachine.class.getDeclaredMethod("displayText", List.class);
        method.setAccessible(true);
        List<Component> lines = new ArrayList<>();
        method.invoke(hatch, lines);
        return lines;
    }

    private static Object field(HeatHatchMachine hatch, String name) throws ReflectiveOperationException {
        var field = HeatHatchMachine.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(hatch);
    }

    private static HeatFixture controller(BlockPos pos) {
        if (MetaMachine.getMachine(world, pos) instanceof HeatFixture fixture) return fixture;
        throw new AssertionError("Missing controller at " + pos);
    }

    private static HeatHatchMachine hatch(BlockPos pos) {
        if (MetaMachine.getMachine(world, pos) instanceof HeatHatchMachine hatch) return hatch;
        throw new AssertionError("Missing hatch at " + pos);
    }

    private static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        checks.add(label);
        GTTrueSteam.LOGGER.info("HEAT NETWORK PASS: {}", label);
    }

    private static void finish(Throwable failure) {
        finished = true;
        JsonObject report = new JsonObject();
        report.addProperty("passed", failure == null);
        report.add("checks", checks);
        if (failure != null) {
            report.addProperty("failure", failure.toString());
            GTTrueSteam.LOGGER.error("HEAT NETWORK FAILED", failure);
        }
        try {
            Files.writeString(Path.of("heat-network-results.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        } catch (Exception exception) {
            GTTrueSteam.LOGGER.error("Could not save network report", exception);
        }
        world.setChunkForced(16, 16, false);
        world.setChunkForced(16, 17, false);
        world.setChunkForced(16, 18, false);
        world.getChunkSource().removeRegionTicket(ENDPOINT_TICKET, new ChunkPos(A), 0, A);
        world.getChunkSource().removeRegionTicket(ENDPOINT_TICKET, new ChunkPos(B), 0, B);
        BlockPos middle = A.offset(0, 0, 16);
        world.getChunkSource().removeRegionTicket(ENDPOINT_TICKET, new ChunkPos(middle), 0, middle);
        world.getServer().halt(false);
    }
}

package site.siredvin.gttruesteam.heatfixture;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamMachines;
import site.siredvin.gttruesteam.machines.parts.HeatHatchMachine;

import java.nio.file.Files;
import java.nio.file.Path;

/** Two separate JVMs exercise the actual chunk-save and managed-field load paths. */
@Mod.EventBusSubscriber(modid = "gttruesteam")
public final class HeatPersistenceChecks {

    private static final BlockPos POS = new BlockPos(64, 120, 64);
    private static final Path SNAPSHOT = Path.of("heat-persistence-snapshot.json");
    private static final JsonArray checks = new JsonArray();
    private static ServerLevel world;
    private static String mode;
    private static long start;
    private static int remaining;
    private static int activeTicks;
    private static boolean finished;

    @SubscribeEvent
    public static void started(ServerStartedEvent event) {
        mode = System.getProperty("gttruesteam.heatTestPersistence", "");
        if (mode.isEmpty()) return;
        world = event.getServer().overworld();
        try {
            check(!Boolean.getBoolean("gttruesteam.heatTestAuto"), "persistence run is isolated from destructive smoke checks");
            check(mode.equals("prepare") || mode.equals("resume"), "recognized persistence mode");
            world.setChunkForced(4, 4, true);
            world.getChunkAt(POS);
            start = world.getGameTime();
            if (mode.equals("prepare")) {
                for (int x = 0; x < 3; x++) world.setBlockAndUpdate(POS.east(x), Blocks.AIR.defaultBlockState());
                HeatFixture.Commands.place(world, POS, false, 0);
                for (int tier = 0; tier < 4; tier++) {
                    BlockPos pos = POS.offset(3 + tier * 3, 0, 4);
                    world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    world.setBlockAndUpdate(pos, TrueSteamMachines.HEAT_HATCHES.get(tier).defaultBlockState());
                }
            } else {
                JsonObject expected = JsonParser.parseString(Files.readString(SNAPSHOT)).getAsJsonObject();
                HeatFixture machine = controller();
                machine.subscribeServerTick(() -> activeTicks++);
                remaining = expected.get("remaining").getAsInt();
                check(machine.getStoredHeat() == expected.get("energy").getAsDouble(), "restart retains overcapacity energy");
                check(machine.isMelting() && machine.getMeltingTicksRemaining() == remaining,
                        "restart restores exact saved countdown without offline advancement or reset");
                check(machine.heatIdentity().equals(expected.get("controllerIdentity").getAsString()), "controller identity survives restart");
                check(((HeatHatchMachine) MetaMachine.getMachine(world, POS.east())).heatIdentity()
                        .equals(expected.get("hatchIdentity").getAsString()), "owned hatch identity survives restart");
                for (int tier = 0; tier < 4; tier++) {
                    HeatHatchMachine hatch = (HeatHatchMachine) MetaMachine.getMachine(world, POS.offset(3 + tier * 3, 0, 4));
                    check(hatch.getTier() == tier + 3 && hatch.sendingCoefficient() == new double[] { 2, 8, 32, 128 }[tier] &&
                            hatch.heatIdentity().equals(expected.getAsJsonArray("tiers").get(tier).getAsString()),
                            "tier variant preserves registration, coefficient and identity across restart: " + tier);
                }
            }
        } catch (Throwable failure) {
            finish(failure);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void tick(TickEvent.LevelTickEvent event) {
        if (finished || world == null || event.level != world || event.phase != TickEvent.Phase.END) return;
        long elapsed = world.getGameTime() - start;
        try {
            if (mode.equals("prepare")) {
                if (elapsed == 60) {
                    HeatFixture machine = controller();
                    check(machine.checkPatternWithLock(), "saved fixture forms");
                    machine.onStructureFormed();
                    machine.changeHeat(-machine.getStoredHeat(), false);
                    check(machine.changeHeat(1100, false) == 1100, "saved fixture accepts overcapacity energy");
                }
                if (elapsed == 28) {
                    check(controller().checkPatternWithLock(), "normalized fixture forms");
                    controller().onStructureFormed();
                    controller().changeHeat(500, false);
                    world.setBlockAndUpdate(POS.east(2), Blocks.GOLD_BLOCK.defaultBlockState());
                }
                if (elapsed == 32) {
                    check(controller().getStoredHeat() == 500 && controller().getHeatCapacity() == 2000 && !controller().isMelting(),
                            "safe reformation changes capacity without creating or discarding heat");
                    world.setBlockAndUpdate(POS.east(2), Blocks.IRON_BLOCK.defaultBlockState());
                }
                if (elapsed == 36) {
                    check(controller().getStoredHeat() == 500 && controller().getHeatCapacity() == 1000 && !controller().isMelting(),
                            "safe lower-capacity reformation retains heat");
                }
                if (elapsed >= 10 && elapsed <= 22 && (elapsed - 10) % 4 == 0) {
                    CompoundTag saved = world.getBlockEntity(POS).saveWithFullMetadata();
                    CompoundTag thermal = containing(saved, "storedHeat");
                    check(thermal != null, "managed NBT contains persisted heat");
                    if (elapsed == 22) thermal.remove("storedHeat");
                    else thermal.putDouble("storedHeat", elapsed == 10 ? -1 : elapsed == 14 ? Double.NaN : Double.POSITIVE_INFINITY);
                    world.removeBlockEntity(POS);
                    BlockEntity restored = BlockEntity.loadStatic(POS, world.getBlockState(POS), saved);
                    check(restored != null, "actual block entity deserializes");
                    world.setBlockEntity(restored);
                }
                if (elapsed >= 12 && elapsed <= 24 && (elapsed - 12) % 4 == 0) {
                    check(controller().getStoredHeat() == 0, "invalid or missing persisted heat normalizes on load: " + elapsed);
                }
                if (elapsed == 67) {
                    HeatFixture machine = controller();
                    check(machine.isMelting() && machine.getMeltingTicksRemaining() == 33, "snapshot captures partially elapsed interval");
                    JsonObject snapshot = new JsonObject();
                    snapshot.addProperty("energy", machine.getStoredHeat());
                    snapshot.addProperty("remaining", machine.getMeltingTicksRemaining());
                    snapshot.addProperty("controllerIdentity", machine.heatIdentity());
                    snapshot.addProperty("hatchIdentity", ((HeatHatchMachine) MetaMachine.getMachine(world, POS.east())).heatIdentity());
                    JsonArray tiers = new JsonArray();
                    for (int tier = 0; tier < 4; tier++) {
                        HeatHatchMachine hatch = (HeatHatchMachine) MetaMachine.getMachine(world, POS.offset(3 + tier * 3, 0, 4));
                        check(hatch.getTier() == tier + 3, "snapshot includes tier variant " + tier);
                        tiers.add(hatch.heatIdentity());
                    }
                    snapshot.add("tiers", tiers);
                    Files.writeString(SNAPSHOT, new GsonBuilder().setPrettyPrinting().create().toJson(snapshot));
                    finish(null);
                }
            } else {
                if (elapsed == 10) {
                    check(controller().checkPatternWithLock(), "restarted melting owner reforms for hatch display");
                    controller().onStructureFormed();
                    HeatHatchMachine hatch = (HeatHatchMachine) MetaMachine.getMachine(world, POS.east());
                    hatch.refreshDisplay();
                    var countdown = HeatHatchMachine.class.getDeclaredField("displayCountdown");
                    countdown.setAccessible(true);
                    check(hatch.resolveOwner().status() == 1 && countdown.getInt(hatch) == remaining - activeTicks,
                            "reloaded hatch display receives preserved countdown rather than a fresh interval");
                }
                if (activeTicks > 0 && activeTicks < remaining) {
                    check(controller().isMelting() && controller().getMeltingTicksRemaining() == remaining - activeTicks,
                            "loaded countdown resumes once per active tick: " + elapsed);
                }
                if (activeTicks == remaining) {
                    check(MetaMachine.getMachine(world, POS) == null && MetaMachine.getMachine(world, POS.east()) == null,
                            "restored episode expires after saved remainder and retains owned targets");
                    check(world.getBlockState(POS.east(2)).is(Blocks.IRON_BLOCK), "restart expiry preserves casing");
                    finish(null);
                }
            }
            if (elapsed > 100) throw new AssertionError("Persistence test timed out");
        } catch (Throwable failure) {
            finish(failure);
        }
    }

    private static HeatFixture controller() {
        if (MetaMachine.getMachine(world, POS) instanceof HeatFixture fixture) return fixture;
        throw new AssertionError("Missing saved controller");
    }

    private static CompoundTag containing(CompoundTag tag, String name) {
        if (tag.contains(name)) return tag;
        for (String key : tag.getAllKeys()) {
            if (tag.get(key) instanceof CompoundTag child) {
                CompoundTag result = containing(child, name);
                if (result != null) return result;
            }
        }
        return null;
    }

    private static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        checks.add(label);
        GTTrueSteam.LOGGER.info("HEAT PERSISTENCE PASS: {}", label);
    }

    private static void finish(Throwable failure) {
        finished = true;
        JsonObject report = new JsonObject();
        report.addProperty("passed", failure == null);
        report.add("checks", checks);
        if (failure != null) {
            report.addProperty("failure", failure.toString());
            GTTrueSteam.LOGGER.error("HEAT PERSISTENCE FAILED", failure);
        }
        try {
            Files.writeString(Path.of("heat-persistence-" + mode + ".json"),
                    new GsonBuilder().setPrettyPrinting().create().toJson(report));
        } catch (Exception exception) {
            GTTrueSteam.LOGGER.error("Could not save persistence report", exception);
        }
        world.setChunkForced(4, 4, false);
        world.getServer().halt(false);
    }
}

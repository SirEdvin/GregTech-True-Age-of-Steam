package site.siredvin.gttruesteam.machines.shared.heat;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamMachines;
import site.siredvin.gttruesteam.heatfixture.HeatFixture;
import site.siredvin.gttruesteam.machines.parts.HeatHatchMachine;

import java.nio.file.Files;
import java.nio.file.Path;

/** Actual chunk eviction/reload and unavailable-target ledger checks in a disposable world. */
@Mod.EventBusSubscriber(modid = "gttruesteam")
public final class HeatChunkChecks {

    private static final BlockPos CONTROLLER = new BlockPos(1024, 120, 1024);
    private static final BlockPos TARGET = new BlockPos(1152, 120, 1024);
    private static final BlockPos REMOTE_HATCH = CONTROLLER.east(HeatFixture.REMOTE_HATCH_DISTANCE);
    private static final int REMOTE_CHUNK_X = REMOTE_HATCH.getX() >> 4;
    private static final JsonArray checks = new JsonArray();
    private static ServerLevel world;
    private static long start;
    private static long phaseTick;
    private static int phase;
    private static int savedRemaining;
    private static int resumedActiveTicks;
    private static boolean unloadObserved;
    private static String controllerIdentity;
    private static String targetIdentity;
    private static boolean finished;
    private static boolean remoteUnloadObserved;
    private static long splitOverheatTick;
    private static String remoteIdentity;

    @SubscribeEvent
    public static void started(ServerStartedEvent event) {
        if (!Boolean.getBoolean("gttruesteam.heatTestChunks")) return;
        world = event.getServer().overworld();
        try {
            world.setChunkForced(64, 64, true);
            world.setChunkForced(72, 64, true);
            for (int x = 0; x < 3; x++) world.setBlockAndUpdate(CONTROLLER.east(x), Blocks.AIR.defaultBlockState());
            world.setBlockAndUpdate(TARGET, Blocks.AIR.defaultBlockState());
            HeatFixture.Commands.place(world, CONTROLLER, false, 0);
            world.setBlockAndUpdate(TARGET, TrueSteamMachines.HEAT_HATCHES.get(2).defaultBlockState());
            controllerIdentity = controller().heatIdentity();
            targetIdentity = ((HeatHatchMachine) MetaMachine.getMachine(world, TARGET)).heatIdentity();
            start = world.getGameTime();
        } catch (Throwable failure) {
            finish(failure);
        }
    }

    @SubscribeEvent
    public static void unloaded(ChunkEvent.Unload event) {
        if (finished || world == null || event.getLevel() != world || !(event.getChunk() instanceof LevelChunk chunk)) return;
        if (phase >= 5 && chunk.getPos().x == REMOTE_CHUNK_X && chunk.getPos().z == 64) remoteUnloadObserved = true;
        if (chunk.getPos().x == 64 && chunk.getPos().z == 64 &&
                chunk.getBlockEntities().get(CONTROLLER) instanceof MetaMachineBlockEntity holder &&
                holder.getMetaMachine() instanceof HeatFixture fixture) {
            savedRemaining = fixture.getMeltingTicksRemaining();
            unloadObserved = true;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void tick(TickEvent.LevelTickEvent event) {
        if (finished || world == null || event.level != world || event.phase != TickEvent.Phase.END) return;
        long now = world.getGameTime();
        try {
            if (phase == 3 && world.isPositionEntityTicking(CONTROLLER)) resumedActiveTicks++;
            if (now - start == 60) {
                check(controller().checkPatternWithLock(), "chunk fixture forms");
                controller().onStructureFormed();
                check(controller().changeHeat(1100, false) == 1100, "chunk fixture starts melting");
            }
            if (now - start == 67) {
                check(controller().getMeltingTicksRemaining() == 33, "release begins with a partially elapsed countdown");
                world.setChunkForced(64, 64, false);
                world.setChunkForced(72, 64, false);
                phase = 1;
            }
            if (phase == 1 && unloadObserved && !world.hasChunkAt(CONTROLLER) && !world.hasChunkAt(TARGET)) {
                check(savedRemaining > 0 && savedRemaining <= 33, "controller really unloaded before expiry");
                CompoundTag targets = new CompoundTag();
                targets.putString(Long.toString(TARGET.asLong()), targetIdentity);
                HeatDestruction.get(world).destroy(world, targets, null);
                check(!world.hasChunkAt(TARGET), "enqueueing deferred failure does not load its chunk");
                phase = 2;
                phaseTick = now;
            }
            if (phase == 2 && now - phaseTick == 60) {
                check(!world.hasChunkAt(CONTROLLER) && !world.hasChunkAt(TARGET), "coordinator does not force-load offline controller or target");
                check(HeatDestruction.get(world).save(new CompoundTag()).getCompound("targets").contains(Long.toString(TARGET.asLong())),
                        "unavailable target remains in persisted removal ledger");
                world.setChunkForced(64, 64, true);
                world.getChunkAt(CONTROLLER);
                check(controller().getStoredHeat() == 1100 && controller().isMelting() &&
                        controller().getMeltingTicksRemaining() == savedRemaining, "real chunk reload preserves exact heat and paused interval");
                check(controller().heatIdentity().equals(controllerIdentity), "real chunk reload preserves controller identity");
                phase = 3;
                phaseTick = now;
            }
            if (phase == 3 && resumedActiveTicks == 3) {
                check(resumedActiveTicks > 0 && controller().getMeltingTicksRemaining() == savedRemaining - resumedActiveTicks,
                        "countdown resumes once per active tick after chunk reload: saved=" + savedRemaining +
                                ", active=" + resumedActiveTicks + ", actual=" + controller().getMeltingTicksRemaining());
                check(controller().checkPatternWithLock(), "reloaded structure reforms");
                controller().onStructureFormed();
                check(controller().changeHeat(-1100, false) == -1100 && !controller().isMelting(), "reloaded controller remains recoverable");
                world.setChunkForced(72, 64, true);
                world.getChunkAt(TARGET);
                phase = 4;
                phaseTick = now;
            }
            if (phase == 4 && now - phaseTick == 2) {
                check(MetaMachine.getMachine(world, TARGET) == null, "matching deferred hatch is removed after actual chunk reload");
                world.setBlockAndUpdate(TARGET, TrueSteamMachines.HEAT_HATCHES.get(2).defaultBlockState());
                CompoundTag stale = new CompoundTag();
                stale.putString(Long.toString(TARGET.asLong()), targetIdentity);
                HeatDestruction.get(world).destroy(world, stale, null);
                check(MetaMachine.getMachine(world, TARGET) instanceof HeatHatchMachine replacement &&
                        !replacement.heatIdentity().equals(targetIdentity), "stale removal identity cannot destroy a replacement hatch");
                check(!HeatDestruction.get(world).save(new CompoundTag()).getCompound("targets").contains(Long.toString(TARGET.asLong())),
                        "completed and stale test target is removed from the ledger");
                world.removeBlock(CONTROLLER, false);
                world.setChunkForced(72, 64, false);
                for (int x = 65; x <= REMOTE_CHUNK_X; x++) world.setChunkForced(x, 64, true);
                HeatFixture.Commands.placeRemote(world, CONTROLLER);
                remoteIdentity = ((HeatHatchMachine) MetaMachine.getMachine(world, REMOTE_HATCH)).heatIdentity();
                phase = 5;
                phaseTick = now;
            }
            if (phase == 5 && now - phaseTick == 60) {
                check(controller().checkPatternWithLock(), "split-chunk fixture forms with remote owned hatch");
                controller().onStructureFormed();
                MultiblockWorldSavedData.getOrCreate(world).addMapping(controller().getMultiblockState());
                check(((HeatHatchMachine) MetaMachine.getMachine(world, REMOTE_HATCH)).resolveOwner().machine() == controller(),
                        "remote hatch belongs to the live controller before unload");
                check(controller().changeHeat(1100, false) == 1100, "split fixture starts full melting interval");
                splitOverheatTick = now;
                for (int x = 65; x <= REMOTE_CHUNK_X; x++) world.setChunkForced(x, 64, false);
                phase = 6;
            }
            if (phase == 6 && now - splitOverheatTick == 20) {
                check(remoteUnloadObserved && world.getChunkSource().getChunkNow(REMOTE_CHUNK_X, 64) == null,
                        "owned hatch chunk really evicts while controller stays loaded: event=" + remoteUnloadObserved +
                                ", state=" + world.getChunkSource().getChunkDebugData(new net.minecraft.world.level.ChunkPos(REMOTE_HATCH)));
                check(world.isPositionEntityTicking(CONTROLLER) && controller().isMelting() &&
                        controller().getMeltingTicksRemaining() == 20, "missing owned hatch does not pause or reset live controller countdown");
            }
            if (phase == 6 && now - splitOverheatTick == 40) {
                check(HeatNetworkManager.loadedMachine(world, CONTROLLER) == null,
                        "live controller expires on deadline with owned hatch unloaded");
                check(world.getChunkSource().getChunkNow(REMOTE_CHUNK_X, 64) == null,
                        "deadline destruction does not force-load remote hatch");
                check(HeatDestruction.get(world).save(new CompoundTag()).getCompound("targets")
                        .getString(Long.toString(REMOTE_HATCH.asLong())).equals(remoteIdentity),
                        "expiry retains unavailable owned hatch identity for deferred destruction");
                world.setChunkForced(REMOTE_CHUNK_X, 64, true);
                world.getChunkAt(REMOTE_HATCH);
                phase = 7;
                phaseTick = now;
            }
            if (phase == 7 && now - phaseTick == 3) {
                check(HeatNetworkManager.loadedMachine(world, REMOTE_HATCH) == null,
                        "owned remote hatch is removed after reload even though its controller is gone");
                check(world.getBlockState(REMOTE_HATCH.west()).is(Blocks.IRON_BLOCK),
                        "deferred destruction preserves adjacent remote casing");
                finish(null);
            }
            if (now - start > 600) throw new AssertionError("Chunk test timed out waiting for actual eviction/reload");
        } catch (Throwable failure) {
            finish(failure);
        }
    }

    private static HeatFixture controller() {
        if (MetaMachine.getMachine(world, CONTROLLER) instanceof HeatFixture fixture) return fixture;
        throw new AssertionError("Missing chunk fixture controller");
    }

    private static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        checks.add(label);
        GTTrueSteam.LOGGER.info("HEAT CHUNK PASS: {}", label);
    }

    private static void finish(Throwable failure) {
        finished = true;
        JsonObject report = new JsonObject();
        report.addProperty("passed", failure == null);
        report.add("checks", checks);
        if (failure != null) {
            report.addProperty("failure", failure.toString());
            GTTrueSteam.LOGGER.error("HEAT CHUNK CHECK FAILED", failure);
        }
        try {
            Files.writeString(Path.of("heat-chunk-results.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        } catch (Exception exception) {
            GTTrueSteam.LOGGER.error("Could not save chunk report", exception);
        }
        world.setChunkForced(64, 64, false);
        world.setChunkForced(72, 64, false);
        for (int x = 65; x <= REMOTE_CHUNK_X; x++) world.setChunkForced(x, 64, false);
        world.getServer().halt(false);
    }
}

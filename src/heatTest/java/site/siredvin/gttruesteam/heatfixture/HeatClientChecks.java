package site.siredvin.gttruesteam.heatfixture;

import com.gregtechceu.gtceu.api.gui.factory.MachineUIFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.machines.parts.HeatHatchMachine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/** Exercises real integrated-server packets and an open menu, with no client controller instance. */
@Mod.EventBusSubscriber(modid = "gttruesteam", value = Dist.CLIENT)
public final class HeatClientChecks {
    private static final BlockPos OWNER = new BlockPos(2048, 120, 2048);
    private static final BlockPos HATCH = OWNER.east(HeatFixture.REMOTE_HATCH_DISTANCE);
    private static final JsonArray checks = new JsonArray();
    private static int phase;
    private static int ticks;
    private static int menu;
    private static volatile int sounds;
    private static boolean finished;
    private static CompletableFuture<Void> operation = CompletableFuture.completedFuture(null);

    @SubscribeEvent
    public static void sound(PlaySoundEvent event) {
        if (Boolean.getBoolean("gttruesteam.heatTestClient") && !finished &&
                event.getOriginalSound().getLocation().toString().equals("minecraft:entity.generic.explode")) sounds++;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (!Boolean.getBoolean("gttruesteam.heatTestClient") || finished || event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
        try {
            if (!operation.isDone()) return;
            operation.join();
            if (++ticks > 600) throw new AssertionError("Client phase " + phase + " timed out");
            if (phase == 0) {
                server(() -> {
                    ServerLevel level = mc.getSingleplayerServer().overworld();
                    for (int x = OWNER.getX() >> 4; x <= HATCH.getX() >> 4; x++) level.setChunkForced(x, OWNER.getZ() >> 4, true);
                    level.removeBlock(OWNER, false);
                    HeatFixture.Commands.placeRemote(level, OWNER);
                    for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++)
                        level.setBlockAndUpdate(HATCH.offset(x, -1, z - 2), Blocks.IRON_BLOCK.defaultBlockState());
                    var player = mc.getSingleplayerServer().getPlayerList().getPlayers().get(0);
                    player.teleportTo(level, HATCH.getX() + 0.5, HATCH.getY(), HATCH.getZ() - 1.5, 0, 0);
                });
                next();
            } else if (phase == 1 && ticks >= 100) {
                server(() -> {
                    ServerLevel level = mc.getSingleplayerServer().overworld();
                    HeatFixture owner = owner(level);
                    if (!owner.checkPatternWithLock()) throw new AssertionError("Remote client fixture did not form");
                    owner.onStructureFormed();
                    MultiblockWorldSavedData.getOrCreate(level).addMapping(owner.getMultiblockState());
                    if (owner.changeHeat(500, false) != 500) throw new AssertionError("Remote seed rejected");
                    MachineUIFactory.INSTANCE.openUI(MetaMachine.getMachine(level, HATCH), mc.getSingleplayerServer().getPlayerList().getPlayers().get(0));
                });
                next();
            } else if (phase == 2 && hatch(mc) != null && number(mc, "displayHeat") == 500 && mc.player.containerMenu != mc.player.inventoryMenu) {
                check(ownerAbsent(mc), "controller chunk and instance are absent on the real client");
                check(number(mc, "displayTemperature") == 305 && number(mc, "displayCapacity") == 1000,
                        "remote hatch receives authoritative heat and characteristics");
                check(mc.screen != null, "real hatch menu is open");
                menu = mc.player.containerMenu.containerId;
                server(() -> owner(mc.getSingleplayerServer().overworld()).changeHeat(100, false));
                next();
            } else if (phase == 3 && number(mc, "displayHeat") == 600) {
                check(mc.player.containerMenu.containerId == menu, "same open remote menu updates without reopening");
                server(() -> owner(mc.getSingleplayerServer().overworld()).changeHeat(600, false));
                next();
            } else if (phase == 4 && Boolean.TRUE.equals(field(mc, "displayMelting"))) {
                check(number(mc, "displayHeat") == 1200 && number(mc, "displayTemperature") > 310 && number(mc, "displayCountdown") > 0,
                        "remote client receives unclamped melting state and active countdown");
                server(() -> owner(mc.getSingleplayerServer().overworld()).changeHeat(-1200, false));
                next();
            } else if (phase == 5 && number(mc, "displayHeat") == 0 && Boolean.FALSE.equals(field(mc, "displayMelting"))) {
                check(number(mc, "displayCountdown") == 0 && mc.player.containerMenu.containerId == menu && sounds == 0,
                        "remote cooling clears countdown without closing menu or receiving failure sound");
                server(() -> mc.getSingleplayerServer().overworld().removeBlock(OWNER.east(), false));
                next();
            } else if (phase == 6 && number(mc, "ownerStatus") == 0) {
                check(number(mc, "displayHeat") == 0 && mc.player.containerMenu.containerId == menu,
                        "nonmelting invalidation clears stale values in surviving open menu");
                server(() -> {
                    ServerLevel level = mc.getSingleplayerServer().overworld();
                    level.setBlockAndUpdate(OWNER.east(), Blocks.IRON_BLOCK.defaultBlockState());
                    HeatFixture owner = owner(level);
                    if (!owner.checkPatternWithLock()) throw new AssertionError("Remote reformation failed");
                    owner.onStructureFormed();
                    owner.changeHeat(500, false);
                });
                next();
            } else if (phase == 7 && number(mc, "displayHeat") == 500) {
                check(mc.player.containerMenu.containerId == menu && ownerAbsent(mc),
                        "reattachment restores values without reopening or client controller tracking");
                server(() -> owner(mc.getSingleplayerServer().overworld()).changeHeat(600, false));
                next();
            } else if (phase == 8 && ticks >= 80 && hatch(mc) == null) {
                check(mc.player.containerMenu == mc.player.inventoryMenu, "destroyed remote hatch closes the active client menu");
                check(sounds == 1, "nearby client receives exactly one hatch explosion sound on expiry (observed " + sounds + ")");
                server(() -> HeatFixture.Commands.placeRemote(mc.getSingleplayerServer().overworld(), OWNER));
                next();
            } else if (phase == 9 && ticks >= 60) {
                server(() -> {
                    ServerLevel level = mc.getSingleplayerServer().overworld();
                    HeatFixture owner = owner(level);
                    if (!owner.checkPatternWithLock()) throw new AssertionError("Second remote fixture did not form");
                    owner.onStructureFormed();
                    MultiblockWorldSavedData.getOrCreate(level).addMapping(owner.getMultiblockState());
                    owner.changeHeat(500, false);
                    MachineUIFactory.INSTANCE.openUI(MetaMachine.getMachine(level, HATCH), mc.getSingleplayerServer().getPlayerList().getPlayers().get(0));
                });
                next();
            } else if (phase == 10 && number(mc, "displayHeat") == 500 && mc.player.containerMenu != mc.player.inventoryMenu) {
                check(ownerAbsent(mc), "second viewed fixture also has no client controller instance");
                server(() -> {
                    ServerLevel level = mc.getSingleplayerServer().overworld();
                    owner(level).changeHeat(600, false);
                    level.removeBlock(OWNER, false);
                });
                next();
            } else if (phase == 11 && ticks >= 40 && hatch(mc) == null) {
                check(mc.player.containerMenu == mc.player.inventoryMenu, "immediate dismantling closes the remote hatch menu");
                check(sounds == 2, "nearby client receives exactly one additional sound on dismantling (observed " + sounds + ")");
                finish(null);
            }
        } catch (Throwable failure) {
            finish(failure);
        }
    }

    private static void next() { phase++; ticks = 0; }

    private static void server(Runnable task) {
        operation = CompletableFuture.runAsync(task, command -> Minecraft.getInstance().getSingleplayerServer().execute(command));
    }

    private static HeatFixture owner(ServerLevel level) {
        return (HeatFixture) MetaMachine.getMachine(level, OWNER);
    }

    private static boolean ownerAbsent(Minecraft mc) {
        return mc.level.getChunkSource().getChunk(OWNER.getX() >> 4, OWNER.getZ() >> 4, ChunkStatus.FULL, false) == null &&
                MetaMachine.getMachine(mc.level, OWNER) == null;
    }

    private static HeatHatchMachine hatch(Minecraft mc) {
        return MetaMachine.getMachine(mc.level, HATCH) instanceof HeatHatchMachine heat ? heat : null;
    }

    private static Object field(Minecraft mc, String name) throws ReflectiveOperationException {
        HeatHatchMachine hatch = hatch(mc);
        if (hatch == null) return null;
        var field = HeatHatchMachine.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(hatch);
    }

    private static double number(Minecraft mc, String name) throws ReflectiveOperationException {
        Object value = field(mc, name);
        return value instanceof Number number ? number.doubleValue() : Double.NaN;
    }

    private static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        checks.add(label);
    }

    private static void finish(Throwable failure) {
        finished = true;
        JsonObject report = new JsonObject();
        report.addProperty("passed", failure == null);
        report.add("checks", checks);
        if (failure != null) {
            report.addProperty("failure", failure.toString());
            GTTrueSteam.LOGGER.error("HEAT CLIENT CHECK FAILED", failure);
        }
        try {
            Files.writeString(Path.of("heat-client-results.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        } catch (Exception exception) {
            GTTrueSteam.LOGGER.error("Could not save client report", exception);
        }
        server(() -> {
            ServerLevel level = Minecraft.getInstance().getSingleplayerServer().overworld();
            for (int x = OWNER.getX() >> 4; x <= HATCH.getX() >> 4; x++) level.setChunkForced(x, OWNER.getZ() >> 4, false);
        });
    }
}

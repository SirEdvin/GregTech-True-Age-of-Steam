package site.siredvin.gttruesteam.machines.shared.heat;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import site.siredvin.gttruesteam.common.Constants;
import site.siredvin.gttruesteam.machines.parts.HeatHatchMachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Mod.EventBusSubscriber(modid = "gttruesteam")
public final class HeatNetworkManager {

    private static final Map<ServerLevel, HeatNetworkManager> LEVELS = new HashMap<>();
    private final Set<BlockPos> hatches = new HashSet<>();
    private final Set<BlockPos> controllers = new HashSet<>();
    private long lastTick = Long.MIN_VALUE;

    private record Pair(BlockPos first, BlockPos second) implements Comparable<Pair> {
        @Override
        public int compareTo(Pair other) {
            int result = first.compareTo(other.first);
            return result == 0 ? second.compareTo(other.second) : result;
        }
    }

    private record Connection(BlockPos first, BlockPos second) {}

    private static HeatNetworkManager get(ServerLevel level) {
        return LEVELS.computeIfAbsent(level, ignored -> new HeatNetworkManager());
    }

    public static void registerHatch(ServerLevel level, BlockPos pos) {
        get(level).hatches.add(pos.immutable());
    }

    public static void unregisterHatch(ServerLevel level, BlockPos pos) {
        var manager = LEVELS.get(level);
        if (manager != null) manager.hatches.remove(pos);
    }

    public static void registerController(ServerLevel level, BlockPos pos) {
        get(level).controllers.add(pos.immutable());
    }

    public static void unregisterController(ServerLevel level, BlockPos pos) {
        var manager = LEVELS.get(level);
        if (manager != null) manager.controllers.remove(pos);
    }

    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        HeatDestruction.get(level).process(level);
        var manager = LEVELS.get(level);
        if (manager == null || manager.lastTick == level.getGameTime()) return;
        manager.lastTick = level.getGameTime();
        if (manager.lastTick % Constants.HEAT_TRANSFER_INTERVAL == 0) manager.exchange(level);
        for (BlockPos pos : List.copyOf(manager.controllers)) {
            if (level.hasChunkAt(pos) && level.isPositionEntityTicking(pos) &&
                    loadedMachine(level, pos) instanceof HeatMultiblockMachine heat) {
                heat.finalizeHeatTick(manager.lastTick);
            }
        }
        for (BlockPos pos : List.copyOf(manager.hatches)) {
            var hatch = hatch(level, pos);
            if (hatch != null) hatch.refreshDisplay();
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) LEVELS.remove(level);
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent event) {
        LEVELS.clear();
    }

    private static HeatHatchMachine hatch(ServerLevel level, BlockPos pos) {
        return loadedMachine(level, pos) instanceof HeatHatchMachine hatch &&
                !hatch.isInValid() ? hatch : null;
    }

    /** World-level reads can renew UNKNOWN tickets even after hasChunkAt succeeds. */
    public static MetaMachine loadedMachine(ServerLevel level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos)) return null;
        var chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        return chunk == null ? null : MetaMachine.getMachine(chunk, pos);
    }

    private static HeatNetwork.Lookup lookup(ServerLevel level) {
        return new HeatNetwork.Lookup() {
            @Override
            public boolean loaded(BlockPos pos) {
                return level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) != null;
            }

            @Override
            public HeatNetwork.Node node(BlockPos pos) {
                var chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
                if (chunk == null) return new HeatNetwork.Node(HeatNetwork.Kind.BLOCKED, null);
                if (chunk.getBlockState(pos).is(GTBlocks.COMPUTER_HEAT_VENT.get())) {
                    return new HeatNetwork.Node(HeatNetwork.Kind.VENT, null);
                }
                var endpoint = hatch(level, pos);
                return endpoint == null ? new HeatNetwork.Node(HeatNetwork.Kind.BLOCKED, null) :
                        new HeatNetwork.Node(HeatNetwork.Kind.HATCH, endpoint.getFrontFacing());
            }
        };
    }

    private void exchange(ServerLevel level) {
        var lookup = lookup(level);
        Map<Pair, List<Connection>> pairs = new TreeMap<>();
        for (BlockPos pos : hatches.stream().sorted().toList()) {
            var source = hatch(level, pos);
            if (source == null) continue;
            var first = source.resolveOwner().machine();
            if (first == null) continue;
            for (BlockPos destination : HeatNetwork.discover(pos, source.getFrontFacing(), lookup)) {
                var endpoint = hatch(level, destination);
                var second = endpoint == null ? null : endpoint.resolveOwner().machine();
                if (second == null || second == first || first.getPos().compareTo(second.getPos()) >= 0) continue;
                pairs.computeIfAbsent(new Pair(first.getPos(), second.getPos()), ignored -> new ArrayList<>())
                        .add(new Connection(pos, destination));
            }
        }
        for (var entry : pairs.entrySet()) {
            HeatMultiblockMachine donor = null;
            HeatMultiblockMachine receiver = null;
            double coefficient = 0;
            for (Connection connection : entry.getValue()) {
                var a = hatch(level, connection.first());
                var b = hatch(level, connection.second());
                if (a == null || b == null) continue;
                var first = a.resolveOwner().machine();
                var second = b.resolveOwner().machine();
                if (first == null || second == null || first == second ||
                        !first.getPos().equals(entry.getKey().first()) || !second.getPos().equals(entry.getKey().second())) continue;
                boolean sendsFirst = first.getTemperature() > second.getTemperature();
                double candidate = sendsFirst ? a.sendingCoefficient() : b.sendingCoefficient();
                if (candidate > coefficient && HeatNetwork.discover(a.getPos(), a.getFrontFacing(), lookup).contains(b.getPos())) {
                    coefficient = candidate;
                    donor = sendsFirst ? first : second;
                    receiver = sendsFirst ? second : first;
                }
            }
            if (donor == null) continue;
            double amount = HeatTransfer.packageJoules(donor.getStoredHeat(), donor.getHeatCapacity(), donor.getMaxTemperature(),
                    receiver.getStoredHeat(), receiver.getHeatCapacity(), receiver.getMaxTemperature(), coefficient);
            if (amount <= 0 || donor.changeHeat(-amount, true) != -amount || receiver.changeHeat(amount, true) != amount) continue;
            // Final mutation contract, server-thread execution, and no callbacks between the two writes.
            double removed = donor.changeHeat(-amount, false);
            double added = receiver.changeHeat(amount, false);
            if (removed != -amount || added != amount) throw new IllegalStateException("Heat transfer acceptance changed");
        }
    }
}

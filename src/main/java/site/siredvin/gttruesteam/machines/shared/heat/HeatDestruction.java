package site.siredvin.gttruesteam.machines.shared.heat;


import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.saveddata.SavedData;

import site.siredvin.gttruesteam.machines.parts.HeatHatchMachine;

import java.util.HashSet;

/** Deferred identity-checked removals survive unavailable target chunks and server restarts. */
public final class HeatDestruction extends SavedData {

    private final CompoundTag pending;
    private boolean processing;

    private HeatDestruction() {
        this(new CompoundTag());
    }

    private HeatDestruction(CompoundTag tag) {
        pending = tag.getCompound("targets").copy();
    }

    public static HeatDestruction get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(HeatDestruction::new, HeatDestruction::new,
                "gttruesteam_heat_destruction");
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.put("targets", pending.copy());
        return tag;
    }

    void destroy(ServerLevel level, CompoundTag targets, BlockPos brokenPosition) {
        for (String key : targets.getAllKeys()) {
            pending.putString(key, targets.getString(key));
        }
        setDirty();
        // onRemove runs after replacement state is installed; never remove that replacement.
        if (brokenPosition != null) {
            String key = Long.toString(brokenPosition.asLong());
            if (targets.contains(key)) {
                pending.remove(key);
                sound(level, brokenPosition);
            }
        }
        process(level);
    }

    void process(ServerLevel level) {
        if (processing) return;
        processing = true;
        try {
            for (String key : new HashSet<>(pending.getAllKeys())) {
                BlockPos pos = BlockPos.of(Long.parseLong(key));
                if (level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) == null) continue;
                var machine = HeatNetworkManager.loadedMachine(level, pos);
                String identity = machine instanceof HeatMultiblockMachine controller ? controller.heatIdentity() :
                        machine instanceof HeatHatchMachine hatch ? hatch.heatIdentity() : "";
                String expected = pending.getString(key);
                pending.remove(key);
                setDirty();
                if (!identity.equals(expected)) continue;
                level.removeBlock(pos, false);
                sound(level, pos);
            }
        } finally {
            processing = false;
        }
    }

    private static void sound(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4, 1);
    }
}

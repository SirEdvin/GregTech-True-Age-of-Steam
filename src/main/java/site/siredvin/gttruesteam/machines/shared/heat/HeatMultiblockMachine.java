package site.siredvin.gttruesteam.machines.shared.heat;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import site.siredvin.gttruesteam.api.IHeatMachine;
import site.siredvin.gttruesteam.common.Constants;
import site.siredvin.gttruesteam.machines.parts.HeatHatchMachine;

import java.util.UUID;

public abstract class HeatMultiblockMachine extends WorkableMultiblockMachine implements IHeatMachine, IMachineLife {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            HeatMultiblockMachine.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);
    @Persisted
    private double storedHeat;
    @Persisted
    private boolean melting;
    @Persisted
    private int meltingTicksRemaining;
    @Persisted
    private String heatIdentity = UUID.randomUUID().toString();
    @Persisted
    private CompoundTag ownedHeatHatches = new CompoundTag();
    @Persisted
    private CompoundTag structureBlocks = new CompoundTag();
    @Persisted
    private boolean destroyed;
    private MultiblockState heatStructure;
    private long episodeStart = Long.MIN_VALUE;
    private long lastFinalizedTick = Long.MIN_VALUE;
    private boolean thermalStructureReady;

    protected HeatMultiblockMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public abstract double getHeatCapacity();

    @Override
    public abstract double getMaxTemperature();

    @Override
    public final double getStoredHeat() {
        return storedHeat;
    }

    @Override
    public final double getTemperature() {
        return HeatTransfer.temperature(storedHeat, getHeatCapacity(), getMaxTemperature());
    }

    @Override
    public final boolean isMelting() {
        return melting;
    }

    @Override
    public final int getMeltingTicksRemaining() {
        return meltingTicksRemaining;
    }

    public final String heatIdentity() {
        return heatIdentity;
    }

    public final boolean hasValidThermalStructure() {
        return thermalStructureReady && isFormed() && !destroyed && !getMultiblockState().hasError() &&
                HeatTransfer.validDefinition(getHeatCapacity(), getMaxTemperature()) && Double.isFinite(getTemperature());
    }

    @Override
    public final double changeHeat(double deltaJoules, boolean simulate) {
        if (!(getLevel() instanceof ServerLevel level) || !level.getServer().isSameThread() || isInValid() ||
                !hasValidThermalStructure()) return 0;
        double accepted = HeatTransfer.acceptedDelta(storedHeat, getHeatCapacity(), getMaxTemperature(), deltaJoules);
        if (!simulate && accepted != 0) {
            storedHeat += accepted;
            reevaluateMelting();
            markDirty();
        }
        return accepted;
    }

    private void reevaluateMelting() {
        if (!hasValidThermalStructure()) return;
        if (storedHeat > getHeatCapacity()) {
            if (!melting) {
                melting = true;
                meltingTicksRemaining = Constants.HEAT_MELTING_TICKS;
                episodeStart = getLevel().getGameTime();
                markDirty();
            }
        } else if (melting) {
            melting = false;
            meltingTicksRemaining = 0;
            markDirty();
        }
    }

    /** Invoked once at level END, after every scheduled pair exchange. */
    final void finalizeHeatTick(long tick) {
        if (isInValid() || destroyed || lastFinalizedTick == tick) return;
        lastFinalizedTick = tick;
        reevaluateMelting();
        if (melting && episodeStart != tick) {
            meltingTicksRemaining = Math.max(0, meltingTicksRemaining - 1);
            markDirty();
            if (meltingTicksRemaining == 0) destroyMeltingStructure(null);
        }
    }

    @Override
    public void onLoad() {
        getPatternLock().lock();
        try {
        if (getLevel() instanceof ServerLevel level) {
            double normalized = HeatTransfer.normalize(storedHeat);
            if (normalized != storedHeat) {
                storedHeat = normalized;
                markDirty();
            }
            // Formation is revalidated before reading subclass characteristics; loading is not cooling.
            thermalStructureReady = false;
            episodeStart = Long.MIN_VALUE;
            lastFinalizedTick = Long.MIN_VALUE;
            if (!structureBlocks.isEmpty()) {
                var state = getMultiblockState();
                state.cache = new LongOpenHashSet();
                for (String key : structureBlocks.getAllKeys()) state.cache.add(Long.parseLong(key));
                state.lastController = this;
                MultiblockWorldSavedData.getOrCreate(level).addMapping(state);
            }
            HeatNetworkManager.registerController(level, getPos());
        }
        super.onLoad();
        } finally {
            getPatternLock().unlock();
        }
    }

    @Override
    public void onUnload() {
        if (getLevel() instanceof ServerLevel level) HeatNetworkManager.unregisterController(level, getPos());
        thermalStructureReady = false;
        super.onUnload();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (!(getLevel() instanceof ServerLevel)) return;
        thermalStructureReady = true;
        // Preserve failure targets during a continuous melting episode, including unloaded hatches.
        if (!melting) ownedHeatHatches = new CompoundTag();
        for (var part : getParts()) {
            if (part instanceof HeatHatchMachine hatch) {
                ownedHeatHatches.putString(Long.toString(hatch.getPos().asLong()), hatch.heatIdentity());
            }
        }
        CompoundTag blocks = new CompoundTag();
        for (BlockPos pos : getMultiblockState().getCache()) {
            if (getLevel().hasChunkAt(pos)) {
                blocks.putString(Long.toString(pos.asLong()), ForgeRegistries.BLOCKS.getKey(getLevel().getBlockState(pos).getBlock()).toString());
            }
        }
        structureBlocks = blocks;
        markDirty();
        // Subclasses may finish deriving characteristics after super.onStructureFormed().
    }

    @Override
    public void onStructureInvalid() {
        thermalStructureReady = false;
        super.onStructureInvalid();
    }

    @Override
    public void onPartUnload() {
        thermalStructureReady = false;
        super.onPartUnload();
    }

    @Override
    public MultiblockState getMultiblockState() {
        if (heatStructure == null) {
            heatStructure = new MultiblockState(getLevel(), getPos()) {
                @Override
                public boolean isPosInCache(BlockPos pos) {
                    return structureBlocks.contains(Long.toString(pos.asLong())) ||
                            (cache != null && super.isPosInCache(pos));
                }

                @Override
                public void onBlockStateChanged(BlockPos pos, BlockState state) {
                    String expected = structureBlocks.getString(Long.toString(pos.asLong()));
                    if (melting && !expected.isEmpty() &&
                            !expected.equals(String.valueOf(ForgeRegistries.BLOCKS.getKey(state.getBlock())))) {
                        destroyMeltingStructure(pos);
                    }
                    if (!destroyed) super.onBlockStateChanged(pos, state);
                }
            };
        }
        return heatStructure;
    }

    @Override
    public void onMachineRemoved() {
        destroyMeltingStructure(getPos());
    }

    public final void destroyMeltingStructure(BlockPos brokenPosition) {
        if (!melting || destroyed || !(getLevel() instanceof ServerLevel level) || !level.getServer().isSameThread()) return;
        destroyed = true;
        markDirty();
        var targets = ownedHeatHatches.copy();
        targets.putString(Long.toString(getPos().asLong()), heatIdentity);
        HeatDestruction.get(level).destroy(level, targets, brokenPosition);
    }
}

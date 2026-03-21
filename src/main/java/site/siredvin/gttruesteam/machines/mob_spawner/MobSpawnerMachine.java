package site.siredvin.gttruesteam.machines.mob_spawner;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MobSpawnerMachine extends WorkableElectricMultiblockMachine {

    @Nullable
    private BlockPos cachedSpawnerPos = null;

    public MobSpawnerMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object @NotNull... args) {
        return new MobSpawnerRecipeLogic(this);
    }

    @Override
    public @NotNull MobSpawnerRecipeLogic getRecipeLogic() {
        return (MobSpawnerRecipeLogic) super.getRecipeLogic();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        this.cachedSpawnerPos = findSpawner();
    }

    /**
     * Searches nearby blocks for a spawner within the structure bounds (5x5x5 around controller).
     */
    @Nullable
    private BlockPos findSpawner() {
        Level level = getLevel();
        if (level == null || level.isClientSide) return null;

        BlockPos controllerPos = getPos();
        for (BlockPos pos : BlockPos.betweenClosed(
                controllerPos.offset(-2, -2, -2),
                controllerPos.offset(2, 2, 2))) {
            if (level.getBlockState(pos).is(Blocks.SPAWNER)) {
                return pos.immutable();
            }
        }
        return null;
    }

    /**
     * Returns the entity type of the mob in the spawner, if the structure is formed
     * and a spawner is present with valid spawn data.
     */
    public Optional<ResourceLocation> getSpawnerMobType() {
        if (!isFormed()) return Optional.empty();

        Level level = getLevel();
        if (level == null || level.isClientSide) return Optional.empty();

        BlockPos spawnerPos = cachedSpawnerPos;
        if (spawnerPos == null) {
            spawnerPos = findSpawner();
            this.cachedSpawnerPos = spawnerPos;
        }
        if (spawnerPos == null) return Optional.empty();

        BlockEntity be = level.getBlockEntity(spawnerPos);
        if (!(be instanceof SpawnerBlockEntity spawner)) return Optional.empty();

        // Read spawn data from NBT
        CompoundTag tag = new CompoundTag();
        spawner.saveAdditional(tag);

        if (tag.contains("SpawnData")) {
            CompoundTag spawnData = tag.getCompound("SpawnData");
            if (spawnData.contains("entity")) {
                CompoundTag entity = spawnData.getCompound("entity");
                if (entity.contains("id")) {
                    try {
                        return Optional.of(new ResourceLocation(entity.getString("id")));
                    } catch (Exception ignored) {
                        // Invalid resource location
                    }
                }
            }
        }
        return Optional.empty();
    }
}

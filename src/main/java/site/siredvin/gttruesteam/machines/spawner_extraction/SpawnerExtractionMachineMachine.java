package site.siredvin.gttruesteam.machines.spawner_extraction;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import site.siredvin.gttruesteam.api.ISpawnerExtractionCondition;

public class SpawnerExtractionMachineMachine extends WorkableElectricMultiblockMachine
        implements ISpawnerExtractionCondition {

    private @Nullable Holder<EntityType<?>> mobInside = null;

    public SpawnerExtractionMachineMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public BlockPos getSpawnerPos() {
        var facing = getFrontFacing().getOpposite();
        var currentPos = getPos();
        return currentPos.relative(facing, 2).above(3);
    }

    protected void refreshMobInside() {
        var level = getLevel();
        if (level == null)
            return;
        var spawnerPos = getSpawnerPos();
        var spawnerEntity = level.getBlockEntity(spawnerPos);
        if (!(spawnerEntity instanceof SpawnerBlockEntity))
            return;
        var data = spawnerEntity.serializeNBT();
        var entityTypeId = data.getCompound("SpawnData").getCompound("entity").getString("id");
        var entityTypeHolder = ForgeRegistries.ENTITY_TYPES.getHolder(ResourceLocation.parse(entityTypeId));
        entityTypeHolder.ifPresent(typeHolder -> mobInside = typeHolder);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
    }

    public @Nullable Holder<EntityType<?>> getMobInside() {
        if (mobInside == null) {
            refreshMobInside();
        }
        return mobInside;
    }

    @Override
    public boolean supportMob(Holder<EntityType<?>> entityType) {
        var mob = getMobInside();
        return mob != null && mob.equals(entityType);
    }

    @Override
    public boolean supportMobType(MobType mobType) {
        var mob = getMobInside();
        return mob != null && mob.is(mobType.getTag());
    }
}

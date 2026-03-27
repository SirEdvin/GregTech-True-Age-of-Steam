package site.siredvin.gttruesteam.machines.spawner_extraction;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SpawnerExtractionMachineMachine extends WorkableElectricMultiblockMachine {

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
}

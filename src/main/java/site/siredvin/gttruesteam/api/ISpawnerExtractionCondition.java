package site.siredvin.gttruesteam.api;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;

import site.siredvin.gttruesteam.machines.spawner_extraction.MobType;

public interface ISpawnerExtractionCondition {

    boolean supportMob(Holder<EntityType<?>> entityType);

    boolean supportMobType(MobType mobType);
}

package site.siredvin.gttruesteam.machines.spawner_extraction;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import site.siredvin.gttruesteam.TrueSteamEntityTypeTags;

public enum MobType {

    NETHER(TrueSteamEntityTypeTags.NETHER);

    private final TagKey<EntityType<?>> tag;

    MobType(TagKey<EntityType<?>> tag) {
        this.tag = tag;
    }

    public TagKey<EntityType<?>> getTag() {
        return tag;
    }
}

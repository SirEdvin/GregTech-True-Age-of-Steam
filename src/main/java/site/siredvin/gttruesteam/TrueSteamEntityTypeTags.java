package site.siredvin.gttruesteam;

import com.tterrag.registrate.providers.ProviderType;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class TrueSteamEntityTypeTags {

    public static final TagKey<EntityType<?>> NETHER = TagKey.create(Registries.ENTITY_TYPE,
            GTTrueSteam.id("nether"));

    public static void sayHi() {
        GTTrueSteam.REGISTRATE.addDataGenerator(ProviderType.ENTITY_TAGS, prov -> prov.addTag(NETHER)
                .add(EntityType.BLAZE, EntityType.MAGMA_CUBE, EntityType.WITHER_SKELETON, EntityType.HOGLIN, EntityType.ZOGLIN, EntityType.PIGLIN, EntityType.PIGLIN_BRUTE));
    }
}

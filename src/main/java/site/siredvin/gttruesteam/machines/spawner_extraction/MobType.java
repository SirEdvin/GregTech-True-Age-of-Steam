package site.siredvin.gttruesteam.machines.spawner_extraction;

import net.minecraft.Util;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamEntityTypeTags;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public enum MobType {

    NETHER(TrueSteamEntityTypeTags.NETHER);

    private final TagKey<EntityType<?>> tag;
    private final String translationKey;

    MobType(TagKey<EntityType<?>> tag) {
        this.tag = tag;
        this.translationKey = Util.makeDescriptionId("mob_type", GTTrueSteam.id(name().toLowerCase()));
    }

    public TagKey<EntityType<?>> getTag() {
        return tag;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public String readableName() {
        String[] words = name().split("_");
        return IntStream.range(0, words.length)
                .mapToObj(
                        i -> i == 0 ? words[i].charAt(0) + words[i].substring(1).toLowerCase() : words[i].toLowerCase())
                .collect(Collectors.joining(" "));
    }
}

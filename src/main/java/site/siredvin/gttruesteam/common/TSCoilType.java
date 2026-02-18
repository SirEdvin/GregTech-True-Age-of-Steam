package site.siredvin.gttruesteam.common;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamMaterials;
import site.siredvin.gttruesteam.api.ICoolingCoilType;

public enum TSCoilType implements StringRepresentable, ICoolingCoilType {

    FROSTBITE_MAGNALIUM("frostbite_magnalium", 10_000, 5, 1, TrueSteamMaterials.FrostbiteMagnalium,
            GTTrueSteam.id("block/coils/machine_coil_frostbite_magnalium"));

    @NotNull
    @Getter
    private final String name;
    @Getter
    private final int level;
    @Getter
    private final int coolingCapacity;
    @Getter
    private final int coolingRate;
    @NotNull
    @Getter
    private final Material material;
    @NotNull
    @Getter
    private final ResourceLocation texture;

    TSCoilType(@NotNull String name, int coolingCapacity, int coolingRate, int level, @NotNull Material material,
               @NotNull ResourceLocation texture) {
        this.name = name;
        this.coolingCapacity = coolingCapacity;
        this.level = level;
        this.material = material;
        this.texture = texture;
        this.coolingRate = coolingRate;
    }

    public int getTier() {
        return this.ordinal();
    }

    @NotNull
    @Override
    public String toString() {
        return getName();
    }

    @Override
    @NotNull
    public String getSerializedName() {
        return name;
    }
}

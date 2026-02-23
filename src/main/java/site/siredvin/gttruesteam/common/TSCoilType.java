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

    FROSTBITE_MAGNALIUM("frostbite_magnalium", 10_000, 5, 1, 0.9f, TrueSteamMaterials.FrostbiteMagnalium,
            GTTrueSteam.id("block/coils/machine_coil_frostbite_magnalium")),
    ESTRANGED_STEEL("estranged_steel", 30_000, 10, 2, 0.8f, TrueSteamMaterials.EstrangedSteel,
            GTTrueSteam.id("block/coils/machine_coil_estranged_steel"));

    @NotNull
    @Getter
    private final String name;
    @Getter
    private final int level;
    @Getter
    private final int coolingCapacity;
    @Getter
    private final int coolingRate;
    @Getter
    private final float activeCoolingReduction;
    @NotNull
    @Getter
    private final Material material;
    @NotNull
    @Getter
    private final ResourceLocation texture;

    TSCoilType(@NotNull String name, int coolingCapacity, int coolingRate, int level, float activeCoolingReduction,
               @NotNull Material material,
               @NotNull ResourceLocation texture) {
        this.name = name;
        this.coolingCapacity = coolingCapacity;
        this.activeCoolingReduction = activeCoolingReduction;
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

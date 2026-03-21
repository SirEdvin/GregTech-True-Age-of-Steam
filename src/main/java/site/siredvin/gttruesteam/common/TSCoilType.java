package site.siredvin.gttruesteam.common;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamConcepts;
import site.siredvin.gttruesteam.TrueSteamMaterials;
import site.siredvin.gttruesteam.api.ICoolingCoilType;

import java.util.function.Supplier;

public enum TSCoilType implements StringRepresentable, ICoolingCoilType {

    FROSTBITE_MAGNALIUM("frostbite_magnalium", 10_000, 5, 1, 0.9f, () -> TrueSteamMaterials.FrostbiteMagnalium,
            GTTrueSteam.id("block/coils/machine_coil_frostbite_magnalium")),
    COOLING_COMETAL("cooling_cometal", 30_000, 10, 2, 0.8f, () -> TrueSteamConcepts.CoolingConcept.getMaterial(),
            GTTrueSteam.id("block/coils/machine_coil_cooling_cometal")),
    ESTRANGED_STEEL("estranged_steel", 60_000, 20, 3, 0.7f, () -> TrueSteamMaterials.EstrangedSteel,
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
    private final Supplier<Material> materialSupplier;
    @NotNull
    @Getter
    private final ResourceLocation texture;

    TSCoilType(@NotNull String name, int coolingCapacity, int coolingRate, int level, float activeCoolingReduction,
               @NotNull Supplier<Material> materialSupplier,
               @NotNull ResourceLocation texture) {
        this.name = name;
        this.coolingCapacity = coolingCapacity;
        this.activeCoolingReduction = activeCoolingReduction;
        this.level = level;
        this.materialSupplier = materialSupplier;
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

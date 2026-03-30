package site.siredvin.gttruesteam.api;

import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface ICoolingCoilType extends ICoilType {

    int getCoolingCapacity();

    int getCoolingRate();

    float getActiveCoolingReduction();

    @NotNull
    String getName();

    int getLevel();

    Supplier<Material> getMaterialSupplier();

    ResourceLocation getTexture();

    @Override
    default int getCoilTemperature() {
        return 0;
    }

    @Override
    default int getEnergyDiscount() {
        return 0;
    }

    default Material getMaterial() {
        return getMaterialSupplier().get();
    }

    @Override
    default int getTier() {
        return getLevel();
    }
}

package site.siredvin.gttruesteam.api;

import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface ICoolingCoilType extends ICoilType {
    int getCoolingCapacity();
    int getCoolingRate();
    @NotNull
    String getName();
    int getLevel();
    int getTier();
    Material getMaterial();
    ResourceLocation getTexture();

    @Override
    default int getCoilTemperature() {
        return 0;
    }

    @Override
    default int getEnergyDiscount() {
        return 0;
    }
}

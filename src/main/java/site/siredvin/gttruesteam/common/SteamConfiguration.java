package site.siredvin.gttruesteam.common;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraftforge.fluids.FluidStack;

public record SteamConfiguration(
                                 double density, Material water, int waterConversionRate, int waterOutput,
                                 int compressionEUt, int compressionDuration) {

    public FluidStack waterOutputStack() {
        return water.getFluid(waterOutput);
    }
}

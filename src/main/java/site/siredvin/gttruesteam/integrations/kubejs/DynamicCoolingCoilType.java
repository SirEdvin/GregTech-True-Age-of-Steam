package site.siredvin.gttruesteam.integrations.kubejs;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.resources.ResourceLocation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.api.ICoolingCoilType;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class DynamicCoolingCoilType implements ICoolingCoilType {

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

    @Override
    public @NotNull String getName() {
        return materialSupplier.get().getName();
    }
}

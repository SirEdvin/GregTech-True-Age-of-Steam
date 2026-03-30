package site.siredvin.gttruesteam.integrations.kubejs;

import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.client.VariantBlockStateGenerator;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.GTTrueSteamAPI;
import site.siredvin.gttruesteam.common.CoolingCoilBlock;

import java.util.function.Supplier;

@Accessors(chain = true, fluent = true)
public class CoolingCoilBlockBuilder extends BlockBuilder {

    @Setter
    public transient int coolingCapacity = 0, level = 0, coolingRate = 1;
    @Setter
    public transient float activeCoolingReduction = 0;
    @NotNull
    public transient Supplier<Material> material = () -> GTMaterials.NULL;
    @Setter
    public transient String texture = "minecraft:missingno";

    public CoolingCoilBlockBuilder(ResourceLocation i) {
        super(i);
        property(GTBlockStateProperties.ACTIVE);
        renderType("cutout_mipped");
        noValidSpawns(true);
    }

    @Override
    protected void generateBlockStateJson(VariantBlockStateGenerator bs) {
        bs.simpleVariant("active=false", newID("block/", "").toString());
        bs.simpleVariant("active=true", newID("block/", "_active").toString());
    }

    @Override
    protected void generateBlockModelJsons(AssetJsonGenerator generator) {
        generator.blockModel(id, m -> {
            m.parent("minecraft:block/cube_all");
            m.texture("all", texture);
        });
        generator.blockModel(id.withSuffix("_active"), m -> {
            m.parent("gtceu:block/cube_2_layer/all");
            m.texture("bot_all", texture);
            m.texture("top_all", texture + "_bloom");
        });
    }

    public CoolingCoilBlockBuilder coilMaterial(@NotNull Supplier<Material> material) {
        this.material = material;
        return this;
    }

    @Override
    public Block createObject() {
        DynamicCoolingCoilType coilType = new DynamicCoolingCoilType(level, coolingCapacity, coolingRate,
                activeCoolingReduction,
                material, ResourceLocation.parse(texture));
        CoolingCoilBlock result = new CoolingCoilBlock(this.createProperties(), coilType);
        GTTrueSteamAPI.COOLING_COILS.put(coilType, () -> result);
        return result;
    }
}

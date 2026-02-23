package site.siredvin.gttruesteam;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.client.model.generators.ModelFile;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import site.siredvin.gttruesteam.api.ICoolingCoilType;
import site.siredvin.gttruesteam.common.CoolingCoilBlock;
import site.siredvin.gttruesteam.common.TSCoilType;

import java.util.function.Supplier;

public class TrueSteamBlocks {

    public static BlockEntry<Block> createCasingBlock(String name, ResourceLocation texture) {
        return createCasingBlock(name, Block::new, texture, () -> Blocks.IRON_BLOCK,
                () -> RenderType::solid);
    }

    public static BlockEntry<Block> createCasingBlock(String name,
                                                      NonNullFunction<BlockBehaviour.Properties, Block> blockSupplier,
                                                      ResourceLocation texture,
                                                      NonNullSupplier<? extends Block> properties,
                                                      Supplier<Supplier<RenderType>> type) {
        return GTTrueSteam.REGISTRATE.block(name, blockSupplier)
                .initialProperties(properties)
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(type)
                .exBlockstate(GTModels.cubeAllModel(texture))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .item(BlockItem::new)
                .build()
                .register();
    }

    public static BlockEntry<Block> SlightlyCorrosionProofCasing = createCasingBlock("slightly_corrosion_proof_casing",
            GTCEu.id("block/casings/gcym/corrosion_proof_casing"));
    public static BlockEntry<Block> InfernalAlloyCasing = createCasingBlock("infernal_alloy_casing",
            GTTrueSteam.id("block/infernal_alloy_casing"));
    public static BlockEntry<Block> FrostOverproofedCasing = createCasingBlock("frost_overproofed_casing",
            GTTrueSteam.id("block/frost_overproofed_casing"));
    public static BlockEntry<CoolingCoilBlock> COIL_FROSTBITE_MAGNALIUM = createCoolingCoil(
            TSCoilType.FROSTBITE_MAGNALIUM);
    public static BlockEntry<CoolingCoilBlock> COIL_ESTRANGED_STEEL = createCoolingCoil(
            TSCoilType.ESTRANGED_STEEL);

    public static BlockEntry<Block> BoilerHusk = GTTrueSteam.REGISTRATE.block("husk_of_the_boiler", Block::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
            .addLayer(() -> RenderType::solid)
            .exBlockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models().cubeBottomTop(ctx.getName(),
                    GTCEu.id("block/casings/steam/steel/side"),
                    GTCEu.id("block/casings/steam/steel/bottom"),
                    GTCEu.id("block/generators/boiler/solar/overlay_top"))))
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
            .item(BlockItem::new)
            .build()
            .register();

    public static BlockEntry<GlassBlock> BronzeGlass = GTTrueSteam.REGISTRATE.block("bronze_glass", GlassBlock::new)
            .initialProperties(() -> Blocks.GLASS)
            .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
            .addLayer(() -> RenderType::cutoutMipped)
            .exBlockstate(GTModels.cubeAllModel(GTTrueSteam.id("block/bronze_glass")))
            .item(BlockItem::new)
            .build()
            .register();

    public static void sayHi() {}

    public static NonNullBiConsumer<DataGenContext<Block, CoolingCoilBlock>, RegistrateBlockstateProvider> createCoilModel(ICoilType coilType) {
        return (ctx, prov) -> {
            String name = ctx.getName();
            ActiveBlock block = ctx.getEntry();
            ModelFile inactive = prov.models().cubeAll(name, coilType.getTexture());
            ModelFile active = prov.models().withExistingParent(name + "_active", GTCEu.id("block/cube_2_layer/all"))
                    .texture("bot_all", coilType.getTexture())
                    .texture("top_all", coilType.getTexture().withSuffix("_bloom"));
            prov.getVariantBuilder(block)
                    .partialState().with(GTBlockStateProperties.ACTIVE, false).modelForState().modelFile(inactive)
                    .addModel()
                    .partialState().with(GTBlockStateProperties.ACTIVE, true).modelForState().modelFile(active)
                    .addModel();
        };
    }

    public static BlockEntry<CoolingCoilBlock> createCoolingCoil(ICoolingCoilType coilType) {
        var coilBlock = GTTrueSteam.REGISTRATE
                .block("%s_coil_block".formatted(coilType.getName()), p -> new CoolingCoilBlock(p, coilType))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(createCoilModel(coilType))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .item(BlockItem::new)
                .build()
                .register();
        GTTrueSteamAPI.COOLING_COILS.put(coilType, coilBlock);
        return coilBlock;
    }
}

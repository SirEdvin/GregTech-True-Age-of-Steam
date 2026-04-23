package site.siredvin.gttruesteam;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
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
import site.siredvin.gttruesteam.common.TooltipBlockItem;

import java.util.function.Supplier;

public class TrueSteamBlocks {

    public static NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> createActiveModel(ResourceLocation texture) {
        return (ctx, prov) -> {
            Block block = ctx.getEntry();
            ModelFile inactive = prov.models().cubeAll(ctx.getName(), texture);
            ModelFile active = prov.models().cubeAll(ctx.getName() + "_active", texture.withSuffix("_active"));
            prov.getVariantBuilder(block)
                    .partialState().with(GTBlockStateProperties.ACTIVE, false).modelForState().modelFile(inactive)
                    .addModel()
                    .partialState().with(GTBlockStateProperties.ACTIVE, true).modelForState().modelFile(active)
                    .addModel();
        };
    }

    public static BlockEntry<Block> createCasingBlock(String name, ResourceLocation texture) {
        return createCasingBlock(name, Block::new, GTModels.cubeAllModel(texture), () -> Blocks.IRON_BLOCK,
                () -> RenderType::solid);
    }

    public static BlockEntry<Block> createActiveCasingBlock(String name, ResourceLocation texture) {
        return createCasingBlock(name, ActiveBlock::new, createActiveModel(texture), () -> Blocks.IRON_BLOCK,
                () -> RenderType::solid);
    }

    public static BlockEntry<Block> createCasingBlock(String name,
                                                      NonNullFunction<BlockBehaviour.Properties, Block> blockSupplier,
                                                      NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> model,
                                                      NonNullSupplier<? extends Block> properties,
                                                      Supplier<Supplier<RenderType>> type) {
        return GTTrueSteam.REGISTRATE.block(name, blockSupplier)
                .initialProperties(properties)
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(type)
                .exBlockstate(model)
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
    public static BlockEntry<Block> ConceptualizedSteelPipeCasing = createActiveCasingBlock(
            "conceptualized_steel_pipe_casing",
            GTTrueSteam.id("block/conceptualized_steel_pipe_casing"));
    public static BlockEntry<Block> ConceptualizedSteelSolidCasing = createCasingBlock(
            "conceptualized_steel_solid_casing",
            GTTrueSteam.id("block/conceptualized_steel_solid_casing"));

    public static BlockEntry<Block> ExtractionInfusedCasing = createCasingBlock("extraction_infused_casing",
            GTTrueSteam.id("block/extraction_infused_casing"));
    public static BlockEntry<Block> ExtractionInfusedPipeCasing = createActiveCasingBlock(
            "extraction_infused_pipe_casing",
            GTTrueSteam.id("block/extraction_infused_pipe_casing"));

    public static BlockEntry<Block> CoolingInfusedPipeCasing = createCasingBlock("cooling_infused_pipe_casing",
            GTTrueSteam.id("block/cooling_infused_pipe_casing"));

    public static BlockEntry<Block> BathingInfusedCasing = createCasingBlock("bathing_infused_casing",
            GTTrueSteam.id("block/bathing_infused_casing"));

    public static BlockEntry<Block> WhirlpoolCasing = GTTrueSteam.REGISTRATE
            .block("whirlpool_casing", Block::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
            .addLayer(() -> RenderType::solid)
            .exBlockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models().cubeBottomTop(
                    ctx.getName(),
                    GTTrueSteam.id("block/bathing_infused_casing"),
                    GTTrueSteam.id("block/bathing_infused_casing"),
                    GTTrueSteam.id("block/whirlpool_casing_top"))))
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
            .item(BlockItem::new)
            .build()
            .register();

    public static BlockEntry<CoolingCoilBlock> COIL_FROSTBITE_MAGNALIUM = createCoolingCoil(
            TSCoilType.FROSTBITE_MAGNALIUM);
    public static BlockEntry<CoolingCoilBlock> COIL_COOLING_COMETAL = createCoolingCoil(
            TSCoilType.COOLING_COMETAL);
    public static BlockEntry<CoolingCoilBlock> COIL_ESTRANGED_STEEL = createCoolingCoil(
            TSCoilType.ESTRANGED_STEEL);

    public static BlockEntry<Block> BoilerHusk = GTTrueSteam.REGISTRATE.block("husk_of_the_boiler", Block::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
            .addLayer(() -> RenderType::solid)
            .exBlockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models().cubeBottomTop(ctx.getName(),
                    GTCEu.id("block/casings/steam/steel/side"),
                    GTCEu.id("block/casings/steam/steel/bottom"),
                    GTTrueSteam.id("block/husk_of_the_boiler_top"))))
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
            .item(BlockItem::new)
            .build()
            .register();

    public static BlockEntry<Block> BeatingBoilerHusk = GTTrueSteam.REGISTRATE.block("beating_boiler_husk", Block::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
            .addLayer(() -> RenderType::solid)
            .exBlockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models().cubeBottomTop(ctx.getName(),
                    GTTrueSteam.id("block/beating_boiler_husk_side"),
                    GTTrueSteam.id("block/heating_casing"),
                    GTTrueSteam.id("block/heating_casing"))))
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
            .item((b, p) -> new TooltipBlockItem(b, p, TrueSteamLang.BEATING_HUSK_TOOLTIP_1,
                    TrueSteamLang.BEATING_HUSK_TOOLTIP_2))
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

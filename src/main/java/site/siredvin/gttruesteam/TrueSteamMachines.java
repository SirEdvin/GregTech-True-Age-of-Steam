package site.siredvin.gttruesteam;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.model.machine.overlays.WorkableOverlays;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.model.builder.MachineModelBuilder;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import site.siredvin.gttruesteam.common.BoilerLevel;
import site.siredvin.gttruesteam.machines.parts.HeatHatchMachine;
import site.siredvin.gttruesteam.machines.shared.heat.DebugHeatMachine;
import site.siredvin.gttruesteam.machines.boilers.ExpandedSteamLiquidBoilerMachine;
import site.siredvin.gttruesteam.machines.boilers.ExpandedSteamSolarBoilerMachine;
import site.siredvin.gttruesteam.machines.boilers.ExpandedSteamSolidBoilerMachine;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.RECIPE_LOGIC_STATUS;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.VENT_DIRECTION;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.VENT_OVERLAY;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.addWorkableOverlays;

public class TrueSteamMachines {

    public static final MultiblockMachineDefinition DEBUG_HEAT_PRODUCER = debugHeatMachine("debug_heat_producer", true);
    public static final MultiblockMachineDefinition DEBUG_HEAT_CONSUMER = debugHeatMachine("debug_heat_consumer", false);

    private static MultiblockMachineDefinition debugHeatMachine(String name, boolean producer) {
        return GTTrueSteam.REGISTRATE.multiblock(name, holder -> new DebugHeatMachine(holder, producer))
                .langValue(producer ? "Debug Heat Producer" : "Debug Heat Consumer")
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(producer ? TrueSteamRecipeTypes.DEBUG_HEAT_PRODUCING : TrueSteamRecipeTypes.DEBUG_HEAT_CONSUMING)
                .appearanceBlock(() -> Blocks.IRON_BLOCK)
                .additionalDisplay((controller, text) -> ((DebugHeatMachine) controller).addDisplayText(text))
                .pattern(def -> FactoryBlockPattern.start()
                        .aisle("XXX", "XXX", "XXX")
                        .aisle("XXX", "X X", "XXX")
                        .aisle("XXX", "XSX", "XXX")
                        .where("S", Predicates.controller(Predicates.blocks(def.get())))
                        .where("X", Predicates.blocks(Blocks.IRON_BLOCK).or(Predicates.abilities(TrueSteamPartAbilities.HEAT)))
                        .where(" ", Predicates.air()).build())
                .tooltips(Component.translatable(producer ? "gttruesteam.debug_heat.producer" : "gttruesteam.debug_heat.consumer"),
                        Component.translatable("gttruesteam.debug_heat.structure"))
                .workableCasingModel(ResourceLocation.fromNamespaceAndPath("minecraft", "block/iron_block"),
                        GTCEu.id("block/multiblock/electric_blast_furnace"))
                .register();
    }

    public static final List<MachineDefinition> HEAT_HATCHES = List.of(GTValues.HV, GTValues.EV, GTValues.IV, GTValues.LuV)
            .stream().map(tier -> GTTrueSteam.REGISTRATE
                    .machine(GTValues.VN[tier].toLowerCase(java.util.Locale.ROOT) + "_heat_hatch",
                            holder -> new HeatHatchMachine(holder, tier))
                    .langValue(GTValues.VN[tier] + " Heat Hatch")
                    .tier(tier)
                    .rotationState(RotationState.ALL)
                    .abilities(TrueSteamPartAbilities.HEAT)
                    .tooltips(Component.translatable("gttruesteam.heat.coefficient", HeatHatchMachine.coefficient(tier)),
                            Component.translatable("gttruesteam.heat.tooltip"),
                            Component.translatable("gtceu.part_sharing.disabled"))
                    .overlayTieredHullModel(GTCEu.id("block/machine/part/data_access_hatch"))
                    .register()).toList();

    public static final MachineDefinition[] REDSTONE_HATCHES = registerRedstoneHatches();

    private static MachineDefinition[] registerRedstoneHatches() {
        var definitions = new MachineDefinition[com.gregtechceu.gtceu.api.GTValues.LuV + 1];
        for (int tier = com.gregtechceu.gtceu.api.GTValues.LV; tier <= com.gregtechceu.gtceu.api.GTValues.LuV; tier++) {
            int hatchTier = tier;
            String tierName = com.gregtechceu.gtceu.api.GTValues.VN[tier];
            definitions[tier] = GTTrueSteam.REGISTRATE.machine(tierName.toLowerCase(java.util.Locale.ROOT) + "_redstone_hatch",
                    holder -> new site.siredvin.gttruesteam.machines.redstone.RedstoneHatchMachine(holder, hatchTier))
                    .tier(tier)
                    .langValue(tierName + " Redstone Output Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(site.siredvin.gttruesteam.machines.redstone.RedstoneHatchMachine.ABILITY)
                    .modelProperty(GTMachineModelProperties.IS_FORMED, false)
                    .overlayTieredHullModel(GTCEu.id("block/machine/part/data_access_hatch"))
                    .tooltips(Component.translatable("gttruesteam.redstone.tooltip", site.siredvin.gttruesteam.machines.redstone.RedstoneHatchMachine.capacityForTier(tier)),
                            Component.translatable("gttruesteam.redstone.priority"))
                    .register();
        }
        return definitions;
    }

    public static final ResourceLocation LC_STEAM_HULL_MODEL = GTTrueSteam.id("block/lava_coated_boiler");
    public static final ResourceLocation IA_STEAM_HULL_MODEL = GTTrueSteam.id("block/infernal_alloy_boiler");
    public static final ResourceLocation HC_STEAM_HULL_MODEL = GTTrueSteam.id("block/heating_charged_boiler");

    public static final List<MachineDefinition> SOLID = registerSteamMachine("solid",
            ExpandedSteamSolidBoilerMachine::new, (boilerLevel, builder) -> builder
                    .recipeType(GTRecipeTypes.STEAM_BOILER_RECIPES)
                    .rotationState(RotationState.ALL)
                    .recipeModifier(ExpandedSteamSolidBoilerMachine::recipeModifier)
                    .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                    .model(createWorkableSteamHullMachineModel(boilerLevel,
                            GTCEu.id("block/generators/boiler/coal")))
                    .tooltips(Component.translatable("gtceu.universal.tooltip.produces_fluid",
                            (ConfigHolder.INSTANCE.machines.smallBoilers.hpSolidBoilerBaseOutput *
                                    boilerLevel.getScaling() *
                                    FluidType.BUCKET_VOLUME / 20000)))
                    .register());

    public static final List<MachineDefinition> LIQUID = registerSteamMachine("liquid",
            ExpandedSteamLiquidBoilerMachine::new, (boilerLevel, builder) -> builder
                    .recipeType(GTRecipeTypes.STEAM_BOILER_RECIPES)
                    .rotationState(RotationState.ALL)
                    .recipeModifier(ExpandedSteamLiquidBoilerMachine::recipeModifier)
                    .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                    .model(createWorkableSteamHullMachineModel(boilerLevel,
                            GTCEu.id("block/generators/boiler/lava")))
                    .tooltips(Component.translatable("gtceu.universal.tooltip.produces_fluid",
                            (ConfigHolder.INSTANCE.machines.smallBoilers.hpLiquidBoilerBaseOutput *
                                    boilerLevel.getScaling() *
                                    FluidType.BUCKET_VOLUME / 20000)))
                    .register());

    public static final List<MachineDefinition> SOLAR = registerSteamMachine("solar",
            ExpandedSteamSolarBoilerMachine::new, (boilerLevel, builder) -> builder
                    .recipeType(GTRecipeTypes.STEAM_BOILER_RECIPES)
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeModifier(ExpandedSteamSolarBoilerMachine::recipeModifier)
                    .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                    .model(createWorkableSteamHullMachineModel(boilerLevel,
                            GTCEu.id("block/generators/boiler/solar")))
                    .tooltips(Component.translatable("gtceu.universal.tooltip.produces_fluid",
                            (ConfigHolder.INSTANCE.machines.smallBoilers.hpSolarBoilerBaseOutput *
                                    boilerLevel.getScaling() *
                                    FluidType.BUCKET_VOLUME / 20000)))
                    .register());

    public static MachineDefinition InfernalDrum = GTMachineUtils.registerDrum(GTTrueSteam.REGISTRATE,
            TrueSteamMaterials.InfernalAlloy, 16 * FluidType.BUCKET_VOLUME, "Infernal alloy drum");

    public static void sayHi() {}

    @Mod.EventBusSubscriber(modid = GTTrueSteam.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class HeatCreativeContents {
        @SubscribeEvent
        public static void addHatches(BuildCreativeModeTabContentsEvent event) {
            if (event.getTab() == GTCreativeModeTabs.MACHINE.get()) {
                HEAT_HATCHES.forEach(definition -> event.accept(definition.asStack()));
                event.accept(DEBUG_HEAT_PRODUCER.asStack());
                event.accept(DEBUG_HEAT_CONSUMER.asStack());
            }
        }
    }

    public static ModelFile steamHullModel(BlockModelProvider models, BoilerLevel boilerLevel) {
        switch (boilerLevel) {
            case INFERNAL -> {
                return models.getExistingFile(IA_STEAM_HULL_MODEL);
            }
            case LAVA_COATED -> {
                return models.getExistingFile(LC_STEAM_HULL_MODEL);
            }
            case HEATING_CHARGED -> {
                return models.getExistingFile(HC_STEAM_HULL_MODEL);
            }
        }
        return models.getExistingFile(LC_STEAM_HULL_MODEL);
    }

    private static List<MachineDefinition> registerSteamMachine(String name,
                                                                BiFunction<IMachineBlockEntity, Double, MetaMachine> factory,
                                                                BiFunction<BoilerLevel, MachineBuilder<MachineDefinition, ?>, MachineDefinition> builder) {
        return Arrays.stream(BoilerLevel.values()).map(bl -> builder.apply(bl,
                GTTrueSteam.REGISTRATE
                        .machine(bl.getTemplate().formatted(name), holder -> factory.apply(holder, bl.getScaling()))
                        .langValue(bl.boilerName(name))))
                .toList();
    }

    private static void makeWorkableOverlayPart(BlockModelProvider models,
                                                MachineModelBuilder<BlockModelBuilder> builder, ModelFile parentModel,
                                                WorkableOverlays overlays, RecipeLogic.Status status) {
        BlockModelBuilder model = models.nested().parent(parentModel);
        addWorkableOverlays(overlays, status, model);
        builder.part(model).condition(RECIPE_LOGIC_STATUS, status);
    }

    public static MachineBuilder.ModelInitializer createWorkableSteamHullMachineModel(BoilerLevel boilerLevel,
                                                                                      ResourceLocation overlayDir) {
        return (ctx, prov, builder) -> {
            WorkableOverlays overlays = WorkableOverlays.get(overlayDir, prov.getExistingFileHelper());
            ModelFile parent = steamHullModel(prov.models(), boilerLevel);

            makeWorkableOverlayPart(prov.models(), builder, parent, overlays, RecipeLogic.Status.IDLE);
            makeWorkableOverlayPart(prov.models(), builder, parent, overlays, RecipeLogic.Status.WORKING);
            makeWorkableOverlayPart(prov.models(), builder, parent, overlays, RecipeLogic.Status.WAITING);
            makeWorkableOverlayPart(prov.models(), builder, parent, overlays, RecipeLogic.Status.SUSPEND);

            if (!builder.getOwner().defaultRenderState().hasProperty(VENT_DIRECTION)) {
                return;
            }

            for (RelativeDirection relative : RelativeDirection.VALUES) {
                Direction dir = relative.global;
                builder.part().modelFile(prov.models().getExistingFile(VENT_OVERLAY))
                        .rotationX(dir == Direction.DOWN ? 90 : dir == Direction.UP ? 270 : 0)
                        .rotationY(dir.getAxis().isVertical() ? 0 : ((int) dir.toYRot() + 180) % 360)
                        .addModel()
                        .condition(VENT_DIRECTION, relative);
            }
        };
    }
    // spotless:on
}

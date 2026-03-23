package site.siredvin.gttruesteam;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.model.machine.overlays.WorkableOverlays;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.model.builder.MachineModelBuilder;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.fluids.FluidType;

import site.siredvin.gttruesteam.common.BoilerLevel;
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

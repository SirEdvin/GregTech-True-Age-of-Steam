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
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.model.builder.MachineModelBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.fluids.FluidType;

import com.mojang.datafixers.util.Pair;
import site.siredvin.gttruesteam.machines.boilers.ExpandedSteamLiquidBoilerMachine;
import site.siredvin.gttruesteam.machines.boilers.ExpandedSteamSolarBoilerMachine;
import site.siredvin.gttruesteam.machines.boilers.ExpandedSteamSolidBoilerMachine;

import java.util.function.BiFunction;

import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.RECIPE_LOGIC_STATUS;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.VENT_DIRECTION;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.VENT_OVERLAY;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.addWorkableOverlays;

public class TrueSteamMachines {

    public static final ResourceLocation LC_STEAM_HULL_MODEL = GTTrueSteam.id("block/lava_coated_boiler");
    public static final ResourceLocation IA_STEAM_HULL_MODEL = GTTrueSteam.id("block/infernal_alloy_boiler");

    public static final Pair<MachineDefinition, MachineDefinition> SOLID = registerSteamMachine("solid",
            ExpandedSteamSolidBoilerMachine::new, (isHigherPressure, builder) -> builder
                    .recipeType(GTRecipeTypes.STEAM_BOILER_RECIPES)
                    .rotationState(RotationState.ALL)
                    .recipeModifier(ExpandedSteamSolidBoilerMachine::recipeModifier)
                    .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                    .model(createWorkableSteamHullMachineModel(isHigherPressure,
                            GTCEu.id("block/generators/boiler/coal")))
                    .tooltips(Component.translatable("gtceu.universal.tooltip.produces_fluid",
                            (ConfigHolder.INSTANCE.machines.smallBoilers.hpSolidBoilerBaseOutput *
                                    (isHigherPressure ? 2.0 : 1.5) *
                                    FluidType.BUCKET_VOLUME / 20000)))
                    .register());

    public static final Pair<MachineDefinition, MachineDefinition> LIQUID = registerSteamMachine("liquid",
            ExpandedSteamLiquidBoilerMachine::new, (isHigherPressure, builder) -> builder
                    .recipeType(GTRecipeTypes.STEAM_BOILER_RECIPES)
                    .rotationState(RotationState.ALL)
                    .recipeModifier(ExpandedSteamLiquidBoilerMachine::recipeModifier)
                    .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                    .model(createWorkableSteamHullMachineModel(isHigherPressure,
                            GTCEu.id("block/generators/boiler/lava")))
                    .tooltips(Component.translatable("gtceu.universal.tooltip.produces_fluid",
                            (ConfigHolder.INSTANCE.machines.smallBoilers.hpLiquidBoilerBaseOutput *
                                    (isHigherPressure ? 2.0 : 1.5) *
                                    FluidType.BUCKET_VOLUME / 20000)))
                    .register());

    public static final Pair<MachineDefinition, MachineDefinition> SOLAR = registerSteamMachine("solar",
            ExpandedSteamSolarBoilerMachine::new, (isHigherPressure, builder) -> builder
                    .recipeType(GTRecipeTypes.STEAM_BOILER_RECIPES)
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeModifier(ExpandedSteamSolarBoilerMachine::recipeModifier)
                    .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                    .model(createWorkableSteamHullMachineModel(isHigherPressure,
                            GTCEu.id("block/generators/boiler/solar")))
                    .tooltips(Component.translatable("gtceu.universal.tooltip.produces_fluid",
                            (ConfigHolder.INSTANCE.machines.smallBoilers.hpSolarBoilerBaseOutput *
                                    (isHigherPressure ? 2.0 : 1.5) *
                                    FluidType.BUCKET_VOLUME / 20000)))
                    .register());

    public static void sayHi() {}

    public static ModelFile steamHullModel(BlockModelProvider models, boolean higherPressure) {
        return models.getExistingFile(higherPressure ? IA_STEAM_HULL_MODEL : LC_STEAM_HULL_MODEL);
    }

    private static Pair<MachineDefinition, MachineDefinition> registerSteamMachine(String name,
                                                                                   BiFunction<IMachineBlockEntity, Boolean, MetaMachine> factory,
                                                                                   BiFunction<Boolean, MachineBuilder<MachineDefinition>, MachineDefinition> builder) {
        MachineDefinition lowTier = builder.apply(false,
                GTTrueSteam.REGISTRATE.machine("lc_%s".formatted(name), holder -> factory.apply(holder, false))
                        .langValue("Lava Coated " + FormattingUtil.toEnglishName(name)));
        MachineDefinition highTier = builder.apply(true,
                GTTrueSteam.REGISTRATE.machine("ia_%s".formatted(name), holder -> factory.apply(holder, true))
                        .langValue("Infernal " + FormattingUtil.toEnglishName(name)));
        return Pair.of(lowTier, highTier);
    }

    private static void makeWorkableOverlayPart(BlockModelProvider models,
                                                MachineModelBuilder<BlockModelBuilder> builder, ModelFile parentModel,
                                                WorkableOverlays overlays, RecipeLogic.Status status) {
        BlockModelBuilder model = models.nested().parent(parentModel);
        addWorkableOverlays(overlays, status, model);
        builder.part(model).condition(RECIPE_LOGIC_STATUS, status);
    }

    public static MachineBuilder.ModelInitializer createWorkableSteamHullMachineModel(boolean higherPressure,
                                                                                      ResourceLocation overlayDir) {
        return (ctx, prov, builder) -> {
            WorkableOverlays overlays = WorkableOverlays.get(overlayDir, prov.getExistingFileHelper());
            ModelFile parent = steamHullModel(prov.models(), higherPressure);

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

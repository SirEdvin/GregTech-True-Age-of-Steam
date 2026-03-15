package site.siredvin.gttruesteam.machines.volcanic_boiler;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;

import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamBlocks;
import site.siredvin.gttruesteam.TrueSteamLang;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;

import static com.gregtechceu.gtceu.api.GTValues.EV;

/**
 * Volcanic Pressure Boiler — a large EV-tier multiblock for industrial-scale
 * superhot steam generation.
 *
 * Structure: 9 layers deep × 7 wide × 7 tall.
 * Parallel capacity scales with installed heating coil temperature (coilTemp / 900).
 * Uses Stainless Infernal Steel Casings and standard GT heating coils.
 */
public class VolcanicPressureBoiler {

    @SuppressWarnings({ "Convert2MethodRef", "FunctionalExpressionCanBeFolded" })
    public static MultiblockMachineDefinition MACHINE = GTTrueSteam.REGISTRATE
            .multiblock("volcanic_pressure_boiler", VolcanicPressureBoilerMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(TrueSteamRecipeTypes.METAPHYSICAL_BOILING)
            .appearanceBlock(() -> TrueSteamBlocks.StainlessInfernalSteelCasing.get())
            .recipeModifier(new VolcanicPressureBoilerRecipeModifier())
            .pattern(definition -> FactoryBlockPattern.start()
                    // Front face: controller at top-center, fluid imports on row 2, energy at bottom
                    .aisle("  CPC  ", "CCCCCCC", "IIIIIII", "CCCCCCC", "CCCCCCC", "CCCCCCC", "  EEE  ")
                    // Coil rings alternate with casing rings for 7 interior layers
                    .aisle("  SSS  ", "SS###SS", "S#####S", "S#####S", "S#####S", "SS###SS", "  SSS  ")
                    .aisle("  CCC  ", "CC###CC", "C#####C", "C#####C", "C#####C", "CC###CC", "  CCC  ")
                    .aisle("  SSS  ", "SS###SS", "S#####S", "S#####S", "S#####S", "SS###SS", "  SSS  ")
                    .aisle("  CCC  ", "CC###CC", "C#####C", "C#####C", "C#####C", "CC###CC", "  CCC  ")
                    .aisle("  SSS  ", "SS###SS", "S#####S", "S#####S", "S#####S", "SS###SS", "  SSS  ")
                    .aisle("  CCC  ", "CC###CC", "C#####C", "C#####C", "C#####C", "CC###CC", "  CCC  ")
                    .aisle("  SSS  ", "SS###SS", "S#####S", "S#####S", "S#####S", "SS###SS", "  SSS  ")
                    // Back face: maintenance at top-center, fluid exports on row 2, muffler at bottom-center
                    .aisle("  CHC  ", "CCCCCCC", "OOOOOOO", "CCCCCCC", "CCCCCCC", "CCCCCCC", "  CMC  ")
                    .where('P', Predicates.controller(Predicates.blocks(definition.get())))
                    .where('C', Predicates.blocks(TrueSteamBlocks.StainlessInfernalSteelCasing.get()))
                    .where('S', Predicates.heatingCoils())
                    .where('H', Predicates.abilities(PartAbility.MAINTENANCE))
                    .where('M', Predicates.abilities(PartAbility.MUFFLER))
                    .where('#', Predicates.blocks(Blocks.AIR))
                    .where(' ', Predicates.any())
                    .where('I',
                            Predicates.blocks(TrueSteamBlocks.StainlessInfernalSteelCasing.get())
                                    .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS)))
                    .where('O',
                            Predicates.blocks(TrueSteamBlocks.StainlessInfernalSteelCasing.get())
                                    .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS)))
                    .where('E',
                            Predicates.blocks(TrueSteamBlocks.StainlessInfernalSteelCasing.get())
                                    .or(Predicates.abilities(PartAbility.INPUT_ENERGY)))
                    .build())
            .shapeInfo(definition -> MultiblockShapeInfo.builder()
                    .aisle("  CPC  ", "CCCCCCC", "IIIIIII", "CCCCCCC", "CCCCCCC", "CCCCCCC", "  EEE  ")
                    .aisle("  SSS  ", "SS###SS", "S#####S", "S#####S", "S#####S", "SS###SS", "  SSS  ")
                    .aisle("  CCC  ", "CC###CC", "C#####C", "C#####C", "C#####C", "CC###CC", "  CCC  ")
                    .aisle("  SSS  ", "SS###SS", "S#####S", "S#####S", "S#####S", "SS###SS", "  SSS  ")
                    .aisle("  CCC  ", "CC###CC", "C#####C", "C#####C", "C#####C", "CC###CC", "  CCC  ")
                    .aisle("  SSS  ", "SS###SS", "S#####S", "S#####S", "S#####S", "SS###SS", "  SSS  ")
                    .aisle("  CCC  ", "CC###CC", "C#####C", "C#####C", "C#####C", "CC###CC", "  CCC  ")
                    .aisle("  SSS  ", "SS###SS", "S#####S", "S#####S", "S#####S", "SS###SS", "  SSS  ")
                    .aisle("  CHC  ", "CCCCCCC", "OOOOOOO", "CCCCCCC", "CCCCCCC", "CCCCCCC", "  CMC  ")
                    .where('P', definition, Direction.NORTH)
                    .where('C', TrueSteamBlocks.StainlessInfernalSteelCasing)
                    .where('S', GTBlocks.COIL_KANTHAL)
                    .where('H', GTMachines.MAINTENANCE_HATCH, Direction.SOUTH)
                    .where('M', GTMachines.MUFFLER_HATCH[EV], Direction.UP)
                    .where('#', Blocks.AIR)
                    .where(' ', Blocks.AIR)
                    .where('I', GTMachines.FLUID_IMPORT_HATCH[EV], Direction.NORTH)
                    .where('O', GTMachines.FLUID_EXPORT_HATCH[EV], Direction.SOUTH)
                    .where('E', GTMachines.ENERGY_INPUT_HATCH[EV], Direction.NORTH)
                    .build())
            .tooltips(
                    TrueSteamLang.VOLCANIC_BOILER_TOOLTIP_1,
                    TrueSteamLang.VOLCANIC_BOILER_TOOLTIP_2)
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .model(GTMachineModels.createWorkableCasingMachineModel(
                    GTCEu.id("block/casings/solid/machine_casing_stainless_steel"),
                    GTCEu.id("block/multiblock/generator/large_steel_boiler")))
            .register();

    public static void sayHi() {}
}

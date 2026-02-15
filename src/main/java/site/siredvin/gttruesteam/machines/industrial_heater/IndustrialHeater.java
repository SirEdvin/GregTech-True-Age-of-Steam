package site.siredvin.gttruesteam.machines.industrial_heater;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamBlocks;

public class IndustrialHeater {

    @SuppressWarnings({ "Convert2MethodRef", "FunctionalExpressionCanBeFolded" })
    public static MultiblockMachineDefinition INDUSTRIAL_HEATER = GTTrueSteam.REGISTRATE
            .multiblock("industrial_fluid_heater", IndustrialHeaterMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(GTRecipeTypes.FLUID_HEATER_RECIPES)
            .appearanceBlock(() -> GCYMBlocks.CASING_INDUSTRIAL_STEAM.get())
            .recipeModifier(new HeaterRecipeModifier())
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle(" CHC ", "COOOC", "COOOC", "COOOC", " CMC ")
                    .aisle(" SSS ", "S###S", "S###S", "S###S", " SSS ")
                    .aisle(" CCC ", "C###C", "C###C", "C###C", " CCC ")
                    .aisle(" SSS ", "S###S", "S###S", "S###S", " SSS ")
                    .aisle(" CCC ", "C###C", "C###C", "C###C", " CCC ")
                    .aisle(" SSS ", "S###S", "S###S", "S###S", " SSS ")
                    .aisle(" CPC ", "CIIIC", "CIIIC", "CIIIC", " EEE ")
                    .where('P', Predicates.controller(Predicates.blocks(definition.get())))
                    .where('C', Predicates.blocks(TrueSteamBlocks.IndustrialBronzeCasing.get()))
                    .where('S', Predicates.heatingCoils())
                    .where('M', Predicates.abilities(PartAbility.MUFFLER))
                    .where('H', Predicates.abilities(PartAbility.MAINTENANCE))
                    .where('#', Predicates.blocks(Blocks.AIR))
                    .where(' ', Predicates.any())
                    .where('E',
                            Predicates.blocks(TrueSteamBlocks.IndustrialBronzeCasing.get())
                                    .or(Predicates.abilities(PartAbility.INPUT_ENERGY)))
                    .where('I',
                            Predicates.blocks(TrueSteamBlocks.IndustrialBronzeCasing.get())
                                    .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS)))
                    .where('O',
                            Predicates.blocks(GCYMBlocks.CASING_INDUSTRIAL_STEAM.get())
                                    .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS)))
                    .build())
            .shapeInfo(definition -> MultiblockShapeInfo.builder()
                    .aisle(" CHC ", "COOOC", "COOOC", "COOOC", " CMC ")
                    .aisle(" SSS ", "S###S", "S###S", "S###S", " SSS ")
                    .aisle(" CCC ", "C###C", "C###C", "C###C", " CCC ")
                    .aisle(" SSS ", "S###S", "S###S", "S###S", " SSS ")
                    .aisle(" CCC ", "C###C", "C###C", "C###C", " CCC ")
                    .aisle(" SSS ", "S###S", "S###S", "S###S", " SSS ")
                    .aisle(" CPC ", "CIIIC", "CIIIC", "CIIIC", " EEE ")
                    .where('P', definition, Direction.SOUTH)
                    .where('C', GCYMBlocks.CASING_INDUSTRIAL_STEAM)
                    .where('S', GTBlocks.COIL_CUPRONICKEL)
                    .where('M', GTMachines.MUFFLER_HATCH[1], Direction.UP)
                    .where('H', GTMachines.MAINTENANCE_HATCH, Direction.NORTH)
                    .where('#', Blocks.AIR)
                    .where(' ', Blocks.AIR)
                    .where('E', GTMachines.ENERGY_INPUT_HATCH[2], Direction.SOUTH)
                    .where('I', GTMachines.FLUID_IMPORT_HATCH[2], Direction.SOUTH)
                    .where('O', GTMachines.FLUID_EXPORT_HATCH[2], Direction.NORTH)
                    .build())
            .additionalDisplay(((iMultiController, components) -> {
                if (iMultiController.isFormed()) {
                    if (iMultiController instanceof IndustrialHeaterMachine industrialHeaterMachine) {
                        var logic = industrialHeaterMachine.getHeaterRecipeLogic();
                        components.add(Component.literal("Cycles until throttle: " + logic.getPureChargers()));
                        components.add(
                                Component.literal("Current heat level: ").append(logic.getHeatLevel().component()));
                    }
                }
            }))
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .model(GTMachineModels.createWorkableCasingMachineModel(
                    GTCEu.id("block/casings/gcym/industrial_steam_casing"),
                    GTCEu.id("block/multiblock/generator/large_bronze_boiler")))
            .register();

    public static void sayHi() {}
}

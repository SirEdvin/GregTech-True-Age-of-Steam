package site.siredvin.gttruesteam.machines.industrial_fluid_pressurizer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.*;

import net.minecraft.world.level.block.Blocks;

import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;

public class IndustrialFluidPressurizer {

    public static MultiblockMachineDefinition MACHINE = GTTrueSteam.REGISTRATE
            .multiblock("industrial_fluid_pressurizer", CoilWorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TrueSteamRecipeTypes.INDUSTRIAL_FLUID_PRESSURIZER)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.OC_NON_PERFECT)
            .appearanceBlock(GTBlocks.CASING_STAINLESS_CLEAN)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle(" F F ", " F F ", " CCC ", " TTT ", " CCC ", " CCC ", " CCC ", " TTT ", " CCC ")
                    .aisle("F   F", "F   F", "CCCCC", "TPPPT", "CPPPC", "C   C", "CPPPC", "TPPPT", "CIIIC")
                    .aisle("     ", "     ", "CCCCC", "TPPPT", "CP PC", "C   C", "CP PC", "TPPPT", "CIIIC")
                    .aisle("F   F", "F   F", "CCCCC", "TPPPT", "CPPPC", "C   C", "CPPPC", "TPPPT", "CIIIC")
                    .aisle(" F F ", " F F ", " CCC ", " TTT ", " CCC ", " CCC ", " CCC ", " TTT ", " CCC ")
                    .aisle("     ", "     ", "     ", "     ", "     ", "  K  ", "     ", "     ", "     ")
                    .aisle("     ", "     ", "     ", "     ", "     ", "  K  ", "     ", "     ", "     ")
                    .aisle(" SSS ", " SSS ", " SSS ", " SSS ", "     ", "  K  ", "     ", "     ", "     ")
                    .aisle(" SSS ", " S S ", " S S ", " S S ", "  K  ", "  K  ", "     ", "     ", "     ")
                    .aisle(" SSS ", " SKS ", " SSS ", " SSS ", "     ", "     ", "     ", "     ", "     ")
                    .where("K", Predicates.controller(Predicates.blocks(definition.get())))
                    .where("F", Predicates.frames(GTMaterials.StainlessSteel))
                    .where("T", Predicates.blocks(GTBlocks.CASING_STAINLESS_TURBINE.get()))
                    .where("K", Predicates.blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
                    .where("P", Predicates.blocks(Blocks.PISTON))
                    .where(
                            "I",
                            Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(3)
                                    .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(0))
                                    .or(Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get()))
                    )
                    .where("S", Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())
                            .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2)
                                    .setPreviewCount(2))
                            .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                            .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1)))
                    .where("C", Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get()))
                    .where(" ", Predicates.any())
                    .build())
            .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                    GTCEu.id("block/multiblock/implosion_compressor"))
            .register();

    public static void sayHi() {}
}

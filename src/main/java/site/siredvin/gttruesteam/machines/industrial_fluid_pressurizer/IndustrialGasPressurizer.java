package site.siredvin.gttruesteam.machines.industrial_fluid_pressurizer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.*;

import net.minecraft.world.level.block.Blocks;

import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;

public class IndustrialGasPressurizer {

    public static MultiblockMachineDefinition MACHINE = GTTrueSteam.REGISTRATE
            .multiblock("industrial_gas_pressurizer", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TrueSteamRecipeTypes.INDUSTRIAL_GAS_PRESSURIZER)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.OC_NON_PERFECT)
            .appearanceBlock(GTBlocks.CASING_STAINLESS_CLEAN)
            .pattern(definition -> FactoryBlockPattern
                    .start(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT)
                    .aisle("      c c ", "      c c ", "      ddd ", "      eee ", "      ddd ", "      ddd ",
                            "      ddd ", "      eee ", "      ddd ")
                    .aisle("ddd  c   c", "did  c   c", "did  ddddd", "ddd  efffe", "     dfffd", "     d   d",
                            "     dfffd", "     efffe", "     dxxxd")
                    .aisle("gdd       ", "idi       ", "idi  ddddd", "dhd  efffe", " h   dfffd", " hhhhd   d",
                            "     dfffd", "     efffe", "     dxxxd")
                    .aisle("ddd  c   c", "did  c   c", "did  ddddd", "ddd  efffe", "     dfffd", "     d   d",
                            "     dfffd", "     efffe", "     dxxxd")
                    .aisle("      c c ", "      c c ", "      ddd ", "      eee ", "      ddd ", "      ddd ",
                            "      ddd ", "      eee ", "      ddd ")
                    .where(" ", Predicates.any())
                    .where("c",
                            Predicates.blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.StainlessSteel)))
                    .where("d", Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get()))
                    .where("e", Predicates.blocks(GTBlocks.CASING_STAINLESS_TURBINE.get()))
                    .where("f", Predicates.blocks(Blocks.PISTON))
                    .where("g", Predicates.controller(Predicates.blocks(definition.get())))
                    .where("h", Predicates.blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
                    .where("x",
                            Predicates.ability(PartAbility.IMPORT_FLUIDS)
                                    .or(Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())))
                    .where("i",
                            Predicates.ability(PartAbility.EXPORT_FLUIDS)
                                    .or(Predicates.ability(PartAbility.IMPORT_ITEMS))
                                    .or(Predicates.ability(PartAbility.EXPORT_FLUIDS))
                                    .or(Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())))
                    .build())
            // .shapeInfo(def -> MultiblockShapeInfo.builder()
            // .aisle(" c c ", " c c ", " ddd ", " eee ", " ddd ", " ddd ", " ddd ", " eee ", " ddd ")
            // .aisle("ddd c c", "did c c", "did ddddd", "ddd efffe", " dfffd", " d d", " dfffd", " efffe", " dxxxd")
            // .aisle("gdd ", "idi ", "idi ddddd", "dhd efffe", " h dfffd", " hhhhd d", " dfffd", " efffe", " dxxxd")
            // .aisle("ddd c c", "did c c", "did ddddd", "ddd efffe", " dfffd", " d d", " dfffd", " efffe", " dxxxd")
            // .aisle(" c c ", " c c ", " ddd ", " eee ", " ddd ", " ddd ", " ddd ", " eee ", " ddd ")
            // .where(' ', Blocks.AIR)
            // .where("c", Predicates.blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.StainlessSteel)))
            // .where("d", Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get()))
            // .where("e", Predicates.blocks(GTBlocks.CASING_STAINLESS_TURBINE.get()))
            // .where("f", Predicates.blocks(Blocks.PISTON))
            // .where("g", Predicates.controller(Predicates.blocks(definition.get())))
            // .where("h", Predicates.blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
            // .where("x",
            // Predicates.ability(PartAbility.IMPORT_FLUIDS).or(Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())))
            // .where("i",
            // Predicates.ability(PartAbility.EXPORT_FLUIDS).or(Predicates.ability(PartAbility.IMPORT_ITEMS)).or(Predicates.ability(PartAbility.EXPORT_FLUIDS)).or(Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())))
            // .build())
            .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                    GTCEu.id("block/multiblock/implosion_compressor"))
            .register();

    public static void sayHi() {}
}

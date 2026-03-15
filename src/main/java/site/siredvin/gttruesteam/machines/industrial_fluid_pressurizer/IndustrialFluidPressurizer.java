package site.siredvin.gttruesteam.machines.industrial_fluid_pressurizer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.*;

import net.minecraft.world.level.block.Blocks;

import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;

public class IndustrialFluidPressurizer {

    public static MultiblockMachineDefinition MACHINE = GTTrueSteam.REGISTRATE
            .multiblock("industrial_fluid_pressurizer", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TrueSteamRecipeTypes.INDUSTRIAL_FLUID_PRESSURIZER)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.OC_NON_PERFECT)
            .appearanceBlock(GTBlocks.CASING_STAINLESS_CLEAN)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("      c c ", "      c c ", "      ddd ", "      eee ", "      ddd ", "      ddd ", "      ddd ", "      eee ", "      ddd ")
                    .aisle("ddd  c   c", "ddd  c   c", "ddd  ddddd", "ddd  efffe", "     dfffd", "     d   d", "     dfffd", "     efffe", "     ddddd")
                    .aisle("gdd       ", "ddd       ", "ddd  ddddd", "dhd  efffe", " h   dfffd", " hhhhd   d", "     dfffd", "     efffe", "     ddddd")
                    .aisle("ddd  c   c", "ddd  c   c", "ddd  ddddd", "ddd  efffe", "     dfffd", "     d   d", "     dfffd", "     efffe", "     ddddd")
                    .aisle("      c c ", "      c c ", "      ddd ", "      eee ", "      ddd ", "      ddd ", "      ddd ", "      eee ", "      ddd ")
                    .where(" ", Predicates.any())
                    .where("c", Predicates.blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.StainlessSteel)))
                    .where("d", Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get()))
                    .where("e", Predicates.blocks(GTBlocks.CASING_STAINLESS_TURBINE.get()))
                    .where("f", Predicates.blocks(Blocks.PISTON))
                    .where("g", Predicates.controller(Predicates.blocks(definition.get())))
                    .where("h", Predicates.blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
                    .build())
            .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                    GTCEu.id("block/multiblock/implosion_compressor"))
            .register();

    public static void sayHi() {

    }
}

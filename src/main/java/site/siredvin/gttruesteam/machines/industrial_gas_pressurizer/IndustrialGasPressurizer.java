package site.siredvin.gttruesteam.machines.industrial_gas_pressurizer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.*;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamLang;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;

public class IndustrialGasPressurizer {

    public static MultiblockMachineDefinition MACHINE = GTTrueSteam.REGISTRATE
            .multiblock("industrial_gas_pressurizer", IndustrialGasPressurizerMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TrueSteamRecipeTypes.INDUSTRIAL_GAS_PRESSURIZER)
            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT, new IndustrialGasPressurizerRecipeModifier())
            .appearanceBlock(GTBlocks.CASING_STAINLESS_CLEAN)
            .pattern(definition -> FactoryBlockPattern
                    .start(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT)
                    .aisle(" c c ", " c c ", " ddd ", " eee ", " ddd ", " ddd ", " ddd ", " eee ", " ddd ")
                    .aisle("c   c", "c   c", "ddddd", "efffe", "dfffd", "d   d", "dfffd", "efffe", "dxxxd")
                    .aisle("     ", "     ", "ddddd", "efffe", "dfffd", "d   d", "dfffd", "efffe", "dxxxd")
                    .aisle("c   c", "c   c", "ddddd", "efffe", "dfffd", "d   d", "dfffd", "efffe", "dxxxd")
                    .aisle(" c c ", " c c ", " ddd ", " eee ", " ddd ", " ddd ", " ddd ", " eee ", " ddd ")
                    .aisle("     ", "     ", "     ", "     ", "     ", "  h  ", "     ", "     ", "     ")
                    .aisle("     ", "     ", "     ", "     ", "     ", "  h  ", "     ", "     ", "     ")
                    .aisle(" ddd ", " did ", " did ", " ddd ", "     ", "  h  ", "     ", "     ", "     ")
                    .aisle(" ddd ", " idi ", " idi ", " dhd ", "  h  ", "  h  ", "     ", "     ", "     ")
                    .aisle(" dgd ", " did ", " did ", " dpd ", "     ", "     ", "     ", "     ", "     ")
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
                                    .or(Predicates.ability(PartAbility.EXPORT_ITEMS))
                                    .or(Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())))
                    .where("p", Predicates.ability(PartAbility.INPUT_ENERGY))
                    .build())
            .additionalDisplay((controller, tooltips) -> {
                if (controller instanceof IndustrialGasPressurizerMachine pressurizerMachine) {
                    tooltips.add(TrueSteamLang.PERFECT_CONDITION);
                    tooltips.add(Component.literal("    " + pressurizerMachine.getState().readableName()));
                }
            })
            .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                    GTCEu.id("block/multiblock/implosion_compressor"))
            .register();

    public static void sayHi() {}
}

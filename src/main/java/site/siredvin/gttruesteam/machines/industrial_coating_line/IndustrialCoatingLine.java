package site.siredvin.gttruesteam.machines.industrial_coating_line;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;

import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamBlocks;
import site.siredvin.gttruesteam.TrueSteamLang;
import site.siredvin.gttruesteam.TrueSteamPredicates;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;

public class IndustrialCoatingLine {

    public static MultiblockMachineDefinition MACHINE = GTTrueSteam.REGISTRATE
            .multiblock("industrial_coating_line", IndustrialCoatingLineMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TrueSteamRecipeTypes.INDUSTRIAL_COATING_LINE)
            .recipeModifier(new IndustrialCoatingLineRecipeModifier())
            .appearanceBlock(() -> TrueSteamBlocks.BathingInfusedCasing.get())
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("ECE", "CCC", "COC")
                    .aisle("CPC", "GFG", "CHC").setRepeatable(1, 12)
                    .aisle("CKC", "CCC", "CIC")
                    .where("C", Predicates.blocks(TrueSteamBlocks.BathingInfusedCasing.get()))
                    .where("G", Predicates.blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                    .where("P", Predicates.blocks(TrueSteamBlocks.WhirlpoolCasing.get()))
                    .where("K", Predicates.controller(Predicates.blocks(definition.get())))
                    .where("I", Predicates.abilities(PartAbility.IMPORT_ITEMS))
                    .where("O", Predicates.abilities(PartAbility.EXPORT_ITEMS))
                    .where("E", Predicates.ability(PartAbility.INPUT_ENERGY)
                            .or(Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())))
                    .where("H", Predicates.ability(PartAbility.IMPORT_FLUIDS)
                            .or(Predicates.blocks(TrueSteamBlocks.BathingInfusedCasing.get())))
                    .where("F", TrueSteamPredicates.industrialCoatingFluidCells())
                    .build())
            .tooltips(
                    TrueSteamLang.INDUSTRIAL_COATING_LINE_TOOLTIP_1,
                    TrueSteamLang.INDUSTRIAL_COATING_LINE_TOOLTIP_2)
            .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                    GTCEu.id("block/multiblock/implosion_compressor"))
            .register();

    public static void sayHi() {}
}

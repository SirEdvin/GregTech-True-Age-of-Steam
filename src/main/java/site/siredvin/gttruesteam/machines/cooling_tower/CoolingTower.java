package site.siredvin.gttruesteam.machines.cooling_tower;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import site.siredvin.gttruesteam.*;
import site.siredvin.gttruesteam.common.Constants;
import site.siredvin.gttruesteam.machines.shared.cooling.PassiveCoolingMachine;

public class CoolingTower {

    public static MultiblockMachineDefinition MACHINE = GTTrueSteam.REGISTRATE
            .multiblock("cooling_tower",
                    (holder) -> new PassiveCoolingMachine(holder, Constants.CT_CAPACITY_BOOST, Constants.CT_RATE_BOOST))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TrueSteamRecipeTypes.FLUID_COOLING)
            .appearanceBlock(() -> GTBlocks.LIGHT_CONCRETE.get())
            .recipeModifier(new CoolingTowerRecipeModifier())
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("   XXXXX   ", "   CCMCC   ", "           ", "           ", "           ", "           ",
                            "           ", "           ", "           ", "           ", "           ", "           ",
                            "           ")
                    .aisle("  X     X  ", "  C  P  C  ", "   CCCCC   ", "    CCC    ", "    CCC    ", "           ",
                            "           ", "           ", "           ", "           ", "           ", "           ",
                            "           ")
                    .aisle(" X       X ", " C   P   C ", "  CSSSSSC  ", "   C   C   ", "   C   C   ", "    CCC    ",
                            "    CCC    ", "    CCC    ", "           ", "           ", "           ", "    CCC    ",
                            "    CCC    ")
                    .aisle("X         X", "C    P    C", " CSSSSSSSC ", "  C     C  ", "  C     C  ", "   C   C   ",
                            "   C   C   ", "   C   C   ", "    CCC    ", "    CCC    ", "    CCC    ", "   C   C   ",
                            "   C   C   ")
                    .aisle("X         X", "I    P    O", " CSSSSSSSC ", " C       C ", " C       C ", "  C     C  ",
                            "  C     C  ", "  C     C  ", "   C   C   ", "   C   C   ", "   CSSSC   ", "  C     C  ",
                            "  C     C  ")
                    .aisle("X         X", "IPPPPPPPPPO", " CSSSPSSSC ", " C   P   C ", " C   P   C ", "  C  P  C  ",
                            "  C  P  C  ", "  C  P  C  ", "   C P C   ", "   C P C   ", "   CSBSC   ", "  C     C  ",
                            "  C     C  ")
                    .aisle("X         X", "I    P    O", " CSSSSSSSC ", " C       C ", " C       C ", "  C     C  ",
                            "  C     C  ", "  C     C  ", "   C   C   ", "   C   C   ", "   CSSSC   ", "  C     C  ",
                            "  C     C  ")
                    .aisle("X         X", "C    P    C", " CSSSSSSSC ", "  C     C  ", "  C     C  ", "   C   C   ",
                            "   C   C   ", "   C   C   ", "    CCC    ", "    CCC    ", "    CCC    ", "   C   C   ",
                            "   C   C   ")
                    .aisle(" X       X ", " C   P   C ", "  CSSSSSC  ", "   C   C   ", "   C   C   ", "    CCC    ",
                            "    CCC    ", "    CCC    ", "           ", "           ", "           ", "    CCC    ",
                            "    CCC    ")
                    .aisle("  X     X  ", "  C  P  C  ", "   CCCCC   ", "    CCC    ", "    CCC    ", "           ",
                            "           ", "           ", "           ", "           ", "           ", "           ",
                            "           ")
                    .aisle("   XXXXX   ", "   CCKCC   ", "           ", "           ", "           ", "           ",
                            "           ", "           ", "           ", "           ", "           ", "           ",
                            "           ")
                    .where("C", Predicates.blocks(GTBlocks.LIGHT_CONCRETE.get()))
                    .where("X",
                            Predicates.blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.TitaniumCarbide)))
                    .where("P", Predicates.blocks(TrueSteamBlocks.CoolingInfusedPipeCasing.get()))
                    .where("S", TrueSteamPredicates.coolingCoils())
                    .where("B", Predicates.ability(PartAbility.MUFFLER))
                    .where("K", Predicates.controller(Predicates.blocks(definition.get())))
                    .where("M", Predicates.ability(PartAbility.MAINTENANCE))
                    .where("O",
                            Predicates.ability(PartAbility.EXPORT_FLUIDS)
                                    .or(Predicates.blocks(GTBlocks.LIGHT_CONCRETE.get())))
                    .where("I",
                            Predicates.ability(PartAbility.IMPORT_FLUIDS)
                                    .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(Predicates.blocks(GTBlocks.LIGHT_CONCRETE.get())))
                    .build())
            .workableCasingModel(
                    GTCEu.id("block/stones/light_concrete/stone"),
                    GTCEu.id("block/multiblock/vacuum_freezer"))
            .register();

    public static void sayHi() {}
}

package site.siredvin.gttruesteam.machines.cim;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.network.chat.Component;

import site.siredvin.gttruesteam.*;

public class ConceptInfusionMatrix {

    public static MultiblockMachineDefinition MACHINE = GTTrueSteam.REGISTRATE
            .multiblock("concept_infusion_matrix", ConceptInfusionMatrixMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TrueSteamRecipeTypes.CONCEPT_INFUSION)
            .appearanceBlock(() -> TrueSteamBlocks.ConceptualizedSteelSolidCasing.get())
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("CCXCXCC", "CGGGGGC", "CGGGGGC", "CCCHCCC", "CGGGGGC", "CGGGGGC", "CCCCCCC")
                    .aisle("CCCCCCC", "G     G", "G     G", "C     C", "G     G", "G     G", "CGGCGGC")
                    .aisle("CCCCCCC", "G     G", "G APA G", "C P P C", "G APA G", "G     G", "CGGCGGC")
                    .aisle("XCCHCCX", "G     G", "G P P G", "H     H", "G P P G", "G     G", "CCCHCCC")
                    .aisle("CCCCCCC", "G     G", "G APA G", "C P P C", "G APA G", "G     G", "CGGCGGC")
                    .aisle("CCCCCCC", "G     G", "G     G", "C     C", "G     G", "G     G", "CGGCGGC")
                    .aisle("CCCKCCC", "CGGGGGC", "CGGGGGC", "CCCHCCC", "CGGGGGC", "CGGGGGC", "CCCCCCC")
                    .where("C", Predicates.blocks(TrueSteamBlocks.ConceptualizedSteelSolidCasing.get()))
                    .where("X", Predicates.abilities(PartAbility.IMPORT_ITEMS, PartAbility.EXPORT_ITEMS).or(
                            Predicates.ability(PartAbility.INPUT_ENERGY, GTValues.ULV).setMaxGlobalLimited(2).or(
                                    Predicates.blocks(TrueSteamBlocks.ConceptualizedSteelSolidCasing.get()))))
                    .where("G", Predicates.blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                    .where("H", Predicates.abilities(PartAbility.PASSTHROUGH_HATCH))
                    .where(" ", Predicates.any())
                    .where("A", TrueSteamPredicates.singleBlockMachines())
                    .where("P", Predicates.blocks(TrueSteamBlocks.ConceptualizedSteelPipeCasing.get()))
                    .where("K", Predicates.controller(Predicates.blocks(definition.get())))
                    /*
                     * I like this structure, but I have no use for it now, so I will just leave it here
                     * .aisle("CCCCC#####CCCCC", "C###C#####C###C", "C###C#####C###C", "C###C#####C###C",
                     * "CCCCC#####CCCCC", "###############", "###############", "###############", "###############",
                     * "###############", "CCCCC#####CCCCC", "C###C#####C###C", "C###C#####C###C", "C###C#####C###C",
                     * "CCCCC#####CCCCC")
                     * .aisle("C###C#####C###C", "###############", "###############", "###############",
                     * "C###C#####C###C", "###############", "###############", "###############", "###############",
                     * "###############", "C###C#####C###C", "###############", "###############", "###############",
                     * "C###C#####C###C")
                     * .aisle("C###C#####C###C", "###############", "##MPPPPPPPPPM##", "##P#########P##",
                     * "C#P#C#####C#P#C", "##P#########P##", "##P#########P##", "##P#########P##", "##P#########P##",
                     * "##P#########P##", "C#P#C#####C#P#C", "##P#########P##", "##MPPPPPPPPPM##", "###############",
                     * "C###C#####C###C")
                     * .aisle("C###C#####C###C", "###############", "##P#########P##", "###############",
                     * "C###C#####C###C", "###############", "###############", "###############", "###############",
                     * "###############", "C###C#####C###C", "###############", "##P#########P##", "###############",
                     * "C###C#####C###C")
                     * .aisle("CCCCC#####CCCCC", "C###C#####C###C", "C#P#C#####C#P#C", "C###C#####C###C",
                     * "CCCCC#####CCCCC", "###############", "###############", "###############", "###############",
                     * "###############", "CCCCC#####CCCCC", "C###C#####C###C", "C#P#C#####C#P#C", "C###C#####C###C",
                     * "CCCCC#####CCCCC")
                     * .aisle("###############", "###############", "##P#########P##", "###############",
                     * "###############", "#####CCCCC#####", "#####C###C#####", "#####C###C#####", "#####C###C#####",
                     * "#####CCCCC#####", "###############", "###############", "##P#########P##", "###############",
                     * "###############")
                     * .aisle("###############", "###############", "##P#########P##", "###############",
                     * "###############", "#####C###C#####", "###############", "###############", "###############",
                     * "#####C###C#####", "###############", "###############", "##P#########P##", "###############",
                     * "###############")
                     * .aisle("###############", "###############", "##P#########P##", "###############",
                     * "###############", "#####C#I#C#####", "#######P#######", "#######X#######", "#######P#######",
                     * "#####C#O#C#####", "###############", "###############", "##P#########P##", "###############",
                     * "###############")
                     * .aisle("###############", "###############", "##P#########P##", "###############",
                     * "###############", "#####C###C#####", "###############", "###############", "###############",
                     * "#####C###C#####", "###############", "###############", "##P#########P##", "###############",
                     * "###############")
                     * .aisle("###############", "###############", "##P#########P##", "###############",
                     * "###############", "#####CCCCC#####", "#####C###C#####", "#####C###C#####", "#####C###C#####",
                     * "#####CCCCC#####", "###############", "###############", "##P#########P##", "###############",
                     * "###############")
                     * .aisle("CCCCC#####CCCCC", "C###C#####C###C", "C#P#C#####C#P#C", "C###C#####C###C",
                     * "CCCCC#####CCCCC", "###############", "###############", "###############", "###############",
                     * "###############", "CCCCC#####CCCCC", "C###C#####C###C", "C#P#C#####C#P#C", "C###C#####C###C",
                     * "CCCCC#####CCCCC")
                     * .aisle("C###C#####C###C", "###############", "##P#########P##", "###############",
                     * "C###C#####C###C", "###############", "###############", "###############", "###############",
                     * "###############", "C###C#####C###C", "###############", "##P#########P##", "###############",
                     * "C###C#####C###C")
                     * .aisle("C###C#####C###C", "###############", "##MPPPPPPPPPM##", "##P#########P##",
                     * "C#P#C#####C#P#C", "##P#########P##", "##P#########P##", "##P#########P##", "##P#########P##",
                     * "##P#########P##", "C#P#C#####C#P#C", "##P#########P##", "##MPPPPPPPPPM##", "###############",
                     * "C###C#####C###C")
                     * .aisle("C###C#####C###C", "###############", "###############", "###############",
                     * "C###C#####C###C", "###############", "###############", "###############", "###############",
                     * "###############", "C###C#####C###C", "###############", "###############", "###############",
                     * "C###C#####C###C")
                     * .aisle("CCCCC#####CCCCC", "C###C#####C###C", "C###C#####C###C", "C###C#####C###C",
                     * "CCCCC#####CCCCC", "###############", "###############", "###############", "###############",
                     * "###############", "CCCCC#####CCCCC", "C###C#####C###C", "C###C#####C###C", "C###C#####C###C",
                     * "CCCCC#####CCCCC")
                     * .where("P", Predicates.blocks(TrueSteamBlocks.ConceptualizedSteelPipeCasing.get()))
                     * .where("C", Predicates.blocks(TrueSteamBlocks.ConceptualizedSteelSolidCasing.get()))
                     * .where("M", TrueSteamPredicates.singleBlockMachines())
                     * .where("X", Predicates.controller(Predicates.blocks(definition.get())))
                     * .where("I", Predicates.ability(PartAbility.IMPORT_ITEMS))
                     * .where("O", Predicates.ability(PartAbility.EXPORT_ITEMS))
                     * .where("#", Predicates.any())
                     */
                    .build())
            // .tooltips(
            // TrueSteamLang.COOLING_BOX_TOOLTIP_1,
            // TrueSteamLang.COOLING_BOX_TOOLTIP_2)
            .additionalDisplay((machine, list) -> {
                if (machine instanceof ConceptInfusionMatrixMachine matrixMachine) {
                    list.add(Component.literal(
                            "Recipe type: " + Component.translatable(matrixMachine.machineRecipe()).getString()));
                    list.add(Component.literal("Is machine working: " + matrixMachine.isMachinesRunning()));
                }
            })
            .tooltips(TrueSteamLang.CIM_TOOLTIP_1, TrueSteamLang.CIM_TOOLTIP_2, TrueSteamLang.CIM_TOOLTIP_3)
            .workableCasingModel(
                    GTTrueSteam.id("block/conceptualized_steel_solid_casing"),
                    GTCEu.id("block/multiblock/vacuum_freezer"))
            .register();

    public static void sayHi() {}
}

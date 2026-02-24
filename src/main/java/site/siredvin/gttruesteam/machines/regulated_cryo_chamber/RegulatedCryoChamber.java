package site.siredvin.gttruesteam.machines.regulated_cryo_chamber;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.network.chat.Component;

import site.siredvin.gttruesteam.*;

public class RegulatedCryoChamber {

    // Coil-Assisted Freezer
    public static MultiblockMachineDefinition MACHINE = GTTrueSteam.REGISTRATE
            .multiblock("regulated_cryo_chamber", RegulatedCryoChamberMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.VACUUM_RECIPES)
            .appearanceBlock(TrueSteamBlocks.FrostOverproofedCasing)
            .recipeModifier(new RegulatedCryoChamberRecipeModifier())
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "CCC", "CCC", "XXX")
                    .aisle("XXX", "C#C", "C#C", "XXX")
                    .aisle("XSX", "CCC", "CCC", "XXX")
                    .where('S', Predicates.controller(Predicates.blocks(definition.getBlock())))
                    .where('X', Predicates.blocks(TrueSteamBlocks.FrostOverproofedCasing.get()).setMinGlobalLimited(9)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, false)))
                    .where('C', TrueSteamPredicates.coolingCoils())
                    .where('#', Predicates.air())
                    .build())
            .tooltips(
                    TrueSteamLang.RCC_TOOLTIP_1,
                    TrueSteamLang.RCC_TOOLTIP_2)
            .workableCasingModel(
                    GTTrueSteam.id("block/frost_overproofed_casing"),
                    GTCEu.id("block/multiblock/vacuum_freezer"))
            .additionalDisplay((machine, components) -> {
                var myMachine = (RegulatedCryoChamberMachine) machine;
                var recipeLogic = myMachine.getRecipeLogic();
                var coil = myMachine.getCoilType();
                var reduction = String.format("%.3f", recipeLogic.getCooldownReduction(coil));
                components.add(Component.translatable(TrueSteamLang.RCC_REDUCTION_KEY, reduction));
            })
            .register();

    public static void sayHi() {}
}

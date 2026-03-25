package site.siredvin.gttruesteam.machines.spawner_extraction;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;

import net.minecraft.world.level.block.Blocks;

import site.siredvin.gttruesteam.*;

public class SpawnerExtractionMachine {

    // 5 wide (X) x 7 tall (Y) x 5 deep (Z) hollow box, all sides ExtractionInfusedCasing
    public static MultiblockMachineDefinition MACHINE = GTTrueSteam.REGISTRATE
            .multiblock("spawner_extraction_machine", SpawnerExtractionMachineMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TrueSteamRecipeTypes.SPAWNER_EXTRACTION)
            .appearanceBlock(() -> TrueSteamBlocks.ExtractionInfusedCasing.get())
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("EEMEE", "EEEEE", "EEEEE", "EEEEE", "EEEEE", "EEEEE", "EEKEE")
                    .aisle("EEEEE", "E   E", "E   E", "E   E", "E   E", "E   E", "EEEEE")
                    .aisle("EEEEE", "E   E", "E   E", "E   E", "E   E", "E   E", "EEEEE")
                    .aisle("EEEEE", "E   E", "E   E", "E   E", "E   E", "E   E", "EEEEE")
                    .aisle("EEEEE", "EEEEE", "EEEEE", "EEEEE", "EEEEE", "EEEEE", "EEEEE")
                    .where("E", Predicates.blocks(TrueSteamBlocks.ExtractionInfusedCasing.get()))
                    .where(" ", Predicates.blocks(Blocks.AIR))
                    .where("K", Predicates.controller(Predicates.blocks(definition.get())))
                    .where("M", Predicates.ability(PartAbility.MAINTENANCE))
                    .build())
            .tooltips(TrueSteamLang.SEM_TOOLTIP_1)
            .workableCasingModel(
                    GTTrueSteam.id("block/extraction_infused_casing"),
                    GTCEu.id("block/multiblock/electric_blast_furnace"))
            .register();

    public static void sayHi() {}
}

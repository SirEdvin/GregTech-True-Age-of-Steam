package site.siredvin.gttruesteam.machines.spawner_extraction;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;

import com.gregtechceu.gtceu.common.data.GTBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

import site.siredvin.gttruesteam.*;

public class SpawnerExtractionMachine {

    public static MultiblockMachineDefinition MACHINE = GTTrueSteam.REGISTRATE
            .multiblock("spawner_extraction_machine", SpawnerExtractionMachineMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TrueSteamRecipeTypes.SPAWNER_EXTRACTION)
            .appearanceBlock(() -> TrueSteamBlocks.ExtractionInfusedCasing.get())
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("EXMXE", "EGGGE", "EGGGE", "EGGGE", "EGGGE", "EGGGE", "EXXXE")
                    .aisle("XEEEX", "EP PE", "EP PE", "EP PE", "EP PE", "EP PE", "XEEEX")
                    .aisle("XEEEX", "EL LE", "EL LE", "EPSPE", "EL LE", "EL LE", "XEEEX")
                    .aisle("XEEEX", "EP PE", "EP PE", "EP PE", "EP PE", "EP PE", "XEEEX")
                    .aisle("EXKXE", "EGGGE", "EGGGE", "EGGGE", "EGGGE", "EGGGE", "EXXXE")
                    .where("E", Predicates.blocks(TrueSteamBlocks.ExtractionInfusedCasing.get()))
                    .where("G", Predicates.blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                    .where(" ", Predicates.blocks(Blocks.AIR))
                    .where("L", Predicates.anyLamp().or(Predicates.blocks(Blocks.GLOWSTONE)))
                    .where("P", Predicates.blocks(TrueSteamBlocks.ExtractionInfusedPipeCasing.get()))
                    .where("K", Predicates.controller(Predicates.blocks(definition.get())))
                    .where("M", Predicates.ability(PartAbility.MAINTENANCE))
                    .where("S", Predicates.blocks(Blocks.SPAWNER))
                    .where("X", Predicates.ability(PartAbility.IMPORT_FLUIDS).setPreviewCount(1)
                            .or(Predicates.ability(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                            .or(Predicates.ability(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                            .or(Predicates.ability(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                            .or(Predicates.ability(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2)).or(Predicates.blocks(TrueSteamBlocks.ExtractionInfusedCasing.get())))
                    .build())
            .tooltips(TrueSteamLang.SEM_TOOLTIP_1)
            .additionalDisplay(((iMultiController, components) -> {
                if (iMultiController instanceof SpawnerExtractionMachineMachine spawnerExtractionMachineMachine) {
                    var mobInside = spawnerExtractionMachineMachine.getMobInside();
                    if (mobInside != null)
                        components.add(Component.literal("Entity: " + mobInside.get().getDescription().getString()));
                }
            }))
            .workableCasingModel(
                    GTTrueSteam.id("block/extraction_infused_casing"),
                    GTCEu.id("block/multiblock/gcym/large_extractor"))
            .register();

    public static void sayHi() {}
}

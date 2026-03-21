package site.siredvin.gttruesteam.machines.mob_spawner;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamBlocks;
import site.siredvin.gttruesteam.TrueSteamLang;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;

public class MobSpawnerMultiblock {

    public static MultiblockMachineDefinition MACHINE = GTTrueSteam.REGISTRATE
            .multiblock("mob_spawner", MobSpawnerMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TrueSteamRecipeTypes.MOB_SPAWNER)
            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT, new MobSpawnerRecipeModifier())
            .appearanceBlock(TrueSteamBlocks.MobTrapCasing)
            .pattern(definition -> FactoryBlockPattern.start()
                    // Aisles go back (index 0) to front (last index, controller face)
                    // Each aisle: strings are Y levels bottom→top, chars are X left→right
                    .aisle("CCC", "CHC", "CCC")   // back slice: H = maintenance
                    .aisle("CCC", "CSC", "CCC")   // middle slice: S = spawner
                    .aisle("CMC", "CPC", "CCC")   // front slice: P = controller, M = muffler
                    .where('P', Predicates.controller(Predicates.blocks(definition.get())))
                    .where('S', Predicates.blocks(Blocks.SPAWNER))
                    .where('H', Predicates.abilities(PartAbility.MAINTENANCE))
                    .where('M', Predicates.abilities(PartAbility.MUFFLER))
                    .where('C',
                            Predicates.blocks(TrueSteamBlocks.MobTrapCasing.get())
                                    .or(Predicates.abilities(PartAbility.IMPORT_ITEMS))
                                    .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS))
                                    .or(Predicates.abilities(PartAbility.INPUT_ENERGY)))
                    .build())
            .shapeInfo(definition -> MultiblockShapeInfo.builder()
                    .aisle("CMC", "CPC", "CCC")
                    .aisle("CCC", "CSC", "CCC")
                    .aisle("CCC", "CHC", "CCC")
                    .where('P', definition, Direction.SOUTH)
                    .where('S', Blocks.SPAWNER)
                    .where('H', GTMachines.MAINTENANCE_HATCH, Direction.NORTH)
                    .where('M', GTMachines.MUFFLER_HATCH[1], Direction.UP)
                    .where('C', TrueSteamBlocks.MobTrapCasing)
                    .build())
            .tooltips(
                    TrueSteamLang.MOB_SPAWNER_TOOLTIP_1,
                    TrueSteamLang.MOB_SPAWNER_TOOLTIP_2,
                    TrueSteamLang.MOB_SPAWNER_TOOLTIP_3)
            .additionalDisplay((controller, tooltips) -> {
                if (controller instanceof MobSpawnerMachine spawnerMachine && spawnerMachine.isFormed()) {
                    spawnerMachine.getSpawnerMobType().ifPresentOrElse(
                            mob -> tooltips.add(Component.translatable(
                                    TrueSteamLang.MOB_SPAWNER_CURRENT_MOB_KEY, mob.toString())),
                            () -> tooltips.add(TrueSteamLang.MOB_SPAWNER_NO_SPAWNER));
                }
            })
            .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_steel"),
                    GTCEu.id("block/multiblock/implosion_compressor"))
            .register();

    public static void sayHi() {}
}

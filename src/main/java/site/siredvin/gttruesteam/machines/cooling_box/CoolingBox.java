package site.siredvin.gttruesteam.machines.cooling_box;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import net.minecraft.world.level.block.Blocks;
import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamBlocks;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;

public class CoolingBox {
     public static MultiblockMachineDefinition Machine = GTTrueSteam.REGISTRATE
     .multiblock("cooling_box", CoolingBoxMachine::new)
     .rotationState(RotationState.NON_Y_AXIS)
     .recipeType(TrueSteamRecipeTypes.FLUID_COOLING)
             .appearanceBlock(TrueSteamBlocks.SlightlyCorrosionProofCasing)
             .pattern(definition -> FactoryBlockPattern.start()
                     .aisle("CCMCC", "CSSSC", "CSSSC", "CSSSC", "CCCCC")
                     .aisle("COOOC", "S   S", "S   S", "S   S", "CIIIC")
                     .aisle("COOOC", "S   S", "S   S", "S   S", "CIIIC")
                     .aisle("COOOC", "S   S", "S   S", "S   S", "CIIIC")
                     .aisle("CCKCC", "CSSSC", "CSSSC", "CSSSC", "CCCCC")
                     .where("C", Predicates.blocks(TrueSteamBlocks.SlightlyCorrosionProofCasing.get()))
                     .where("S", Predicates.blocks(Blocks.ICE, Blocks.BLUE_ICE, Blocks.PACKED_ICE))
                     .where(" ", Predicates.blocks(Blocks.AIR))
                     .where("K", Predicates.controller(Predicates.blocks(definition.get())))
                     .where("M", Predicates.ability(PartAbility.MAINTENANCE))
                     .where("O", Predicates.ability(PartAbility.EXPORT_FLUIDS))
                     .where("I", Predicates.ability(PartAbility.IMPORT_FLUIDS))
                     .build()
             )
             .workableCasingModel(
                     GTCEu.id("block/casings/gcym/corrosion_proof_casing"),
                     GTCEu.id("block/multiblock/vacuum_freezer"))
             .register();

     public static void sayHi() {}
}

package site.siredvin.gttruesteam.machines.coating_shrine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.StairsShape;

import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamBlocks;
import site.siredvin.gttruesteam.TrueSteamLang;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;
import site.siredvin.gttruesteam.client.CoatingShrineRenderer;

import java.util.function.Supplier;

public class CoatingShrine {

    private static Supplier<BlockInfo[]> BLOCK_SUPPLIER = () -> new BlockInfo[] {
            BlockInfo.fromBlock(Blocks.STONE_BRICKS) };
    private static Supplier<BlockInfo[]> FLUID_SUPPLIER = () -> new BlockInfo[] { BlockInfo.fromBlock(Blocks.LAVA) };

    @SuppressWarnings("deprecation")
    public static MultiblockMachineDefinition MACHINE = GTTrueSteam.REGISTRATE
            .multiblock("coating_shrine", CoatingShrineMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TrueSteamRecipeTypes.COATING)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("BSHSB", "|###|", "|###|", "|###|", "SSSSS")
                    .aisle("SBBBS", "#####", "#####", "#####", "SCCCS")
                    .aisle("IPFPO", "#####", "#####", "#####", "SCCCS")
                    .aisle("SBBBS", "#####", "#####", "#####", "SCCCS")
                    .aisle("BSKSB", "|###|", "|###|", "|###|", "SSSSS")
                    .where("S", Predicates.blockTag(BlockTags.STAIRS))
                    .where("B",
                            Predicates.custom(x -> x.getBlockState().isSolid(), BLOCK_SUPPLIER)
                                    .addTooltips(Component.literal("Any solid block can be used")))
                    .where("|", Predicates.blockTag(BlockTags.WALLS))
                    .where("#", Predicates.any())
                    .where("C", Predicates.blocks(TrueSteamBlocks.BronzeGlass.get()))
                    .where("I", Predicates.ability(PartAbility.EXPORT_ITEMS))
                    .where("O", Predicates.ability(PartAbility.IMPORT_ITEMS))
                    .where("K", Predicates.controller(Predicates.blocks(definition.get())))
                    .where("H", Predicates.blocks(TrueSteamBlocks.BoilerHusk.get()))
                    .where("F", Predicates.custom(x -> {
                        var fluidState = x.getBlockState().getFluidState();
                        return !fluidState.isEmpty() && fluidState.isSource();
                    }, FLUID_SUPPLIER).addTooltips(Component.literal("Any fluid source can be used")))
                    .where("P", Predicates.blocks(GTBlocks.CASING_STEEL_PIPE.get()))
                    .build())
            .shapeInfo(definition -> MultiblockShapeInfo.builder()
                    .aisle("B2H2B", "|###|", "|###|", "|###|", "72226")
                    .aisle("3BBB4", "#####", "#####", "#####", "3CCC4")
                    .aisle("IPFPO", "#####", "#####", "#####", "3CCC4")
                    .aisle("3BBB4", "#####", "#####", "#####", "3CCC4")
                    .aisle("B1K1B", "|###|", "|###|", "|###|", "51118")
                    .where('1',
                            Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH))
                    .where('2',
                            Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH))
                    .where('3',
                            Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST))
                    .where('4',
                            Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST))
                    .where('5',
                            Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH)
                                    .setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT))
                    .where('6',
                            Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST)
                                    .setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT))
                    .where('7',
                            Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST)
                                    .setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT))
                    .where('8',
                            Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH)
                                    .setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT))
                    .where('S', Blocks.STONE_BRICK_STAIRS)
                    .where('B', Blocks.STONE_BRICKS)
                    .where('|', Blocks.STONE_BRICK_WALL)
                    .where('#', Blocks.AIR)
                    .where('C', TrueSteamBlocks.BronzeGlass.get())
                    .where('I', GTMachines.ITEM_EXPORT_BUS[1], Direction.WEST)
                    .where('O', GTMachines.ITEM_IMPORT_BUS[1], Direction.EAST)
                    .where('K', definition, Direction.SOUTH)
                    .where('H', TrueSteamBlocks.BoilerHusk.get())
                    .where('F', Blocks.LAVA)
                    .where('P', GTBlocks.CASING_STEEL_PIPE.get())
                    .build())
            .additionalDisplay(((iMultiController, components) -> {
                if (iMultiController.isFormed()) {
                    if (iMultiController instanceof CoatingShrineMachine coatingShrineMachine) {
                        var logic = coatingShrineMachine.getRecipeLogic();
                        components.add(TrueSteamLang.COATING_CHARGES.append(" ")
                                .append(Integer.toString(logic.getCoatingCharges())));
                    }
                }
            }))
            .tooltips(
                    TrueSteamLang.COATING_SHRINE_TOOLTIP_1, TrueSteamLang.COATING_SHRINE_TOOLTIP_2)
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .model(GTMachineModels.createWorkableCasingMachineModel(
                    GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/machines/chemical_bath"))
                    .andThen(b -> b.addDynamicRenderer(CoatingShrineRenderer::new)))
            .register();

    public static void sayHi() {}
}

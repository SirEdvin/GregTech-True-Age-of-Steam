package site.siredvin.gttruesteam;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import site.siredvin.gttruesteam.api.ICoolingCoilType;
import site.siredvin.gttruesteam.common.AlwaysAirTraceabilityPredicate;
import site.siredvin.gttruesteam.common.CoolingCoilBlock;

import java.util.*;
import java.util.function.Supplier;

public class TrueSteamPredicates {

    public static String COOLING_COIL_TYPE_MARK = "CoolingCoilType";
    public static String MACHINE_TYPE_MARK = "MachineType";
    public static String MACHINE_POS_LIST_MARK = "MachinePos";
    public static String BEATING_BOILER_HUSK_MARK = "BeatingBoiler";
    public static String INDUSTRIAL_COATING_FLUID_POS_MARK = "IndustrialCoatingFluidPos";
    public static List<Block> MV_MACHINES = new ArrayList<>();

    public static TraceabilityPredicate coolingCoils() {
        return new TraceabilityPredicate(blockWorldState -> {
            var blockState = blockWorldState.getBlockState();
            for (Map.Entry<ICoolingCoilType, Supplier<CoolingCoilBlock>> entry : GTTrueSteamAPI.COOLING_COILS
                    .entrySet()) {
                if (blockState.is(entry.getValue().get())) {
                    var stats = entry.getKey();
                    Object currentCoil = blockWorldState.getMatchContext().getOrPut(COOLING_COIL_TYPE_MARK, stats);
                    if (!currentCoil.equals(stats)) {
                        blockWorldState.setError(new PatternStringError(TrueSteamLang.COIL_ERROR_KEY));
                        return false;
                    }
                    return true;
                }
            }
            return false;
        }, () -> GTTrueSteamAPI.COOLING_COILS.entrySet().stream()
                .sorted(Comparator.comparingInt(value -> value.getKey().getLevel()))
                .map(coil -> BlockInfo.fromBlockState(coil.getValue().get().defaultBlockState()))
                .toArray(BlockInfo[]::new))
                .addTooltips(TrueSteamLang.COIL_ERROR);
    }

    private static final String[] STOP_WORDS = new String[] {
            "hatch", "hp_", "hull", "buffer", "converter", "transformer", "pipeline", "chest", "tank"
    };

    private static boolean isMVMachine(MachineDefinition m) {
        if (m.getTier() != GTValues.MV)
            return false;
        var id = m.getId().toString();
        for (var word : STOP_WORDS) {
            if (id.contains(word))
                return false;
        }
        return true;
    }

    private static void ensureList() {
        if (MV_MACHINES.isEmpty()) {
            MV_MACHINES = GTRegistries.MACHINES.values().stream()
                    .filter(TrueSteamPredicates::isMVMachine).map(MachineDefinition::getBlock).toList();
        }
    }

    public static TraceabilityPredicate singleBlockMachines() {
        return new TraceabilityPredicate(blockWorldState -> {
            ensureList();
            var block = blockWorldState.getBlockState().getBlock();
            var isMachine = MV_MACHINES.contains(block);
            if (!isMachine)
                return false;
            @SuppressWarnings("DataFlowIssue")
            var currentBlock = ForgeRegistries.BLOCKS.getKey(block).toString();
            Object currentRecipeType = blockWorldState.getMatchContext().getOrPut(MACHINE_TYPE_MARK, currentBlock);
            if (!currentRecipeType.equals(currentBlock)) {
                blockWorldState.setError(new PatternStringError(TrueSteamLang.MACHINE_ERROR_KEY));
                return false;
            }
            List<BlockPos> machinePoses = blockWorldState.getMatchContext().getOrPut(MACHINE_POS_LIST_MARK,
                    new ArrayList<>());
            machinePoses.add(blockWorldState.getPos());
            return true;
        }, () -> {
            ensureList();
            return MV_MACHINES.stream()
                    .map(m -> new BlockInfo(m.defaultBlockState(), true))
                    .toArray(BlockInfo[]::new);
        });
    }

    public static TraceabilityPredicate industrialCoatingFluidCells() {
        var fluidBlockInfo = new BlockInfo[] { BlockInfo.fromBlock(Blocks.LAVA) };
        return new TraceabilityPredicate(blockWorldState -> {
            var fluidState = blockWorldState.getBlockState().getFluidState();
            if (fluidState.isEmpty() || !fluidState.isSource()) return false;
            List<BlockPos> fluidPoses = blockWorldState.getMatchContext().getOrPut(INDUSTRIAL_COATING_FLUID_POS_MARK,
                    new ArrayList<>());
            fluidPoses.add(blockWorldState.getPos());
            return true;
        }, () -> fluidBlockInfo)
                .addTooltips(net.minecraft.network.chat.Component.literal("Any fluid source can be used"));
    }

    public static TraceabilityPredicate optionalBeatingBoilerHusk() {
        return new AlwaysAirTraceabilityPredicate(blockWorldState -> {
            var blockState = blockWorldState.getBlockState();
            var isBeatingHusk = blockState.is(TrueSteamBlocks.BeatingBoilerHusk.get());
            Boolean currentBeatingHuskMark = blockWorldState.getMatchContext().getOrPut(BEATING_BOILER_HUSK_MARK,
                    isBeatingHusk);
            if (currentBeatingHuskMark != isBeatingHusk) {
                blockWorldState.setError(new PatternStringError(TrueSteamLang.BEATING_HUSK_ERROR_KEY));
                return false;
            }
            return true;
        }, () -> new BlockInfo[] { BlockInfo.fromBlock(Blocks.AIR),
                BlockInfo.fromBlock(TrueSteamBlocks.BeatingBoilerHusk.get()) })
                .addTooltips(TrueSteamLang.OPTIONAL_UPGRADE);
    }
}

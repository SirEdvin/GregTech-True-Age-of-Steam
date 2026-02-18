package site.siredvin.gttruesteam;

import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import site.siredvin.gttruesteam.api.ICoolingCoilType;
import site.siredvin.gttruesteam.common.CoolingCoilBlock;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Supplier;

public class TrueSteamPredicates {
    public static String COOLING_COIL_TYPE_MARK = "CoolingCoilType";

    public static TraceabilityPredicate coolingCoils() {
        return new TraceabilityPredicate(blockWorldState -> {
            var blockState = blockWorldState.getBlockState();
            for (Map.Entry<ICoolingCoilType, Supplier<CoolingCoilBlock>> entry : GTTrueSteamAPI.COOLING_COILS.entrySet()) {
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
                .sorted(Comparator.comparingInt(value -> value.getKey().getTier()))
                .map(coil -> BlockInfo.fromBlockState(coil.getValue().get().defaultBlockState()))
                .toArray(BlockInfo[]::new))
                .addTooltips(TrueSteamLang.COIL_ERROR);
    }
}

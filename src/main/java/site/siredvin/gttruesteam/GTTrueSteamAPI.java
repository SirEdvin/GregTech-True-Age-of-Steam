package site.siredvin.gttruesteam;

import com.gregtechceu.gtceu.common.block.CoilBlock;
import site.siredvin.gttruesteam.api.ICoolingCoilType;
import site.siredvin.gttruesteam.common.CoolingCoilBlock;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GTTrueSteamAPI {
    public static final Map<ICoolingCoilType, Supplier<CoolingCoilBlock>> COOLING_COILS = new HashMap<>();
}

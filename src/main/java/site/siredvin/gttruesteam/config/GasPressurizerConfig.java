package site.siredvin.gttruesteam.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class GasPressurizerConfig {

    public static ForgeConfigSpec.DoubleValue percentageIncrease;

    public static void setupConfig(ForgeConfigSpec.Builder builder) {
        builder.push("industrial_gas_pressurizer");
        percentageIncrease = builder
                .defineInRange("percentageIncrease", 0.25, 0, 5);
        builder.pop();
    }
}

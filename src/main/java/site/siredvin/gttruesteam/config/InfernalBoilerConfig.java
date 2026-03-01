package site.siredvin.gttruesteam.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class InfernalBoilerConfig {

    public static ForgeConfigSpec.DoubleValue basicSupremeCoef;
    public static ForgeConfigSpec.DoubleValue coilSupremeCoef;

    public static void setupConfig(ForgeConfigSpec.Builder builder) {
        builder.push("infernal_boiler");
        basicSupremeCoef = builder
                .defineInRange("basicSupremeCoef", 2, 0.1, 100);
        coilSupremeCoef = builder
                .defineInRange("coilSupremeCoef", 0.5, 0.1, 100);
        builder.pop();
    }
}

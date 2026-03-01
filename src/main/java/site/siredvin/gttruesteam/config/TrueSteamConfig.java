package site.siredvin.gttruesteam.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class TrueSteamConfig {

    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        setupConfig(configBuilder);
        COMMON_SPEC = configBuilder.build();
    }

    private static void setupConfig(ForgeConfigSpec.Builder builder) {
        InfernalBoilerConfig.setupConfig(builder);
    }
}

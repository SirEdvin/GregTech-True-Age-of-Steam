package site.siredvin.gttruesteam.common;

public class Constants {

    public static int FLUID_INGOT = 144;
    public static int FLUID_BLOCK = FLUID_INGOT * 9;
    public static int BASE_STEAM_TEMPERATURE = 373;
    public static double BASE_STEAM_DENSITY = 0.5;
    public static int TEMPERATURE_TO_ENERGY = 50;
    public static int STEAM_COMPRESSION_COEF = 20;
    public static int STEAM_CRITICAL_BOOST = 2;
    public static int METAPHYSICS_STEAM_BOILING_OUTPUT = 6000;

    public static int BASE_FLUID_COOLING_REQUIREMENT = 15000;
    public static int FLUID_COOLING_REQUIRED_COEF = 12;
    public static int FLUID_COOLING_COST_COEF = 10;
    public static int FLUID_COOLING_OVER_5K_REQUIRED_BONUS = 8;
    public static int FLUID_COOLING_OVER_5K_COST_BONUS = 5;
    public static int FLUID_COOLING_OVER_5K_REQUIRED_COEF = FLUID_COOLING_REQUIRED_COEF +
            FLUID_COOLING_OVER_5K_COST_BONUS;
    public static int FLUID_COOLING_OVER_5K_COST_COEF = FLUID_COOLING_COST_COEF + FLUID_COOLING_OVER_5K_REQUIRED_BONUS;
}

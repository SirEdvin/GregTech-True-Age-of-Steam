package site.siredvin.gttruesteam;

import net.minecraft.Util;
import net.minecraft.network.chat.MutableComponent;

public class TrueSteamLang {

    public static String COIL_ERROR_KEY = Util.makeDescriptionId("error", GTTrueSteam.id("cooling_coils"));
    public static String MACHINE_ERROR_KEY = Util.makeDescriptionId("error", GTTrueSteam.id("machine"));
    public static String BEATING_HUSK_ERROR_KEY = Util.makeDescriptionId("error", GTTrueSteam.id("beating_husk"));

    public static String COIL_COOLING_CAPACITY_KEY = Util.makeDescriptionId("tooltip",
            GTTrueSteam.id("cooling_coils_capacity"));
    public static String COIL_COOLING_REDUCTION_KEY = Util.makeDescriptionId("tooltip",
            GTTrueSteam.id("cooling_coils_reduction"));
    public static String COOLING_CAPACITY_MESSAGE_KEY = Util.makeDescriptionId("message",
            GTTrueSteam.id("cooling_capacity"));
    public static String COATING_CHARGES_MESSAGE_KEY = Util.makeDescriptionId("message",
            GTTrueSteam.id("coating_charges"));
    public static String PERFECT_CONDITION_KEY = Util.makeDescriptionId("tooltip", GTTrueSteam.id("perfect_condition"));

    public static String CHARGING_CYCLES_KEY = Util.makeDescriptionId("tooltip", GTTrueSteam.id("charging_cycles"));
    public static String COOLING_CONSUMED_KEY = Util.makeDescriptionId("tooltip", GTTrueSteam.id("cooling_consumed"));
    public static String RCC_REDUCTION_KEY = Util.makeDescriptionId("tooltip", GTTrueSteam.id("rcc_reduction"));

    public static String COOLING_REQUIRED_KEY = Util.makeDescriptionId("condition", GTTrueSteam.id("cooling_capacity"));
    public static String INNER_RECIPE_TYPE_CONDITION_KEY = Util.makeDescriptionId("condition",
            GTTrueSteam.id("inner_recipe_type"));

    public static MutableComponent COATING_FLUID_CONDITION = GTTrueSteam.REGISTRATE.addLang("condition",
            GTTrueSteam.id("coating_fluid"), "Coating fluid:");
    public static MutableComponent COOLING_CAPACITY_CONDITION = GTTrueSteam.REGISTRATE.addRawLang(COOLING_REQUIRED_KEY,
            "Cooling required: %d");
    public static MutableComponent INNER_RECIPE_TYPE_CONDITION = GTTrueSteam.REGISTRATE.addRawLang(
            INNER_RECIPE_TYPE_CONDITION_KEY,
            "Recipe inside: ");
    public static MutableComponent BEATING_HUSK_CONDITION = GTTrueSteam.REGISTRATE.addLang(
            "condition", GTTrueSteam.id("beating_husk"),
            "Beating husks required");

    public static MutableComponent COATING_CHARGES = GTTrueSteam.REGISTRATE.addLang("tooltip",
            GTTrueSteam.id("coating_charges"), "Coating charges: ");
    public static MutableComponent COIL_ERROR = GTTrueSteam.REGISTRATE.addRawLang(COIL_ERROR_KEY,
            "All cooling coils should be the same");
    public static MutableComponent MACHINE_ERROR = GTTrueSteam.REGISTRATE.addRawLang(MACHINE_ERROR_KEY,
            "All machines should be the same");
    public static MutableComponent BEATING_HUSK_ERROR = GTTrueSteam.REGISTRATE.addRawLang(BEATING_HUSK_ERROR_KEY,
            "All slots should be occupied by beating husk if at least one present");

    public static MutableComponent COIL_COOLING_CAPACITY = GTTrueSteam.REGISTRATE.addRawLang(COIL_COOLING_CAPACITY_KEY,
            "§bBase Cooling Capacity: §f%d ηK");
    public static MutableComponent COIL_COOLING_REDUCTION = GTTrueSteam.REGISTRATE.addRawLang(
            COIL_COOLING_REDUCTION_KEY,
            "§bMax duration reduction: §f%d%%");
    public static MutableComponent CHARGING_CYCLES = GTTrueSteam.REGISTRATE.addRawLang(CHARGING_CYCLES_KEY,
            "Charging for %d cycles");
    public static MutableComponent OVERHEATABLE = GTTrueSteam.REGISTRATE.addLang("tooltip",
            GTTrueSteam.id("overheatable"), "§cOverheat§f-able");
    public static MutableComponent COOLING_CONSUMED = GTTrueSteam.REGISTRATE.addRawLang(COOLING_CONSUMED_KEY,
            "Cooling consumed: %d");
    public static MutableComponent RCC_REDUCTION = GTTrueSteam.REGISTRATE.addRawLang(RCC_REDUCTION_KEY,
            "Duration reduction factor: %s");
    public static MutableComponent PERFECT_CONDITION = GTTrueSteam.REGISTRATE.addRawLang(PERFECT_CONDITION_KEY,
            "Perfect condition:");

    public static MutableComponent COATING_SHRINE_TOOLTIP_1 = GTTrueSteam.REGISTRATE.addLang(
            "item_tooltip",
            GTTrueSteam.id("coating_shrine_1"),
            "§6Coating allows you to infuse items with fluid properties. Fluid in the middle impact recipes.");
    public static MutableComponent COATING_SHRINE_TOOLTIP_2 = GTTrueSteam.REGISTRATE.addLang(
            "item_tooltip",
            GTTrueSteam.id("coating_shrine_2"),
            "§7Coating is primitive and imperfect process, fluid in the middle will be consume each 100 crafts. You can right click on controller to get about of charges left.");
    public static MutableComponent COOLING_BOX_TOOLTIP_1 = GTTrueSteam.REGISTRATE.addLang(
            "item_tooltip",
            GTTrueSteam.id("cooling_box_1"),
            "§3Surprisingly effective device, that works by using cooling coils to accumulate cooling capacity and discharge it to cool fluids or items");
    public static MutableComponent COOLING_BOX_TOOLTIP_2 = GTTrueSteam.REGISTRATE.addLang(
            "item_tooltip",
            GTTrueSteam.id("cooling_box_2"),
            "§7Recipes usually shows amount of accumulated cooling capacity required as well as how capacity they will consume. You can right click it to get accumulate cooling capacity. Better cooling coils impact maximum capacity as well as capacity generation over time.");
    public static MutableComponent INFERNAL_BOILER_TOOLTIP_1 = GTTrueSteam.REGISTRATE.addLang(
            "item_tooltip",
            GTTrueSteam.id("infernal_boiler_1"),
            "§cBoiler build from liquid blazes and with stronger version of bronze§f. This almost unnatural device can generate superhot steam in large quantities nearly free!");
    public static MutableComponent INFERNAL_BOILER_TOOLTIP_2 = GTTrueSteam.REGISTRATE.addLang(
            "item_tooltip",
            GTTrueSteam.id("infernal_boiler_2"),
            "§7Boiler has two important mechanics.\n  First one is heat. Each time boiler completes recipe, it slowly gather heat and after some time increases heat level. Each heat level doubles the possible parallels. Last heat level - Supreme, also overheats recipes, multiplying its output.");
    public static MutableComponent INFERNAL_BOILER_TOOLTIP_3 = GTTrueSteam.REGISTRATE.addLang(
            "item_tooltip",
            GTTrueSteam.id("infernal_boiler_3"),
            "§7  Second one is boiler charging. Periodically boiler need to run specific recipes, that can restore its charge, or it will start throttling and dropping heat.");
    public static MutableComponent RCC_TOOLTIP_1 = GTTrueSteam.REGISTRATE.addLang(
            "item_tooltip",
            GTTrueSteam.id("rcc_1"),
            "§3Slightly better aluminium box");
    public static MutableComponent RCC_TOOLTIP_2 = GTTrueSteam.REGISTRATE.addLang(
            "item_tooltip",
            GTTrueSteam.id("rcc_2"),
            "§7Slighly better version of vacuum freezer, but without batching. When working continuously, reduce recipe duration based on coils and also has some parallel capacity based on coil levels. When level of coil better than voltage requirement of recipe, switches to perfect OC");

    public static MutableComponent CIM_TOOLTIP_1 = GTTrueSteam.REGISTRATE.addLang(
            "item_tooltip", GTTrueSteam.id("cim_1"),
            "§3Everything is just a concept, waiting to be materialized");
    public static MutableComponent CIM_TOOLTIP_2 = GTTrueSteam.REGISTRATE.addLang(
            "item_tooltip", GTTrueSteam.id("cim_2"),
            "§7Large matrix that can be used for creating a concept catalysts, special symbols of specific concepts, that can be used to modify conceptualized steel properties.");
    public static MutableComponent CIM_TOOLTIP_3 = GTTrueSteam.REGISTRATE.addLang(
            "item_tooltip", GTTrueSteam.id("cim_3"),
            "§7MV machines inside decided which concept it will be. They need to be all the same and need to run all the time during crafting.");

    public static MutableComponent IGP_TOOLTIP_1 = GTTrueSteam.REGISTRATE.addLang(
            "item_tooltip", GTTrueSteam.id("igp_1"),
            "§bMore dense versions of steam is always appreciate");
    public static MutableComponent IGP_TOOLTIP_2 = GTTrueSteam.REGISTRATE.addLang(
            "item_tooltip", GTTrueSteam.id("igp_2"),
            "§7Machine for compressing gases into their dense variants. By default you lose up to 25% of initial gas mass without perfect conditions.");
    public static MutableComponent IGP_TOOLTIP_3 = GTTrueSteam.REGISTRATE.addLang(
            "item_tooltip", GTTrueSteam.id("igp_3"),
            "§7Achieving perfect condition is no simple feat. It can occur on two seconds intervals are require all input hatches to be filled from 35% to 85% of its capacity. Some hatches (like ME hatches) don't have capacity, which makes perfect condition impossible.");

    public static MutableComponent COOLING_CAPACITY_MESSAGE = GTTrueSteam.REGISTRATE
            .addRawLang(COOLING_CAPACITY_MESSAGE_KEY, "Current cooling capacity: %d");
    public static MutableComponent COATING_CHARGES_MESSAGE = GTTrueSteam.REGISTRATE
            .addRawLang(COATING_CHARGES_MESSAGE_KEY, "Left coating charges: %d");

    public static void sayHi() {
        // GTTrueSteam.REGISTRATE.addRawLang(TrueSteamRecipes.)
        GTTrueSteam.REGISTRATE.addRawLang("block.gtceu.liquid_ender_air", "Liquid ender air");
        GTTrueSteam.REGISTRATE.addRawLang("block.gtceu.blaze", "Liquid blaze");
        GTTrueSteam.REGISTRATE.addRawLang("block.gtceu.ice", "Liquid ice");
    }
}

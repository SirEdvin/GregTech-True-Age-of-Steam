package site.siredvin.gttruesteam;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

import java.util.function.Supplier;

public class TrueSteamStats {

    public static Supplier<Stat<ResourceLocation>> INFERNAL_MAINTAIN_RECIPE_PERFORMED;
    public static Supplier<Stat<ResourceLocation>> PERFECT_CONDITION_RECIPE_CRAFTED;

    public static void init() {
        INFERNAL_MAINTAIN_RECIPE_PERFORMED = register("infernal_maintain_recipe_performed", "Infernal boiler charged", StatFormatter.DEFAULT);
        PERFECT_CONDITION_RECIPE_CRAFTED = register("perfect_condition_recipe_crafted", "Pressurized perfect condition reached", StatFormatter.DEFAULT);
    }

    private static Supplier<Stat<ResourceLocation>> register(String name, String displayName, StatFormatter formatter) {
        ResourceLocation id = GTTrueSteam.id(name);
        var c1 = GTTrueSteam.REGISTRATE.simple(name, Registries.CUSTOM_STAT, () -> id);
        GTTrueSteam.REGISTRATE.addRawLang("stat." + GTTrueSteam.MOD_ID + "." + name, displayName);
        return c1.lazyMap(p1 -> Stats.CUSTOM.get(id, formatter));
    }
}

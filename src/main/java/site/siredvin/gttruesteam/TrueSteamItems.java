package site.siredvin.gttruesteam;

import net.minecraft.world.item.Item;

import com.tterrag.registrate.util.entry.ItemEntry;

public class TrueSteamItems {

    public static ItemEntry<Item> PurifiedInfernalDust = GTTrueSteam.REGISTRATE
            .item("purified_infernal_dust", Item::new)
            .initialProperties(() -> new Item.Properties().stacksTo(1).fireResistant())
            .defaultModel()
            .register();

    public static void sayHi() {}
}

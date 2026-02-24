package site.siredvin.gttruesteam;

import net.minecraft.world.item.Item;

import com.tterrag.registrate.util.entry.ItemEntry;

public class TrueSteamItems {

    public static ItemEntry<Item> PurifiedInfernalDust = GTTrueSteam.REGISTRATE
            .item("purified_infernal_dust", Item::new)
            .initialProperties(() -> new Item.Properties().fireResistant())
            .defaultModel()
            .register();

    public static ItemEntry<Item> ReEtchedCircuit = GTTrueSteam.REGISTRATE
            .item("reeteched_circuit", Item::new)
            .lang("Re-Etched Circuit")
            .initialProperties(Item.Properties::new)
            .defaultModel()
            .register();

    public static ItemEntry<Item> InfernalCircuit = GTTrueSteam.REGISTRATE
            .item("infernal_circuit", Item::new)
            .initialProperties(() -> new Item.Properties().stacksTo(64).fireResistant())
            .defaultModel()
            .register();

    public static ItemEntry<Item> RawInfernalCircuit = GTTrueSteam.REGISTRATE
            .item("raw_infernal_circuit", Item::new)
            .initialProperties(() -> new Item.Properties().stacksTo(64).fireResistant())
            .defaultModel()
            .register();

    public static void sayHi() {}
}

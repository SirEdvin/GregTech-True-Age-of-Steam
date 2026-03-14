package site.siredvin.gttruesteam;

import net.minecraft.world.item.Item;

import com.tterrag.registrate.util.entry.ItemEntry;
import site.siredvin.gttruesteam.common.GlowingItem;

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

    public static ItemEntry<Item> EmptyCatalyst = GTTrueSteam.REGISTRATE
            .item("empty_catalyst", Item::new)
            .initialProperties(() -> new Item.Properties().stacksTo(64))
            .defaultModel()
            .register();

    public static ItemEntry<Item> CompressionSpringPack = GTTrueSteam.REGISTRATE
            .item("compression_spring_pack", Item::new)
            .initialProperties(Item.Properties::new)
            .defaultModel()
            .register();

    public static ItemEntry<GlowingItem> InfusedCompressionSpringPack = GTTrueSteam.REGISTRATE
            .item("infused_compression_spring_pack", GlowingItem::new)
            .initialProperties(Item.Properties::new)
            .model(((ctx, prov) -> {
                prov.generated(ctx, prov.itemTexture(CompressionSpringPack));
            }))
            .register();

    public static ItemEntry<Item> SlightlyCompressedCompressionSpringPack = GTTrueSteam.REGISTRATE
            .item("slightly_compressed_compression_spring_pack", Item::new)
            .initialProperties(Item.Properties::new)
            .defaultModel()
            .register();

    public static ItemEntry<GlowingItem> InfusedSlightlyCompressedCompressionSpringPack = GTTrueSteam.REGISTRATE
            .item("infused_slightly_compressed_compression_spring_pack", GlowingItem::new)
            .initialProperties(Item.Properties::new)
            .model(((ctx, prov) -> {
                prov.generated(ctx, prov.itemTexture(SlightlyCompressedCompressionSpringPack));
            }))
            .register();

    public static ItemEntry<Item> SomewhatCompressedCompressionSpringPack = GTTrueSteam.REGISTRATE
            .item("somewhat_compressed_compression_spring_pack", Item::new)
            .initialProperties(Item.Properties::new)
            .defaultModel()
            .register();

    public static ItemEntry<GlowingItem> InfusedSomewhatCompressedCompressionSpringPack = GTTrueSteam.REGISTRATE
            .item("infused_somewhat_compressed_compression_spring_pack", GlowingItem::new)
            .initialProperties(Item.Properties::new)
            .model(((ctx, prov) -> {
                prov.generated(ctx, prov.itemTexture(SomewhatCompressedCompressionSpringPack));
            }))
            .register();

    public static ItemEntry<Item> CompressedCompressionSpringPack = GTTrueSteam.REGISTRATE
            .item("compressed_compression_spring_pack", Item::new)
            .initialProperties(Item.Properties::new)
            .defaultModel()
            .register();

    public static ItemEntry<GlowingItem> InfusedCompressedCompressionSpringPack = GTTrueSteam.REGISTRATE
            .item("infused_compressed_compression_spring_pack", GlowingItem::new)
            .initialProperties(Item.Properties::new)
            .model(((ctx, prov) -> {
                prov.generated(ctx, prov.itemTexture(CompressedCompressionSpringPack));
            }))
            .register();

    public static void sayHi() {}
}

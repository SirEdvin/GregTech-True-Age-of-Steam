package site.siredvin.gttruesteam;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;

import java.util.function.Consumer;


public class TrueSteamAdvancements implements ForgeAdvancementProvider.AdvancementGenerator {

    private static final ResourceLocation BACKGROUND = new ResourceLocation("minecraft",
            "textures/gui/advancements/backgrounds/stone.png");

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> writer,
            ExistingFileHelper existingFileHelper) {

        // Root: obtain the Husk of the Boiler to unlock the tree
        AdvancementHolder root = Advancement.Builder.advancement()
                .display(new DisplayInfo(
                        new ItemStack(TrueSteamBlocks.BoilerHusk.get()),
                        Component.translatable("advancements.gttruesteam.root.title"),
                        Component.translatable("advancements.gttruesteam.root.description"),
                        BACKGROUND, FrameType.TASK, false, false, false))
                .addCriterion("has_boiler_husk",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                TrueSteamBlocks.BoilerHusk.get().asItem()))
                .save(writer, GTTrueSteam.MOD_ID + ":root", existingFileHelper);

        // Bronze glass – early decorative milestone alongside the infernal path
        Advancement.Builder.advancement()
                .parent(root)
                .display(new DisplayInfo(
                        new ItemStack(TrueSteamBlocks.BronzeGlass.get()),
                        Component.translatable("advancements.gttruesteam.bronze_glass_age.title"),
                        Component.translatable("advancements.gttruesteam.bronze_glass_age.description"),
                        null, FrameType.TASK, true, true, false))
                .addCriterion("has_bronze_glass",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                TrueSteamBlocks.BronzeGlass.get().asItem()))
                .save(writer, GTTrueSteam.MOD_ID + ":bronze_glass_age", existingFileHelper);

        // Spring Into Action – begin the compression line
        AdvancementHolder springLoaded = Advancement.Builder.advancement()
                .parent(root)
                .display(new DisplayInfo(
                        new ItemStack(TrueSteamItems.CompressionSpringPack.get()),
                        Component.translatable("advancements.gttruesteam.spring_into_action.title"),
                        Component.translatable("advancements.gttruesteam.spring_into_action.description"),
                        null, FrameType.TASK, true, true, false))
                .addCriterion("has_compression_spring_pack",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                TrueSteamItems.CompressionSpringPack.get()))
                .save(writer, GTTrueSteam.MOD_ID + ":spring_into_action", existingFileHelper);

        // Under Pressure – fully compress a spring pack (goal)
        AdvancementHolder underPressure = Advancement.Builder.advancement()
                .parent(springLoaded)
                .display(new DisplayInfo(
                        new ItemStack(TrueSteamItems.CompressedCompressionSpringPack.get()),
                        Component.translatable("advancements.gttruesteam.under_pressure.title"),
                        Component.translatable("advancements.gttruesteam.under_pressure.description"),
                        null, FrameType.GOAL, true, true, false))
                .addCriterion("has_compressed_spring_pack",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                TrueSteamItems.CompressedCompressionSpringPack.get()))
                .save(writer, GTTrueSteam.MOD_ID + ":under_pressure", existingFileHelper);

        // Infused Mastery – obtain an infused compressed spring pack (challenge)
        Advancement.Builder.advancement()
                .parent(underPressure)
                .display(new DisplayInfo(
                        new ItemStack(TrueSteamItems.InfusedCompressedCompressionSpringPack.get()),
                        Component.translatable("advancements.gttruesteam.infused_mastery.title"),
                        Component.translatable("advancements.gttruesteam.infused_mastery.description"),
                        null, FrameType.CHALLENGE, true, true, false))
                .addCriterion("has_infused_compressed_spring_pack",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                TrueSteamItems.InfusedCompressedCompressionSpringPack.get()))
                .save(writer, GTTrueSteam.MOD_ID + ":infused_mastery", existingFileHelper);

        // Hellish Ambitions – craft the Infernal Alloy Casing to start the infernal path
        AdvancementHolder hellishAmbitions = Advancement.Builder.advancement()
                .parent(root)
                .display(new DisplayInfo(
                        new ItemStack(TrueSteamBlocks.InfernalAlloyCasing.get()),
                        Component.translatable("advancements.gttruesteam.hellish_ambitions.title"),
                        Component.translatable("advancements.gttruesteam.hellish_ambitions.description"),
                        null, FrameType.TASK, true, true, false))
                .addCriterion("has_infernal_alloy_casing",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                TrueSteamBlocks.InfernalAlloyCasing.get().asItem()))
                .save(writer, GTTrueSteam.MOD_ID + ":hellish_ambitions", existingFileHelper);

        // Heart of the Boiler – obtain the Beating Boiler Husk upgrade
        Advancement.Builder.advancement()
                .parent(hellishAmbitions)
                .display(new DisplayInfo(
                        new ItemStack(TrueSteamBlocks.BeatingBoilerHusk.get()),
                        Component.translatable("advancements.gttruesteam.heart_of_the_boiler.title"),
                        Component.translatable("advancements.gttruesteam.heart_of_the_boiler.description"),
                        null, FrameType.TASK, true, true, false))
                .addCriterion("has_beating_boiler_husk",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                TrueSteamBlocks.BeatingBoilerHusk.get().asItem()))
                .save(writer, GTTrueSteam.MOD_ID + ":heart_of_the_boiler", existingFileHelper);

        // Chill Factor – craft the Frost Overproofed Casing for the cooling path
        Advancement.Builder.advancement()
                .parent(hellishAmbitions)
                .display(new DisplayInfo(
                        new ItemStack(TrueSteamBlocks.FrostOverproofedCasing.get()),
                        Component.translatable("advancements.gttruesteam.chill_factor.title"),
                        Component.translatable("advancements.gttruesteam.chill_factor.description"),
                        null, FrameType.TASK, true, true, false))
                .addCriterion("has_frost_casing",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                TrueSteamBlocks.FrostOverproofedCasing.get().asItem()))
                .save(writer, GTTrueSteam.MOD_ID + ":chill_factor", existingFileHelper);

        // Circuit Forged in Fire – obtain the Infernal Circuit (goal)
        Advancement.Builder.advancement()
                .parent(hellishAmbitions)
                .display(new DisplayInfo(
                        new ItemStack(TrueSteamItems.InfernalCircuit.get()),
                        Component.translatable("advancements.gttruesteam.circuit_forged_in_fire.title"),
                        Component.translatable("advancements.gttruesteam.circuit_forged_in_fire.description"),
                        null, FrameType.GOAL, true, true, false))
                .addCriterion("has_infernal_circuit",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                TrueSteamItems.InfernalCircuit.get()))
                .save(writer, GTTrueSteam.MOD_ID + ":circuit_forged_in_fire", existingFileHelper);

        // Conceptualized Steel – craft Conceptualized Steel Solid Casing (challenge)
        Advancement.Builder.advancement()
                .parent(hellishAmbitions)
                .display(new DisplayInfo(
                        new ItemStack(TrueSteamBlocks.ConceptualizedSteelSolidCasing.get()),
                        Component.translatable("advancements.gttruesteam.conceptualized_steel.title"),
                        Component.translatable("advancements.gttruesteam.conceptualized_steel.description"),
                        null, FrameType.CHALLENGE, true, true, false))
                .addCriterion("has_conceptualized_steel_casing",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                TrueSteamBlocks.ConceptualizedSteelSolidCasing.get().asItem()))
                .save(writer, GTTrueSteam.MOD_ID + ":conceptualized_steel", existingFileHelper);
    }
}

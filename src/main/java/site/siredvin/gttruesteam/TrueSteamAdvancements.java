package site.siredvin.gttruesteam;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateAdvancementProvider;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.resources.ResourceLocation;

import site.siredvin.gttruesteam.machines.cim.ConceptInfusionMatrix;
import site.siredvin.gttruesteam.machines.coating_shrine.CoatingShrine;
import site.siredvin.gttruesteam.machines.cooling_box.CoolingBox;
import site.siredvin.gttruesteam.machines.industrial_gas_pressurizer.IndustrialGasPressurizer;
import site.siredvin.gttruesteam.machines.industrial_heater.InfernalBoiler;

public class TrueSteamAdvancements {

    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("minecraft",
            "textures/gui/advancements/backgrounds/stone.png");

    public static void init() {
        GTTrueSteam.REGISTRATE.addDataGenerator(ProviderType.ADVANCEMENT, TrueSteamAdvancements::generate);
    }

    private static void generate(RegistrateAdvancementProvider provider) {
        // Tab root - defines the advancement tab for this mod
        Advancement root = Advancement.Builder.advancement()
                .display(
                        CoatingShrine.MACHINE.getItem().getDefaultInstance(),
                        provider.title(GTTrueSteam.MOD_ID, "root", "True Age of Steam"),
                        provider.desc(GTTrueSteam.MOD_ID, "root",
                                "Delve into the secrets of infernal steam engineering"),
                        BACKGROUND,
                        FrameType.TASK,
                        false, false, false)
                .addCriterion("tick", InventoryChangeTrigger.TriggerInstance.hasItems(new net.minecraft.world.level.ItemLike[0]))
                .save(provider, GTTrueSteam.id("root").toString());

        // Husk of the Boiler
        Advancement husk = Advancement.Builder.advancement()
                .parent(root)
                .display(
                        TrueSteamBlocks.BoilerHusk.asStack(),
                        provider.title(GTTrueSteam.MOD_ID, "husk_of_the_boiler", "Husk of the Boiler"),
                        provider.desc(GTTrueSteam.MOD_ID, "husk_of_the_boiler",
                                "The empty shell of a boiler, waiting to be repurposed"),
                        null,
                        FrameType.TASK,
                        true, true, false)
                .addCriterion("has_boiler_husk",
                        InventoryChangeTrigger.TriggerInstance.hasItems(TrueSteamBlocks.BoilerHusk))
                .save(provider, GTTrueSteam.id("husk_of_the_boiler").toString());

        // Coating Shrine - the gateway to coated materials
        Advancement coatingShrine = Advancement.Builder.advancement()
                .parent(husk)
                .display(
                        CoatingShrine.MACHINE.getItem().getDefaultInstance(),
                        provider.title(GTTrueSteam.MOD_ID, "coating_shrine", "Shrine of Coating"),
                        provider.desc(GTTrueSteam.MOD_ID, "coating_shrine",
                                "Build a Coating Shrine to imbue materials with fluid properties"),
                        null,
                        FrameType.GOAL,
                        true, true, false)
                .addCriterion("has_coating_shrine",
                        InventoryChangeTrigger.TriggerInstance.hasItems(CoatingShrine.MACHINE.getItem()))
                .save(provider, GTTrueSteam.id("coating_shrine").toString());

        // Lava coating: Steel → Lava Coated Steel
        Advancement lavaCoated = Advancement.Builder.advancement()
                .parent(coatingShrine)
                .display(
                        ChemicalHelper.get(TagPrefix.ingot, TrueSteamMaterials.LavaCoatedSteel),
                        provider.title(GTTrueSteam.MOD_ID, "lava_coated", "Lava Coated"),
                        provider.desc(GTTrueSteam.MOD_ID, "lava_coated",
                                "Coat steel with lava to forge a resilient new alloy"),
                        null,
                        FrameType.TASK,
                        true, true, false)
                .addCriterion("has_lava_coated_steel",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                ChemicalHelper.get(TagPrefix.ingot, TrueSteamMaterials.LavaCoatedSteel).getItem()))
                .save(provider, GTTrueSteam.id("lava_coated").toString());

        // Any of the three LC single-block boilers
        Advancement.Builder.advancement()
                .parent(lavaCoated)
                .display(
                        TrueSteamMachines.SOLID.getFirst().getItem().getDefaultInstance(),
                        provider.title(GTTrueSteam.MOD_ID, "lc_boiler", "Lava Coated Boiler"),
                        provider.desc(GTTrueSteam.MOD_ID, "lc_boiler",
                                "Upgrade a steam boiler with Lava Coated Steel"),
                        null,
                        FrameType.TASK,
                        true, true, false)
                .addCriterion("has_lc_solid",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                TrueSteamMachines.SOLID.getFirst().getItem()))
                .addCriterion("has_lc_liquid",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                TrueSteamMachines.LIQUID.getFirst().getItem()))
                .addCriterion("has_lc_solar",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                TrueSteamMachines.SOLAR.getFirst().getItem()))
                .requirements(RequirementsStrategy.OR)
                .save(provider, GTTrueSteam.id("lc_boiler").toString());

        // Blaze coating: Aluminium Bronze → Infernal Alloy
        Advancement infernalAlloy = Advancement.Builder.advancement()
                .parent(lavaCoated)
                .display(
                        ChemicalHelper.get(TagPrefix.ingot, TrueSteamMaterials.InfernalAlloy),
                        provider.title(GTTrueSteam.MOD_ID, "infernal_alloy", "Infernal Alloy"),
                        provider.desc(GTTrueSteam.MOD_ID, "infernal_alloy",
                                "Bathe aluminium bronze in liquid blaze to forge the ultimate infernal material"),
                        null,
                        FrameType.TASK,
                        true, true, false)
                .addCriterion("has_infernal_alloy",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                ChemicalHelper.get(TagPrefix.ingot, TrueSteamMaterials.InfernalAlloy).getItem()))
                .save(provider, GTTrueSteam.id("infernal_alloy").toString());

        // Infernal Boiler multiblock
        Advancement infernalBoiler = Advancement.Builder.advancement()
                .parent(infernalAlloy)
                .display(
                        InfernalBoiler.MACHINE.getItem().getDefaultInstance(),
                        provider.title(GTTrueSteam.MOD_ID, "infernal_boiler", "Infernal Engineering"),
                        provider.desc(GTTrueSteam.MOD_ID, "infernal_boiler",
                                "Assemble the Infernal Boiler multiblock to generate superhot steam"),
                        null,
                        FrameType.GOAL,
                        true, true, false)
                .addCriterion("has_infernal_boiler",
                        InventoryChangeTrigger.TriggerInstance.hasItems(InfernalBoiler.MACHINE.getItem()))
                .save(provider, GTTrueSteam.id("infernal_boiler").toString());

        // First infernal maintain cycle
        Advancement firstMaintain = Advancement.Builder.advancement()
                .parent(infernalBoiler)
                .display(
                        InfernalBoiler.MACHINE.getItem().getDefaultInstance(),
                        provider.title(GTTrueSteam.MOD_ID, "first_maintain", "Feed the Beast"),
                        provider.desc(GTTrueSteam.MOD_ID, "first_maintain",
                                "Perform a maintenance recipe to restore infernal boiler charges"),
                        null,
                        FrameType.GOAL,
                        true, true, false)
                .addCriterion("first_maintain",
                        TrueSteamCriteria.INFERNAL_MAINTENANCE.atLeast(1))
                .save(provider, GTTrueSteam.id("first_maintain").toString());

        // 100 infernal maintain cycles
        Advancement.Builder.advancement()
                .parent(firstMaintain)
                .display(
                        InfernalBoiler.MACHINE.getItem().getDefaultInstance(),
                        provider.title(GTTrueSteam.MOD_ID, "hundred_maintains", "Loyal Servant"),
                        provider.desc(GTTrueSteam.MOD_ID, "hundred_maintains",
                                "Perform 100 maintenance recipes to keep the infernal boiler charged"),
                        null,
                        FrameType.CHALLENGE,
                        true, true, false)
                .addCriterion("hundred_maintains",
                        TrueSteamCriteria.INFERNAL_MAINTENANCE.atLeast(100))
                .save(provider, GTTrueSteam.id("hundred_maintains").toString());

        // Purified Infernal Dust - end of infernal processing chain
        Advancement purifiedDust = Advancement.Builder.advancement()
                .parent(infernalBoiler)
                .display(
                        TrueSteamItems.PurifiedInfernalDust.asStack(),
                        provider.title(GTTrueSteam.MOD_ID, "purified_infernal_dust", "Purified Infernal Dust"),
                        provider.desc(GTTrueSteam.MOD_ID, "purified_infernal_dust",
                                "Scan infernal slug to obtain purified infernal dust"),
                        null,
                        FrameType.TASK,
                        true, true, false)
                .addCriterion("has_purified_infernal_dust",
                        InventoryChangeTrigger.TriggerInstance.hasItems(TrueSteamItems.PurifiedInfernalDust))
                .save(provider, GTTrueSteam.id("purified_infernal_dust").toString());

        // Conceptualized Steel - child of purified dust
        Advancement conceptualizedSteel = Advancement.Builder.advancement()
                .parent(purifiedDust)
                .display(
                        ChemicalHelper.get(TagPrefix.ingot, TrueSteamMaterials.ConceptualizedSteel),
                        provider.title(GTTrueSteam.MOD_ID, "conceptualized_steel", "Conceptualized Steel"),
                        provider.desc(GTTrueSteam.MOD_ID, "conceptualized_steel",
                                "Mix purified infernal dust into a steel that transcends material limits"),
                        null,
                        FrameType.GOAL,
                        true, true, false)
                .addCriterion("has_conceptualized_steel",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                ChemicalHelper.get(TagPrefix.ingot, TrueSteamMaterials.ConceptualizedSteel).getItem()))
                .save(provider, GTTrueSteam.id("conceptualized_steel").toString());

        // Concept Infusion Matrix - child of conceptualized steel
        Advancement cim = Advancement.Builder.advancement()
                .parent(conceptualizedSteel)
                .display(
                        ConceptInfusionMatrix.MACHINE.getItem().getDefaultInstance(),
                        provider.title(GTTrueSteam.MOD_ID, "concept_infusion_matrix", "Concept Infusion Matrix"),
                        provider.desc(GTTrueSteam.MOD_ID, "concept_infusion_matrix",
                                "Assemble the Concept Infusion Matrix to materialize abstract concepts into catalysts"),
                        null,
                        FrameType.GOAL,
                        true, true, false)
                .addCriterion("has_cim",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ConceptInfusionMatrix.MACHINE.getItem()))
                .save(provider, GTTrueSteam.id("concept_infusion_matrix").toString());

        // Concept catalyst challenges - one per RecipeConcept
        Advancement.Builder.advancement()
                .parent(cim)
                .display(
                        TrueSteamConcepts.ExtractionConcept.getCatalysts().get(0).asStack(),
                        provider.title(GTTrueSteam.MOD_ID, "extraction_concept", "Concept of Extraction"),
                        provider.desc(GTTrueSteam.MOD_ID, "extraction_concept",
                                "Infuse the concept of extraction into a catalyst"),
                        null,
                        FrameType.CHALLENGE,
                        true, true, false)
                .addCriterion("has_extraction_catalyst",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                TrueSteamConcepts.ExtractionConcept.getCatalysts().get(0)))
                .save(provider, GTTrueSteam.id("extraction_concept").toString());

        Advancement compressionConcept = Advancement.Builder.advancement()
                .parent(cim)
                .display(
                        TrueSteamConcepts.CompressionConcept.getCatalysts().get(0).asStack(),
                        provider.title(GTTrueSteam.MOD_ID, "compression_concept", "Concept of Compression"),
                        provider.desc(GTTrueSteam.MOD_ID, "compression_concept",
                                "Infuse the concept of compression into a catalyst"),
                        null,
                        FrameType.CHALLENGE,
                        true, true, false)
                .addCriterion("has_compression_catalyst",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                TrueSteamConcepts.CompressionConcept.getCatalysts().get(0)))
                .save(provider, GTTrueSteam.id("compression_concept").toString());

        // Industrial Gas Pressurizer - child of compression concept
        Advancement igp = Advancement.Builder.advancement()
                .parent(compressionConcept)
                .display(
                        IndustrialGasPressurizer.MACHINE.getItem().getDefaultInstance(),
                        provider.title(GTTrueSteam.MOD_ID, "industrial_gas_pressurizer",
                                "Industrial Gas Pressurizer"),
                        provider.desc(GTTrueSteam.MOD_ID, "industrial_gas_pressurizer",
                                "Assemble the Industrial Gas Pressurizer to compress gases into their dense variants"),
                        null,
                        FrameType.GOAL,
                        true, true, false)
                .addCriterion("has_igp",
                        InventoryChangeTrigger.TriggerInstance.hasItems(IndustrialGasPressurizer.MACHINE.getItem()))
                .save(provider, GTTrueSteam.id("industrial_gas_pressurizer").toString());

        // First perfect condition recipe
        Advancement firstPerfectCondition = Advancement.Builder.advancement()
                .parent(igp)
                .display(
                        IndustrialGasPressurizer.MACHINE.getItem().getDefaultInstance(),
                        provider.title(GTTrueSteam.MOD_ID, "first_perfect_condition", "Perfectly Balanced"),
                        provider.desc(GTTrueSteam.MOD_ID, "first_perfect_condition",
                                "Complete a recipe in perfect condition in the Industrial Gas Pressurizer"),
                        null,
                        FrameType.GOAL,
                        true, true, false)
                .addCriterion("first_perfect_condition",
                        TrueSteamCriteria.PERFECT_CONDITION.atLeast(1))
                .save(provider, GTTrueSteam.id("first_perfect_condition").toString());

        // 100 perfect condition recipes
        Advancement.Builder.advancement()
                .parent(firstPerfectCondition)
                .display(
                        IndustrialGasPressurizer.MACHINE.getItem().getDefaultInstance(),
                        provider.title(GTTrueSteam.MOD_ID, "hundred_perfect_conditions",
                                "Absolute Precision"),
                        provider.desc(GTTrueSteam.MOD_ID, "hundred_perfect_conditions",
                                "Complete 100 recipes in perfect condition in the Industrial Gas Pressurizer"),
                        null,
                        FrameType.CHALLENGE,
                        true, true, false)
                .addCriterion("hundred_perfect_conditions",
                        TrueSteamCriteria.PERFECT_CONDITION.atLeast(100))
                .save(provider, GTTrueSteam.id("hundred_perfect_conditions").toString());

        Advancement.Builder.advancement()
                .parent(cim)
                .display(
                        TrueSteamConcepts.HeatingConcept.getCatalysts().get(0).asStack(),
                        provider.title(GTTrueSteam.MOD_ID, "heating_concept", "Concept of Heating"),
                        provider.desc(GTTrueSteam.MOD_ID, "heating_concept",
                                "Infuse the concept of heating into a catalyst"),
                        null,
                        FrameType.CHALLENGE,
                        true, true, false)
                .addCriterion("has_heating_catalyst",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                TrueSteamConcepts.HeatingConcept.getCatalysts().get(0)))
                .save(provider, GTTrueSteam.id("heating_concept").toString());

        Advancement.Builder.advancement()
                .parent(cim)
                .display(
                        TrueSteamConcepts.PolarizationConcept.getCatalysts().get(0).asStack(),
                        provider.title(GTTrueSteam.MOD_ID, "polarization_concept", "Concept of Polarization"),
                        provider.desc(GTTrueSteam.MOD_ID, "polarization_concept",
                                "Infuse the concept of polarization into a catalyst"),
                        null,
                        FrameType.CHALLENGE,
                        true, true, false)
                .addCriterion("has_polarization_catalyst",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                TrueSteamConcepts.PolarizationConcept.getCatalysts().get(0)))
                .save(provider, GTTrueSteam.id("polarization_concept").toString());

        Advancement steamConcept = Advancement.Builder.advancement()
                .parent(cim)
                .display(
                        TrueSteamConcepts.SteamConcept.getCatalysts().get(0).asStack(),
                        provider.title(GTTrueSteam.MOD_ID, "steam_concept", "Concept of Steam"),
                        provider.desc(GTTrueSteam.MOD_ID, "steam_concept",
                                "Infuse the concept of steam into a catalyst"),
                        null,
                        FrameType.CHALLENGE,
                        true, true, false)
                .addCriterion("has_steam_catalyst",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                TrueSteamConcepts.SteamConcept.getCatalysts().get(0)))
                .save(provider, GTTrueSteam.id("steam_concept").toString());

        // 20 beating husks in inventory - upgrade for infernal boiler
        Advancement.Builder.advancement()
                .parent(steamConcept)
                .display(
                        TrueSteamBlocks.BeatingBoilerHusk.asStack(),
                        provider.title(GTTrueSteam.MOD_ID, "beating_husks", "Heart of the Boiler"),
                        provider.desc(GTTrueSteam.MOD_ID, "beating_husks",
                                "Collect 20 Beating Husks to fully upgrade the Infernal Boiler"),
                        null,
                        FrameType.CHALLENGE,
                        true, true, false)
                .addCriterion("has_20_beating_husks",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                ItemPredicate.Builder.item()
                                        .of(TrueSteamBlocks.BeatingBoilerHusk)
                                        .withCount(MinMaxBounds.Ints.atLeast(20))
                                        .build()))
                .save(provider, GTTrueSteam.id("beating_husks").toString());

        // Infernal Circuit - child of purified dust
        Advancement.Builder.advancement()
                .parent(purifiedDust)
                .display(
                        TrueSteamItems.InfernalCircuit.asStack(),
                        provider.title(GTTrueSteam.MOD_ID, "infernal_circuit", "Circuit Burns"),
                        provider.desc(GTTrueSteam.MOD_ID, "infernal_circuit",
                                "Forge an Infernal Circuit from purified infernal components"),
                        null,
                        FrameType.CHALLENGE,
                        true, true, false)
                .addCriterion("has_infernal_circuit",
                        InventoryChangeTrigger.TriggerInstance.hasItems(TrueSteamItems.InfernalCircuit))
                .save(provider, GTTrueSteam.id("infernal_circuit").toString());

        // Water coating: Cobalt Brass → Corrosion Tempered Brass
        Advancement corrosionTempered = Advancement.Builder.advancement()
                .parent(coatingShrine)
                .display(
                        ChemicalHelper.get(TagPrefix.ingot, TrueSteamMaterials.CorrosionTemperedBrass),
                        provider.title(GTTrueSteam.MOD_ID, "corrosion_tempered", "Corrosion Tempered"),
                        provider.desc(GTTrueSteam.MOD_ID, "corrosion_tempered",
                                "Temper cobalt brass with water to resist the harshest conditions"),
                        null,
                        FrameType.TASK,
                        true, true, false)
                .addCriterion("has_corrosion_tempered_brass",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                ChemicalHelper.get(TagPrefix.ingot, TrueSteamMaterials.CorrosionTemperedBrass)
                                        .getItem()))
                .save(provider, GTTrueSteam.id("corrosion_tempered").toString());

        // Cooling Box multiblock
        Advancement.Builder.advancement()
                .parent(corrosionTempered)
                .display(
                        CoolingBox.MACHINE.getItem().getDefaultInstance(),
                        provider.title(GTTrueSteam.MOD_ID, "cooling_box", "Cooling Box"),
                        provider.desc(GTTrueSteam.MOD_ID, "cooling_box",
                                "Assemble the Cooling Box multiblock to harness the power of cold"),
                        null,
                        FrameType.GOAL,
                        true, true, false)
                .addCriterion("has_cooling_box",
                        InventoryChangeTrigger.TriggerInstance.hasItems(CoolingBox.MACHINE.getItem()))
                .save(provider, GTTrueSteam.id("cooling_box").toString());

        // Ice coating: Magnalium → Frostbite Magnalium
        Advancement frostbitten = Advancement.Builder.advancement()
                .parent(coatingShrine)
                .display(
                        ChemicalHelper.get(TagPrefix.ingot, TrueSteamMaterials.FrostbiteMagnalium),
                        provider.title(GTTrueSteam.MOD_ID, "frostbitten", "Frostbitten"),
                        provider.desc(GTTrueSteam.MOD_ID, "frostbitten",
                                "Infuse magnalium with ice to create a cryogenically enhanced alloy"),
                        null,
                        FrameType.TASK,
                        true, true, false)
                .addCriterion("has_frostbite_magnalium",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                ChemicalHelper.get(TagPrefix.ingot, TrueSteamMaterials.FrostbiteMagnalium).getItem()))
                .save(provider, GTTrueSteam.id("frostbitten").toString());

        // Cooling Coils - any of the three
        Advancement.Builder.advancement()
                .parent(frostbitten)
                .display(
                        TrueSteamBlocks.COIL_FROSTBITE_MAGNALIUM.asStack(),
                        provider.title(GTTrueSteam.MOD_ID, "cooling_coil", "Cooling Coil"),
                        provider.desc(GTTrueSteam.MOD_ID, "cooling_coil",
                                "Craft a cooling coil to accumulate and discharge cooling capacity"),
                        null,
                        FrameType.TASK,
                        true, true, false)
                .addCriterion("has_frostbite_magnalium_coil",
                        InventoryChangeTrigger.TriggerInstance.hasItems(TrueSteamBlocks.COIL_FROSTBITE_MAGNALIUM))
                .addCriterion("has_cooling_cometal_coil",
                        InventoryChangeTrigger.TriggerInstance.hasItems(TrueSteamBlocks.COIL_COOLING_COMETAL))
                .addCriterion("has_estranged_steel_coil",
                        InventoryChangeTrigger.TriggerInstance.hasItems(TrueSteamBlocks.COIL_ESTRANGED_STEEL))
                .requirements(RequirementsStrategy.OR)
                .save(provider, GTTrueSteam.id("cooling_coil").toString());
    }
}

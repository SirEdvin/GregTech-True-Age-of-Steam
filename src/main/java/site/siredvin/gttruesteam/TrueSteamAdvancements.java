package site.siredvin.gttruesteam;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateAdvancementProvider;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.resources.ResourceLocation;

import site.siredvin.gttruesteam.machines.coating_shrine.CoatingShrine;
import site.siredvin.gttruesteam.machines.cooling_box.CoolingBox;
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
                        false, false, true)
                .addCriterion("impossible", new ImpossibleTrigger.TriggerInstance())
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
        Advancement.Builder.advancement()
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

package site.siredvin.gttruesteam;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import com.tterrag.registrate.util.entry.BlockEntry;
import site.siredvin.gttruesteam.machines.cim.ConceptInfusionMatrix;
import site.siredvin.gttruesteam.machines.coating_shrine.CoatingShrine;
import site.siredvin.gttruesteam.machines.cooling_box.CoolingBox;
import site.siredvin.gttruesteam.machines.industrial_gas_pressurizer.IndustrialGasPressurizer;
import site.siredvin.gttruesteam.machines.industrial_heater.InfernalBoiler;
import site.siredvin.gttruesteam.machines.regulated_cryo_chamber.RegulatedCryoChamber;
import site.siredvin.gttruesteam.recipe.condition.CoatingFluidCondition;
import site.siredvin.gttruesteam.recipe.condition.CoolingCapacityCondition;

import java.util.List;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;
import static site.siredvin.gttruesteam.TrueSteamRecipeTypes.FLUID_COOLING;
import static site.siredvin.gttruesteam.TrueSteamRecipeTypes.METAPHYSICAL_BOILING;

public class TrueSteamRecipes {

    private static void casingRecipe(Material casingMaterial, BlockEntry<Block> casingOutput,
                                     Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, casingOutput.getId(), casingOutput.asStack(),
                "PhP",
                "PFP",
                "PwP",
                'P', new MaterialEntry(TagPrefix.plate, casingMaterial),
                'F', new MaterialEntry(TagPrefix.frameGt, casingMaterial));

        ASSEMBLER_RECIPES.recipeBuilder(casingOutput.getId())
                .inputItems(TagPrefix.frameGt, casingMaterial, 1)
                .inputItems(TagPrefix.plate, casingMaterial, 6)
                .outputItems(casingOutput)
                .duration(200)
                .EUt(16)
                .circuitMeta(1)
                .save(provider);
    }

    private static void pipeCasingRecipe(Material casingMaterial, BlockEntry<Block> casingOutput,
                                         Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, casingOutput.getId(), casingOutput.asStack(),
                "P|P", "|F|", "P|P", 'P', new MaterialEntry(TagPrefix.plate, casingMaterial),
                'F', new MaterialEntry(TagPrefix.frameGt, casingMaterial), '|',
                new MaterialEntry(TagPrefix.pipeNormalFluid, casingMaterial));
    }

    private static void registerInfernalActivationRecipes(Consumer<FinishedRecipe> provider, Material material,
                                                          Material source) {
        MIXER_RECIPES.recipeBuilder(material.getResourceLocation())
                .inputFluids(source.getFluid(1000), GTMaterials.NetherAir.getFluid(3000))
                .inputItems(TagPrefix.dust, GTMaterials.Redstone)
                .outputFluids(material.getFluid(4000))
                .EUt(60)
                .duration(200)
                .save(provider);
        MIXER_RECIPES.recipeBuilder(material.getResourceLocation().withSuffix("_slug"))
                .inputFluids(source.getFluid(1000), GTMaterials.NetherAir.getFluid(3000))
                .inputItems(TagPrefix.dust, TrueSteamMaterials.InfernalSlug)
                .outputFluids(material.getFluid(4000))
                .circuitMeta(1)
                .EUt(60)
                .duration(200)
                .save(provider);
    }

    public static void registerInfernalChargingLoop(Consumer<FinishedRecipe> provider) {
        registerInfernalActivationRecipes(provider, TrueSteamMaterials.ActivatedBlaze, GTMaterials.Blaze);
        registerInfernalActivationRecipes(provider, TrueSteamMaterials.ActivatedInfernalTar,
                TrueSteamMaterials.InfernalTar);
        METAPHYSICAL_BOILING.recipeBuilder(TrueSteamMaterials.OverheatedInfernalSlurry.getResourceLocation())
                .inputFluids(TrueSteamMaterials.ActivatedBlaze.getFluid(8000))
                .outputFluids(TrueSteamMaterials.OverheatedInfernalSlurry.getFluid(8000))
                .EUt(8)
                .circuitMeta(2)
                .addData(TrueSteamRecipeTypes.INFERNAL_CYCLES_DATA_KEY, 512)
                .duration(1000)
                .save(provider);
        METAPHYSICAL_BOILING.recipeBuilder(TrueSteamMaterials.OverheatedInfernalSludge.getResourceLocation())
                .inputFluids(TrueSteamMaterials.ActivatedInfernalTar.getFluid(8000))
                .outputFluids(TrueSteamMaterials.OverheatedInfernalSludge.getFluid(8000))
                .EUt(8)
                .circuitMeta(2)
                .addData(TrueSteamRecipeTypes.INFERNAL_CYCLES_DATA_KEY, 256)
                .duration(1000)
                .save(provider);
        METAPHYSICAL_BOILING.recipeBuilder(TrueSteamMaterials.OverheatedInfernalEmulsion.getResourceLocation())
                .inputFluids(TrueSteamMaterials.OverchargedBlaze.getFluid(8000))
                .outputFluids(TrueSteamMaterials.OverheatedInfernalEmulsion.getFluid(8000))
                .EUt(8)
                .circuitMeta(2)
                .addData(TrueSteamRecipeTypes.INFERNAL_CYCLES_DATA_KEY, 1024)
                .duration(1000)
                .save(provider);
        FLUID_COOLING.recipeBuilder(TrueSteamMaterials.InfernalSlurry.getResourceLocation())
                .inputFluids(TrueSteamMaterials.OverheatedInfernalSlurry.getFluid(4000))
                .outputFluids(TrueSteamMaterials.InfernalSlurry.getFluid(4000))
                .addData(TrueSteamRecipeTypes.COOLING_CONSUMED, 500)
                .addCondition(new CoolingCapacityCondition(5000))
                .duration(200)
                .save(provider);
        FLUID_COOLING.recipeBuilder(TrueSteamMaterials.InfernalSludge.getResourceLocation())
                .inputFluids(TrueSteamMaterials.OverheatedInfernalSludge.getFluid(4000))
                .outputFluids(TrueSteamMaterials.InfernalSludge.getFluid(4000))
                .addData(TrueSteamRecipeTypes.COOLING_CONSUMED, 500)
                .addCondition(new CoolingCapacityCondition(5000))
                .duration(200)
                .save(provider);
        FLUID_COOLING.recipeBuilder(TrueSteamMaterials.InfernalEmulsion.getResourceLocation())
                .inputFluids(TrueSteamMaterials.OverheatedInfernalEmulsion.getFluid(4000))
                .outputFluids(TrueSteamMaterials.InfernalEmulsion.getFluid(4000))
                .addData(TrueSteamRecipeTypes.COOLING_CONSUMED, 500)
                .addCondition(new CoolingCapacityCondition(5000))
                .duration(200)
                .save(provider);
        FLUID_COOLING.recipeBuilder(GTTrueSteam.id("cooling_water_into_ice"))
                .inputFluids(GTMaterials.Water.getFluid(1000))
                .outputFluids(GTMaterials.Ice.getFluid(1000))
                .addData(TrueSteamRecipeTypes.COOLING_CONSUMED, 250)
                .addCondition(new CoolingCapacityCondition(2000))
                .duration(200)
                .save(provider);
        CENTRIFUGE_RECIPES.recipeBuilder(GTTrueSteam.id("infernal_slurry_separation"))
                .inputFluids(TrueSteamMaterials.InfernalSlurry.getFluid(1000))
                .outputFluids(TrueSteamMaterials.DilutedBlaze.getFluid(250))
                .outputItems(TagPrefix.dust, TrueSteamMaterials.InfernalSlug, 2)
                .EUt(60)
                .duration(100)
                .save(provider);
        CENTRIFUGE_RECIPES.recipeBuilder(GTTrueSteam.id("infernal_sludge_separation"))
                .inputFluids(TrueSteamMaterials.InfernalSludge.getFluid(1000))
                .outputItems(TagPrefix.dust, TrueSteamMaterials.InfernalSlug, 2)
                .EUt(60)
                .duration(100)
                .save(provider);
        DISTILLERY_RECIPES.recipeBuilder(GTTrueSteam.id("diluted_blaze_into_blaze"))
                .inputFluids(TrueSteamMaterials.DilutedBlaze.getFluid(1000))
                .outputFluids(GTMaterials.Blaze.getFluid(950))
                .circuitMeta(1)
                .EUt(30)
                .duration(24)
                .save(provider);
        DISTILLERY_RECIPES.recipeBuilder(GTTrueSteam.id("diluted_blaze_into_nether_air"))
                .inputFluids(TrueSteamMaterials.DilutedBlaze.getFluid(1000))
                .outputFluids(GTMaterials.NetherAir.getFluid(950))
                .circuitMeta(2)
                .EUt(30)
                .duration(48)
                .save(provider);
        MIXER_RECIPES.recipeBuilder(TrueSteamMaterials.OverchargedBlaze.getResourceLocation())
                .inputFluids(TrueSteamMaterials.ActivatedBlaze.getFluid(2000))
                .inputItems(TrueSteamItems.PurifiedInfernalDust)
                .notConsumable(TrueSteamConcepts.HeatingConcept.getCatalysts().get(0))
                .outputFluids(TrueSteamMaterials.OverchargedBlaze.getFluid(2000))
                .duration(200).EUt(VH[HV]).save(provider);
        MIXER_RECIPES.recipeBuilder(TrueSteamMaterials.OverchargedBlaze.getResourceLocation().withSuffix("_recycle"))
                .inputFluids(TrueSteamMaterials.InfernalEmulsion.getFluid(2000))
                .chancedInput(TrueSteamItems.PurifiedInfernalDust.asStack(), 500, 0)
                .notConsumable(TrueSteamConcepts.ExtractionConcept.getCatalysts().get(0))
                .outputItems(TagPrefix.dust, TrueSteamMaterials.InfernalSlug, 2)
                .outputFluids(TrueSteamMaterials.OverchargedBlaze.getFluid(2000))
                .duration(200).EUt(VH[HV]).save(provider);
    }

    private static void generateBoilerRecipe(MachineDefinition boiler, List<MachineDefinition> next,
                                             Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, next.get(0).getId(), next.get(0).asStack(),
                "PPP",
                "PwP",
                " B ",
                'B', boiler.asStack(),
                'P', new MaterialEntry(TagPrefix.plate, TrueSteamMaterials.LavaCoatedSteel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, next.get(1).getId(), next.get(1).asStack(),
                "PPP",
                "PwP",
                " B ",
                'B', next.get(0).asStack(),
                'P', new MaterialEntry(TagPrefix.plate, TrueSteamMaterials.InfernalAlloy));
        VanillaRecipeHelper.addShapedRecipe(provider, true, next.get(2).getId(), next.get(2).asStack(),
                "PPP",
                "PwP",
                " B ",
                'B', next.get(1).asStack(),
                'P', new MaterialEntry(TagPrefix.plate, TrueSteamConcepts.HeatingConcept.getMaterial()));
        ASSEMBLER_RECIPES.recipeBuilder(next.get(0).getId())
                .inputItems(TagPrefix.plate, TrueSteamMaterials.LavaCoatedSteel, 5)
                .inputItems(boiler.asStack()).circuitMeta(4)
                .outputItems(next.get(0).asStack())
                .duration(100).EUt(VH[MV]).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder(next.get(1).getId())
                .inputItems(TagPrefix.plate, TrueSteamMaterials.LavaCoatedSteel, 5)
                .inputItems(next.get(0).asStack()).circuitMeta(4)
                .outputItems(next.get(1).asStack())
                .duration(100).EUt(VH[MV]).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder(next.get(2).getId())
                .inputItems(TagPrefix.plate, TrueSteamMaterials.LavaCoatedSteel, 5)
                .inputItems(next.get(1).asStack()).circuitMeta(4)
                .outputItems(next.get(2).asStack())
                .duration(100).EUt(VH[MV]).save(provider);
    }

    private static void registerBoilerRecipes(Consumer<FinishedRecipe> provider) {
        generateBoilerRecipe(GTMachines.STEAM_SOLAR_BOILER.second(), TrueSteamMachines.SOLAR, provider);
        generateBoilerRecipe(GTMachines.STEAM_LIQUID_BOILER.second(), TrueSteamMachines.LIQUID, provider);
        generateBoilerRecipe(GTMachines.STEAM_SOLID_BOILER.second(), TrueSteamMachines.SOLID, provider);
    }

    private static void registerReEtchingRecipe(String tier, TagKey<Item> tag, int amount,
                                                Consumer<FinishedRecipe> provider) {
        CHEMICAL_BATH_RECIPES.recipeBuilder(GTTrueSteam.id("reetched_from_" + tier))
                .inputItems(tag).inputFluids(GTMaterials.SulfuricAcid.getFluid(100))
                .outputItems(TrueSteamItems.ReEtchedCircuit, amount)
                .EUt(128).duration(100).save(provider);
    }

    private static void registerCoolingCoilsRecipes(Consumer<FinishedRecipe> provider) {
        ASSEMBLER_RECIPES.recipeBuilder(TrueSteamBlocks.COIL_FROSTBITE_MAGNALIUM.getId())
                .inputItems(TagPrefix.foil, TrueSteamMaterials.FrostbiteMagnalium, 8)
                .inputItems(TagPrefix.pipeSmallFluid, GTMaterials.Aluminium, 8)
                .inputFluids(GTMaterials.TinAlloy.getFluid(144))
                .outputItems(TrueSteamBlocks.COIL_FROSTBITE_MAGNALIUM.asStack())
                .duration(200).EUt(30).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(TrueSteamBlocks.COIL_ESTRANGED_STEEL.getId())
                .inputItems(TagPrefix.foil, TrueSteamMaterials.EstrangedSteel, 8)
                .inputItems(TagPrefix.pipeSmallFluid, GTMaterials.Titanium, 8)
                .inputFluids(GTMaterials.TinAlloy.getFluid(144))
                .outputItems(TrueSteamBlocks.COIL_ESTRANGED_STEEL.asStack())
                .duration(200).EUt(30).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(TrueSteamBlocks.COIL_COOLING_COMETAL.getId())
                .inputItems(TagPrefix.foil, TrueSteamConcepts.CoolingConcept.getMaterial(), 8)
                .inputItems(TagPrefix.pipeSmallFluid, GTMaterials.StainlessSteel, 8)
                .inputFluids(GTMaterials.TinAlloy.getFluid(144))
                .outputItems(TrueSteamBlocks.COIL_COOLING_COMETAL.asStack())
                .duration(200).EUt(30).save(provider);
    }

    private static void registerSpringRecipes(Consumer<FinishedRecipe> provider) {
        COMPRESSOR_RECIPES.recipeBuilder(TrueSteamItems.CompressionSpringPack.getId())
                .inputItems(TagPrefix.spring, TrueSteamConcepts.CompressionConcept.getMaterial(), 3)
                .outputItems(TrueSteamItems.CompressionSpringPack).duration(100).EUt(64).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder(TrueSteamItems.SlightlyCompressedCompressionSpringPack.getId())
                .inputItems(TrueSteamItems.InfusedCompressionSpringPack)
                .outputItems(TrueSteamItems.SlightlyCompressedCompressionSpringPack.asStack())
                .duration(100).EUt(64).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder(TrueSteamItems.SomewhatCompressedCompressionSpringPack.getId())
                .inputItems(TrueSteamItems.InfusedSlightlyCompressedCompressionSpringPack)
                .outputItems(TrueSteamItems.SomewhatCompressedCompressionSpringPack.asStack())
                .duration(100).EUt(64).save(provider);

        COMPRESSOR_RECIPES.recipeBuilder(TrueSteamItems.CompressedCompressionSpringPack.getId())
                .inputItems(TrueSteamItems.InfusedSomewhatCompressedCompressionSpringPack)
                .outputItems(TrueSteamItems.CompressedCompressionSpringPack.asStack())
                .duration(100).EUt(64).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder(TrueSteamItems.InfusedCompressionSpringPack.getId())
                .inputItems(TrueSteamItems.CompressionSpringPack)
                .inputFluids(TrueSteamConcepts.CompressionConcept.getInfusedAir().getFluid(100))
                .chancedItemOutputLogic(ChanceLogic.XOR)
                .chancedOutput(TrueSteamItems.InfusedCompressionSpringPack.asStack(), 9000, 0)
                .chancedOutput(TrueSteamItems.InfusedSlightlyCompressedCompressionSpringPack.asStack(), 1000, 0)
                .duration(100).EUt(64).save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder(TrueSteamItems.InfusedSlightlyCompressedCompressionSpringPack.getId())
                .inputItems(TrueSteamItems.SlightlyCompressedCompressionSpringPack)
                .inputFluids(TrueSteamConcepts.CompressionConcept.getInfusedAir().getFluid(100))
                .chancedItemOutputLogic(ChanceLogic.XOR)
                .chancedOutput(TrueSteamItems.InfusedSlightlyCompressedCompressionSpringPack.asStack(), 9000, 0)
                .chancedOutput(TrueSteamItems.InfusedSomewhatCompressedCompressionSpringPack.asStack(), 1000, 0)
                .duration(100).EUt(64).save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder(TrueSteamItems.InfusedSomewhatCompressedCompressionSpringPack.getId())
                .inputItems(TrueSteamItems.SomewhatCompressedCompressionSpringPack)
                .inputFluids(TrueSteamConcepts.CompressionConcept.getInfusedAir().getFluid(100))
                .chancedItemOutputLogic(ChanceLogic.XOR)
                .chancedOutput(TrueSteamItems.InfusedSomewhatCompressedCompressionSpringPack.asStack(), 9000, 0)
                .chancedOutput(TrueSteamItems.InfusedCompressedCompressionSpringPack.asStack(), 1000, 0)
                .duration(100).EUt(64).save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder(TrueSteamItems.InfusedCompressedCompressionSpringPack.getId())
                .inputItems(TrueSteamItems.CompressedCompressionSpringPack)
                .inputFluids(TrueSteamConcepts.CompressionConcept.getInfusedAir().getFluid(100))
                .outputItems(TrueSteamItems.InfusedCompressedCompressionSpringPack)
                .duration(100).EUt(64).save(provider);
    }

    public static void registerRecipes(Consumer<FinishedRecipe> provider) {
        registerInfernalChargingLoop(provider);
        registerBoilerRecipes(provider);

        ALLOY_SMELTER_RECIPES.recipeBuilder(GTTrueSteam.id("bronze_glass"))
                .inputItems(TagPrefix.block, GTMaterials.Glass)
                .inputItems(TagPrefix.ingot, GTMaterials.Bronze)
                .duration(200)
                .EUt(8)
                .save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, true, TrueSteamBlocks.BoilerHusk.getId(),
                TrueSteamBlocks.BoilerHusk.asStack(),
                " h ",
                " C ",
                " w ",
                'C', GTMachines.STEAM_SOLAR_BOILER.second().asStack());

        EXTRACTOR_RECIPES.recipeBuilder(TrueSteamBlocks.BoilerHusk.getId().withSuffix("_extraction"))
                .inputItems(GTMachines.STEAM_SOLAR_BOILER.second().asStack())
                .EUt(VH[HV]).duration(200).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(TrueSteamBlocks.BeatingBoilerHusk.getId())
                .inputItems(TrueSteamBlocks.BoilerHusk)
                .inputItems(TagPrefix.plate, TrueSteamConcepts.HeatingConcept.getMaterial(), 5)
                .inputItems(TagPrefix.rod, TrueSteamConcepts.SteamConcept.getMaterial(), 4)
                .outputItems(TrueSteamBlocks.BeatingBoilerHusk)
                .circuitMeta(1)
                .duration(200)
                .EUt(256).save(provider);

        casingRecipe(TrueSteamMaterials.CorrosionTemperedBrass, TrueSteamBlocks.SlightlyCorrosionProofCasing, provider);
        casingRecipe(TrueSteamMaterials.InfernalAlloy, TrueSteamBlocks.InfernalAlloyCasing, provider);
        casingRecipe(TrueSteamConcepts.ExtractionConcept.getMaterial(), TrueSteamBlocks.ExtractionInfusedCasing,
                provider);
        pipeCasingRecipe(TrueSteamConcepts.ExtractionConcept.getMaterial(), TrueSteamBlocks.ExtractionInfusedPipeCasing,
                provider);

        TrueSteamRecipeTypes.COATING.recipeBuilder(TrueSteamMaterials.LavaCoatedSteel.getResourceLocation())
                .inputItems(TagPrefix.ingot, GTMaterials.Steel)
                .outputItems(TagPrefix.ingot, TrueSteamMaterials.LavaCoatedSteel)
                .duration(200)
                .addCondition(new CoatingFluidCondition(GTMaterials.Lava.getFluid()))
                .save(provider);

        TrueSteamRecipeTypes.COATING.recipeBuilder(TrueSteamMaterials.CorrosionTemperedBrass.getResourceLocation())
                .inputItems(TagPrefix.ingot, GTMaterials.CobaltBrass)
                .outputItems(TagPrefix.ingot, TrueSteamMaterials.CorrosionTemperedBrass)
                .duration(200)
                .addCondition(new CoatingFluidCondition(GTMaterials.Water.getFluid()))
                .save(provider);

        TrueSteamRecipeTypes.COATING.recipeBuilder(TrueSteamMaterials.FrostbiteMagnalium.getResourceLocation())
                .inputItems(TagPrefix.ingot, GTMaterials.Magnalium)
                .outputItems(TagPrefix.ingot, TrueSteamMaterials.FrostbiteMagnalium)
                .duration(200)
                .addCondition(new CoatingFluidCondition(GTMaterials.Ice.getFluid()))
                .save(provider);

        ALLOY_SMELTER_RECIPES.recipeBuilder(TrueSteamMaterials.AluminiumBronze.getResourceLocation())
                .inputItems(TagPrefix.ingot, GTMaterials.Aluminium)
                .inputItems(TagPrefix.ingot, GTMaterials.Bronze)
                .outputItems(TagPrefix.ingot, TrueSteamMaterials.AluminiumBronze, 2)
                .duration(200).EUt(32).save(provider);

        ALLOY_SMELTER_RECIPES.recipeBuilder(TrueSteamBlocks.BronzeGlass.getId())
                .inputItems(TagPrefix.block, GTMaterials.Glass)
                .inputItems(TagPrefix.ingot, GTMaterials.Bronze)
                .outputItems(TrueSteamBlocks.BronzeGlass, 1)
                .duration(200).EUt(32).save(provider);

        TrueSteamRecipeTypes.COATING.recipeBuilder(TrueSteamMaterials.InfernalAlloy.getResourceLocation())
                .inputItems(TagPrefix.ingot, TrueSteamMaterials.AluminiumBronze)
                .outputItems(TagPrefix.ingot, TrueSteamMaterials.InfernalAlloy)
                .duration(200)
                .addCondition(new CoatingFluidCondition(GTMaterials.Blaze.getFluid()))
                .save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, true, InfernalBoiler.MACHINE.getId(),
                InfernalBoiler.MACHINE.asStack(),
                "TST",
                "SCS",
                "TST",
                'C', TrueSteamBlocks.InfernalAlloyCasing.asStack(),
                'S', CustomTags.MV_CIRCUITS,
                'T', new MaterialEntry(TagPrefix.cableGtSingle, GTMaterials.Copper));

        VanillaRecipeHelper.addShapedRecipe(provider, true, CoatingShrine.MACHINE.getId(),
                CoatingShrine.MACHINE.asStack(),
                "TST",
                "SCS",
                "TST",
                'C', GTBlocks.STEEL_HULL.asStack(),
                'S', CustomTags.ULV_CIRCUITS,
                'T', new MaterialEntry(TagPrefix.pipeNormalItem, GTMaterials.Tin));

        VanillaRecipeHelper.addShapedRecipe(provider, true, CoolingBox.MACHINE.getId(), CoolingBox.MACHINE.asStack(),
                "TST",
                "SCS",
                "TST",
                'C', TrueSteamBlocks.SlightlyCorrosionProofCasing.asStack(),
                'S', CustomTags.MV_CIRCUITS,
                'T', new MaterialEntry(TagPrefix.pipeNormalFluid, GTMaterials.Aluminium));

        SCANNER_RECIPES.recipeBuilder(TrueSteamItems.PurifiedInfernalDust.getId())
                .inputItems(TagPrefix.dust, TrueSteamMaterials.InfernalSlug)
                .chancedOutput(TrueSteamItems.PurifiedInfernalDust.asStack(), 500, 500)
                .duration(200)
                .EUt(30)
                .save(provider);

        LASER_ENGRAVER_RECIPES.recipeBuilder(TrueSteamItems.PurifiedInfernalDust.getId().withSuffix("_better"))
                .inputItems(TagPrefix.dust, TrueSteamMaterials.InfernalSlug, 9)
                .notConsumable(TagPrefix.lens, TrueSteamMaterials.InfernalCompound)
                .outputItems(TrueSteamItems.PurifiedInfernalDust)
                .duration(200).EUt(VH[HV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(TrueSteamItems.PurifiedInfernalDust.getId().withSuffix("_even_better"))
                .inputItems(TagPrefix.dust, TrueSteamMaterials.InfernalSlug, 3)
                .inputFluids(TrueSteamConcepts.ExtractionConcept.getInfusedAir().getFluid(500),
                        TrueSteamConcepts.HeatingConcept.getInfusedAir().getFluid(500))
                .outputItems(TrueSteamItems.PurifiedInfernalDust)
                .duration(400).EUt(VH[HV]).save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, true, "infernal_drum", TrueSteamMachines.InfernalDrum.asStack(),
                " h ",
                "PRP", "PRP", 'P', new MaterialEntry(TagPrefix.plate, TrueSteamMaterials.InfernalAlloy), 'R',
                new MaterialEntry(TagPrefix.rodLong, TrueSteamMaterials.InfernalAlloy));
        VanillaRecipeHelper.addShapelessRecipe(provider, "infernal_drum", TrueSteamMachines.InfernalDrum.asStack(),
                TrueSteamMachines.InfernalDrum.asStack());

        ASSEMBLER_RECIPES.recipeBuilder("infernal_drum").EUt(16)
                .inputItems(TagPrefix.rodLong, TrueSteamMaterials.InfernalAlloy, 2)
                .inputItems(TagPrefix.plate, TrueSteamMaterials.InfernalAlloy, 4)
                .outputItems(TrueSteamMachines.InfernalDrum).duration(200).circuitMeta(2)
                .addMaterialInfo(true).save(provider);

        TrueSteamRecipeTypes.COATING.recipeBuilder(TrueSteamBlocks.FrostOverproofedCasing.getId())
                .inputItems(GTBlocks.CASING_ALUMINIUM_FROSTPROOF)
                .outputItems(TrueSteamBlocks.FrostOverproofedCasing)
                .duration(200)
                .addCondition(new CoatingFluidCondition(GTMaterials.Ice.getFluid()))
                .save(provider);

        MIXER_RECIPES.recipeBuilder(TrueSteamMaterials.ConceptualizedSteel.getResourceLocation())
                .inputItems(TagPrefix.dust, GTMaterials.StainlessSteel)
                .inputItems(TagPrefix.dust, GTMaterials.EnderPearl)
                .inputItems(TagPrefix.dust, GTMaterials.Amethyst)
                .outputItems(TagPrefix.dust, TrueSteamMaterials.ConceptualizedSteel, 3)
                .EUt(30)
                .duration(100)
                .circuitMeta(1)
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder(TrueSteamMaterials.InfernalTar.getResourceLocation().withSuffix("_crimson"))
                .inputItems(ItemTags.CRIMSON_STEMS, 16)
                .circuitMeta(1).outputFluids(TrueSteamMaterials.InfernalTar.getFluid(8000))
                .duration(640).EUt(64).save(provider);

        PYROLYSE_RECIPES.recipeBuilder(TrueSteamMaterials.InfernalTar.getResourceLocation().withSuffix("_warped"))
                .inputItems(ItemTags.WARPED_STEMS, 16)
                .circuitMeta(1).outputFluids(TrueSteamMaterials.InfernalTar.getFluid(8000))
                .duration(640).EUt(64).save(provider);

        MIXER_RECIPES.recipeBuilder(TrueSteamMaterials.StainlessInfernalSteel.getResourceLocation())
                .inputItems(TagPrefix.dust, GTMaterials.StainlessSteel, 7)
                .inputItems(TagPrefix.dust, GTMaterials.Netherrack, 5)
                .inputItems(TrueSteamItems.PurifiedInfernalDust, 2)
                .outputItems(TagPrefix.dust, TrueSteamMaterials.StainlessInfernalSteel, 14)
                .EUt(30)
                .duration(100)
                .circuitMeta(1)
                .save(provider);

        MIXER_RECIPES.recipeBuilder(TrueSteamMaterials.InfernalCompound.getResourceLocation())
                .inputItems(TagPrefix.dust, GTMaterials.Amethyst, 2)
                .inputItems(TagPrefix.dust, GTMaterials.Netherrack, 2)
                .inputItems(TrueSteamItems.PurifiedInfernalDust, 2)
                .outputItems(TagPrefix.dust, TrueSteamMaterials.InfernalCompound, 6)
                .EUt(30)
                .duration(100)
                .circuitMeta(1)
                .save(provider);

        TrueSteamRecipeTypes.COATING.recipeBuilder(TrueSteamMaterials.EstrangedSteel.getResourceLocation())
                .inputItems(TagPrefix.ingot, TrueSteamConcepts.CoolingConcept.getMaterial())
                .outputItems(TagPrefix.ingot, TrueSteamMaterials.EstrangedSteel)
                .duration(200)
                .addCondition(new CoatingFluidCondition(GTMaterials.LiquidEnderAir.getFluid()))
                .save(provider);

        registerReEtchingRecipe("mv", CustomTags.MV_CIRCUITS, 1, provider);
        registerReEtchingRecipe("hv", CustomTags.HV_CIRCUITS, 2, provider);
        registerReEtchingRecipe("ev", CustomTags.EV_CIRCUITS, 4, provider);
        registerReEtchingRecipe("iv", CustomTags.IV_CIRCUITS, 8, provider);

        LASER_ENGRAVER_RECIPES.recipeBuilder(TrueSteamItems.RawInfernalCircuit.getId())
                .inputItems(TrueSteamItems.ReEtchedCircuit)
                .notConsumable(TagPrefix.lens, TrueSteamMaterials.InfernalCompound)
                .outputItems(TrueSteamItems.RawInfernalCircuit)
                .duration(900).EUt(VA[MV]).save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(TrueSteamItems.InfernalCircuit.getId()).EUt(24).duration(400)
                .inputItems(TrueSteamItems.RawInfernalCircuit, 2)
                .inputItems(CustomTags.RESISTORS, 2)
                .inputItems(CustomTags.DIODES, 2)
                .inputItems(TagPrefix.wireFine, TrueSteamMaterials.ConceptualizedSteel, 4)
                .inputItems(TagPrefix.bolt, TrueSteamMaterials.StainlessInfernalSteel, 4)
                .outputItems(TrueSteamItems.InfernalCircuit, 2)
                .save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, true, RegulatedCryoChamber.MACHINE.getId(),
                RegulatedCryoChamber.MACHINE.asStack(),
                "CCC",
                "IFI",
                "WIW",
                'C', TrueSteamBlocks.FrostOverproofedCasing,
                'F', GTMultiMachines.VACUUM_FREEZER.asStack(),
                'I', TrueSteamItems.InfernalCircuit,
                'W', new MaterialEntry(TagPrefix.cableGtSingle, TrueSteamMaterials.ConceptualizedSteel));

        CHEMICAL_RECIPES.recipeBuilder(TrueSteamMaterials.ActivatedBlaze.getResourceLocation().withSuffix("_burning"))
                .inputFluids(TrueSteamMaterials.ActivatedBlaze.getFluid(8000))
                .inputFluids(GTMaterials.Oxygen.getFluid(4000))
                .outputItems(TagPrefix.dust, TrueSteamMaterials.InfernalSlug, 16)
                .duration(400).EUt(120).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder(TrueSteamItems.EmptyCatalyst.getId())
                .inputItems(TagPrefix.gemExquisite, TrueSteamMaterials.InfernalCompound)
                .inputFluids(GTMaterials.DyeBlack.getFluid(720)).outputItems(TrueSteamItems.EmptyCatalyst)
                .duration(200).EUt(250).save(provider);

        TrueSteamConcepts.CONCEPTS.forEach(c -> c.registerRecipes(provider));
        TrueSteamSteams.STEAMS.forEach(c -> c.registerRecipes(provider));

        casingRecipe(TrueSteamMaterials.ConceptualizedSteel, TrueSteamBlocks.ConceptualizedSteelSolidCasing, provider);
        pipeCasingRecipe(TrueSteamMaterials.ConceptualizedSteel, TrueSteamBlocks.ConceptualizedSteelPipeCasing,
                provider);

        VanillaRecipeHelper.addShapedRecipe(provider, true, ConceptInfusionMatrix.MACHINE.getId(),
                ConceptInfusionMatrix.MACHINE.asStack(),
                "III", "HCH", "WHW", 'I', TrueSteamItems.InfernalCircuit, 'H', CustomTags.HV_CIRCUITS, 'W',
                new MaterialEntry(TagPrefix.wireGtDouble, TrueSteamMaterials.ConceptualizedSteel), 'C',
                TrueSteamBlocks.ConceptualizedSteelSolidCasing);

        VanillaRecipeHelper.addShapedRecipe(provider, true, IndustrialGasPressurizer.MACHINE.getId(),
                IndustrialGasPressurizer.MACHINE.asStack(),
                "III", "HCH", "WHW", 'I', TrueSteamItems.InfernalCircuit, 'H', CustomTags.HV_CIRCUITS, 'W',
                new MaterialEntry(TagPrefix.plate, TrueSteamConcepts.CompressionConcept.getMaterial()), 'C',
                new MaterialEntry(TagPrefix.frameGt, TrueSteamConcepts.CompressionConcept.getMaterial()));

        TrueSteamRecipeTypes.SPAWNER_EXTRACTION.recipeBuilder("spawner_loot_extraction")
                .notConsumable(Ingredient.of(ItemTags.SWORDS))
                .inputFluids(TrueSteamConcepts.ExtractionConcept.getInfusedAir().getFluid(250))
                .duration(200)
                .EUt(30)
                .addData(TrueSteamRecipeTypes.LOOT_TABLE_DROPS, true)
                .save(provider);

        registerSpringRecipes(provider);
        registerCoolingCoilsRecipes(provider);
    }
}

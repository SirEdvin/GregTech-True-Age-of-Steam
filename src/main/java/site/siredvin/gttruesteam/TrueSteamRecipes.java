package site.siredvin.gttruesteam;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.level.block.Block;

import com.mojang.datafixers.util.Pair;
import com.tterrag.registrate.util.entry.BlockEntry;
import site.siredvin.gttruesteam.machines.coating_shrine.CoatingShrine;
import site.siredvin.gttruesteam.machines.cooling_box.CoolingBox;
import site.siredvin.gttruesteam.machines.industrial_heater.InfernalBoiler;
import site.siredvin.gttruesteam.recipe.condition.CoatingFluidCondition;
import site.siredvin.gttruesteam.recipe.condition.CoolingCapacityCondition;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;
import static site.siredvin.gttruesteam.TrueSteamRecipeTypes.FLUID_COOLING;
import static site.siredvin.gttruesteam.TrueSteamRecipeTypes.METAPHYSICAL_BOILING;

public class TrueSteamRecipes {

    private static void steamFuel(Material current, double density, Consumer<FinishedRecipe> provider) {
        STEAM_TURBINE_FUELS.recipeBuilder(GTTrueSteam.id(current.getName() + "_fuel"))
                .inputFluids(
                        current.getFluid((int) (320 / density)))
                .outputFluids(
                        GTMaterials.DistilledWater.getFluid(4))
                .duration(10).EUt(-32).save(provider);
    }

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

    public static void registerInfernalChargingLoop(Consumer<FinishedRecipe> provider) {
        MIXER_RECIPES.recipeBuilder(TrueSteamMaterials.ActivatedBlaze.getResourceLocation())
                .inputFluids(GTMaterials.Blaze.getFluid(1000), GTMaterials.NetherAir.getFluid(3000))
                .inputItems(TagPrefix.dust, GTMaterials.Redstone)
                .outputFluids(TrueSteamMaterials.ActivatedBlaze.getFluid(4000))
                .EUt(60)
                .duration(200)
                .save(provider);
        METAPHYSICAL_BOILING.recipeBuilder(TrueSteamMaterials.OverheatedInfernalSlurry.getResourceLocation())
                .inputFluids(TrueSteamMaterials.ActivatedBlaze.getFluid(8000))
                .outputFluids(TrueSteamMaterials.OverheatedInfernalSlurry.getFluid(8000))
                .EUt(8)
                .circuitMeta(2)
                .addData(TrueSteamRecipeTypes.INFERNAL_CYCLES_DATA_KEY, 512)
                .duration(1000)
                .save(provider);
        FLUID_COOLING.recipeBuilder(TrueSteamMaterials.InfernalSlurry.getResourceLocation())
                .inputFluids(TrueSteamMaterials.OverheatedInfernalSlurry.getFluid(4000))
                .outputFluids(TrueSteamMaterials.InfernalSlurry.getFluid(4000))
                .addData(TrueSteamRecipeTypes.COOLING_CONSUMED, 500)
                .addCondition(new CoolingCapacityCondition(5000))
                .save(provider);
        CENTRIFUGE_RECIPES.recipeBuilder(GTTrueSteam.id("infernal_slurry_separation"))
                .inputFluids(TrueSteamMaterials.InfernalSlurry.getFluid(1000))
                .outputFluids(TrueSteamMaterials.DilutedBlaze.getFluid(250))
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
    }

    private static void generateBoilerRecipe(MachineDefinition boiler, Pair<MachineDefinition, MachineDefinition> next,
                                             Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, next.getFirst().getId(), next.getFirst().asStack(),
                "PPP",
                "PwP",
                " B ",
                'B', boiler.asStack(),
                'P', new MaterialEntry(TagPrefix.plate, TrueSteamMaterials.LavaCoatedSteel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, next.getSecond().getId(), next.getSecond().asStack(),
                "PPP",
                "PwP",
                " B ",
                'B', next.getFirst().asStack(),
                'P', new MaterialEntry(TagPrefix.plate, TrueSteamMaterials.InfernalAlloy));
    }

    private static void registerBoilerRecipes(Consumer<FinishedRecipe> provider) {
        generateBoilerRecipe(GTMachines.STEAM_SOLAR_BOILER.second(), TrueSteamMachines.SOLAR, provider);
        generateBoilerRecipe(GTMachines.STEAM_LIQUID_BOILER.second(), TrueSteamMachines.LIQUID, provider);
        generateBoilerRecipe(GTMachines.STEAM_SOLID_BOILER.second(), TrueSteamMachines.SOLID, provider);
    }

    public static void registerRecipes(Consumer<FinishedRecipe> provider) {
        steamFuel(TrueSteamMaterials.SuperhotSteam, 2.1, provider);
        registerInfernalChargingLoop(provider);
        registerBoilerRecipes(provider);

        METAPHYSICAL_BOILING.recipeBuilder(GTTrueSteam.id("boiling_water"))
                .inputFluids(GTMaterials.DistilledWater.getFluid(18))
                .outputFluids(TrueSteamMaterials.SuperhotSteam.getFluid(3000))
                .circuitMeta(1)
                .EUt(30)
                .duration(900)
                .addData(TrueSteamRecipeTypes.OVERHEATED_KEY, true).save(provider);

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

        casingRecipe(TrueSteamMaterials.CorrosionTemperedBrass, TrueSteamBlocks.SlightlyCorrosionProofCasing, provider);
        casingRecipe(TrueSteamMaterials.InfernalAlloy, TrueSteamBlocks.InfernalAlloyCasing, provider);

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

        ASSEMBLER_RECIPES.recipeBuilder(TrueSteamBlocks.COIL_FROSTBITE_MAGNALIUM.getId())
                .inputItems(TagPrefix.foil, TrueSteamMaterials.FrostbiteMagnalium, 8)
                .inputItems(TagPrefix.pipeSmallFluid, GTMaterials.Aluminium, 8)
                .inputFluids(GTMaterials.TinAlloy.getFluid(144))
                .outputItems(TrueSteamBlocks.COIL_FROSTBITE_MAGNALIUM.asStack())
                .duration(200).EUt(30).save(provider);

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
    }
}

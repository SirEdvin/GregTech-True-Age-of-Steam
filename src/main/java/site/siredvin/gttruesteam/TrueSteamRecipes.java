package site.siredvin.gttruesteam;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.level.block.Block;

import com.tterrag.registrate.util.entry.BlockEntry;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class TrueSteamRecipes {

    private static void steamFuel(Material current, double density, Consumer<FinishedRecipe> provider) {
        STEAM_TURBINE_FUELS.recipeBuilder(GTTrueSteam.id(current.getName() + "_fuel"))
                .inputFluids(
                        current.getFluid((int) (320 / density)))
                .outputFluids(
                        GTMaterials.DistilledWater.getFluid(4))
                .duration(10).EUt(-32).save(provider);
    }

    private static void heatingSteamRecipes(Material old, Material current, double density,
                                            Consumer<FinishedRecipe> provider) {
        FLUID_HEATER_RECIPES.recipeBuilder(current.getResourceLocation()).inputFluids(
                old.getFluid(500)).circuitMeta(1).outputFluids(current.getFluid(1000)).duration(30).EUt(20)
                .save(provider);
        steamFuel(current, density, provider);
    }

    private static void pressuringSteamRecipes(Material raw, Material result, int coefficient, double rawDensity,
                                               Consumer<FinishedRecipe> provider) {
        steamFuel(result, coefficient * rawDensity, provider);
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

    public static void registerRecipes(Consumer<FinishedRecipe> provider) {
        heatingSteamRecipes(GTMaterials.Steam, TrueSteamMaterials.WarmSteam, 0.6, provider);
        heatingSteamRecipes(TrueSteamMaterials.WarmSteam, TrueSteamMaterials.HotSteam, 0.7, provider);
        heatingSteamRecipes(TrueSteamMaterials.HotSteam, TrueSteamMaterials.SuperhotSteam, 0.8, provider);

        pressuringSteamRecipes(TrueSteamMaterials.WarmSteam, TrueSteamMaterials.PressurisedSteam, 2, 0.6, provider);
        pressuringSteamRecipes(TrueSteamMaterials.HotSteam, TrueSteamMaterials.HighPressurisedSteam, 4, 0.7, provider);
        pressuringSteamRecipes(TrueSteamMaterials.SuperhotSteam, TrueSteamMaterials.ExtremelyPressurisedSteam, 8, 0.8,
                provider);

        FLUID_HEATER_RECIPES.recipeBuilder("hydrochloric_cleaning")
                .inputFluids(GTMaterials.HydrochloricAcid.getFluid(10000))
                .outputFluids(TrueSteamMaterials.HydrochloricAcidSlurry.getFluid(15000))
                .duration(20 * 100)
                .circuitMeta(2)
                .addData(TrueSteamRecipeTypes.PURE_CYCLES_DATA_KEY, 256)
                .EUt(8)
                .save(provider);
        CENTRIFUGE_RECIPES.recipeBuilder("hydrochloric_acid_slurry_separation")
                .inputFluids(TrueSteamMaterials.HydrochloricAcidSlurry.getFluid(3000))
                .outputFluids(TrueSteamMaterials.OverheatedHydrochloricAcid.getFluid(2000),
                        GTMaterials.SaltWater.getFluid(1000))
                .EUt(120)
                .duration(200)
                .save(provider);
        TrueSteamRecipeTypes.FLUID_COOLING.recipeBuilder("cooling_hydrochloric_acid")
                .inputFluids(TrueSteamMaterials.OverheatedHydrochloricAcid.getFluid(10000))
                .outputFluids(GTMaterials.HydrochloricAcid.getFluid(10000))
                .duration(2000)
                .save(provider);

        ARC_FURNACE_RECIPES.recipeBuilder("steam_tempered_bronze")
                .inputFluids(GTMaterials.Steam.getFluid(63))
                .inputItems(TagPrefix.ingot, GTMaterials.Bronze)
                .outputItems(TagPrefix.ingot, TrueSteamMaterials.SteamTemperedBronze)
                .EUt(60)
                .duration(63)
                .save(provider);

        ARC_FURNACE_RECIPES.recipeBuilder("corrosion_tempered_alloy")
                .inputFluids(TrueSteamMaterials.SteamOxygenMixture.getFluid(63))
                .inputItems(TagPrefix.ingot, GTMaterials.CobaltBrass)
                .outputItems(TagPrefix.ingot, TrueSteamMaterials.CorrosionTemperedAlloy)
                .EUt(60)
                .duration(63)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("steam_oxygen_mixture")
                .inputFluids(GTMaterials.Oxygen.getFluid(400), GTMaterials.Steam.getFluid(600))
                .outputFluids(TrueSteamMaterials.SteamOxygenMixture.getFluid(1000))
                .duration(200)
                .EUt(32)
                .save(provider);

        casingRecipe(TrueSteamMaterials.CorrosionTemperedAlloy, TrueSteamBlocks.SlightlyCorrosionProofCasing, provider);
        casingRecipe(TrueSteamMaterials.SteamTemperedBronze, TrueSteamBlocks.IndustrialBronzeCasing, provider);
    }
}

package site.siredvin.gttruesteam.common;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.common.data.models.GTModels;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import lombok.Getter;
import site.siredvin.gttruesteam.*;
import site.siredvin.gttruesteam.recipe.condition.BeatingHuskCondition;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.STEAM_TURBINE_FUELS;
import static site.siredvin.gttruesteam.TrueSteamRecipeTypes.METAPHYSICAL_BOILING;

public class SteamRecord {

    @Getter
    private final Material basicSteam;

    @Getter
    private final Material denseSteam;

    @Getter
    private final Material criticalSteam;

    @Getter
    private final Material denseCriticalSteam;

    @Getter
    private final SteamConfiguration configuration;

    @Getter
    private final Supplier<Block> solidifiedDenseSteam;

    @Getter
    private final Supplier<Block> solidifiedDenseCriticalSteam;

    public SteamRecord(SteamConfiguration configuration, Material basicSteam, Material denseSteam,
                       Material criticalSteam, Material denseCriticalSteam, Supplier<Block> solidifiedDenseSteam,
                       Supplier<Block> solidifiedDenseCriticalSteam) {
        this.configuration = configuration;
        this.basicSteam = basicSteam;
        this.denseSteam = denseSteam;
        this.criticalSteam = criticalSteam;
        this.denseCriticalSteam = denseCriticalSteam;
        this.solidifiedDenseSteam = solidifiedDenseSteam;
        this.solidifiedDenseCriticalSteam = solidifiedDenseCriticalSteam;
    }

    private void registerPressurizeRecipe(Consumer<FinishedRecipe> provider, Material output, Material original) {
        TrueSteamRecipeTypes.INDUSTRIAL_GAS_PRESSURIZER.recipeBuilder(output.getResourceLocation())
                .inputFluids(original.getFluid(256000))
                .outputFluids(output.getFluid(9600))
                .circuitMeta(1)
                .addData(TrueSteamRecipeTypes.ASSUMED_PERCENTAGE, 75)
                .EUt(configuration.compressionEUt()).duration(configuration.compressionDuration()).save(provider);
        TrueSteamRecipeTypes.INDUSTRIAL_GAS_PRESSURIZER
                .recipeBuilder(output.getResourceLocation().withSuffix("_boosted"))
                .inputFluids(original.getFluid(256000))
                .circuitMeta(2)
                .inputItems(TrueSteamItems.InfusedCompressedCompressionSpringPack)
                .outputFluids(output.getFluid(12800))
                .outputItems(TagPrefix.spring, TrueSteamConcepts.CompressionConcept.getMaterial(), 3)
                .addData(TrueSteamRecipeTypes.ASSUMED_PERCENTAGE, 100)
                .EUt(configuration.compressionEUt()).duration(configuration.compressionDuration()).save(provider);
    }

    public void registerRecipes(Consumer<FinishedRecipe> provider) {
        var steamToBurn = configuration.waterConversionRate() * configuration.waterOutput();
        STEAM_TURBINE_FUELS.recipeBuilder(basicSteam.getResourceLocation().withSuffix("_fuel"))
                .inputFluids(basicSteam.getFluid(steamToBurn))
                .outputFluids(configuration.waterOutputStack())
                .duration((int) (steamToBurn * configuration.density() / 32)).EUt(-32).save(provider);
        STEAM_TURBINE_FUELS.recipeBuilder(criticalSteam.getResourceLocation().withSuffix("_fuel"))
                .inputFluids(criticalSteam.getFluid(steamToBurn))
                .outputFluids(basicSteam.getFluid(steamToBurn))
                .duration((int) (steamToBurn * configuration.density() / 32)).EUt(-32).save(provider);
        STEAM_TURBINE_FUELS.recipeBuilder(denseSteam.getResourceLocation().withSuffix("_fuel"))
                .inputFluids(denseSteam.getFluid(steamToBurn / Constants.STEAM_COMPRESSION_COEF))
                .outputFluids(configuration.waterOutputStack())
                .duration((int) (steamToBurn * configuration.density() / 32)).EUt(-32).save(provider);
        STEAM_TURBINE_FUELS.recipeBuilder(denseCriticalSteam.getResourceLocation().withSuffix("_fuel"))
                .inputFluids(denseCriticalSteam.getFluid(steamToBurn / Constants.STEAM_COMPRESSION_COEF))
                .outputFluids(basicSteam.getFluid(steamToBurn))
                .duration((int) (steamToBurn * configuration.density() / 32)).EUt(-32).save(provider);
        METAPHYSICAL_BOILING.recipeBuilder(basicSteam.getResourceLocation())
                .inputFluids(configuration.water()
                        .getFluid(Constants.METAPHYSICS_STEAM_BOILING_OUTPUT / configuration.waterConversionRate()))
                .outputFluids(basicSteam.getFluid(Constants.METAPHYSICS_STEAM_BOILING_OUTPUT))
                .circuitMeta(1)
                .EUt(30)
                .duration(640)
                .addData(TrueSteamRecipeTypes.OVERHEATED_KEY, true).save(provider);
        METAPHYSICAL_BOILING.recipeBuilder(criticalSteam.getResourceLocation())
                .inputFluids(configuration.water()
                        .getFluid(Constants.METAPHYSICS_STEAM_BOILING_OUTPUT / configuration.waterConversionRate()))
                .outputFluids(criticalSteam.getFluid(Constants.METAPHYSICS_STEAM_BOILING_OUTPUT))
                .circuitMeta(3)
                .EUt(30)
                .duration(960)
                .addCondition(new BeatingHuskCondition())
                .addData(TrueSteamRecipeTypes.OVERHEATED_KEY, true).save(provider);
        registerPressurizeRecipe(provider, denseSteam, basicSteam);
        registerPressurizeRecipe(provider, denseCriticalSteam, criticalSteam);
    }

    public static int calculateTemperature(double density) {
        return (int) (Constants.BASE_STEAM_TEMPERATURE +
                (density - Constants.BASE_STEAM_DENSITY) * Constants.TEMPERATURE_TO_ENERGY);
    }

    public static class Builder {

        private String baseName = null;
        private String criticalName = null;
        private Material water = null;
        private int waterConversionRate = 1000;
        private int waterOutput = 4;
        private double density = Constants.BASE_STEAM_DENSITY;
        private int compressionEUt = 0;
        private int compressionDuration = 0;

        public Builder baseName(String baseName) {
            this.baseName = baseName;
            return this;
        }

        public Builder criticalName(String criticalName) {
            this.criticalName = criticalName;
            return this;
        }

        public Builder water(Material water) {
            this.water = water;
            return this;
        }

        public Builder density(double density) {
            this.density = density;
            return this;
        }

        public Builder waterConversionRate(int waterConversionRate) {
            this.waterConversionRate = waterConversionRate;
            return this;
        }

        public Builder compression(int EUt, int duration) {
            this.compressionEUt = EUt;
            this.compressionDuration = duration;
            return this;
        }

        public Builder waterOutput(int waterOutput) {
            this.waterOutput = waterOutput;
            return this;
        }

        public SteamRecord build() {
            assert baseName != null;
            assert criticalName != null;
            assert water != null;
            assert density != Constants.BASE_STEAM_DENSITY;
            assert compressionDuration != 0;
            assert compressionEUt != 0;
            var configuration = new SteamConfiguration(density, water, waterConversionRate, waterOutput, compressionEUt,
                    compressionDuration);
            var baseSteam = new Material.Builder(GTTrueSteam.id(baseName + "_steam"))
                    .gas(new FluidBuilder()
                            .state(FluidState.GAS)
                            .temperature(calculateTemperature(density))
                            .customStill())
                    .buildAndRegister();
            var denseSteam = new Material.Builder(GTTrueSteam.id("dense_" + baseName + "_steam")).gas(new FluidBuilder()
                    .state(FluidState.GAS)
                    .temperature(calculateTemperature(density * Constants.STEAM_COMPRESSION_COEF))
                    .customStill()).buildAndRegister();
            var criticalSteam = new Material.Builder(GTTrueSteam.id(criticalName + "_steam")).gas(new FluidBuilder()
                    .state(FluidState.GAS)
                    .temperature(calculateTemperature(density * Constants.STEAM_CRITICAL_BOOST))
                    .customStill()).buildAndRegister();
            var denseCriticalSteam = new Material.Builder(GTTrueSteam.id("dense_" + criticalName + "_steam"))
                    .gas(new FluidBuilder()
                            .state(FluidState.GAS)
                            .temperature(calculateTemperature(
                                    density * Constants.STEAM_COMPRESSION_COEF * Constants.STEAM_CRITICAL_BOOST))
                            .customStill())
                    .buildAndRegister();
            var solidifiedDenseSteam = GTTrueSteam.REGISTRATE
                    .block("solidified_dense_" + baseName + "_steam", Block::new)
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                    .addLayer(() -> RenderType::solid)
                    .exBlockstate(
                            GTModels.cubeAllModel(GTTrueSteam.id("block/fluids/fluid.dense_" + baseName + "_steam")))
                    .item(BlockItem::new)
                    .build()
                    .register();

            var solidifiedDenseCriticalSteam = GTTrueSteam.REGISTRATE
                    .block("solidified_dense_" + criticalName + "_steam", Block::new)
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                    .addLayer(() -> RenderType::solid)
                    .exBlockstate(GTModels
                            .cubeAllModel(GTTrueSteam.id("block/fluids/fluid.dense_" + criticalName + "_steam")))
                    .item(BlockItem::new)
                    .build()
                    .register();
            return new SteamRecord(
                    configuration, baseSteam, denseSteam, criticalSteam, denseCriticalSteam, solidifiedDenseSteam,
                    solidifiedDenseCriticalSteam);
        }
    }
}

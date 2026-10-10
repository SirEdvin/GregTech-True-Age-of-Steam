package site.siredvin.gttruesteam.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;

import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamSteams;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.common.data.GTMaterials.Steam;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.CRACKING_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.DISTILLATION_RECIPES;

public final class CriticalSteamCrackingRecipes {

    private static boolean initialized;

    private CriticalSteamCrackingRecipes() {}

    public static void init() {
        if (initialized) return;
        chain(CRACKING_RECIPES, CriticalSteamCrackingRecipes::deriveCracking);
        chain(DISTILLATION_RECIPES, CriticalSteamCrackingRecipes::deriveDistillation);
        initialized = true;
    }

    private static void chain(GTRecipeType type, BiConsumer<GTRecipeBuilder, Consumer<FinishedRecipe>> discovery) {
        var previous = type.recipeBuilder(GTTrueSteam.id("cracking_discovery")).onSave;
        type.onRecipeBuild((source, provider) -> {
            if (previous != null) previous.accept(source, provider);
            if (source.id.getNamespace().equals(GTCEu.MOD_ID)) discovery.accept(source, provider);
        });
    }

    private static void deriveCracking(GTRecipeBuilder source, Consumer<FinishedRecipe> provider) {
        var inputs = source.input.getOrDefault(FluidRecipeCapability.CAP, List.of());
        var outputs = source.output.getOrDefault(FluidRecipeCapability.CAP, List.of());
        if (inputs.size() != 2 || outputs.size() != 1) return;
        int agent = matches(inputs.get(0), Steam) ? 0 : matches(inputs.get(1), Steam) ? 1 : -1;
        if (agent < 0) return;
        for (var feed : CrackingFeedstock.all()) {
            for (boolean severe : new boolean[] {false, true}) {
                var regular = feed.regular(severe);
                if (!matches(inputs.get(1 - agent), feed.raw()) || !matches(outputs.get(0), regular)) continue;
                int regularAmount = FluidRecipeCapability.CAP.of(outputs.get(0).content).getAmount();
                int steamAmount = FluidRecipeCapability.CAP.of(inputs.get(agent).content).getAmount();
                for (var steam : TrueSteamSteams.STEAMS) {
                    String prefix = (severe ? "severely_" : "lightly_") + steam.getCriticalSteam().getName();
                    var initial = source.copy(GTTrueSteam.id(prefix + "_crack_" + feed.raw().getName()))
                            .onSave(null).duration(source.duration / 2);
                    initial.input.get(FluidRecipeCapability.CAP).set(agent,
                            substitute(inputs.get(agent), steam.getCriticalSteam(), steamAmount, true));
                    initial.output.get(FluidRecipeCapability.CAP).set(0,
                            substitute(outputs.get(0), steam.getCrackedFluids().get(regular), regularAmount, false));
                    initial.save(provider);

                    var recovery = source.copy(GTTrueSteam.id(prefix + "_residue_crack_" + feed.raw().getName())).onSave(null);
                    recovery.input.get(FluidRecipeCapability.CAP).set(agent,
                            substitute(inputs.get(agent), steam.getCrackingResidue(), CrackingYield.RESIDUE_CONSUMED, true));
                    recovery.output.get(FluidRecipeCapability.CAP).set(0,
                            substitute(outputs.get(0), regular, CrackingYield.residueOutput(regularAmount,
                                    steam.getConfiguration().crackingYieldCoefficient()), false));
                    recovery.save(provider);
                }
                return;
            }
        }
    }

    private static void deriveDistillation(GTRecipeBuilder source, Consumer<FinishedRecipe> provider) {
        var inputs = source.input.getOrDefault(FluidRecipeCapability.CAP, List.of());
        if (inputs.size() != 1) return;
        for (var feed : CrackingFeedstock.all()) {
            for (boolean severe : new boolean[] {false, true}) {
                var regular = feed.regular(severe);
                if (!matches(inputs.get(0), regular)) continue;
                int amount = Math.multiplyExact(FluidRecipeCapability.CAP.of(inputs.get(0).content).getAmount(), 2);
                if (amount % 10 != 0) throw new IllegalArgumentException("Fractional cracking residue for " + source.id);
                for (var steam : TrueSteamSteams.STEAMS) {
                    var custom = steam.getCrackedFluids().get(regular);
                    var distillation = source.copy(GTTrueSteam.id("distill_" + custom.getName()))
                            .onSave(null).disableDistilleryRecipes(true).duration(Math.multiplyExact(source.duration, 2));
                    distillation.input.get(FluidRecipeCapability.CAP).set(0, substitute(inputs.get(0), custom, amount, true));
                    distillation.chance(ChanceLogic.getMaxChancedValue()).maxChance(ChanceLogic.getMaxChancedValue())
                            .tierChanceBoost(0).perTick(false).outputFluids(steam.getCrackingResidue().getFluid(amount / 10))
                            .save(provider);
                }
                return;
            }
        }
    }

    private static boolean matches(Content content, Material material) {
        var ingredient = FluidRecipeCapability.CAP.of(content.content);
        if (ingredient.values.length != 1) return false;
        var fluid = material.getFluid();
        var value = ingredient.values[0];
        // Compare tag identities directly: tags need not be bound during data generation.
        return value instanceof FluidIngredient.FluidValue direct && direct.fluid() == fluid ||
                value instanceof FluidIngredient.TagValue tag &&
                        tag.tag().equals(TagUtil.createFluidTag(BuiltInRegistries.FLUID.getKey(fluid).getPath()));
    }

    private static Content substitute(Content source, Material material, int amount, boolean input) {
        var original = FluidRecipeCapability.CAP.of(source.content);
        var nbt = original.getNbt() == null ? null : original.getNbt().copy();
        var fluid = material.getFluid();
        var replacement = input ? FluidIngredient.of(TagUtil.createFluidTag(BuiltInRegistries.FLUID.getKey(fluid).getPath()), amount, nbt) :
                FluidIngredient.of(fluid, amount, nbt);
        return new Content(replacement, source.chance, source.maxChance, source.tierChanceBoost);
    }
}

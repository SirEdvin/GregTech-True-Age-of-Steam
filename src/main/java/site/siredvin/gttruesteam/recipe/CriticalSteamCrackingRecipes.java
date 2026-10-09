package site.siredvin.gttruesteam.recipe;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.data.recipes.FinishedRecipe;

import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.common.SteamRecord;

import java.util.List;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.CRACKING_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.DISTILLATION_RECIPES;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust;

public final class CriticalSteamCrackingRecipes {

    private static final int REGULAR_AMOUNT = 1000;


    private record Severity(String name, int circuit, int duration, int EUt) {}

    private CriticalSteamCrackingRecipes() {}

    public static void register(Consumer<FinishedRecipe> provider, SteamRecord steam) {
        var feedstocks = CrackingFeedstock.all();
        var severities = List.of(new Severity("lightly", 1, 80, 240), new Severity("severely", 3, 160, VA[HV]));

        int residueOutput = CrackingYield.residueOutput(REGULAR_AMOUNT, steam.getConfiguration().crackingYieldCoefficient());
        for (var feedstock : feedstocks) {
            for (var severity : severities) {
                Material cracked = severity.circuit() == 1 ? feedstock.light() : feedstock.severe();
                var custom = steam.getCrackedFluids().get(cracked);
                String prefix = severity.name() + "_" + steam.getCriticalSteam().getName();
                CRACKING_RECIPES.recipeBuilder(GTTrueSteam.id(prefix + "_crack_" + feedstock.raw().getName()))
                        .circuitMeta(severity.circuit())
                        .inputFluids(feedstock.raw().getFluid(REGULAR_AMOUNT))
                        .inputFluids(steam.getCriticalSteam().getFluid(REGULAR_AMOUNT))
                        .outputFluids(custom.getFluid(REGULAR_AMOUNT))
                        .duration(severity.duration() / 2).EUt(severity.EUt()).save(provider);
                CRACKING_RECIPES.recipeBuilder(GTTrueSteam.id(prefix + "_residue_crack_" + feedstock.raw().getName()))
                        .circuitMeta(severity.circuit())
                        .inputFluids(feedstock.raw().getFluid(REGULAR_AMOUNT))
                        .inputFluids(steam.getCrackingResidue().getFluid(CrackingYield.RESIDUE_CONSUMED))
                        .outputFluids(cracked.getFluid(residueOutput))
                        .duration(severity.duration()).EUt(severity.EUt()).save(provider);
                boolean severe = severity.circuit() == 3;
                var distillation = DISTILLATION_RECIPES.recipeBuilder(GTTrueSteam.id("distill_" + custom.getName()))
                        .disableDistilleryRecipes(true)
                        .inputFluids(custom.getFluid(2000))
                        .chancedOutput(dust, Carbon, feedstock.carbonChance(severe), 0)
                        .duration(240).EUt(VA[MV]);
                int[] amounts = feedstock.amounts(severe);
                for (int i = 0; i < amounts.length; i++) {
                    distillation.outputFluids(feedstock.products().get(i).getFluid(amounts[i]));
                }
                distillation.outputFluids(steam.getCrackingResidue().getFluid(2 * CrackingYield.RESIDUE_PRODUCED)).save(provider);
            }
        }
    }
}

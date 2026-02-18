package site.siredvin.gttruesteam;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import site.siredvin.gttruesteam.recipe.condition.CoatingFluidCondition;
import site.siredvin.gttruesteam.recipe.condition.CoolingCapacityCondition;

public class TrueSteamRecipeConditions {

    public static RecipeConditionType<CoatingFluidCondition> COATING_FLUID;
    public static RecipeConditionType<CoolingCapacityCondition> COOLING_CAPACITY;

    public static void registerConditions(GTCEuAPI.RegisterEvent<String, RecipeConditionType<?>> event) {
        COATING_FLUID = GTRegistries.RECIPE_CONDITIONS.register("coating_fluid", //
                new RecipeConditionType<>(CoatingFluidCondition::new, CoatingFluidCondition.CODEC));
        COOLING_CAPACITY = GTRegistries.RECIPE_CONDITIONS.register("cooling_capacity", //
                new RecipeConditionType<>(CoolingCapacityCondition::new, CoolingCapacityCondition.CODEC));
    }
}

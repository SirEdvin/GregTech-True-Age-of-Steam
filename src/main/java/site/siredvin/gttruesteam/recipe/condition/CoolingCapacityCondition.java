package site.siredvin.gttruesteam.recipe.condition;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;

import net.minecraft.network.chat.Component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.TrueSteamLang;
import site.siredvin.gttruesteam.TrueSteamRecipeConditions;
import site.siredvin.gttruesteam.api.ICoolingMachine;

@NoArgsConstructor
public class CoolingCapacityCondition extends RecipeCondition<CoolingCapacityCondition> {

    // spotless:off
    public static final Codec<CoolingCapacityCondition> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("requiredCapacity").forGetter(CoolingCapacityCondition::getRequiredCapacity)
            ).apply(instance, CoolingCapacityCondition::new));
    // spotless:on

    public CoolingCapacityCondition(int requiredCapacity) {
        super();
        this.requiredCapacity = requiredCapacity;
    }

    @Getter
    private int requiredCapacity = 0;

    @Override
    public RecipeConditionType<CoolingCapacityCondition> getType() {
        return TrueSteamRecipeConditions.COOLING_CAPACITY;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable(TrueSteamLang.COOLING_REQUIRED_KEY, this.requiredCapacity);
    }

    @Override
    protected boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        var machine = recipeLogic.machine;
        if (machine instanceof ICoolingMachine coolingMachine) {
            return coolingMachine.getCoolingCapacity() > this.requiredCapacity;
        }
        return false;
    }

    @Override
    public CoolingCapacityCondition createTemplate() {
        return new CoolingCapacityCondition();
    }
}

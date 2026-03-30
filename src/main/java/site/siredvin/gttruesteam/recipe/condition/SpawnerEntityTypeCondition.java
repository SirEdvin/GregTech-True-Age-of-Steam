package site.siredvin.gttruesteam.recipe.condition;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.TrueSteamLang;
import site.siredvin.gttruesteam.TrueSteamRecipeConditions;
import site.siredvin.gttruesteam.api.ISpawnerExtractionCondition;

import java.util.Objects;

@NoArgsConstructor
public class SpawnerEntityTypeCondition extends RecipeCondition<SpawnerEntityTypeCondition> {

    // spotless:off
    public static final Codec<SpawnerEntityTypeCondition> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("entityType").forGetter(SpawnerEntityTypeCondition::getEntityType)
            ).apply(instance, SpawnerEntityTypeCondition::new));
    // spotless:on

    public SpawnerEntityTypeCondition(@NotNull String entityType) {
        super();
        this.entityType = entityType;
    }

    public SpawnerEntityTypeCondition(@NotNull EntityType<?> type) {
        this(Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(type)).toString());
    }

    @Getter
    private @NotNull String entityType = "";

    @Override
    public RecipeConditionType<SpawnerEntityTypeCondition> getType() {
        return TrueSteamRecipeConditions.SPAWNER_ENTITY_TYPE;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable(TrueSteamLang.SPAWNER_ENTITY_TYPE_CONDITION_KEY,
                this.getHolder().get().getDescription().getString());
    }

    public Holder<EntityType<?>> getHolder() {
        return ForgeRegistries.ENTITY_TYPES.getHolder(ResourceLocation.parse(this.entityType)).get();
    }

    @Override
    protected boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        var machine = recipeLogic.machine;
        if (machine instanceof ISpawnerExtractionCondition spawnerMachine) {
            return spawnerMachine.supportMob(getHolder());
        }
        return false;
    }

    @Override
    public SpawnerEntityTypeCondition createTemplate() {
        return new SpawnerEntityTypeCondition();
    }
}

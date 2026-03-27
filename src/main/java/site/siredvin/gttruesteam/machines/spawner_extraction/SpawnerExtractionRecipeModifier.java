package site.siredvin.gttruesteam.machines.spawner_extraction;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;

import java.util.ArrayList;

public class SpawnerExtractionRecipeModifier implements RecipeModifier {

    @Override
    public @NotNull ModifierFunction getModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!recipe.data.contains(TrueSteamRecipeTypes.LOOT_TABLE_DROPS))
            return ModifierFunction.IDENTITY;
        if (!(machine instanceof SpawnerExtractionMachineMachine spawnerMachine))
            return ModifierFunction.IDENTITY;

        return baseRecipe -> {
            var mob = spawnerMachine.getMobInside();
            if (mob == null) return baseRecipe;

            var level = machine.getLevel();
            if (!(level instanceof ServerLevel serverLevel)) return baseRecipe;

            var entityType = mob.value();
            var lootTableLoc = entityType.getDefaultLootTable();
            var lootTable = serverLevel.getServer().getLootData().getLootTable(lootTableLoc);

            var fakeEntity = entityType.create(serverLevel);
            if (fakeEntity == null) return baseRecipe;

            fakeEntity.setPos(Vec3.atCenterOf(machine.getPos()));

            var lootParams = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.THIS_ENTITY, fakeEntity)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(machine.getPos()))
                    .withParameter(LootContextParams.DAMAGE_SOURCE, serverLevel.damageSources().magic())
                    .create(LootContextParamSets.ENTITY);

            var drops = lootTable.getRandomItems(lootParams);
            if (drops.isEmpty()) return baseRecipe;

            int maxChance = ChanceLogic.getMaxChancedValue();
            var newContents = drops.stream()
                    .map(stack -> new Content(SizedIngredient.create(stack), maxChance, maxChance, 0))
                    .toList();

            var copy = baseRecipe.copy();
            var existing = copy.outputs.getOrDefault(GTRecipeCapabilities.ITEM, new ArrayList<>());
            var combined = new ArrayList<>(existing);
            combined.addAll(newContents);
            copy.outputs.put(GTRecipeCapabilities.ITEM, combined);
            return copy;
        };
    }
}

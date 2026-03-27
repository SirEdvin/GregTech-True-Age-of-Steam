package site.siredvin.gttruesteam.machines.spawner_extraction;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpawnerExtractionRecipeModifier implements RecipeModifier {

    // Wood sword base damage (3.0) as the no-reduction reference point.
    // Duration multiplier = REFERENCE_DAMAGE / totalDamage, capped at 0.25 minimum.
    private static final double REFERENCE_DAMAGE = 3.0;

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

            var sword = findSword(spawnerMachine);
            if (sword.isEmpty()) return baseRecipe;

            // Duration reduction based on total attack damage (base item + Sharpness).
            // Sharpness formula (Java 1.9+): +1.0 at level 1, +0.5 per additional level.
            double baseDamage = sword.getAttributeModifiers(EquipmentSlot.MAINHAND)
                    .get(Attributes.ATTACK_DAMAGE).stream()
                    .mapToDouble(am -> am.getAmount())
                    .sum();
            int sharpnessLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, sword);
            double sharpnessBonus = sharpnessLevel > 0 ? sharpnessLevel * 0.5 + 0.5 : 0.0;
            double totalDamage = baseDamage + sharpnessBonus;
            double durationMultiplier = totalDamage > 0 ? Math.max(0.25, REFERENCE_DAMAGE / totalDamage) : 1.0;

            // Looting level improves quantity of mob drops via LOOTING_MOD param.
            int lootingLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MOB_LOOTING, sword);

            // Roll the mob's default loot table using a temporary fake entity for context.
            var entityType = mob.value();
            var lootTable = serverLevel.getServer().getLootData()
                    .getLootTable(entityType.getDefaultLootTable());

            var fakeEntity = entityType.create(serverLevel);
            if (fakeEntity == null) return baseRecipe;
            fakeEntity.setPos(Vec3.atCenterOf(machine.getPos()));

            var lootParams = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.THIS_ENTITY, fakeEntity)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(machine.getPos()))
                    .withParameter(LootContextParams.DAMAGE_SOURCE, serverLevel.damageSources().magic())
                    .withOptionalParameter(LootContextParams.LOOTING_MOD, lootingLevel)
                    .create(LootContextParamSets.ENTITY);

            var drops = lootTable.getRandomItems(lootParams);

            var copy = baseRecipe.copy();
            copy.duration = (int) (copy.duration * durationMultiplier);

            if (!drops.isEmpty()) {
                int maxChance = ChanceLogic.getMaxChancedValue();
                var newContents = drops.stream()
                        .map(stack -> new Content(SizedIngredient.create(stack), maxChance, maxChance, 0))
                        .toList();
                var combined = new ArrayList<>(copy.outputs.getOrDefault(GTRecipeCapabilities.ITEM, List.of()));
                combined.addAll(newContents);
                copy.outputs.put(GTRecipeCapabilities.ITEM, combined);
            }

            return copy;
        };
    }

    static ItemStack findSword(SpawnerExtractionMachineMachine machine) {
        var handlers = machine.getCapabilitiesFlat()
                .getOrDefault(IO.IN, Map.of())
                .getOrDefault(GTRecipeCapabilities.ITEM, List.of());
        for (var handler : handlers) {
            for (var content : handler.getContents()) {
                if (content instanceof ItemStack stack && !stack.isEmpty() && stack.is(ItemTags.SWORDS)) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }
}

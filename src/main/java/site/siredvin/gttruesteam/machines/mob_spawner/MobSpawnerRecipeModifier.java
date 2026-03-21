package site.siredvin.gttruesteam.machines.mob_spawner;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.Tags;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.stream.Collectors;

public class MobSpawnerRecipeModifier implements RecipeModifier {

    @Override
    public @NotNull ModifierFunction getModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof MobSpawnerMachine spawnerMachine)) return ModifierFunction.IDENTITY;

        ItemStack sword = findSword(spawnerMachine);
        if (sword.isEmpty()) return ModifierFunction.IDENTITY;

        int looting = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MOB_LOOTING, sword);
        int sharpness = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, sword);

        if (looting == 0 && sharpness == 0) return ModifierFunction.IDENTITY;

        final double outputMultiplier = 1.0 + 0.5 * looting;
        final double durationMultiplier = Math.max(0.1, 1.0 - 0.1 * sharpness);

        return baseRecipe -> {
            var copy = baseRecipe.copy();

            // Multiply fluid outputs based on looting
            if (looting > 0) {
                var outputFluids = copy.outputs.get(GTRecipeCapabilities.FLUID);
                if (outputFluids != null) {
                    var newFluids = outputFluids.stream().map(x -> {
                        if (x.content instanceof FluidIngredient fluidIngredient) {
                            var stackCopy = fluidIngredient.copy();
                            stackCopy.setAmount((int) (stackCopy.getAmount() * outputMultiplier));
                            return new Content(stackCopy, x.chance, x.maxChance, x.tierChanceBoost);
                        }
                        return x;
                    }).collect(Collectors.toList());
                    copy.outputs.put(GTRecipeCapabilities.FLUID, newFluids);
                }
            }

            // Reduce duration based on sharpness
            if (sharpness > 0) {
                copy.duration = (int) Math.max(1, copy.duration * durationMultiplier);
            }

            return copy;
        };
    }

    public static ItemStack findSword(MobSpawnerMachine machine) {
        var inputHandlers = machine.getCapabilitiesFlat()
                .getOrDefault(IO.IN, Collections.emptyMap())
                .getOrDefault(ItemRecipeCapability.CAP, Collections.emptyList());

        for (var handler : inputHandlers) {
            if (handler instanceof NotifiableItemStackHandler itemHandler) {
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    ItemStack stack = itemHandler.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.is(Tags.Items.TOOLS_SWORDS)) {
                        return stack;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }
}

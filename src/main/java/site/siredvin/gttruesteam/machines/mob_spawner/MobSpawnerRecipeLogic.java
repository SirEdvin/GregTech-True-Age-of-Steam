package site.siredvin.gttruesteam.machines.mob_spawner;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.Tags;

import java.util.Collections;
import java.util.Random;

public class MobSpawnerRecipeLogic extends RecipeLogic {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MobSpawnerRecipeLogic.class,
            RecipeLogic.MANAGED_FIELD_HOLDER);

    private static final Random RANDOM = new Random();

    public MobSpawnerRecipeLogic(IRecipeLogicMachine machine) {
        super(machine);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onRecipeFinish() {
        super.onRecipeFinish();
        damageSword();
    }

    private void damageSword() {
        if (!(machine instanceof MobSpawnerMachine spawnerMachine)) return;

        var inputHandlers = spawnerMachine.getCapabilitiesFlat()
                .getOrDefault(IO.IN, Collections.emptyMap())
                .getOrDefault(ItemRecipeCapability.CAP, Collections.emptyList());

        for (var handler : inputHandlers) {
            if (handler instanceof NotifiableItemStackHandler itemHandler) {
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    ItemStack stack = itemHandler.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.is(Tags.Items.TOOLS_SWORDS)) {
                        applySwordDamage(itemHandler, i, stack);
                        return;
                    }
                }
            }
        }
    }

    private void applySwordDamage(NotifiableItemStackHandler itemHandler, int slot, ItemStack sword) {
        // If the item is not damageable or is unbreakable, skip
        if (!sword.isDamageableItem()) return;

        // Check Unbreaking enchantment - roll to prevent damage
        int unbreaking = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, sword);
        if (unbreaking > 0 && RANDOM.nextInt(unbreaking + 1) != 0) return;

        int newDamage = sword.getDamageValue() + 1;
        if (newDamage >= sword.getMaxDamage()) {
            // Sword breaks
            itemHandler.extractItem(slot, 1, false);
        } else {
            sword.setDamageValue(newDamage);
        }
    }
}

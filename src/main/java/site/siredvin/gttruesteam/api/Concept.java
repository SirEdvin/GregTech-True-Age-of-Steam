package site.siredvin.gttruesteam.api;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;

import com.tterrag.registrate.util.entry.ItemEntry;

import java.util.function.Consumer;

public interface Concept {

    Material getMaterial();

    ItemEntry<Item> getCatalyst();

    void registerRecipes(Consumer<FinishedRecipe> provider);
}

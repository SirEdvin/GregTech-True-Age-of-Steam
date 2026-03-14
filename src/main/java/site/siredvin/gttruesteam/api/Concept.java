package site.siredvin.gttruesteam.api;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;

import com.tterrag.registrate.util.entry.ItemEntry;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public interface Concept {

    @NotNull
    Material getMaterial();

    @NotNull
    Material getInfusedAir();

    List<ItemEntry<Item>> getCatalysts();

    void registerRecipes(Consumer<FinishedRecipe> provider);
}

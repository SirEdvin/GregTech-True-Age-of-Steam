package site.siredvin.gttruesteam.common;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;

import com.tterrag.registrate.util.entry.ItemEntry;
import lombok.Getter;
import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamItems;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;
import site.siredvin.gttruesteam.recipe.condition.InnerRecipeTypeCondition;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.SHINY;

public class RecipeConcept extends AbstractConcept {

    @Getter
    private int circuit;

    @Getter
    private GTRecipeType recipeType;

    public RecipeConcept(Material material, Material infusingMaterial, ItemEntry<Item> catalyst, int circuit,
                         GTRecipeType recipeType) {
        super(material, infusingMaterial, List.of(catalyst));
        this.circuit = circuit;
        this.recipeType = recipeType;
    }

    @Override
    public void registerRecipes(Consumer<FinishedRecipe> provider) {
        var catalyst = this.catalysts.get(0);
        TrueSteamRecipeTypes.CONCEPT_INFUSION.recipeBuilder(catalyst.getId())
                .inputItems(TrueSteamItems.EmptyCatalyst)
                .EUt(8)
                .circuitMeta(circuit)
                .duration(3600)
                .outputItems(catalyst)
                .addCondition(new InnerRecipeTypeCondition(recipeType.registryName.toLanguageKey()))
                .save(provider);
        super.registerRecipes(provider);
    }

    public static RecipeConcept create(String name, int primaryColor, GTRecipeType recipeType, int circuit) {
        return create(name, primaryColor, recipeType, circuit, Collections.emptyList(), null);
    }

    public static RecipeConcept create(String name, int primaryColor, GTRecipeType recipeType, int circuit,
                                       Collection<MaterialFlag> flags) {
        return create(name, primaryColor, recipeType, circuit, flags, null);
    }

    public static RecipeConcept create(String name, int primaryColor, GTRecipeType recipeType, int circuit,
                                       Collection<MaterialFlag> flags,
                                       @Nullable Consumer<Material.Builder> buildingHook) {
        var materialBuilder = new Material.Builder(GTTrueSteam.id(name + "_infused_cometal"))
                .ingot(3).fluid()
                .appendFlags(flags)
                .color(primaryColor).secondaryColor(0x032620)
                .iconSet(SHINY);
        if (buildingHook != null) {
            buildingHook.accept(materialBuilder);
        }
        var material = materialBuilder.buildAndRegister();
        var infusingMaterial = new Material.Builder(GTTrueSteam.id(name + "_infused_air"))
                .gas().color(primaryColor).buildAndRegister();
        var catalyst = GTTrueSteam.REGISTRATE
                .item(name + "_catalyst", Item::new)
                .initialProperties(() -> new Item.Properties().stacksTo(8))
                .defaultModel()
                .register();
        return new RecipeConcept(material, infusingMaterial, catalyst, circuit, recipeType);
    }
}

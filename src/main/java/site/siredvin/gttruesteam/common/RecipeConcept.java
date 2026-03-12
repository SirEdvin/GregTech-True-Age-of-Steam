package site.siredvin.gttruesteam.common;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;

import com.tterrag.registrate.util.entry.ItemEntry;
import lombok.Getter;
import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamItems;
import site.siredvin.gttruesteam.TrueSteamMaterials;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;
import site.siredvin.gttruesteam.api.Concept;
import site.siredvin.gttruesteam.recipe.condition.InnerRecipeTypeCondition;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.SHINY;

public class RecipeConcept implements Concept {

    @Getter
    private Material material;

    @Getter
    private int circuit;

    @Getter
    private ItemEntry<Item> catalyst;

    @Getter
    private GTRecipeType recipeType;

    public RecipeConcept(Material material, ItemEntry<Item> catalyst, GTRecipeType recipeType, int circuit) {
        this.material = material;
        this.catalyst = catalyst;
        this.recipeType = recipeType;
        this.circuit = circuit;
    }

    @Override
    public void registerRecipes(Consumer<FinishedRecipe> provider) {
        TrueSteamRecipeTypes.CONCEPT_INFUSION.recipeBuilder(catalyst.getId())
                .inputItems(TrueSteamItems.EmptyCatalyst)
                .EUt(8)
                .circuitMeta(circuit)
                .duration(3600)
                .outputItems(catalyst)
                .addCondition(new InnerRecipeTypeCondition(recipeType.registryName.toLanguageKey()))
                .save(provider);
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(material.getResourceLocation())
                .notConsumable(catalyst)
                .inputFluids(TrueSteamMaterials.ConceptualizedSteel.getFluid(144))
                .outputFluids(material.getFluid(144))
                .EUt(256)
                .duration(600)
                .save(provider);
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
        var catalyst = GTTrueSteam.REGISTRATE
                .item(name + "_catalyst", Item::new)
                .initialProperties(() -> new Item.Properties().stacksTo(8))
                .defaultModel()
                .register();
        return new RecipeConcept(material, catalyst, recipeType, circuit);
    }
}

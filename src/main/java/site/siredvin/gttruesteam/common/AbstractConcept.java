package site.siredvin.gttruesteam.common;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;

import com.tterrag.registrate.util.entry.ItemEntry;
import lombok.Getter;
import site.siredvin.gttruesteam.TrueSteamMaterials;
import site.siredvin.gttruesteam.api.Concept;

import java.util.List;
import java.util.function.Consumer;

public class AbstractConcept implements Concept {

    @Getter
    protected Material material;
    @Getter
    protected Material infusedAir;
    @Getter
    protected List<ItemEntry<Item>> catalysts;

    public AbstractConcept(Material material, Material infusedAir, List<ItemEntry<Item>> catalysts) {
        this.material = material;
        this.infusedAir = infusedAir;
        this.catalysts = catalysts;
    }

    @Override
    public void registerRecipes(Consumer<FinishedRecipe> provider) {
        var builder = GTRecipeTypes.MIXER_RECIPES.recipeBuilder(material.getResourceLocation());
        this.catalysts.forEach(builder::notConsumable);
        builder.inputFluids(TrueSteamMaterials.ConceptualizedSteel.getFluid(Constants.FLUID_BLOCK))
                .outputFluids(material.getFluid(Constants.FLUID_BLOCK))
                .EUt(256)
                .duration(600)
                .save(provider);
        var infusedBuilder = GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder(infusedAir.getResourceLocation())
                .inputFluids(GTMaterials.Oxygen.getFluid(1000), GTMaterials.Nitrogen.getFluid(3900));
        this.catalysts.forEach(infusedBuilder::notConsumable);
        infusedBuilder.outputFluids(infusedAir.getFluid(10000))
                .EUt(132)
                .duration(300)
                .save(provider);
    }
}

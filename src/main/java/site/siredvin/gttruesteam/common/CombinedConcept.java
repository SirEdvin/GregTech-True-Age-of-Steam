package site.siredvin.gttruesteam.common;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag;

import net.minecraft.world.item.Item;

import com.tterrag.registrate.util.entry.ItemEntry;
import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.api.Concept;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.SHINY;

public class CombinedConcept extends AbstractConcept {

    public CombinedConcept(Material material, Material infusingMaterial, List<ItemEntry<Item>> catalysts) {
        super(material, infusingMaterial, catalysts);
    }

    public static CombinedConcept create(String name, int primaryColor, List<Concept> concepts) {
        return create(name, primaryColor, concepts, Collections.emptyList(), null);
    }

    public static CombinedConcept create(String name, int primaryColor, List<Concept> concepts,
                                         Collection<MaterialFlag> flags) {
        return create(name, primaryColor, concepts, flags, null);
    }

    public static CombinedConcept create(String name, int primaryColor, List<Concept> concepts,
                                         Collection<MaterialFlag> flags,
                                         @javax.annotation.Nullable Consumer<Material.Builder> buildingHook) {
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
        return new CombinedConcept(material, infusingMaterial,
                concepts.stream().flatMap(x -> x.getCatalysts().stream()).toList());
    }
}

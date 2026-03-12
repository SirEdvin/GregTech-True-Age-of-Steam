package site.siredvin.gttruesteam;

import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import site.siredvin.gttruesteam.api.Concept;
import site.siredvin.gttruesteam.common.RecipeConcept;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrueSteamConcepts {

    public static List<Concept> CONCEPTS = new ArrayList<>();
    public static Concept ExtractionConcept = register(
            RecipeConcept.create("extraction", 0x113c44, GTRecipeTypes.EXTRACTOR_RECIPES, 0));
    public static Concept CompressionConcept = register(
            RecipeConcept.create("compression", 0x434d14, GTRecipeTypes.COMPRESSOR_RECIPES, 1));
    public static Concept HeatingConcept = register(
            RecipeConcept.create("heating", 0x872913, GTRecipeTypes.FLUID_HEATER_RECIPES, 2));
    public static Concept PolarizationConcept = register(
            RecipeConcept.create(
                    "polarization", 0x1993b0,
                    GTRecipeTypes.POLARIZER_RECIPES, 3, Collections.emptyList(),
                    builder -> builder.secondaryColor(0x9c2c16)));

    public static Concept register(Concept concept) {
        CONCEPTS.add(concept);
        return concept;
    }

    public static void sayHi() {}
}

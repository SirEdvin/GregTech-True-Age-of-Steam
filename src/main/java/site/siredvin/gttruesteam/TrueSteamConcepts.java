package site.siredvin.gttruesteam;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import site.siredvin.gttruesteam.api.Concept;
import site.siredvin.gttruesteam.common.CombinedConcept;
import site.siredvin.gttruesteam.common.RecipeConcept;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrueSteamConcepts {

    public static List<Concept> CONCEPTS = new ArrayList<>();
    public static Concept ExtractionConcept = register(
            RecipeConcept.create("extraction", 0x113c44, GTRecipeTypes.EXTRACTOR_RECIPES, 0));
    public static Concept CompressionConcept = register(
            RecipeConcept.create(
                    "compression", 0x434d14, GTRecipeTypes.COMPRESSOR_RECIPES, 1,
                    List.of(MaterialFlags.GENERATE_ROD, MaterialFlags.GENERATE_LONG_ROD,
                            MaterialFlags.GENERATE_SPRING, MaterialFlags.GENERATE_PLATE,
                            MaterialFlags.GENERATE_FRAME)));
    public static Concept HeatingConcept = register(
            RecipeConcept.create("heating", 0x872913, GTRecipeTypes.FLUID_HEATER_RECIPES, 2, List.of(
                    MaterialFlags.GENERATE_PLATE, MaterialFlags.GENERATE_ROD),
                    builder -> builder.fluidPipeProperties(5400, 150, true)));
    public static Concept PolarizationConcept = register(
            RecipeConcept.create(
                    "polarization", 0x1993b0,
                    GTRecipeTypes.POLARIZER_RECIPES, 3, Collections.emptyList(),
                    builder -> builder.secondaryColor(0x9c2c16)));

    public static Concept SteamConcept = register(
            RecipeConcept.create(
                    "steam", 0xc9c9c9, GTRecipeTypes.STEAM_TURBINE_FUELS, 4, Collections.emptyList()));

    public static Concept CoolingConcept = register(
            CombinedConcept.create("cooling", 0x107AB0, List.of(HeatingConcept, PolarizationConcept), List.of(
                    MaterialFlags.GENERATE_FOIL)));

    public static Concept register(Concept concept) {
        CONCEPTS.add(concept);
        return concept;
    }

    public static void sayHi() {}
}

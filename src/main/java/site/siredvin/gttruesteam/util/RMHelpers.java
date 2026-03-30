package site.siredvin.gttruesteam.util;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

public class RMHelpers {

    public static int calculateOC(GTRecipe recipe) {
        var euT = recipe.getInputEUt().voltage();
        for (int i = GTValues.V.length - 1; i > -1; i--) {
            if (GTValues.V[i] < euT) {
                return i;
            }
        }
        return 0;
    }
}

package site.siredvin.gttruesteam.recipe;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import java.util.List;

import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

/** Pinned GTCEu 7.5.1 feedstock and distillation mappings. */
public record CrackingFeedstock(Material raw, Material light, Material severe, List<Material> products,
                               int[] lightAmounts, int[] severeAmounts) {

    public static List<CrackingFeedstock> all() {
        var oilProducts = List.of(Toluene, Benzene, Butene, Butadiene, Propane, Propene, Ethane, Ethylene, Methane);
        var heavyProducts = new java.util.ArrayList<>(List.of(LightFuel, Naphtha));
        heavyProducts.addAll(oilProducts);
        var lightProducts = new java.util.ArrayList<>(List.of(HeavyFuel, Naphtha));
        lightProducts.addAll(oilProducts);
        var naphthaProducts = new java.util.ArrayList<>(List.of(HeavyFuel, LightFuel));
        naphthaProducts.addAll(oilProducts);
        return List.of(
                new CrackingFeedstock(LightFuel, LightlySteamCrackedLightFuel, SeverelySteamCrackedLightFuel, lightProducts,
                        new int[] {150,400,40,200,75,60,20,150,10,50,50},
                        new int[] {50,100,30,150,65,50,50,250,50,250,250}),
                new CrackingFeedstock(HeavyFuel, LightlySteamCrackedHeavyFuel, SeverelySteamCrackedHeavyFuel, heavyProducts,
                        new int[] {300,50,25,125,25,15,3,30,5,50,50},
                        new int[] {100,125,80,400,80,50,10,100,15,150,150}),
                new CrackingFeedstock(Naphtha, LightlySteamCrackedNaphtha, SeverelySteamCrackedNaphtha, naphthaProducts,
                        new int[] {75,150,40,150,80,150,15,200,35,200,200},
                        new int[] {25,50,20,100,50,50,15,300,65,500,500}),
                new CrackingFeedstock(RefineryGas, LightlySteamCrackedGas, SeverelySteamCrackedGas,
                        List.of(Propene, Ethane, Ethylene, Methane, Helium),
                        new int[] {45,8,85,1026,20}, new int[] {8,45,92,1018,20}));
    }

    public Material regular(boolean isSevere) {
        return isSevere ? severe : light;
    }

    public int[] amounts(boolean isSevere) {
        return isSevere ? severeAmounts : lightAmounts;
    }

    public String carbonChance(boolean isSevere) {
        return isSevere && raw != RefineryGas ? "1/3" : "1/9";
    }
}

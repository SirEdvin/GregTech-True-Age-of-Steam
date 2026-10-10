package site.siredvin.gttruesteam.recipe;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import java.util.List;

import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

/** Supported materials; recipe contents are discovered from GTCEu's builders. */
public record CrackingFeedstock(Material raw, Material light, Material severe) {

    public static List<CrackingFeedstock> all() {
        return List.of(
                new CrackingFeedstock(LightFuel, LightlySteamCrackedLightFuel, SeverelySteamCrackedLightFuel),
                new CrackingFeedstock(HeavyFuel, LightlySteamCrackedHeavyFuel, SeverelySteamCrackedHeavyFuel),
                new CrackingFeedstock(Naphtha, LightlySteamCrackedNaphtha, SeverelySteamCrackedNaphtha),
                new CrackingFeedstock(RefineryGas, LightlySteamCrackedGas, SeverelySteamCrackedGas));
    }

    public Material regular(boolean isSevere) {
        return isSevere ? severe : light;
    }
}

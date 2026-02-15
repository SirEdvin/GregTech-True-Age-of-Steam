package site.siredvin.gttruesteam;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttributes;

import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class TrueSteamMaterials {

    // Steam related gases
    public static Material WarmSteam;
    public static Material HotSteam;
    public static Material SuperhotSteam;
    public static Material PressurisedSteam;
    public static Material HighPressurisedSteam;
    public static Material ExtremelyPressurisedSteam;

    // Coolants
    public static Material HydrochloricAcidSlurry;
    public static Material OverheatedHydrochloricAcid;

    // Ingots
    public static Material SteamTemperedBronze;
    public static Material CorrosionTemperedAlloy;

    // Misc crap
    public static Material SteamOxygenMixture;

    private static Material buildSteam(String name, int temp, int color, int secondaryColor) {
        return new Material.Builder(GTTrueSteam.id(name))
                .components(Hydrogen, 2, Oxygen, 1)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .gas(temp).color(color).secondaryColor(secondaryColor).buildAndRegister();
    }

    private static void initSteams() {
        WarmSteam = buildSteam("warm_steam", 450, 0xD9D9D9, 0xFFF1C1);
        HotSteam = buildSteam("hot_steam", 600, 0xB3B3B3, 0xFFB347);
        SuperhotSteam = buildSteam("superhot_steam", 750, 0x7A7A7A, 0xFF6A00);
        PressurisedSteam = buildSteam("pressurized_steam", 900, 0xD3D3D3, 0xC0C0C0);
        HighPressurisedSteam = buildSteam("high_pressurized_steam", 2400, 0xD3D3D3, 0xA9A9A9);
        ExtremelyPressurisedSteam = buildSteam("extremely_pressurized_steam", 6000, 0xD3D3D3, 0x808080);
    }

    private static void initCoolants() {
        HydrochloricAcidSlurry = new Material.Builder(GTTrueSteam.id("hydrochloric_acid_slurry"))
                .components(HydrochloricAcid, 2, SaltWater, 1)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .liquid(new FluidBuilder().temperature(700).attribute(FluidAttributes.ACID)).color(0xFFFDD0)
                .buildAndRegister();
        OverheatedHydrochloricAcid = new Material.Builder(GTTrueSteam.id("overheated_hydrochloric_acid"))
                .components(HydrochloricAcid, 1)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .liquid(new FluidBuilder().temperature(700).attribute(FluidAttributes.ACID)).color(0xF2F0EF)
                .buildAndRegister();
    }

    private static void initSolidMaterials() {
        SteamTemperedBronze = new Material.Builder(GTTrueSteam.id("steam_tempered_bronze"))
                .components(Bronze, 2, Steam, 1)
                .ingot()
                .flags(
                        MaterialFlags.DISABLE_DECOMPOSITION,
                        MaterialFlags.GENERATE_PLATE,
                        MaterialFlags.GENERATE_ROD,
                        MaterialFlags.GENERATE_FRAME)
                .color(0xA97142).buildAndRegister();
        CorrosionTemperedAlloy = new Material.Builder(GTTrueSteam.id("corrosion_tempered_alloy"))
                .components(CobaltBrass, 1, Oxygen, 1)
                .ingot()
                .flags(
                        MaterialFlags.DISABLE_DECOMPOSITION,
                        MaterialFlags.GENERATE_PLATE,
                        MaterialFlags.GENERATE_ROD,
                        MaterialFlags.GENERATE_FRAME)
                .color(0x0F5132).buildAndRegister();
    }

    private static void initMisc() {
        SteamOxygenMixture = new Material.Builder(GTTrueSteam.id("steam_oxygen_mixture"))
                .components(Steam, 3, Oxygen, 2)
                .gas(300).flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .color(0xA3B8BF).buildAndRegister();
    }

    public static void init() {
        initSteams();
        initCoolants();
        initMisc();
        initSolidMaterials();
    }
}

package site.siredvin.gttruesteam;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class TrueSteamMaterials {

    // Steam related gases
    public static Material HellishWater;

    // Coolants
    public static Material ActivatedBlaze;
    public static Material OverchargedBlaze;
    public static Material OverheatedInfernalSlurry;
    public static Material InfernalSlurry;
    public static Material OverheatedInfernalEmulsion;
    public static Material InfernalEmulsion;
    public static Material DilutedBlaze;
    public static Material InfernalSlug;
    public static Material InfernalTar;
    public static Material ActivatedInfernalTar;
    public static Material OverheatedInfernalSludge;
    public static Material InfernalSludge;

    // Ingots
    public static Material CorrosionTemperedBrass;
    public static Material LavaCoatedSteel;
    public static Material FrostbiteMagnalium;
    public static Material AluminiumBronze;
    public static Material InfernalAlloy;
    public static Material ConceptualizedSteel;
    public static Material EstrangedSteel;
    public static Material StainlessInfernalSteel;
    public static Material InfernalCompound;

    private static void initEnrichers() {
        ActivatedBlaze = new Material.Builder(GTTrueSteam.id("activated_blaze"))
                .components(Blaze, 1, NetherAir, 3, Redstone, 1)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .liquid(4000)
                .color(0xfff94d, false).secondaryColor(0xff1f0a).iconSet(FINE)
                .buildAndRegister();
        OverchargedBlaze = new Material.Builder(GTTrueSteam.id("overcharged_blaze"))
                .liquid(5000)
                .color(0xfff963, false).secondaryColor(0xff1f0a).iconSet(FINE)
                .buildAndRegister();
        OverheatedInfernalSlurry = new Material.Builder(GTTrueSteam.id("overheated_infernal_slurry"))
                .liquid(600)
                .color(0xfff94d, false).secondaryColor(0xe01300).iconSet(FINE)
                .buildAndRegister();
        InfernalSlurry = new Material.Builder(GTTrueSteam.id("infernal_slurry"))
                .liquid()
                .color(0xfff94d, false).secondaryColor(0xa30e00).iconSet(FINE)
                .buildAndRegister();
        OverheatedInfernalEmulsion = new Material.Builder(GTTrueSteam.id("overheated_infernal_emulsion"))
                .liquid(600)
                .color(0xff884d, false).secondaryColor(0xe01300).iconSet(FINE)
                .buildAndRegister();
        InfernalEmulsion = new Material.Builder(GTTrueSteam.id("infernal_emulsion"))
                .liquid()
                .color(0xff7f4d, false).secondaryColor(0xa30e00).iconSet(FINE)
                .buildAndRegister();
        InfernalSlug = new Material.Builder(GTTrueSteam.id("infernal_slug"))
                .dust()
                .color(0xfff94d, false).secondaryColor(0xa30e00).iconSet(RADIOACTIVE)
                .buildAndRegister();
        DilutedBlaze = new Material.Builder(GTTrueSteam.id("diluted_blaze"))
                .components(Blaze, 2, Sulfur, 1)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .liquid(4000)
                .color(0xfff94d, false).secondaryColor(0xff1f0a).iconSet(FINE)
                .buildAndRegister();
        InfernalTar = new Material.Builder(GTTrueSteam.id("infernal_tar"))
                .liquid(350)
                .flags(MaterialFlags.FLAMMABLE, MaterialFlags.STICKY)
                .color(0x451a1a).buildAndRegister();
        ActivatedInfernalTar = new Material.Builder(GTTrueSteam.id("activate_infernal_tar"))
                .liquid(2500)
                .flags(MaterialFlags.FLAMMABLE, MaterialFlags.STICKY)
                .color(0x561a1a).buildAndRegister();
        OverheatedInfernalSludge = new Material.Builder(GTTrueSteam.id("overheated_infernal_sludge"))
                .liquid(600)
                .color(0x8c2a2a).iconSet(FINE)
                .buildAndRegister();
        InfernalSludge = new Material.Builder(GTTrueSteam.id("infernal_sludge"))
                .liquid(300)
                .color(0x6b2020).iconSet(FINE)
                .buildAndRegister();
    }

    private static void initSolidMaterials() {
        CorrosionTemperedBrass = new Material.Builder(GTTrueSteam.id("corrosion_tempered_brass"))
                .components(CobaltBrass, 1, Oxygen, 1)
                .ingot()
                .flags(
                        MaterialFlags.DISABLE_DECOMPOSITION,
                        MaterialFlags.GENERATE_PLATE,
                        MaterialFlags.GENERATE_ROD,
                        MaterialFlags.GENERATE_FRAME)
                .color(0x0F5132).secondaryColor(0x104c52).iconSet(METALLIC).buildAndRegister();
        LavaCoatedSteel = new Material.Builder(GTTrueSteam.id("lava_coated_steel"))
                .components(Steel, 1)
                .ingot(3)
                .flags(
                        MaterialFlags.DISABLE_DECOMPOSITION,
                        MaterialFlags.GENERATE_PLATE,
                        MaterialFlags.GENERATE_ROD,
                        MaterialFlags.GENERATE_FRAME)
                .color(0xa38e8e).secondaryColor(0x121c37).iconSet(METALLIC).buildAndRegister();
        FrostbiteMagnalium = new Material.Builder(GTTrueSteam.id("frostbite_magnalium"))
                .components(Magnalium, 1)
                .ingot(3)
                .flags(
                        MaterialFlags.DISABLE_DECOMPOSITION,
                        MaterialFlags.GENERATE_PLATE,
                        MaterialFlags.GENERATE_ROD,
                        MaterialFlags.GENERATE_FRAME,
                        MaterialFlags.GENERATE_FOIL)
                .color(0x81aceb).secondaryColor(0x57739e).iconSet(METALLIC).buildAndRegister();
        AluminiumBronze = new Material.Builder(GTTrueSteam.id("aluminium_bronze"))
                .components(Aluminium, 1, Bronze, 1)
                .ingot(3)
                .flags().color(0xD89C7D).secondaryColor(0x916954).iconSet(METALLIC).buildAndRegister();
        InfernalAlloy = new Material.Builder(GTTrueSteam.id("infernal_alloy"))
                .ingot(3)
                .flags(
                        MaterialFlags.GENERATE_PLATE,
                        MaterialFlags.GENERATE_ROD,
                        MaterialFlags.GENERATE_FRAME,
                        MaterialFlags.GENERATE_LONG_ROD)
                .fluidPipeProperties(4100, 10, false)
                .color(0xdb4c24).secondaryColor(0x6b2511).iconSet(METALLIC).buildAndRegister();
        ConceptualizedSteel = new Material.Builder(GTTrueSteam.id("conceptualized_steel"))
                .ingot(3).fluid()
                .blast(b -> b.temp(1700, BlastProperty.GasTier.LOW)
                        .blastStats(VA[HV], 1100))
                .components(StainlessSteel, 1, EnderPearl, 1, Amethyst, 1)
                .flags(
                        MaterialFlags.GENERATE_PLATE,
                        MaterialFlags.GENERATE_ROD,
                        MaterialFlags.GENERATE_FRAME,
                        MaterialFlags.GENERATE_FINE_WIRE)
                .fluidPipeProperties(2600, 100, true, true, true, false)
                .color(0xb3f5ea).secondaryColor(0x032620)
                .cableProperties(V[HV], 1, 2)
                .iconSet(METALLIC).buildAndRegister();
        EstrangedSteel = new Material.Builder(GTTrueSteam.id("estranged_steel"))
                .ingot(3)
                .flags(
                        MaterialFlags.GENERATE_PLATE,
                        MaterialFlags.GENERATE_ROD,
                        MaterialFlags.GENERATE_LONG_ROD,
                        MaterialFlags.GENERATE_FOIL)
                .color(0x785eeb).secondaryColor(0x6446eb).iconSet(METALLIC).buildAndRegister();
        StainlessInfernalSteel = new Material.Builder(GTTrueSteam.id("stainless_infernal_steel"))
                .ingot(3)
                .flags(
                        MaterialFlags.GENERATE_PLATE,
                        MaterialFlags.GENERATE_ROD,
                        MaterialFlags.GENERATE_LONG_ROD,
                        MaterialFlags.GENERATE_BOLT_SCREW)
                .blast(b -> b.temp(1700, BlastProperty.GasTier.LOW)
                        .blastStats(VA[HV], 1100))
                .color(0xe64c24).secondaryColor(0x822511).iconSet(SHINY)
                .buildAndRegister();
        InfernalCompound = new Material.Builder(GTTrueSteam.id("infernal_compound"))
                .gem()
                .flags(
                        MaterialFlags.GENERATE_LENS,
                        MaterialFlags.CRYSTALLIZABLE)
                .color(0xe64c24).secondaryColor(0x822511).iconSet(SHINY)
                .buildAndRegister();
    }

    private static void initMisc() {}

    public static void init() {
        HellishWater = new Material.Builder(GTTrueSteam.id("hellish_water"))
                .liquid(400).color(0x780408).secondaryColor(0x6C2DC7).buildAndRegister();
        initEnrichers();
        initMisc();
        initSolidMaterials();
    }
}

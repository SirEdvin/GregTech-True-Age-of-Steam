package site.siredvin.gttruesteam.crackingfixture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import site.siredvin.gttruesteam.TrueSteamSteams;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Mod.EventBusSubscriber(modid = "gttruesteam", value = Dist.CLIENT)
public final class CrackingClientChecks {
    private static boolean finished;

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (!Boolean.getBoolean("gttruesteam.crackingTest") || finished || event.phase != TickEvent.Phase.END) return;
        var minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof TitleScreen || minecraft.screen instanceof AccessibilityOnboardingScreen)) return;
        finished = true;
        var report = new JsonObject();
        var checks = new JsonArray();
        try {
            var variants = List.of(TrueSteamSteams.SUPERHOT, TrueSteamSteams.HELLISH);
            String[] names = { "Supercritical Steam Cracking Residue", "Most Hellish Steam Cracking Residue" };
            int[] colors = { 0x9666CC, 0x663399 };
            for (int i = 0; i < variants.size(); i++) {
                var residue = variants.get(i).getCrackingResidue();
                String key = "material.gttruesteam." + residue.getName();
                require(I18n.get(key).equals(names[i]), "localized material name " + key);
                require(I18n.get(residue.getFluid().getFluidType().getDescriptionId()).equals(names[i]), "localized fluid name " + key);
                require(ForgeRegistries.FLUIDS.getKey(residue.getFluid()).equals(residue.getResourceLocation()), "registered fluid ID " + key);
                require(residue.getMaterialRGB() == colors[i], "fluid color " + key);
                checks.add(names[i] + ": material/fluid names, registry ID, and color verified");
            }
            report.addProperty("passed", true);
        } catch (Throwable failure) {
            report.addProperty("passed", false);
            report.addProperty("failure", failure.toString());
            failure.printStackTrace();
        }
        report.add("checks", checks);
        try {
            Files.writeString(Path.of("cracking-client.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        } catch (Exception failure) { throw new RuntimeException(failure); }
        minecraft.stop();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

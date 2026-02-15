package site.siredvin.gttruesteam;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;

import site.siredvin.gttruesteam.machines.industrial_fluid_pressurizer.IndustrialFluidPressurizer;

import static com.lowdragmc.lowdraglib.gui.texture.ProgressTexture.FillDirection.LEFT_TO_RIGHT;

public class TrueSteamRecipeTypes {

    public static String PURE_CYCLES_DATA_KEY = "pure_cycles";

    public static GTRecipeType INDUSTRIAL_FLUID_PRESSURIZER = GTRecipeTypes
            .register("industrial_fluid_pressurizer", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 0, 2, 2)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setIconSupplier(() -> IndustrialFluidPressurizer.MACHINE.getItem().getDefaultInstance())
            .setSound(GTSoundEntries.COMPRESSOR);

    public static GTRecipeType FLUID_COOLING = GTRecipeTypes.register("fluid_cooling", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 0, 1, 1)
            .setEUIO(IO.NONE)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.TURBINE);

    public static void init() {
        GTRecipeTypes.FLUID_HEATER_RECIPES.addDataInfo(data -> {
            if (data.contains(PURE_CYCLES_DATA_KEY)) {
                return "Cleans for " + data.getInt(PURE_CYCLES_DATA_KEY) + " cycles";
            }
            return "";
        });
    }
}

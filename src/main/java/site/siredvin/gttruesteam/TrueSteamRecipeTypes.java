package site.siredvin.gttruesteam;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;

import com.gregtechceu.gtceu.integration.xei.entry.fluid.FluidEntryList;
import com.gregtechceu.gtceu.integration.xei.entry.fluid.FluidStackList;
import com.gregtechceu.gtceu.integration.xei.handlers.fluid.CycleFluidEntryHandler;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import site.siredvin.gttruesteam.machines.coating_shrine.CoatingShrine;
import site.siredvin.gttruesteam.machines.cooling_box.CoolingBox;
import site.siredvin.gttruesteam.machines.industrial_heater.InfernalBoiler;
import site.siredvin.gttruesteam.recipe.condition.CoatingFluidCondition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.lowdragmc.lowdraglib.gui.texture.ProgressTexture.FillDirection.LEFT_TO_RIGHT;

public class TrueSteamRecipeTypes {

    public static String INFERNAL_CYCLES_DATA_KEY = "infernal_cycles";
    public static String OVERHEATED_KEY = "overheated";
    public static String COOLING_CONSUMED = "cooling_consumed";
//
//    public static GTRecipeType INDUSTRIAL_FLUID_PRESSURIZER = GTRecipeTypes
//            .register("industrial_fluid_pressurizer", GTRecipeTypes.MULTIBLOCK)
//            .setMaxIOSize(1, 0, 2, 2)
//            .setEUIO(IO.IN)
//            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
//            .setIconSupplier(() -> IndustrialFluidPressurizer.MACHINE.getItem().getDefaultInstance())
//            .setSound(GTSoundEntries.COMPRESSOR);

    public static GTRecipeType FLUID_COOLING = register("fluid_cooling", GTRecipeTypes.MULTIBLOCK, "Fluid cooling")
            .setMaxIOSize(1, 0, 1, 1)
            .setEUIO(IO.NONE)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setIconSupplier(() -> CoolingBox.MACHINE.getItem().getDefaultInstance())
            .addDataInfo((data) -> {
                if (data.contains(COOLING_CONSUMED)) {
                    return "Cooling consumed: " + data.getInt(COOLING_CONSUMED);
                }
                return "";
            })
            .setSound(GTSoundEntries.TURBINE);
    public static GTRecipeType METAPHYSICAL_BOILING = register("metaphysical_boiling", GTRecipeTypes.MULTIBLOCK, "Metaphysical boiling")
            .setMaxIOSize(1, 1, 1, 2)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setIconSupplier(() -> InfernalBoiler.INFERNAL_BOILER.asStack())
            .setUiBuilder((recipe, widgetGroup) -> {
                if (recipe.data.contains(INFERNAL_CYCLES_DATA_KEY)) {
                    widgetGroup.addWidget(new LabelWidget(
                            -7,
                            widgetGroup.getSize().height,
                            Component.translatable(TrueSteamLang.CHARGING_CYCLES_KEY,recipe.data.getInt(INFERNAL_CYCLES_DATA_KEY))
                    ));
                    widgetGroup.setSizeHeight(widgetGroup.getSizeHeight() + 10);
                }
                if (recipe.data.contains(OVERHEATED_KEY)) {
                    widgetGroup.addWidget(new LabelWidget(
                            -7,
                            widgetGroup.getSize().height,
                            TrueSteamLang.OVERHEATABLE
                    ));
                    widgetGroup.setSizeHeight(widgetGroup.getSizeHeight() + 10);
                }
            });
    public static GTRecipeType COATING = register("coating", GTRecipeTypes.MULTIBLOCK, "Coating")
            .setMaxIOSize(1, 1, 0, 0)
            .setEUIO(IO.NONE)
            .setIconSupplier(() -> CoatingShrine.MACHINE.getItem().getDefaultInstance())
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setUiBuilder((recipe, widgetGroup) -> {
                List<Fluid> fluids = new ArrayList<>();
                for (RecipeCondition condition : recipe.conditions) {
                    if (condition instanceof CoatingFluidCondition coatingFluid) {
                        fluids.add(coatingFluid.getRequiredFluidRecord());
                    }
                }
                if (fluids.isEmpty()) {
                    return;
                }

                int xOffset = 35;
                int yOffset = 0;
                int i = 0;
                for (Fluid set : fluids) {
                    List<FluidEntryList> slots = Collections.singletonList(FluidStackList.of(new FluidStack(set, 1000)));
                    TankWidget tank = new TankWidget(new CycleFluidEntryHandler(slots),
                            widgetGroup.getSize().width - 30 - xOffset, widgetGroup.getSize().height - 30 + yOffset,
                            false, false)
                            .setBackground(GuiTextures.FLUID_SLOT).setShowAmount(false);
                    widgetGroup.addWidget(tank);

                    i++;
                    xOffset = 20 * (2 - (i % 3)) - 5;
                    yOffset = 20 * (i / 3);
                }
            });

    @SuppressWarnings("deprecation")
    public static GTRecipeType register(String name, String group, String displayName, RecipeType<?>... proxyRecipes) {
        var id = GTTrueSteam.id(name);
        var recipeType = new GTRecipeType(id, group, proxyRecipes);
        GTRegistries.register(BuiltInRegistries.RECIPE_TYPE, recipeType.registryName, recipeType);
        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, recipeType.registryName, new GTRecipeSerializer());
        GTRegistries.RECIPE_TYPES.register(recipeType.registryName, recipeType);
        GTTrueSteam.REGISTRATE.addRawLang(id.toLanguageKey(), displayName);
        return recipeType;
    }

    public static void init() {
    }
}

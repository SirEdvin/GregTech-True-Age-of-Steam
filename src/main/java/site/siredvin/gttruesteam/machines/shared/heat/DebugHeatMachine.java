package site.siredvin.gttruesteam.machines.shared.heat;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.capability.recipe.IO;

import net.minecraft.network.chat.Component;

import java.util.Iterator;
import java.util.List;

/** Creative-only recipe-driven test machine; deliberately permits overheating. */
public final class DebugHeatMachine extends HeatMultiblockMachine {
    private final boolean producer;

    public DebugHeatMachine(IMachineBlockEntity holder, boolean producer) {
        super(holder);
        this.producer = producer;
    }

    @Override
    public double getHeatCapacity() { return 1000; }

    @Override
    public double getMaxTemperature() { return 310; }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new RecipeLogic(this) {
            @Override
            protected ActionResult matchRecipe(GTRecipe recipe) {
                return recipe.inputs.isEmpty() && recipe.outputs.isEmpty() && !recipe.hasTick() ?
                        ActionResult.SUCCESS : super.matchRecipe(recipe);
            }

            @Override
            protected ActionResult handleRecipeIO(GTRecipe recipe, IO io) {
                return recipe.inputs.isEmpty() && recipe.outputs.isEmpty() && !recipe.hasTick() ?
                        ActionResult.SUCCESS : super.handleRecipeIO(recipe, io);
            }

            @Override
            public Iterator<GTRecipe> searchRecipe() {
                // Input-free recipes have no capability proxies for the normal indexed lookup.
                return getLevel().getRecipeManager().getAllRecipesFor(getRecipeType()).iterator();
            }

            @Override
            public void handleRecipeWorking() {
                if (!producer && changeHeat(-1, true) != -1) {
                    setWaiting(Component.translatable("gttruesteam.debug_heat.insufficient"));
                    return;
                }
                super.handleRecipeWorking();
            }
        };
    }

    @Override
    public boolean onWorking() {
        if (!super.onWorking()) return false;
        double delta = producer ? 1 : -1;
        return changeHeat(delta, false) == delta;
    }

    public void addDisplayText(List<Component> text) {
        text.add(Component.translatable("gttruesteam.heat.temperature", String.format(java.util.Locale.ROOT, "%.6g", getTemperature())));
        text.add(Component.translatable("gttruesteam.heat.stored", String.format(java.util.Locale.ROOT, "%.6g", getStoredHeat())));
        text.add(Component.translatable("gttruesteam.heat.capacity", String.format(java.util.Locale.ROOT, "%.6g", getHeatCapacity())));
        text.add(Component.translatable("gttruesteam.heat.maximum", String.format(java.util.Locale.ROOT, "%.6g", getMaxTemperature())));
        if (isMelting()) text.add(Component.translatable("gttruesteam.heat.melting", getMeltingTicksRemaining()));
    }
}

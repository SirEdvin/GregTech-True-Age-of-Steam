package site.siredvin.gttruesteam.machines.shared.heat;

import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import net.minecraft.world.entity.player.Player;
import net.minecraft.ChatFormatting;
import java.util.Locale;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.capability.recipe.IO;

import net.minecraft.network.chat.Component;

import java.util.Iterator;
import java.util.List;

/** Creative-only recipe-driven test machine; deliberately permits overheating. */
public final class DebugHeatMachine extends HeatMultiblockMachine implements IFancyUIMachine {
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
        text.add(Component.translatable(producer ? "gttruesteam.debug_heat.producing" : "gttruesteam.debug_heat.consuming").withStyle(ChatFormatting.GOLD));
        text.add(Component.translatable("gttruesteam.debug_heat." + (!isFormed() ? "incomplete" : getRecipeLogic().isWorking() ? "working" : getRecipeLogic().isWaiting() ? "waiting" : "idle"))
                .withStyle(getRecipeLogic().isWorking() ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
        text.add(Component.translatable("gttruesteam.heat.temperature", String.format(Locale.ROOT, "%.6g", getTemperature())));
        text.add(Component.translatable("gttruesteam.heat.stored", String.format(Locale.ROOT, "%.6g", getStoredHeat())));
        text.add(Component.translatable("gttruesteam.heat.capacity", String.format(Locale.ROOT, "%.6g", getHeatCapacity())));
        text.add(Component.translatable("gttruesteam.heat.maximum", String.format(Locale.ROOT, "%.6g", getMaxTemperature())));
        if (isMelting()) text.add(Component.translatable("gttruesteam.heat.melting", getMeltingTicksRemaining()));
        for (int i = 2; i < text.size(); i++) {
            text.set(i, text.get(i).copy().withStyle(i == 2 || i == 3 ? ChatFormatting.AQUA : ChatFormatting.GRAY));
        }
        if (isMelting()) text.set(text.size() - 1, text.get(text.size() - 1).copy().withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 190, 125);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(new DraggableScrollableWidgetGroup(4, 4, 182, 117)
                .setBackground(GuiTextures.DISPLAY)
                .addWidget(new LabelWidget(4, 5, getBlockState().getBlock().getDescriptionId()))
                .addWidget(new ComponentPanelWidget(4, 19, this::addDisplayText)
                        .textSupplier(isRemote() ? null : this::addDisplayText).setMaxWidthLimit(170)));
        return group;
    }

    @Override
    public ModularUI createUI(Player player) {
        return new ModularUI(198, 208, this, player)
                .widget(new FancyMachineUIWidget(this, 198, 208));
    }
}

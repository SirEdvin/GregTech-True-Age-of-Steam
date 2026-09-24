package site.siredvin.gttruesteam.machines.parts;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import site.siredvin.gttruesteam.common.Constants;
import site.siredvin.gttruesteam.machines.shared.heat.HeatMultiblockMachine;
import site.siredvin.gttruesteam.machines.shared.heat.HeatNetworkManager;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class HeatHatchMachine extends TieredPartMachine implements IFancyUIMachine, IMachineLife {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            HeatHatchMachine.class, TieredPartMachine.MANAGED_FIELD_HOLDER);
    @Persisted
    private String heatIdentity = UUID.randomUUID().toString();
    @DescSynced
    private int ownerStatus;
    @DescSynced
    private double displayHeat;
    @DescSynced
    private double displayCapacity;
    @DescSynced
    private double displayTemperature;
    @DescSynced
    private double displayMaximum;
    @DescSynced
    private boolean displayMelting;
    @DescSynced
    private int displayCountdown;

    public HeatHatchMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
        coefficient(tier);
    }

    public static double coefficient(int tier) {
        return switch (tier) {
            case GTValues.HV -> Constants.HEAT_HV_COEFFICIENT;
            case GTValues.EV -> Constants.HEAT_EV_COEFFICIENT;
            case GTValues.IV -> Constants.HEAT_IV_COEFFICIENT;
            case GTValues.LuV -> Constants.HEAT_LUV_COEFFICIENT;
            default -> throw new IllegalArgumentException("Unsupported heat hatch tier: " + tier);
        };
    }

    public double sendingCoefficient() {
        return coefficient(getTier());
    }

    public String heatIdentity() {
        return heatIdentity;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public boolean canShared() {
        return false;
    }

    public record Owner(int status, HeatMultiblockMachine machine) {}

    /** Never call getControllers(): its lazy rebuilding lookup can access unavailable chunks. */
    public Owner resolveOwner() {
        if (!(getLevel() instanceof ServerLevel level) || isInValid() || controllerPositions.size() != 1) return new Owner(0, null);
        var pos = controllerPositions.iterator().next();
        if (!(HeatNetworkManager.loadedMachine(level, pos) instanceof HeatMultiblockMachine heat) ||
                heat.isInValid() || !heat.isFormed() || heat.getMultiblockState().hasError() ||
                heat.getParts().stream().noneMatch(part -> part == this)) return new Owner(0, null);
        if (!heat.hasValidThermalStructure()) return new Owner(2, null);
        return new Owner(1, heat);
    }

    public void refreshDisplay() {
        if (!(getLevel() instanceof ServerLevel)) return;
        Owner owner = resolveOwner();
        ownerStatus = owner.status();
        var heat = owner.machine();
        displayHeat = heat == null ? 0 : heat.getStoredHeat();
        displayCapacity = heat == null ? 0 : heat.getHeatCapacity();
        displayTemperature = heat == null ? 0 : heat.getTemperature();
        displayMaximum = heat == null ? 0 : heat.getMaxTemperature();
        displayMelting = heat != null && heat.isMelting();
        displayCountdown = heat == null ? 0 : heat.getMeltingTicksRemaining();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel level) {
            HeatNetworkManager.registerHatch(level, getPos());
            subscribeServerTick(this::refreshDisplay);
        }
    }

    @Override
    public void onUnload() {
        if (getLevel() instanceof ServerLevel level) HeatNetworkManager.unregisterHatch(level, getPos());
        super.onUnload();
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        refreshDisplay();
    }

    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        refreshDisplay();
    }

    @Override
    public void onMachineRemoved() {
        // This hook precedes onUnload and ownership detachment, unlike generic invalidation.
        for (var controller : List.copyOf(controllers)) {
            if (controller instanceof HeatMultiblockMachine heat) heat.destroyMeltingStructure(getPos());
        }
    }

    private void displayText(List<Component> lines) {
        lines.add(Component.translatable("gttruesteam.heat.tier", GTValues.VN[getTier()]));
        lines.add(Component.translatable("gttruesteam.heat.coefficient", sendingCoefficient()));
        if (ownerStatus != 1) {
            lines.add(Component.translatable(ownerStatus == 2 ? "gttruesteam.heat.invalid" : "gttruesteam.heat.unavailable"));
            return;
        }
        lines.add(Component.translatable("gttruesteam.heat.temperature", formatValue(displayTemperature)));
        lines.add(Component.translatable("gttruesteam.heat.stored", formatValue(displayHeat)));
        lines.add(Component.translatable("gttruesteam.heat.capacity", formatValue(displayCapacity)));
        lines.add(Component.translatable("gttruesteam.heat.maximum", formatValue(displayMaximum)));
        if (displayMelting) lines.add(Component.translatable("gttruesteam.heat.melting", displayCountdown));
    }

    private static String formatValue(double value) {
        return String.format(Locale.ROOT, "%.6g", value);
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 220, 150);
        group.addWidget(new ComponentPanelWidget(4, 4, this::displayText).setMaxWidthLimit(212));
        return group;
    }
}

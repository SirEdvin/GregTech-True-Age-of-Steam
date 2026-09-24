package site.siredvin.gttruesteam.machines.redstone;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;

import org.jetbrains.annotations.Nullable;
import site.siredvin.gttruesteam.api.RedstoneObservable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RedstoneHatchMachine extends TieredPartMachine implements IFancyUIMachine {

    public static final PartAbility ABILITY = new PartAbility("gttruesteam_redstone_output");
    private List<RedstoneRule> rules = List.of();
    private int output;

    public RedstoneHatchMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
        capacityForTier(tier);
    }

    public static int capacityForTier(int tier) {
        return switch (tier) {
            case GTValues.LV -> 1;
            case GTValues.MV -> 2;
            case GTValues.HV -> 3;
            case GTValues.EV -> 4;
            case GTValues.IV -> 5;
            case GTValues.LuV -> 6;
            default -> throw new IllegalArgumentException("Unsupported redstone hatch tier");
        };
    }

    public static TraceabilityPredicate optionalPredicate() {
        return Predicates.abilities(ABILITY).setMaxGlobalLimited(1);
    }

    public int capacity() {
        return capacityForTier(getTier());
    }

    public List<RedstoneRule> rules() {
        return rules;
    }

    public int output() {
        return output;
    }

    @Nullable
    public RedstoneObservable provider() {
        if (!(getLevel() instanceof ServerLevel level) || controllerPositions.size() != 1) return null;
        var pos = controllerPositions.iterator().next();
        var chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk == null) return null;
        var machine = MetaMachine.getMachine(chunk, pos);
        if (machine instanceof IMultiController controller && controller.isFormed() &&
                !machine.isInValid() && machine instanceof RedstoneObservable observable) return observable;
        return null;
    }

    public boolean saveRule(int index, RedstoneRule rule) {
        var provider = provider();
        if (isRemote() || provider == null || !rule.isValid() || index < 0 || index > rules.size() ||
                index >= capacity() || provider.redstoneValues().stream().noneMatch(
                        value -> value.id().equals(rule.valueId()) && value.type() == rule.type())) return false;
        var updated = new ArrayList<>(rules);
        if (index == updated.size()) updated.add(rule);
        else updated.set(index, rule);
        replaceRules(updated);
        return true;
    }

    public boolean deleteRule(int index) {
        if (isRemote() || index < 0 || index >= rules.size()) return false;
        var updated = new ArrayList<>(rules);
        updated.remove(index);
        replaceRules(updated);
        return true;
    }

    public boolean moveRule(int index, int destination) {
        if (isRemote() || index < 0 || index >= rules.size() || destination < 0 || destination >= rules.size()) return false;
        var updated = new ArrayList<>(rules);
        Collections.swap(updated, index, destination);
        replaceRules(updated);
        return true;
    }

    private void replaceRules(List<RedstoneRule> updated) {
        rules = List.copyOf(updated);
        getHolder().getSelf().setChanged();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        output = 0;
        subscribeServerTick(() -> setOutput(RedstoneRule.evaluate(rules, capacity(), provider())));
    }

    private void setOutput(int signal) {
        if (output != signal) {
            output = signal;
            if (getLevel() != null && !isRemote()) updateSignal();
        }
    }

    @Override
    public void removedFromController(IMultiController controller) {
        setOutput(0);
        super.removedFromController(controller);
    }

    @Override
    public void onUnload() {
        setOutput(0);
        super.onUnload();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        if (!isRemote()) updateSignal();
    }

    @Override
    public int getOutputSignal(@Nullable Direction side) {
        return side != null && side.getOpposite() == getFrontFacing() ? output : 0;
    }

    @Override
    public int getOutputDirectSignal(Direction side) {
        return getOutputSignal(side);
    }

    @Override
    public boolean canConnectRedstone(Direction side) {
        return side == getFrontFacing();
    }

    @Override
    public boolean canShared() {
        return false;
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.put("RedstoneRules", RedstoneRuleCodec.encode(rules));
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        rules = RedstoneRuleCodec.decode(tag.getList("RedstoneRules", Tag.TAG_COMPOUND), capacity());
        output = 0;
    }

    @Override
    public boolean hasPlayerInventory() {
        return false;
    }

    @Override
    public Widget createUIWidget() {
        return new RedstoneHatchUI(this);
    }
}

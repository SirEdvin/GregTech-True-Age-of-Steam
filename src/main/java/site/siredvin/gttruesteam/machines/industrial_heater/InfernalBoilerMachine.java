package site.siredvin.gttruesteam.machines.industrial_heater;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import net.minecraft.server.level.ServerLevel;

import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.TrueSteamCriteria;
import site.siredvin.gttruesteam.TrueSteamPredicates;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;
import site.siredvin.gttruesteam.TrueSteamStats;

public class InfernalBoilerMachine extends CoilWorkableElectricMultiblockMachine implements site.siredvin.gttruesteam.api.RedstoneObservable {

    @Override
    public java.util.List<Descriptor> redstoneValues() {
        return java.util.List.of(new Descriptor("heat_counter", "gttruesteam.redstone.heat_counter", Type.INTEGER),
                new Descriptor("heat_level", "gttruesteam.redstone.heat_level", Type.STRING),
                new Descriptor("cycles_until_throttle", "gttruesteam.redstone.cycles_until_throttle", Type.INTEGER));
    }

    @Override
    public java.util.Optional<Value> readRedstoneValue(String id) {
        if (!isFormed()) return java.util.Optional.empty();
        return switch (id) {
            case "heat_counter" -> java.util.Optional.of(Value.integer(getRecipeLogic().getCycleCounter()));
            case "heat_level" -> java.util.Optional.of(Value.string(getRecipeLogic().getHeatLevel().name()));
            case "cycles_until_throttle" -> java.util.Optional.of(Value.integer(getRecipeLogic().getInfernalCharges()));
            default -> java.util.Optional.empty();
        };
    }

    private int resetCounter = 0;

    public InfernalBoilerMachine(IMachineBlockEntity holder) {
        super(holder);
        this.subscribeServerTick(() -> {
            if (!this.isActive()) {
                this.resetCounter++;
                if (this.resetCounter >= 20 * this.getCoilType().getLevel()) {
                    this.getRecipeLogic().decreaseCycleCounter();
                    this.resetCounter = 0;
                }
            }
        });
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object @NotNull... args) {
        return new InfernalBoilerRecipeLogic(this);
    }

    @Override
    public @NotNull InfernalBoilerRecipeLogic getRecipeLogic() {
        return (InfernalBoilerRecipeLogic) super.getRecipeLogic();
    }

    public boolean isBeatingHuskPresent() {
        if (!this.isFormed) {
            return false;
        }
        return getMultiblockState().getMatchContext().getOrDefault(TrueSteamPredicates.BEATING_BOILER_HUSK_MARK, false);
    }

    @Override
    public void afterWorking() {
        var logic = this.getRecipeLogic();
        logic.trackCycle(this.getCoilType().getLevel());
        var lastRecipe = logic.getLastRecipe();
        if (lastRecipe != null && lastRecipe.data.contains(TrueSteamRecipeTypes.INFERNAL_CYCLES_DATA_KEY)) {
            logic.setInfernalCharges(lastRecipe.data.getInt(TrueSteamRecipeTypes.INFERNAL_CYCLES_DATA_KEY));
            // noinspection DataFlowIssue
            if (!this.getLevel().isClientSide && this.getPlayerOwner() != null) {
                var player = ((ServerLevel) this.getLevel()).getServer()
                        .getPlayerList().getPlayer(this.getPlayerOwner().getUUID());
                if (player != null) {
                    player.awardStat(TrueSteamStats.INFERNAL_MAINTAIN_RECIPE_PERFORMED.get());
                    TrueSteamCriteria.INFERNAL_MAINTENANCE.trigger(player);
                }
            }
        }
        super.afterWorking();
    }
}

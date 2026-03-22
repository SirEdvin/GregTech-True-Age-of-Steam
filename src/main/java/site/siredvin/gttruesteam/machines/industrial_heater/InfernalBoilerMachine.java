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

public class InfernalBoilerMachine extends CoilWorkableElectricMultiblockMachine {

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

package site.siredvin.gttruesteam.machines.shared.cooling;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.TrueSteamLang;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;
import site.siredvin.gttruesteam.api.IPassiveCoolingMachine;

public class PassiveCoolingMachine extends CoolingCoilMachine implements IPassiveCoolingMachine {

    public PassiveCoolingMachine(IMachineBlockEntity holder) {
        this(holder, 1, 1);
    }

    public PassiveCoolingMachine(IMachineBlockEntity holder, int capacityFactor, int rateFactor) {
        super(holder, capacityFactor, rateFactor);
        this.subscribeServerTick(() -> {
            if (!this.isActive()) {
                var recipeLogic = this.getRecipeLogic();
                var coilType = this.getCoilType();
                recipeLogic.tickAggregatedCoolingCapacity(coilType);
            }
        });
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        var recipeLogic = getRecipeLogic();
        var lastRecipe = recipeLogic.getLastRecipe();
        if (lastRecipe != null && lastRecipe.data.contains(TrueSteamRecipeTypes.COOLING_CONSUMED)) {
            recipeLogic.setAggregatedCoolingCapacity(
                    recipeLogic.getAggregatedCoolingCapacity() -
                            lastRecipe.data.getInt(TrueSteamRecipeTypes.COOLING_CONSUMED));
        }
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        if (hand == InteractionHand.MAIN_HAND && world.isClientSide) {
            player.displayClientMessage(Component.translatable(TrueSteamLang.COOLING_CAPACITY_MESSAGE_KEY,
                    this.getRecipeLogic().getAggregatedCoolingCapacity()), false);
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public @NotNull PassiveCoolingRecipeLogic getRecipeLogic() {
        return (PassiveCoolingRecipeLogic) super.getRecipeLogic();
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object @NotNull... args) {
        return new PassiveCoolingRecipeLogic(this, (Integer) args[0], (Integer) args[1]);
    }

    @Override
    public int getCoolingCapacity() {
        return this.getRecipeLogic().getAggregatedCoolingCapacity();
    }
}

package site.siredvin.gttruesteam.machines.cooling_box;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.TrueSteamLang;
import site.siredvin.gttruesteam.TrueSteamPredicates;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;
import site.siredvin.gttruesteam.api.ICoolingCoilType;
import site.siredvin.gttruesteam.api.ICoolingMachine;
import site.siredvin.gttruesteam.common.TSCoilType;

public class CoolingBoxMachine extends WorkableMultiblockMachine implements ICoolingMachine {

    @Getter
    private ICoolingCoilType coilType = TSCoilType.FROSTBITE_MAGNALIUM;

    public CoolingBoxMachine(IMachineBlockEntity holder) {
        super(holder);
        this.subscribeServerTick(() -> {
            if (!this.isActive()) {
                var recipeLogic = this.getRecipeLogic();
                var coilType = this.getCoilType();
                recipeLogic.tickAggregatedCoolingCapacity(coilType);
            }
        });
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        var type = getMultiblockState().getMatchContext().get(TrueSteamPredicates.COOLING_COIL_TYPE_MARK);
        if (type instanceof ICoolingCoilType coil) {
            this.coilType = coil;
        }
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
    public @NotNull CoolingBoxRecipeLogic getRecipeLogic() {
        return (CoolingBoxRecipeLogic) super.getRecipeLogic();
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object @NotNull... args) {
        return new CoolingBoxRecipeLogic(this);
    }

    @Override
    public int getCoolingCapacity() {
        return this.getRecipeLogic().getAggregatedCoolingCapacity();
    }
}

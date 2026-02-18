package site.siredvin.gttruesteam.machines.coating_shrine;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.NotNull;
import site.siredvin.gttruesteam.TrueSteamLang;

import java.util.Objects;

public class CoatingShrineMachine extends WorkableMultiblockMachine {

    public CoatingShrineMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public BlockPos getFluidPos() {
        var opposite = getFrontFacing().getOpposite();
        return getPos().offset(opposite.getStepX() * 2, 0, opposite.getStepZ() * 2);
    }

    public BlockState getFluid() {
        return Objects.requireNonNull(getLevel()).getBlockState(getFluidPos());
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object @NotNull... args) {
        return new CoatingShrineRecipeLogic(this);
    }

    @Override
    public @NotNull CoatingShrineRecipeLogic getRecipeLogic() {
        return (CoatingShrineRecipeLogic) super.getRecipeLogic();
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        if (hand == InteractionHand.MAIN_HAND && world.isClientSide) {
            player.displayClientMessage(Component.translatable(TrueSteamLang.COATING_CHARGES_MESSAGE_KEY,
                    this.getRecipeLogic().getCoatingCharges()), false);
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        var recipeLogic = this.getRecipeLogic();
        recipeLogic.finishWork();
        if (recipeLogic.getCoatingCharges() <= 0) {
            var level = getLevel();
            if (level != null) {
                level.setBlockAndUpdate(getFluidPos(), Blocks.AIR.defaultBlockState());
                recipeLogic.setCoatingCharges(100);
            }
        }
    }
}

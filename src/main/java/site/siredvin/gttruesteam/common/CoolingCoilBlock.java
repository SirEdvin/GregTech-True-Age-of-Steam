package site.siredvin.gttruesteam.common;

import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.utils.GTUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.siredvin.gttruesteam.TrueSteamLang;
import site.siredvin.gttruesteam.api.ICoolingCoilType;

import java.util.List;

public class CoolingCoilBlock extends ActiveBlock {
    private ICoolingCoilType coilType;
    public CoolingCoilBlock(Properties properties, ICoolingCoilType coilType) {
        super(properties);
        this.coilType = coilType;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> tooltip,
                                @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (GTUtil.isShiftDown()) {
            tooltip.add(Component.translatable(TrueSteamLang.COIL_COOLING_CAPACITY_KEY, coilType.getCoolingCapacity()));
        } else {
            tooltip.add(Component.translatable("block.gtceu.wire_coil.tooltip_extended_info"));
        }
    }
}

package site.siredvin.gttruesteam.common;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class TooltipBlockItem extends BlockItem {

    private final Component[] extraTooltips;

    public TooltipBlockItem(Block block, Properties properties, Component... extraTooltips) {
        super(block, properties);
        this.extraTooltips = extraTooltips;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.addAll(Arrays.asList(extraTooltips));
    }
}

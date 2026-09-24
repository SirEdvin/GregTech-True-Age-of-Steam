package site.siredvin.gttruesteam.machines.redstone;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class RedstoneHatchUI extends WidgetGroup {
    private final RedstoneHatchMachine machine;
    private final WidgetGroup rows;
    private final RedstoneRuleWidget[] editors;
    private int lastCount = -1;

    public RedstoneHatchUI(RedstoneHatchMachine machine) {
        super(0, 0, 376, 218);
        this.machine = machine;
        setBackground(GuiTextures.BACKGROUND_INVERSE);
        addWidget(new WidgetGroup(4, 4, 368, 28)
                .addWidget(new ComponentPanelWidget(4, 4, lines -> {
                    lines.add(Component.translatable("gttruesteam.redstone." +
                            (machine.provider() == null ? "disconnected" : "connected")));
                    lines.add(Component.translatable("gttruesteam.redstone.capacity", machine.rules().size(),
                            machine.capacity(), machine.output()));
                }).setMaxWidthLimit(352)).setBackground(GuiTextures.DISPLAY));
        addWidget(new ComponentPanelWidget(20, 37, lines -> lines.add(Component.translatable("gttruesteam.redstone.condition"))));
        addWidget(new ComponentPanelWidget(262, 37, lines -> lines.add(Component.translatable("gttruesteam.redstone.output"))));
        rows = new WidgetGroup(0, 0, 360, RedstoneRuleWidget.HEIGHT);
        var viewport = new DraggableScrollableWidgetGroup(4, 50, 368, 164).setYScrollBarWidth(4)
                .setYBarStyle(GuiTextures.BACKGROUND_INVERSE, GuiTextures.BUTTON);
        editors = new RedstoneRuleWidget[machine.capacity()];
        // Earlier rows' dropdowns must draw and receive clicks above later rows.
        for (int index = machine.capacity() - 1; index >= 0; index--) {
            editors[index] = new RedstoneRuleWidget(machine, index, viewport);
            rows.addWidget(editors[index]);
        }
        addWidget(viewport.addWidget(rows));
        showRows(machine.rules().size());
    }

    private void showRows(int saved) {
        int count = Math.min(saved + 1, machine.capacity());
        for (int index = 0; index < editors.length; index++) editors[index].setVisible(index < count);
        rows.setSize(new Size(360, count * RedstoneRuleWidget.HEIGHT + 70));
    }

    @Override
    public void detectAndSendChanges() {
        int count = machine.rules().size();
        if (count != lastCount) {
            lastCount = count;
            showRows(count);
            writeUpdateInfo(100, buffer -> buffer.writeVarInt(count));
        }
        super.detectAndSendChanges();
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 100) showRows(buffer.readVarInt());
        else super.readUpdateInfo(id, buffer);
    }
}

package site.siredvin.gttruesteam.machines.redstone;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.SelectorWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import site.siredvin.gttruesteam.api.RedstoneObservable;
import site.siredvin.gttruesteam.api.RedstoneObservable.Type;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RedstoneHatchUI extends WidgetGroup {

    private final RedstoneHatchMachine machine;
    private int selected = -1;
    private String valueId = "";
    private Type type;
    private RedstoneRule.Operator operator;
    private String operand = "";
    private String strength = "15";
    private String message = "";
    private final TextFieldWidget operandField;
    private final ButtonWidget addButton;
    private final ButtonWidget upButton;
    private final ButtonWidget downButton;
    private final ButtonWidget deleteButton;
    private int lastFlags = -1;

    public RedstoneHatchUI(RedstoneHatchMachine machine) {
        super(0, 0, 300, 228);
        this.machine = machine;
        setBackground(GuiTextures.BACKGROUND_INVERSE);
        addWidget(new DraggableScrollableWidgetGroup(4, 4, 292, 116).setBackground(GuiTextures.DISPLAY)
                .addWidget(new ComponentPanelWidget(4, 5, this::addRuleDisplay)
                        .setMaxWidthLimit(276)
                        .clickHandler((data, click) -> {
                            if (click.isRemote) return;
                            try {
                                select(Integer.parseInt(data));
                            } catch (NumberFormatException ignored) {}
                        })));
        addButton = button(4, 124, 60, 16, key("add"), () -> select(machine.rules().size()));
        upButton = button(68, 124, 60, 16, key("up"), () -> move(-1));
        downButton = button(132, 124, 60, 16, key("down"), () -> move(1));
        deleteButton = button(196, 124, 96, 16, key("delete"), () -> {
            if (machine.deleteRule(selected)) select(Math.min(selected, machine.rules().size()));
        });
        addWidget(addButton);
        addWidget(upButton);
        addWidget(downButton);
        addWidget(deleteButton);
        var valueSelector = new SelectorWidget(4, 144, 182, 18, List.of(), -1)
                .setButtonBackground(GuiTextures.BUTTON)
                .setCandidatesSupplier(() -> descriptors().stream().map(RedstoneObservable.Descriptor::labelKey).toList())
                .setSupplier(() -> descriptors().stream().filter(value -> value.id().equals(valueId))
                        .map(RedstoneObservable.Descriptor::labelKey).findFirst().orElse(valueId))
                .setOnChanged(label -> {
                    if (isRemote()) return;
                    descriptors().stream().filter(value -> value.labelKey().equals(label)).findFirst().ifPresent(value -> {
                        valueId = value.id();
                        type = value.type();
                        operator = type == Type.BOOLEAN ? RedstoneRule.Operator.IS_TRUE : RedstoneRule.Operator.EQUAL;
                        operand = type == Type.INTEGER || type == Type.FLOAT ? "0" : "";
                    });
                });
        var operatorSelector = new SelectorWidget(190, 144, 102, 18, List.of(), -1)
                .setButtonBackground(GuiTextures.BUTTON)
                .setCandidatesSupplier(() -> Arrays.stream(RedstoneRule.Operator.values()).filter(op -> op.supports(type))
                        .map(op -> key(op.name().toLowerCase(Locale.ROOT))).toList())
                .setSupplier(() -> operator == null ? "" : key(operator.name().toLowerCase(Locale.ROOT)))
                .setOnChanged(label -> {
                    if (isRemote()) return;
                    Arrays.stream(RedstoneRule.Operator.values()).filter(op -> op.supports(type) &&
                            key(op.name().toLowerCase(Locale.ROOT)).equals(label)).findFirst().ifPresent(op -> operator = op);
                });
        operandField = boundedField(4, 182, 256, () -> operand, value -> {
            if (!isRemote() && value.length() <= 256) operand = value;
        });
        operandField.setHoverTooltips(key("operand"));
        addWidget(operandField);
        addWidget(boundedField(190, 44, 2, () -> strength, value -> {
            if (!isRemote() && value.length() <= 2) strength = value;
        }).setHoverTooltips(key("strength")));
        addWidget(button(238, 166, 54, 18, key("save"), this::save));
        addWidget(new DraggableScrollableWidgetGroup(4, 188, 292, 36).setBackground(GuiTextures.DISPLAY)
                .addWidget(new ComponentPanelWidget(4, 3, lines -> {
                    lines.add(Component.literal(currentValue()));
                    lines.add(Component.literal(message));
                    lines.add(Component.translatable(key("priority")));
                }).setSpace(1).setMaxWidthLimit(276)));
        // Popups must receive input before the fields they overlap.
        addWidget(valueSelector);
        addWidget(operatorSelector);
        select(0);
    }

    private void addRuleDisplay(List<Component> lines) {
        lines.add(Component.translatable(key(machine.provider() == null ? "disconnected" : "connected")));
        lines.add(Component.translatable(key("capacity"), machine.rules().size(), machine.capacity(), machine.output()));
        for (int index = 0; index < machine.capacity(); index++) {
            var row = Component.literal("[" + (index + 1) + "]");
            lines.add(ComponentPanelWidget.withButton(row, Integer.toString(index)).copy()
                    .append(Component.literal(" " + summary(index))));
        }
    }

    private TextFieldWidget boundedField(int x, int width, int maximum, Supplier<String> supplier,
                                        Consumer<String> responder) {
        return new TextFieldWidget(x, 166, width, 18, supplier, responder) {
            @Override
            public void handleClientAction(int id, FriendlyByteBuf buffer) {
                if (id == 1) {
                    int start = buffer.readerIndex();
                    String submitted = buffer.readUtf();
                    buffer.readerIndex(start);
                    if (submitted.length() > maximum) {
                        message = text("invalid");
                        return;
                    }
                }
                super.handleClientAction(id, buffer);
            }
        }.setMaxStringLength(maximum);
    }

    private static String key(String name) {
        return "gttruesteam.redstone." + name;
    }

    private static String text(String name, Object... args) {
        return Component.translatable(key(name), args).getString();
    }

    private ButtonWidget button(int x, int y, int width, int height, String label, Runnable action) {
        return new ButtonWidget(x, y, width, height,
                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(label)), click -> {
                    if (!click.isRemote) action.run();
                });
    }

    private List<RedstoneObservable.Descriptor> descriptors() {
        var provider = machine.provider();
        return provider == null ? List.of() : provider.redstoneValues();
    }

    private String summary(int index) {
        if (index >= machine.rules().size()) return "";
        var rule = machine.rules().get(index);
        boolean available = rule.isValid() && descriptors().stream().anyMatch(
                value -> value.id().equals(rule.valueId()) && value.type() == rule.type()) &&
                machine.provider() != null && machine.provider().readRedstoneValue(rule.valueId()).isPresent();
        String prefix = selected == index ? "> " : "";
        if (!available) return prefix + text("unavailable") + " " + rule.valueId();
        return prefix + rule.valueId() + " " + text(rule.operator().name().toLowerCase(Locale.ROOT)) + " " +
                rule.operand() + " → " + rule.strength();
    }

    private String currentValue() {
        var provider = machine.provider();
        if (provider == null || type == null) return text("unavailable");
        return text("current", text(type.name().toLowerCase(Locale.ROOT)),
                provider.readRedstoneValue(valueId).map(value -> value.value().toString()).orElse(text("unavailable")));
    }

    private void select(int index) {
        if (index < 0 || index > machine.rules().size() || index >= machine.capacity()) {
            return;
        }
        selected = index;
        message = "";
        if (index < machine.rules().size()) {
            var rule = machine.rules().get(index);
            valueId = rule.valueId();
            type = rule.type();
            operator = rule.operator();
            operand = rule.operand();
            strength = Integer.toString(rule.strength());
        } else {
            var values = descriptors();
            valueId = values.isEmpty() ? "" : values.get(0).id();
            type = values.isEmpty() ? null : values.get(0).type();
            operator = type == Type.BOOLEAN ? RedstoneRule.Operator.IS_TRUE : RedstoneRule.Operator.EQUAL;
            operand = type == Type.INTEGER || type == Type.FLOAT ? "0" : "";
            strength = "15";
        }
    }

    private void move(int delta) {
        if (machine.moveRule(selected, selected + delta)) select(selected + delta);
    }

    private void save() {
        try {
            var rule = new RedstoneRule(valueId, type, operator, operand, Integer.parseInt(strength));
            message = text(machine.saveRule(selected, rule) ? "saved" : "invalid");
        } catch (NumberFormatException ignored) {
            message = text("invalid");
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int flags = (type != null && type != Type.BOOLEAN ? 1 : 0) |
                (machine.provider() != null && machine.rules().size() < machine.capacity() ? 2 : 0) |
                (selected > 0 && selected < machine.rules().size() ? 4 : 0) |
                (selected >= 0 && selected + 1 < machine.rules().size() ? 8 : 0) |
                (selected >= 0 && selected < machine.rules().size() ? 16 : 0);
        if (flags != lastFlags) {
            lastFlags = flags;
            applyFlags(flags);
            writeUpdateInfo(100, buffer -> buffer.writeVarInt(flags));
        }
    }

    private void applyFlags(int flags) {
        operandField.setVisible((flags & 1) != 0);
        operandField.setActive((flags & 1) != 0);
        addButton.setActive((flags & 2) != 0);
        upButton.setActive((flags & 4) != 0);
        downButton.setActive((flags & 8) != 0);
        deleteButton.setActive((flags & 16) != 0);
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 100) applyFlags(buffer.readVarInt());
    }
}

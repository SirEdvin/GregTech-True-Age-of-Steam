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
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** An independent draft and atomic Save action for one rule. */
public class RedstoneRuleWidget extends WidgetGroup {
    public static final int HEIGHT = 40;
    private final RedstoneHatchMachine machine;
    private final int index;
    private final DraggableScrollableWidgetGroup viewport;
    private RedstoneRule observed;
    private String valueId = "";
    private Type type;
    private RedstoneRule.Operator operator;
    private String operand = "";
    private String strength = "15";
    private String message = "";
    private final TextFieldWidget operandField;
    private final ButtonWidget saveButton;
    private final ButtonWidget deleteButton;
    private int lastFlags = -1;

    public RedstoneRuleWidget(RedstoneHatchMachine machine, int index, DraggableScrollableWidgetGroup viewport) {
        super(0, index * HEIGHT, 360, HEIGHT);
        this.machine = machine;
        this.index = index;
        this.viewport = viewport;
        refresh();
        addWidget(new ComponentPanelWidget(3, 8, lines -> lines.add(Component.literal(Integer.toString(index + 1)))));
        var valueSelector = selector(16, 112)
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
                        message = "";
                    });
                });
        var operatorSelector = selector(132, 40)
                .setButtonBackground(GuiTextures.BUTTON)
                .setCandidatesSupplier(() -> Arrays.stream(RedstoneRule.Operator.values()).filter(op -> op.supports(type))
                        .map(op -> key(op.name().toLowerCase(Locale.ROOT))).toList())
                .setSupplier(() -> operator == null ? "" : key(operator.name().toLowerCase(Locale.ROOT)))
                .setOnChanged(label -> {
                    if (isRemote()) return;
                    Arrays.stream(RedstoneRule.Operator.values()).filter(op -> op.supports(type) &&
                            key(op.name().toLowerCase(Locale.ROOT)).equals(label)).findFirst().ifPresent(op -> operator = op);
                });
        operandField = field(176, 64, 256, () -> operand, value -> operand = value);
        operandField.setHoverTooltips(key("operand"));
        addWidget(operandField);
        addWidget(new ComponentPanelWidget(244, 8, lines -> lines.add(Component.literal("→"))));
        addWidget(field(258, 28, 2, () -> strength, value -> strength = value).setHoverTooltips(key("strength")));
        saveButton = button(290, 40, key("save"), this::save);
        deleteButton = button(334, 20, "×", () -> {
            if (machine.deleteRule(index)) refresh();
        });
        deleteButton.setHoverTooltips(key("delete"));
        addWidget(saveButton);
        addWidget(deleteButton);
        addWidget(new DraggableScrollableWidgetGroup(16, 24, 338, 14)
                .addWidget(new ComponentPanelWidget(4, 4, lines -> lines.add(Component.literal(status())))
                        .setMaxWidthLimit(324)));
        // Popups must receive input before the fields they overlap.
        addWidget(valueSelector);
        addWidget(operatorSelector);
    }

    private SelectorWidget selector(int x, int width) {
        return new SelectorWidget(x, 4, width, 18, List.of(), -1) {
            @Override
            public void setShow(boolean show) {
                if (show) {
                    int overflow = getPosition().y + getSize().height + popUp.getSize().height -
                            viewport.getPosition().y - viewport.getSize().height;
                    if (overflow > 0) viewport.setScrollYOffset(viewport.getScrollYOffset() + overflow);
                }
                super.setShow(show);
            }
        };
    }

    private static String key(String name) {
        return "gttruesteam.redstone." + name;
    }

    private static String text(String name, Object... args) {
        return Component.translatable(key(name), args).getString();
    }

    private List<RedstoneObservable.Descriptor> descriptors() {
        var provider = machine.provider();
        return provider == null ? List.of() : provider.redstoneValues();
    }

    private RedstoneRule stored() {
        return index < machine.rules().size() ? machine.rules().get(index) : null;
    }

    private void refresh() {
        observed = stored();
        message = "";
        if (observed != null) {
            valueId = observed.valueId();
            type = observed.type();
            operator = observed.operator();
            operand = observed.operand();
            strength = Integer.toString(observed.strength());
        } else {
            var values = descriptors();
            valueId = values.isEmpty() ? "" : values.get(0).id();
            type = values.isEmpty() ? null : values.get(0).type();
            operator = type == Type.BOOLEAN ? RedstoneRule.Operator.IS_TRUE : RedstoneRule.Operator.EQUAL;
            operand = type == Type.INTEGER || type == Type.FLOAT ? "0" : "";
            strength = "15";
        }
    }

    private String status() {
        if (!message.isEmpty()) return message;
        if (observed != null && !observed.isValid()) return text("invalid");
        var provider = machine.provider();
        if (provider == null || type == null || descriptors().stream().noneMatch(value ->
                value.id().equals(valueId) && value.type() == type)) return text("unavailable");
        return text("current", text(type.name().toLowerCase(Locale.ROOT)),
                provider.readRedstoneValue(valueId).map(value -> value.value().toString()).orElse(text("unavailable")));
    }

    private void save() {
        try {
            var rule = new RedstoneRule(valueId, type, operator, operand, Integer.parseInt(strength));
            if (machine.saveRule(index, rule)) {
                observed = stored();
                message = text("saved");
            } else message = text("invalid");
        } catch (NumberFormatException ignored) {
            message = text("invalid");
        }
    }

    private ButtonWidget button(int x, int width, String label, Runnable action) {
        return new ButtonWidget(x, 4, width, 18, new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(label)), click -> {
            if (!click.isRemote) action.run();
        });
    }

    private TextFieldWidget field(int x, int width, int maximum, Supplier<String> supplier, Consumer<String> responder) {
        return new TextFieldWidget(x, 4, width, 18, supplier, value -> {
            if (!isRemote()) { responder.accept(value); message = ""; }
        }) {
            @Override
            public void handleClientAction(int id, FriendlyByteBuf buffer) {
                if (id == 1) {
                    int start = buffer.readerIndex();
                    String submitted = buffer.readUtf();
                    buffer.readerIndex(start);
                    if (submitted.length() > maximum) { message = text("invalid"); return; }
                }
                super.handleClientAction(id, buffer);
            }
        }.setMaxStringLength(maximum);
    }

    @Override
    public void detectAndSendChanges() {
        if (!Objects.equals(stored(), observed) || type == null && !descriptors().isEmpty()) refresh();
        int flags = (type != null && type != Type.BOOLEAN ? 1 : 0) |
                (machine.provider() != null && index <= machine.rules().size() ? 2 : 0) |
                (index < machine.rules().size() ? 4 : 0);
        if (flags != lastFlags) {
            lastFlags = flags;
            applyFlags(flags);
            writeUpdateInfo(100, buffer -> buffer.writeVarInt(flags));
        }
        super.detectAndSendChanges();
    }

    private void applyFlags(int flags) {
        operandField.setVisible((flags & 1) != 0);
        operandField.setActive((flags & 1) != 0);
        saveButton.setActive((flags & 2) != 0);
        deleteButton.setActive((flags & 4) != 0);
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 100) applyFlags(buffer.readVarInt());
        else super.readUpdateInfo(id, buffer);
    }
}

package site.siredvin.gttruesteam.machines.industrial_heater;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import lombok.Getter;

import java.util.Locale;

public enum HeatLevel {

    NONE(0, 1, false, TextColor.fromRgb(0xD3D3D3)),
    BASIC(16, 2, false, TextColor.fromRgb(0xFFB343)),
    ADVANCED(32, 4, false, TextColor.fromRgb(0xE86100)),
    PROGRESSIVE(64, 8, true, TextColor.fromRgb(0xED2100)),
    SUPREME(128, 16, true, TextColor.fromRgb(0x81D8D0)),
    MAX(256, 16, true, TextColor.fromRgb(0x000000));

    public static final HeatLevel[] HEAT_LEVELS = new HeatLevel[] {
            SUPREME, PROGRESSIVE, ADVANCED, BASIC, NONE
    };

    @Getter
    private final int maxParallels;
    @Getter
    private final boolean perfectOverlock;
    private final TextColor color;
    @Getter
    private final int requiredLevel;

    HeatLevel(int requiredLevel, int maxParallels, boolean perfectOverlock, TextColor color) {
        this.maxParallels = maxParallels;
        this.perfectOverlock = perfectOverlock;
        this.color = color;
        this.requiredLevel = requiredLevel;
    }

    public Component component() {
        var baseName = name().toLowerCase(Locale.ROOT);
        return Component.literal(
                Character.toUpperCase(baseName.charAt(0)) + baseName.substring(1))
                .withStyle(Style.EMPTY.withColor(color));
    }
}

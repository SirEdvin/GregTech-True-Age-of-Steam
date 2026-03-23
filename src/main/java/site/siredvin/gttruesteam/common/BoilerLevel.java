package site.siredvin.gttruesteam.common;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import lombok.Getter;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public enum BoilerLevel {

    LAVA_COATED(2.0, "lc_%s"),
    INFERNAL(4.0, "ia_%s"),
    HEATING_CHARGED(8.0, "hc_%s");

    @Getter
    private final double scaling;

    @Getter
    private final String template;

    BoilerLevel(double scaling, String template) {
        this.scaling = scaling;
        this.template = template;
    }

    public String readableName() {
        String[] words = name().split("_");
        return IntStream.range(0, words.length)
                .mapToObj(
                        i -> i == 0 ? words[i].charAt(0) + words[i].substring(1).toLowerCase() : words[i].toLowerCase())
                .collect(Collectors.joining(" "));
    }

    public String boilerName(String boilerName) {
        return readableName() + " " + FormattingUtil.toEnglishName(boilerName) + " Boiler";
    }
}

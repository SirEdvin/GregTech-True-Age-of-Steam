package site.siredvin.gttruesteam.machines.industrial_gas_pressurizer;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public enum PerfectConditionState {

    REACHED,
    UNREACHABLE,
    ON_COOLDOWN,
    TOO_LOW_FLUID_LEVEL,
    TOO_HIGH_FLUID_LEVEL;

    public String readableName() {
        String[] words = name().split("_");
        return IntStream.range(0, words.length)
                .mapToObj(
                        i -> i == 0 ? words[i].charAt(0) + words[i].substring(1).toLowerCase() : words[i].toLowerCase())
                .collect(Collectors.joining(" "));
    }
}

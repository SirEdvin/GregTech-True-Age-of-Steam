package site.siredvin.gttruesteam.recipe;

import java.math.BigDecimal;

public final class CrackingYield {

    public static final int RESIDUE_PRODUCED = 100;
    public static final int RESIDUE_CONSUMED = 1000;

    private CrackingYield() {}

    public static int initialOutput(int regularOutput) {
        if (regularOutput <= 0 || regularOutput % 2 != 0) {
            throw new IllegalArgumentException("Regular cracking output must be positive and divisible by two");
        }
        return regularOutput / 2;
    }

    public static int residueOutput(int regularOutput, double coefficient) {
        int initial = initialOutput(regularOutput);
        if (!Double.isFinite(coefficient) || coefficient <= 1) {
            throw new IllegalArgumentException("Cracking yield coefficient must be finite and greater than one");
        }
        int initialCrafts = RESIDUE_CONSUMED / RESIDUE_PRODUCED;
        return BigDecimal.valueOf(coefficient)
                .multiply(BigDecimal.valueOf(regularOutput))
                .multiply(BigDecimal.valueOf(initialCrafts + 1))
                .subtract(BigDecimal.valueOf(initial).multiply(BigDecimal.valueOf(initialCrafts)))
                .intValueExact();
    }
}

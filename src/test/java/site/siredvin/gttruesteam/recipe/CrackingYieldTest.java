package site.siredvin.gttruesteam.recipe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CrackingYieldTest {

    @ParameterizedTest
    @CsvSource({ "1000, 1.5, 500, 11500", "1000, 1.7, 500, 13700", "200, 1.5, 100, 2300", "200, 1.7, 100, 2740" })
    void derivesExactElevenCraftYield(int regular, double coefficient, int initial, int recovery) {
        assertEquals(initial, CrackingYield.initialOutput(regular));
        assertEquals(recovery, CrackingYield.residueOutput(regular, coefficient));
        int crafts = CrackingYield.RESIDUE_CONSUMED / CrackingYield.RESIDUE_PRODUCED;
        assertEquals(0, CrackingYield.RESIDUE_CONSUMED % CrackingYield.RESIDUE_PRODUCED);
        assertEquals(BigDecimal.valueOf(coefficient).multiply(BigDecimal.valueOf((crafts + 1L) * regular)).intValueExact(),
                crafts * initial + recovery);
    }

    @ParameterizedTest
    @ValueSource(ints = { -1000, 0, 1, 999 })
    void rejectsInvalidRegularOutput(int regular) {
        assertThrows(IllegalArgumentException.class, () -> CrackingYield.initialOutput(regular));
        assertThrows(IllegalArgumentException.class, () -> CrackingYield.residueOutput(regular, 1.5));
    }

    @ParameterizedTest
    @ValueSource(doubles = { -1, 0, 1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY })
    void rejectsInvalidCoefficient(double coefficient) {
        assertThrows(IllegalArgumentException.class, () -> CrackingYield.residueOutput(1000, coefficient));
    }

    @Test
    void rejectsFractionalMillibucketsRatherThanTruncating() {
        assertThrows(ArithmeticException.class, () -> CrackingYield.residueOutput(2, 1.7));
    }

    @Test
    void rejectsOutputOverflow() {
        assertThrows(ArithmeticException.class, () -> CrackingYield.residueOutput(Integer.MAX_VALUE - 1, 1.5));
    }
}

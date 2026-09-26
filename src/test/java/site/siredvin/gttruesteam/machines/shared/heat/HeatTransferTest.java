package site.siredvin.gttruesteam.machines.shared.heat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeatTransferTest {

    @Test
    void ambientMidpointThresholdAndOvercapacity() {
        assertEquals(300, HeatTransfer.temperature(0, 1000, 310));
        assertEquals(305, HeatTransfer.temperature(500, 1000, 310));
        assertEquals(310, HeatTransfer.temperature(1000, 1000, 310));
        assertEquals(320, HeatTransfer.temperature(2000, 1000, 310));
        assertEquals(2000, HeatTransfer.acceptedDelta(0, 1000, 310, 2000));
    }

    @Test
    void fractionalAndBoundedExtraction() {
        assertEquals(0.5, HeatTransfer.acceptedDelta(0, 1000, 310, 0.5));
        assertEquals(-0.5, HeatTransfer.acceptedDelta(0.5, 1000, 310, -1));
        assertEquals(0, HeatTransfer.acceptedDelta(0, 1000, 310, -1));
    }

    @Test
    void rejectsInvalidDefinitionsAndNonfiniteState() {
        for (double capacity : new double[] { 0, -1, Double.NaN, Double.POSITIVE_INFINITY }) {
            assertFalse(HeatTransfer.validDefinition(capacity, 310));
            assertEquals(0, HeatTransfer.acceptedDelta(0, capacity, 310, 1));
        }
        for (double maximum : new double[] { 300, 0, Double.NaN, Double.POSITIVE_INFINITY }) {
            assertFalse(HeatTransfer.validDefinition(1000, maximum));
        }
        assertFalse(HeatTransfer.validDefinition(Double.MIN_VALUE, 310));
        assertEquals(0, HeatTransfer.acceptedDelta(0, 1000, 310, Double.NaN));
        assertEquals(0, HeatTransfer.acceptedDelta(Double.MAX_VALUE, Double.MAX_VALUE, 310, Double.MAX_VALUE));
        assertEquals(0, HeatTransfer.acceptedDelta(0, 1, 1e300, 1e300));
    }

    @Test
    void normalizesSavedHeatWithoutCapacityClamp() {
        assertEquals(0, HeatTransfer.normalize(Double.NaN));
        assertEquals(0, HeatTransfer.normalize(-1));
        assertEquals(0, HeatTransfer.normalize(Double.POSITIVE_INFINITY));
        assertEquals(Double.MAX_VALUE, HeatTransfer.normalize(Double.MAX_VALUE));
    }

    @Test
    void tierPackagesAndEquilibrium() {
        double[] coefficients = { 2, 8, 32, 128 };
        double[] expected = { 10, 40, 160, 1000.0 / 3 };
        for (int i = 0; i < coefficients.length; i++) {
            double amount = HeatTransfer.packageJoules(500, 1000, 310, 0, 2000, 310, coefficients[i]);
            assertEquals(expected[i], amount, 1e-10);
            assertEquals(500, (500 - amount) + amount, 1e-10);
            assertTrue(HeatTransfer.temperature(500 - amount, 1000, 310) + 1e-9 >=
                    HeatTransfer.temperature(amount, 2000, 310));
        }
    }

    @Test
    void receiverSafeCapacityIsNotAnAdmissionLimit() {
        double amount = HeatTransfer.packageJoules(100000, 100000, 1300, 1, 1, 310, 128);
        assertTrue(amount > 1);
        assertTrue(HeatTransfer.temperature(1 + amount, 1, 310) > 310);
    }

    @Test
    void subJouleAndEqualTemperature() {
        assertTrue(HeatTransfer.packageJoules(0.5, 1000, 310, 0, 1000, 310, 2) > 0);
        assertEquals(0, HeatTransfer.packageJoules(500, 1000, 310, 1000, 2000, 310, 128));
    }

    @Test
    void repeatedExchangesConserveEnergyWithoutOvershoot() {
        double hot = 1000.125;
        double cold = 0.0625;
        double total = hot + cold;
        for (int i = 0; i < 10000; i++) {
            double amount = HeatTransfer.packageJoules(hot, 1000, 310, cold, 2000, 310, 2);
            hot -= amount;
            cold += amount;
            assertEquals(total, hot + cold, total * 1e-12);
            assertTrue(HeatTransfer.temperature(hot, 1000, 310) + 1e-9 >= HeatTransfer.temperature(cold, 2000, 310));
        }
    }
}

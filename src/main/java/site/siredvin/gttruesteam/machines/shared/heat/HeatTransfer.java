package site.siredvin.gttruesteam.machines.shared.heat;

public final class HeatTransfer {

    public static final double AMBIENT_K = 300;
    public static final double TEMPERATURE_EPSILON = 1e-9;

    private HeatTransfer() {}

    public static boolean validDefinition(double capacity, double maximum) {
        double slope = (maximum - AMBIENT_K) / capacity;
        return Double.isFinite(capacity) && capacity > 0 && Double.isFinite(maximum) && maximum > AMBIENT_K &&
                Double.isFinite(slope) && slope > 0;
    }

    public static double normalize(double heat) {
        return Double.isFinite(heat) && heat >= 0 ? heat : 0;
    }

    public static double temperature(double heat, double capacity, double maximum) {
        if (!validDefinition(capacity, maximum) || !Double.isFinite(heat) || heat < 0) return Double.NaN;
        return AMBIENT_K + heat * ((maximum - AMBIENT_K) / capacity);
    }

    public static double acceptedDelta(double heat, double capacity, double maximum, double requested) {
        if (!Double.isFinite(requested) || !Double.isFinite(temperature(heat, capacity, maximum))) return 0;
        double next = Math.max(0, heat + requested);
        if (!Double.isFinite(next) || !Double.isFinite(temperature(next, capacity, maximum))) return 0;
        return next - heat;
    }

    /** One representable package, shared by both stores; never capped by receiver safe capacity. */
    public static double packageJoules(double hot, double hotCapacity, double hotMaximum,
                                       double cold, double coldCapacity, double coldMaximum, double coefficient) {
        double difference = temperature(hot, hotCapacity, hotMaximum) -
                temperature(cold, coldCapacity, coldMaximum);
        if (!Double.isFinite(difference) || difference <= TEMPERATURE_EPSILON ||
                !Double.isFinite(coefficient) || coefficient <= 0) return 0;
        double slopes = (hotMaximum - AMBIENT_K) / hotCapacity + (coldMaximum - AMBIENT_K) / coldCapacity;
        if (!Double.isFinite(slopes)) return 0;
        double amount = Math.min(hot, Math.min(difference / slopes, coefficient * difference));
        // Round down to the coarser endpoint's ULP so both stores accept the same binary amount.
        double quantum = Math.max(Math.ulp(hot), Math.ulp(cold + amount));
        amount = Math.floor(amount / quantum) * quantum;
        for (int attempt = 0; attempt < 4 && amount > 0 && Double.isFinite(amount); attempt++, amount -= quantum) {
            double removed = -acceptedDelta(hot, hotCapacity, hotMaximum, -amount);
            double added = acceptedDelta(cold, coldCapacity, coldMaximum, amount);
            if (removed == amount && added == amount &&
                    temperature(hot - amount, hotCapacity, hotMaximum) + TEMPERATURE_EPSILON >=
                            temperature(cold + amount, coldCapacity, coldMaximum)) return amount;
        }
        return 0;
    }
}

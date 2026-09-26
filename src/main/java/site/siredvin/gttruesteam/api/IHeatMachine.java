package site.siredvin.gttruesteam.api;

public interface IHeatMachine {

    double getStoredHeat();

    /** Safe energy threshold in joules, not a storage admission limit. */
    double getHeatCapacity();

    double getTemperature();

    double getMaxTemperature();

    /** Returns the accepted signed joules; simulation never changes thermal state. */
    double changeHeat(double deltaJoules, boolean simulate);

    boolean isMelting();

    int getMeltingTicksRemaining();
}

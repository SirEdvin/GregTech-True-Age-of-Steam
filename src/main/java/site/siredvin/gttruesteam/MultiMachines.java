package site.siredvin.gttruesteam;

import site.siredvin.gttruesteam.machines.cooling_box.CoolingBox;
import site.siredvin.gttruesteam.machines.industrial_fluid_pressurizer.IndustrialFluidPressurizer;
import site.siredvin.gttruesteam.machines.industrial_heater.IndustrialHeater;

public class MultiMachines {

    public static void sayHi() {
        IndustrialHeater.sayHi();
        IndustrialFluidPressurizer.sayHi();
        CoolingBox.sayHi();
    }
}

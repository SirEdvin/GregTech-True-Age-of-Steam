package site.siredvin.gttruesteam;

import site.siredvin.gttruesteam.machines.coating_shrine.CoatingShrine;
import site.siredvin.gttruesteam.machines.cooling_box.CoolingBox;;
import site.siredvin.gttruesteam.machines.industrial_heater.InfernalBoiler;

public class MultiMachines {

    public static void sayHi() {
        InfernalBoiler.sayHi();
//        IndustrialFluidPressurizer.sayHi();
        CoolingBox.sayHi();
        CoatingShrine.sayHi();
    }
}

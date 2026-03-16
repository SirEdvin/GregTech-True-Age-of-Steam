package site.siredvin.gttruesteam;

import site.siredvin.gttruesteam.machines.cim.ConceptInfusionMatrix;
import site.siredvin.gttruesteam.machines.coating_shrine.CoatingShrine;
import site.siredvin.gttruesteam.machines.cooling_box.CoolingBox;
import site.siredvin.gttruesteam.machines.industrial_gas_pressurizer.IndustrialGasPressurizer;
import site.siredvin.gttruesteam.machines.industrial_heater.InfernalBoiler;
import site.siredvin.gttruesteam.machines.regulated_cryo_chamber.RegulatedCryoChamber;

public class MultiMachines {

    public static void sayHi() {
        InfernalBoiler.sayHi();
        IndustrialGasPressurizer.sayHi();
        CoolingBox.sayHi();
        CoatingShrine.sayHi();
        RegulatedCryoChamber.sayHi();
        ConceptInfusionMatrix.sayHi();
    }
}

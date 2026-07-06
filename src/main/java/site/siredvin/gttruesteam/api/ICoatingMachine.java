package site.siredvin.gttruesteam.api;

import net.minecraft.world.level.material.Fluid;

public interface ICoatingMachine {

    boolean hasFluid(Fluid fluid);

    int countFluidCells(Fluid fluid);
}

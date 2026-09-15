package site.siredvin.gttruesteam.machines.cim;

import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ConceptInfusionMatrixMachineTest {

    private MetaMachine inner;
    private RecipeLogic innerLogic;

    @Before
    public void setUp() {
        inner = mock(MetaMachine.class, withSettings().extraInterfaces(IRecipeLogicMachine.class));
        innerLogic = mock(RecipeLogic.class);
        when(((IRecipeLogicMachine) inner).getRecipeLogic()).thenReturn(innerLogic);
        // GTCEu keeps this true even when the recipe is waiting for power.
        when(((IRecipeLogicMachine) inner).isActive()).thenReturn(true);
        when(innerLogic.isWorking()).thenCallRealMethod();
    }

    private void setInnerStatus(RecipeLogic.Status status) throws ReflectiveOperationException {
        var field = RecipeLogic.class.getDeclaredField("status");
        field.setAccessible(true);
        field.set(innerLogic, status);
    }

    @Test
    public void acceptsWorkingRecipe() throws ReflectiveOperationException {
        setInnerStatus(RecipeLogic.Status.WORKING);
        assertTrue(ConceptInfusionMatrixMachine.isInnerMachineRunning(inner));
    }

    @Test
    public void rejectsRecipeWaitingForPower() throws ReflectiveOperationException {
        setInnerStatus(RecipeLogic.Status.WAITING);
        assertFalse(ConceptInfusionMatrixMachine.isInnerMachineRunning(inner));
    }

    @Test
    public void rejectsIdleAndSuspendedRecipes() throws ReflectiveOperationException {
        setInnerStatus(RecipeLogic.Status.IDLE);
        assertFalse(ConceptInfusionMatrixMachine.isInnerMachineRunning(inner));
        setInnerStatus(RecipeLogic.Status.SUSPEND);
        assertFalse(ConceptInfusionMatrixMachine.isInnerMachineRunning(inner));
    }

    @Test
    public void acceptsRecipeAfterPowerReturns() throws ReflectiveOperationException {
        setInnerStatus(RecipeLogic.Status.WORKING);
        assertTrue(ConceptInfusionMatrixMachine.isInnerMachineRunning(inner));
        setInnerStatus(RecipeLogic.Status.WAITING);
        assertFalse(ConceptInfusionMatrixMachine.isInnerMachineRunning(inner));
        setInnerStatus(RecipeLogic.Status.WORKING);
        assertTrue(ConceptInfusionMatrixMachine.isInnerMachineRunning(inner));
    }

    @Test
    public void rejectsMachinesWithoutRecipeLogic() {
        var workable = mock(MetaMachine.class, withSettings().extraInterfaces(IWorkable.class));
        when(((IWorkable) workable).isActive()).thenReturn(true);
        assertFalse(ConceptInfusionMatrixMachine.isInnerMachineRunning(workable));
        assertFalse(ConceptInfusionMatrixMachine.isInnerMachineRunning(mock(MetaMachine.class)));
    }
}

package site.siredvin.gttruesteam.machines.redstone;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;

import site.siredvin.gttruesteam.api.RedstoneObservable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/** Common observations remain available without an addon-specific controller interface. */
public record MultiblockRedstoneObservations(IMultiController controller) implements RedstoneObservable {

    private static final List<Descriptor> RECIPE_VALUES = List.of(
            new Descriptor("recipe_progress_ticks", "gttruesteam.redstone.recipe_progress_ticks", Type.INTEGER),
            new Descriptor("recipe_progress_percent", "gttruesteam.redstone.recipe_progress_percent", Type.FLOAT),
            new Descriptor("recipe_duration_ticks", "gttruesteam.redstone.recipe_duration_ticks", Type.INTEGER),
            new Descriptor("recipe_id", "gttruesteam.redstone.recipe_id", Type.STRING));

    @Override
    public List<Descriptor> redstoneValues() {
        var values = new LinkedHashMap<String, Descriptor>();
        if (controller instanceof RedstoneObservable custom) {
            custom.redstoneValues().forEach(value -> values.put(value.id(), value));
        }
        if (controller instanceof IRecipeLogicMachine) {
            RECIPE_VALUES.forEach(value -> values.put(value.id(), value));
        }
        return List.copyOf(values.values());
    }

    @Override
    public Optional<Value> readRedstoneValue(String id) {
        if (!controller.isFormed()) return Optional.empty();
        if (controller instanceof IRecipeLogicMachine machine) {
            var logic = machine.getRecipeLogic();
            boolean hasRecipe = !logic.isIdle() && logic.getLastRecipe() != null;
            switch (id) {
                case "recipe_progress_ticks":
                    return Optional.of(Value.integer(hasRecipe ? logic.getProgress() : 0));
                case "recipe_progress_percent":
                    return Optional.of(Value.floating(hasRecipe && logic.getDuration() > 0 ?
                            Math.max(0.0, Math.min(100.0, 100.0 * logic.getProgress() / logic.getDuration())) : 0.0));
                case "recipe_duration_ticks":
                    return Optional.of(Value.integer(hasRecipe ? logic.getDuration() : 0));
                case "recipe_id":
                    return hasRecipe ? Optional.of(Value.string(logic.getLastRecipe().id.toString())) : Optional.empty();
            }
        }
        return controller instanceof RedstoneObservable custom ? custom.readRedstoneValue(id) : Optional.empty();
    }
}

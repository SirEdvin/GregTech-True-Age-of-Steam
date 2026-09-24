package site.siredvin.gttruesteam.machines.shared.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import site.siredvin.gttruesteam.common.Constants;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public final class HeatNetwork {

    public enum Kind { BLOCKED, VENT, HATCH }

    public record Node(Kind kind, Direction front) {}

    public interface Lookup {
        boolean loaded(BlockPos pos);

        Node node(BlockPos pos);
    }

    private record Step(BlockPos previous, BlockPos position, int distance) {}

    private HeatNetwork() {}

    public static Set<BlockPos> discover(BlockPos source, Direction front, Lookup lookup) {
        Set<BlockPos> destinations = new HashSet<>();
        Set<BlockPos> visitedVents = new HashSet<>();
        ArrayDeque<Step> queue = new ArrayDeque<>();
        queue.add(new Step(source, source.relative(front), 1));
        while (!queue.isEmpty()) {
            Step step = queue.removeFirst();
            if (step.distance() > Constants.HEAT_NETWORK_RANGE || !lookup.loaded(step.position())) continue;
            Node node = lookup.node(step.position());
            if (node.kind() == Kind.HATCH) {
                if (!step.position().equals(source) && step.position().relative(node.front()).equals(step.previous())) {
                    destinations.add(step.position());
                }
            } else if (node.kind() == Kind.VENT && visitedVents.add(step.position()) &&
                    step.distance() < Constants.HEAT_NETWORK_RANGE) {
                for (Direction direction : Direction.values()) {
                    queue.addLast(new Step(step.position(), step.position().relative(direction), step.distance() + 1));
                }
            }
        }
        return destinations;
    }
}

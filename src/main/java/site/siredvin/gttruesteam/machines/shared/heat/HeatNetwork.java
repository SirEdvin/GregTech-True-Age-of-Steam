package site.siredvin.gttruesteam.machines.shared.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import site.siredvin.gttruesteam.common.Constants;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public final class HeatNetwork {

    public enum Kind { BLOCKED, VENT, HATCH }

    public record Node(Kind kind, Direction front, int connections, double coefficient) {
        public Node(Kind kind, Direction front) { this(kind, front, 63, 16); }
        public Node(Kind kind, Direction front, int connections) { this(kind, front, connections, 16); }
        public boolean connected(Direction direction) { return (connections & (1 << direction.ordinal())) != 0; }
    }

    public interface Lookup {
        boolean loaded(BlockPos pos);

        Node node(BlockPos pos);
    }

    private record Step(BlockPos previous, BlockPos position, int distance) {}

    private HeatNetwork() {}

    public record Trace(Set<BlockPos> hatches, Set<BlockPos> vents) {}

    public record Component(Set<BlockPos> hatches, Set<BlockPos> pipes, double coefficient) {}

    /** Whole loaded component, including branches beyond the pairwise transfer range. */
    public static Component component(BlockPos source, Direction front, Lookup lookup) {
        Set<BlockPos> hatches = new HashSet<>();
        Set<BlockPos> pipes = new HashSet<>();
        hatches.add(source);
        double coefficient = Double.POSITIVE_INFINITY;
        ArrayDeque<Step> queue = new ArrayDeque<>();
        queue.add(new Step(source, source.relative(front), 1));
        while (!queue.isEmpty()) {
            Step step = queue.removeFirst();
            if (!lookup.loaded(step.position())) continue;
            Node node = lookup.node(step.position());
            Direction incoming = Direction.fromDelta(step.previous().getX() - step.position().getX(),
                    step.previous().getY() - step.position().getY(), step.previous().getZ() - step.position().getZ());
            if (node.kind() == Kind.HATCH) {
                if (node.front() == incoming) hatches.add(step.position());
            } else if (node.kind() == Kind.VENT && node.connected(incoming) && pipes.add(step.position())) {
                coefficient = Math.min(coefficient, Double.isFinite(node.coefficient()) && node.coefficient() > 0 ? node.coefficient() : 0);
                for (Direction direction : Direction.values()) if (node.connected(direction)) {
                    queue.addLast(new Step(step.position(), step.position().relative(direction), 0));
                }
            }
        }
        return new Component(Set.copyOf(hatches), Set.copyOf(pipes), pipes.isEmpty() ? 0 : coefficient);
    }

    public static Trace trace(BlockPos source, Direction front, Lookup lookup) {
        Set<BlockPos> vents = new HashSet<>();
        Set<BlockPos> hatches = discover(source, front, lookup, vents);
        return new Trace(Set.copyOf(hatches), Set.copyOf(vents));
    }

    public static Set<BlockPos> discover(BlockPos source, Direction front, Lookup lookup) {
        return discover(source, front, lookup, null);
    }

    private static Set<BlockPos> discover(BlockPos source, Direction front, Lookup lookup, Set<BlockPos> vents) {
        Set<BlockPos> destinations = new HashSet<>();
        Set<BlockPos> visitedVents = new HashSet<>();
        ArrayDeque<Step> queue = new ArrayDeque<>();
        queue.add(new Step(source, source.relative(front), 1));
        while (!queue.isEmpty()) {
            Step step = queue.removeFirst();
            if (step.distance() > Constants.HEAT_NETWORK_RANGE || !lookup.loaded(step.position())) continue;
            Node node = lookup.node(step.position());
            Direction incoming = Direction.fromDelta(step.previous().getX() - step.position().getX(),
                    step.previous().getY() - step.position().getY(), step.previous().getZ() - step.position().getZ());
            if (node.kind() == Kind.VENT && !node.connected(incoming)) continue;
            if (node.kind() == Kind.HATCH) {
                if (!step.position().equals(source) && step.position().relative(node.front()).equals(step.previous())) {
                    destinations.add(step.position());
                }
            } else if (node.kind() == Kind.VENT && visitedVents.add(step.position()) &&
                    step.distance() < Constants.HEAT_NETWORK_RANGE) {
                if (vents != null) vents.add(step.position());
                for (Direction direction : Direction.values()) {
                    if (!node.connected(direction)) continue;
                    queue.addLast(new Step(step.position(), step.position().relative(direction), step.distance() + 1));
                }
            }
        }
        return destinations;
    }
}

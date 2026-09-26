package site.siredvin.gttruesteam.machines.shared.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class HeatNetworkTest {

    private static final BlockPos SOURCE = BlockPos.ZERO;

    private static class World implements HeatNetwork.Lookup {
        final Map<BlockPos, HeatNetwork.Node> nodes = new HashMap<>();
        final Set<BlockPos> unloaded = new HashSet<>();

        void vent(int x, int y, int z) {
            nodes.put(new BlockPos(x, y, z), new HeatNetwork.Node(HeatNetwork.Kind.VENT, null));
        }

        BlockPos hatch(int x, int y, int z, Direction front) {
            BlockPos pos = new BlockPos(x, y, z);
            nodes.put(pos, new HeatNetwork.Node(HeatNetwork.Kind.HATCH, front));
            return pos;
        }

        @Override
        public boolean loaded(BlockPos pos) {
            return !unloaded.contains(pos);
        }

        @Override
        public HeatNetwork.Node node(BlockPos pos) {
            assertFalse(unloaded.contains(pos), "Must check loaded before dereferencing");
            return nodes.getOrDefault(pos, new HeatNetwork.Node(HeatNetwork.Kind.BLOCKED, null));
        }

        Set<BlockPos> discover() {
            return HeatNetwork.discover(SOURCE, Direction.EAST, this);
        }
    }

    @Test
    void directFrontFacesAndRotation() {
        var world = new World();
        var target = world.hatch(1, 0, 0, Direction.WEST);
        assertEquals(Set.of(target), world.discover());
        world.hatch(1, 0, 0, Direction.EAST);
        assertTrue(world.discover().isEmpty());
        world.hatch(1, 0, 0, Direction.WEST);
        assertTrue(HeatNetwork.discover(SOURCE, Direction.NORTH, world).isEmpty());
    }

    @Test
    void visualTraceSharesConnectivityAndCollectsVents() {
        var world = new World();
        world.vent(1, 0, 0);
        world.vent(2, 0, 0);
        var target = world.hatch(3, 0, 0, Direction.WEST);
        var trace = HeatNetwork.trace(SOURCE, Direction.EAST, world);
        assertEquals(world.discover(), trace.hatches());
        assertEquals(Set.of(target), trace.hatches());
        assertEquals(Set.of(new BlockPos(1, 0, 0), new BlockPos(2, 0, 0)), trace.vents());
        world.unloaded.add(new BlockPos(2, 0, 0));
        trace = HeatNetwork.trace(SOURCE, Direction.EAST, world);
        assertTrue(trace.hatches().isEmpty());
        assertEquals(Set.of(new BlockPos(1, 0, 0)), trace.vents());
    }

    @Test
    void directVisualConnectionNeedsNoVentAndRejectsBackFace() {
        var world = new World();
        var target = world.hatch(1, 0, 0, Direction.WEST);
        var trace = HeatNetwork.trace(SOURCE, Direction.EAST, world);
        assertEquals(Set.of(target), trace.hatches());
        assertTrue(trace.vents().isEmpty());
        world.hatch(1, 0, 0, Direction.EAST);
        assertTrue(HeatNetwork.trace(SOURCE, Direction.EAST, world).hatches().isEmpty());
    }

    @Test
    void everyDesignatedFaceSupportsDirectContact() {
        for (Direction face : Direction.values()) {
            var world = new World();
            BlockPos target = SOURCE.relative(face);
            world.hatch(target.getX(), target.getY(), target.getZ(), face.getOpposite());
            assertEquals(Set.of(target), HeatNetwork.discover(SOURCE, face, world));
        }
    }

    @Test
    void denseCyclicVolumeHasBoundedLookupCost() {
        int[] lookups = { 0 };
        var world = new HeatNetwork.Lookup() {
            @Override
            public boolean loaded(BlockPos pos) {
                return true;
            }

            @Override
            public HeatNetwork.Node node(BlockPos pos) {
                lookups[0]++;
                return new HeatNetwork.Node(HeatNetwork.Kind.VENT, null);
            }
        };
        long start = System.nanoTime();
        assertTrue(HeatNetwork.discover(SOURCE, Direction.EAST, world).isEmpty());
        long elapsed = System.nanoTime() - start;
        assertTrue(lookups[0] < 400000, "Depth bound must terminate even an infinite vent volume");
        System.out.println("Dense vent traversal: " + lookups[0] + " lookups, " + elapsed + " ns");
    }

    @Test
    void inclusive32Exclusive33AndNoPassThrough() {
        var world = new World();
        for (int x = 1; x < 32; x++) world.vent(x, 0, 0);
        var at32 = world.hatch(32, 0, 0, Direction.WEST);
        world.hatch(33, 0, 0, Direction.WEST);
        assertEquals(Set.of(at32), world.discover());
        world.vent(32, 0, 0);
        assertTrue(world.discover().isEmpty());
    }

    @Test
    void turnsVerticalBranchesLoopsAndAlternateRoutes() {
        var world = new World();
        world.vent(1, 0, 0);
        world.vent(1, 1, 0);
        world.vent(2, 1, 0);
        world.vent(2, 0, 0);
        var a = world.hatch(3, 1, 0, Direction.WEST);
        var b = world.hatch(1, 2, 0, Direction.DOWN);
        assertEquals(Set.of(a, b), world.discover());
        world.nodes.remove(new BlockPos(1, 1, 0));
        assertEquals(Set.of(a), world.discover());
        world.vent(1, 1, 0);
        assertEquals(Set.of(a, b), world.discover());
    }

    @Test
    void unloadedGapSolidAndDiagonalDoNotConduct() {
        var world = new World();
        world.vent(1, 0, 0);
        world.vent(2, 0, 0);
        world.hatch(3, 0, 0, Direction.WEST);
        world.unloaded.add(new BlockPos(2, 0, 0));
        assertTrue(world.discover().isEmpty());
        world.unloaded.clear();
        assertEquals(1, world.discover().size());
        world.nodes.remove(new BlockPos(2, 0, 0));
        world.vent(2, 1, 0);
        assertTrue(world.discover().isEmpty());
    }

    @Test
    void longDetourDoesNotBecomeRadiusSearch() {
        var world = new World();
        for (int x = 1; x <= 17; x++) world.vent(x, 0, 0);
        world.vent(17, 0, 1);
        world.vent(17, 0, 2);
        for (int x = 2; x < 17; x++) world.vent(x, 0, 2);
        world.hatch(1, 0, 2, Direction.EAST);
        assertTrue(world.discover().isEmpty());
    }
}

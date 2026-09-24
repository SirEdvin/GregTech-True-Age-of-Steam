package site.siredvin.gttruesteam.heatfixture;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamMachines;
import site.siredvin.gttruesteam.machines.parts.HeatHatchMachine;
import site.siredvin.gttruesteam.machines.shared.heat.HeatNetworkManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Real-world smoke assertions, opt-in and confined to the isolated development run. */
@Mod.EventBusSubscriber(modid = "gttruesteam")
public final class HeatRuntimeChecks {

    private static final BlockPos A = new BlockPos(0, 120, 0);
    private static final BlockPos B = new BlockPos(0, 120, 6);
    private static final BlockPos C = new BlockPos(0, 120, 12);
    private static final JsonArray checks = new JsonArray();
    private static ServerLevel world;
    private static long start;
    private static long overheatTick;
    private static long rescueTick;
    private static long insufficientTick;
    private static boolean finished;
    private static boolean reversedRegistration;
    private static int failureSounds;
    private static boolean validFailureSounds;
    private static final List<Witness> witnesses = new ArrayList<>();

    private record Witness(Pig entity, float health, Vec3 position) {}

    @SubscribeEvent
    public static void started(ServerStartedEvent event) {
        if (!Boolean.getBoolean("gttruesteam.heatTestAuto")) return;
        world = event.getServer().overworld();
        setup();
    }

    private static void setup() {
        for (Witness witness : witnesses) witness.entity().discard();
        witnesses.clear();
        overheatTick = 0;
        rescueTick = 0;
        insufficientTick = 0;
        world.setChunkForced(0, 0, true);
        world.setChunkForced(0, 1, true);
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 4; x++) world.setBlockAndUpdate(new BlockPos(x, 120, z), Blocks.AIR.defaultBlockState());
        }
        HeatFixture.Commands.place(world, A, false, 3);
        HeatFixture.Commands.place(world, B, true, 0);
        HeatFixture.Commands.place(world, C, false, 1);
        for (int z = 1; z < 6; z++) world.setBlockAndUpdate(A.offset(1, 0, z), GTBlocks.COMPUTER_HEAT_VENT.getDefaultState());
        MetaMachine.getMachine(world, A.east()).setFrontFacing(Direction.SOUTH);
        MetaMachine.getMachine(world, B.east()).setFrontFacing(Direction.NORTH);
        for (BlockPos pos : List.of(A, C)) {
            Pig pig = EntityType.PIG.create(world);
            if (pig == null) throw new AssertionError("Could not create collateral-effect witness");
            pig.setNoAi(true);
            pig.setNoGravity(true);
            pig.setPos(pos.getX() + 3.5, pos.getY(), pos.getZ() + 0.5);
            check(world.addFreshEntity(pig), "spawned vulnerable nearby collateral-effect witness");
            witnesses.add(new Witness(pig, pig.getHealth(), pig.position()));
        }
        failureSounds = 0;
        validFailureSounds = true;
        start = world.getGameTime();
        GTTrueSteam.LOGGER.info("HEAT RUNTIME CHECKS started at {}", start);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void sound(PlayLevelSoundEvent.AtPosition event) {
        if (finished || world == null || event.getLevel() != world || event.getSound() == null ||
                event.getSound().value() != SoundEvents.GENERIC_EXPLODE) return;
        var position = event.getPosition();
        if (position.x < 0 || position.x > 4 || position.y < 120 || position.y > 121 ||
                position.z < 0 || position.z > 16) return;
        failureSounds++;
        validFailureSounds &= !event.isCanceled() && event.getNewVolume() > 0 && event.getSource() == SoundSource.BLOCKS;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void tick(TickEvent.LevelTickEvent event) {
        if (finished || world == null || event.level != world || event.phase != TickEvent.Phase.END) return;
        long elapsed = world.getGameTime() - start;
        try {
            if (elapsed == 60) {
                form(A);
                form(B);
                form(C);
                HeatFixture a = controller(A);
                reverseRegistrations();
                HeatFixture b = controller(B);
                check(a.getHeatCapacity() == 1000 && a.getMaxTemperature() == 310 &&
                        b.getHeatCapacity() == 2000 && b.getMaxTemperature() == 320, "structure-derived characteristics");
                check(a.getTemperature() == 300 && a.getStoredHeat() == 0, "empty ambient store");
                check(a.changeHeat(1100, true) == 1100 && a.getStoredHeat() == 0 && !a.isMelting(), "simulation leaves energy and melting unchanged");
                check(a.changeHeat(Double.NaN, false) == 0, "nonfinite mutation rejected");
                check(a.changeHeat(1100, false) == 1100 && a.isMelting() && a.getMeltingTicksRemaining() == 40,
                        "overcapacity acceptance and fresh interval");
                check(((HeatHatchMachine) MetaMachine.getMachine(world, A.east())).resolveOwner().machine() == a,
                        "melting owner remains eligible for cooling");
            }
            if (elapsed == 81) {
                HeatFixture a = controller(A);
                HeatFixture b = controller(B);
                check(Math.abs(a.getStoredHeat() - 550) < 1e-8 && Math.abs(b.getStoredHeat() - 550) < 1e-8,
                        "real vent exchange conserves energy and reaches equilibrium");
                check(!a.isMelting() && a.getMeltingTicksRemaining() == 0, "scheduled cooling rescues and resets countdown");
                checkSounds(0, "cooling recovery emits no failure sound");
                world.removeBlock(A.offset(1, 0, 3), false);
                a.changeHeat(-a.getStoredHeat(), false);
                check(a.changeHeat(1000, false) == 1000 && !a.isMelting(), "exact capacity is safe");
                check(a.changeHeat(0.5, false) == 0.5 && a.isMelting(), "fractional threshold crossing");
                a.changeHeat(-0.5, false);
                check(!a.isMelting(), "fractional cooling recovery");
                a.changeHeat(1, false);
                overheatTick = world.getGameTime();
                check(a.getMeltingTicksRemaining() == 40, "later episode gets a full interval");
                // Dismantling the independent C structure must not wait for a level-tick deadline.
                HeatFixture c = controller(C);
                c.changeHeat(1100, false);
                world.removeBlock(C.east(2), false);
                check(MetaMachine.getMachine(world, C) == null && MetaMachine.getMachine(world, C.east()) == null,
                        "casing break immediately removes controller and own hatch");
                checkSounds(2, "casing break emits one sound per owned target");
                check(MetaMachine.getMachine(world, B) == b && world.getBlockState(B.east(2)).is(Blocks.GOLD_BLOCK),
                        "dismantling leaves unrelated controller and casing intact");
            }
            if (overheatTick > 0 && world.getGameTime() == overheatTick + 5) {
                MetaMachine.getMachine(world, A.east()).onUnload();
                check(controller(A).isMelting() && controller(A).getMeltingTicksRemaining() == 35,
                        "part unload callback neither destroys nor resets the ticking controller");
            }
            if (overheatTick > 0 && world.getGameTime() == overheatTick + 39) {
                check(controller(A).getMeltingTicksRemaining() == 1, "countdown neither expires early nor pauses without a vent path");
            }
            if (overheatTick > 0 && world.getGameTime() == overheatTick + 40) {
                check(MetaMachine.getMachine(world, A) == null && MetaMachine.getMachine(world, A.east()) == null,
                        "continuous overheating expires at the full interval");
                check(world.getBlockState(A.east(2)).is(Blocks.IRON_BLOCK) &&
                        world.getBlockState(A.offset(1, 0, 1)).is(GTBlocks.COMPUTER_HEAT_VENT.get()),
                        "expiry preserves surrounding casing and vents");
                checkSounds(4, "expiry emits one sound per owned target without repeating dismantling");
            }
            if (elapsed == 122) {
                HeatFixture.Commands.place(world, A, false, 3);
                MetaMachine.getMachine(world, A.east()).setFrontFacing(Direction.SOUTH);
                HeatFixture.Commands.place(world, C, false, 1);
                world.setBlockAndUpdate(C.east(2), TrueSteamMachines.HEAT_HATCHES.get(2).defaultBlockState());
            }
            if (elapsed == 130) {
                form(C);
                controller(C).changeHeat(1100, false);
                world.removeBlock(C, false);
                check(MetaMachine.getMachine(world, C.east()) == null && MetaMachine.getMachine(world, C.east(2)) == null,
                        "controller break retains and removes both hatch targets");
                checkSounds(7, "controller break sounds include broken controller and both hatches once");
                HeatFixture.Commands.place(world, C, false, 1);
                world.setBlockAndUpdate(C.east(2), TrueSteamMachines.HEAT_HATCHES.get(2).defaultBlockState());
            }
            if (elapsed == 138) {
                form(C);
                controller(C).changeHeat(1100, false);
                world.removeBlock(C.east(reversedRegistration ? 2 : 1), false);
                check(MetaMachine.getMachine(world, C) == null && MetaMachine.getMachine(world, C.east()) == null &&
                        MetaMachine.getMachine(world, C.east(2)) == null,
                        "hatch break retains controller and sibling targets through reentrant removal");
                checkSounds(10, "hatch break does not duplicate reentrant failure sounds");
                HeatFixture.Commands.place(world, C, true, 1);
            }
            if (elapsed == 146) {
                form(C);
                controller(C).changeHeat(1500, false);
                check(!controller(C).isMelting(), "larger structure safely holds 1500 J");
                world.setBlockAndUpdate(C.east(2), Blocks.IRON_BLOCK.defaultBlockState());
            }
            if (elapsed == 148) {
                check(controller(C).getStoredHeat() == 1500 && controller(C).isMelting(),
                        "lower-capacity reformation retains joules and starts melting");
                controller(C).changeHeat(-500, false);
                check(!controller(C).isMelting(), "cooling exactly to reduced safe capacity recovers");
                world.removeBlock(C.east(2), false);
                check(MetaMachine.getMachine(world, C) != null && MetaMachine.getMachine(world, C.east()) != null,
                        "dismantling after recovery does not trigger thermal failure");
                checkSounds(10, "recovered dismantling emits no failure sound");
            }
            if (elapsed >= 150 && rescueTick == 0 && world.getGameTime() % 20 == 0) {
                form(A);
                reverseRegistrations();
                controller(B).changeHeat(-controller(B).getStoredHeat(), false);
                controller(A).changeHeat(1001, false);
                rescueTick = world.getGameTime();
                check(controller(A).getMeltingTicksRemaining() == 40, "deadline test starts on scheduled cadence");
            }
            if (rescueTick > 0 && world.getGameTime() == rescueTick + 39) {
                check(controller(A).getMeltingTicksRemaining() == 1, "deadline rescue reaches last remaining tick");
                world.setBlockAndUpdate(A.offset(1, 0, 3), GTBlocks.COMPUTER_HEAT_VENT.getDefaultState());
            }
            if (rescueTick > 0 && world.getGameTime() == rescueTick + 40) {
                check(!controller(A).isMelting() && controller(A).getMeltingTicksRemaining() == 0,
                        "scheduled final-tick cooling cancels expiry before destruction");
                check(MetaMachine.getMachine(world, A.east()) != null, "rescued hatch survives deadline");
                checkSounds(10, "final-tick rescue emits no failure sound");
                check(Math.abs(controller(A).getStoredHeat() + controller(B).getStoredHeat() - 1001) < 1e-8,
                        "deadline rescue conserves total energy");
                world.removeBlock(A.offset(1, 0, 3), false);
                controller(A).changeHeat(-controller(A).getStoredHeat(), false);
                controller(B).changeHeat(-controller(B).getStoredHeat(), false);
                world.setBlockAndUpdate(A.east(), TrueSteamMachines.HEAT_HATCHES.get(0).defaultBlockState());
                MetaMachine.getMachine(world, A.east()).setFrontFacing(Direction.SOUTH);
            }
            if (rescueTick > 0 && world.getGameTime() >= rescueTick + 60 && insufficientTick == 0 && world.getGameTime() % 20 == 0) {
                form(A);
                reverseRegistrations();
                controller(A).changeHeat(1100, false);
                insufficientTick = world.getGameTime();
            }
            if (insufficientTick > 0 && world.getGameTime() == insufficientTick + 10) {
                controller(A).changeHeat(100, false);
                check(controller(A).getMeltingTicksRemaining() == 30, "additional heating does not restart countdown");
                controller(A).changeHeat(-100, true);
                check(controller(A).getMeltingTicksRemaining() == 30 && controller(A).getStoredHeat() == 1200,
                        "cooling simulation preserves an active episode");
                controller(A).changeHeat(-100, false);
                check(controller(A).isMelting() && controller(A).getMeltingTicksRemaining() == 30,
                        "insufficient cooling does not reset countdown");
            }
            if (insufficientTick > 0 && world.getGameTime() == insufficientTick + 39) {
                world.setBlockAndUpdate(A.offset(1, 0, 3), GTBlocks.COMPUTER_HEAT_VENT.getDefaultState());
            }
            if (insufficientTick > 0 && world.getGameTime() == insufficientTick + 40) {
                check(MetaMachine.getMachine(world, A) == null && MetaMachine.getMachine(world, A.east()) == null,
                        "insufficient final-tick cooling still destroys owned targets without extra grace tick");
                check(controller(B).getStoredHeat() == 22, "scheduled cooling happened before insufficient-rescue expiry");
                checkSounds(12, "insufficient rescue emits exactly one sound per destroyed target");
                finish(null);
            }
            if (elapsed > 300) throw new AssertionError("Runtime fixture timed out");
        } catch (Throwable failure) {
            GTTrueSteam.LOGGER.error("HEAT RUNTIME CHECKS FAILED", failure);
            finish(failure);
        }
    }

    private static HeatFixture controller(BlockPos pos) {
        var machine = MetaMachine.getMachine(world, pos);
        if (!(machine instanceof HeatFixture fixture)) throw new AssertionError("Missing fixture at " + pos);
        return fixture;
    }

    private static void reverseRegistrations() {
        if (!reversedRegistration) return;
        for (BlockPos pos : List.of(A, B, C)) {

            HeatNetworkManager.unregisterHatch(world, pos.east());
            HeatNetworkManager.unregisterHatch(world, pos.east(2));
        }
        for (BlockPos pos : List.of(C, B, A)) {
            for (BlockPos hatch : List.of(pos.east(2), pos.east())) {
                if (MetaMachine.getMachine(world, hatch) instanceof HeatHatchMachine)
                    HeatNetworkManager.registerHatch(world, hatch);
            }

        }
    }

    private static void form(BlockPos pos) {
        HeatFixture fixture = controller(pos);
        check(fixture.checkPatternWithLock(), "pattern matched at " + pos);
        fixture.onStructureFormed();
        MultiblockWorldSavedData.getOrCreate(world).addMapping(fixture.getMultiblockState());
    }

    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
        checks.add((reversedRegistration ? "reversed registration: " : "original registration: ") + name);
        GTTrueSteam.LOGGER.info("HEAT CHECK PASS: {}", name);
    }

    private static void checkSounds(int expected, String name) {
        check(validFailureSounds && failureSounds == expected, name + " (observed " + failureSounds + ")");
        for (Witness witness : witnesses) {
            Pig pig = witness.entity();
            check(pig.isAlive() && pig.getHealth() == witness.health() && !pig.isOnFire() &&
                    pig.position().equals(witness.position()) && pig.getDeltaMovement().lengthSqr() == 0,
                    name + ": nearby entity has no damage, movement, knockback or fire at " + witness.position());
        }
        boolean noFire = true;
        for (BlockPos pos : BlockPos.betweenClosed(A.offset(-1, -1, -1), C.offset(4, 2, 1))) {
            if (world.getBlockState(pos).is(Blocks.FIRE) || world.getBlockState(pos).is(Blocks.SOUL_FIRE)) noFire = false;
        }
        check(noFire, name + ": no surrounding fire");
    }

    private static void finish(Throwable failure) {
        if (failure == null && !reversedRegistration) {
            reversedRegistration = true;
            setup();
            return;
        }
        finished = true;
        for (Witness witness : witnesses) witness.entity().discard();
        witnesses.clear();
        JsonObject report = new JsonObject();
        report.addProperty("passed", failure == null);
        report.add("checks", checks);
        if (failure != null) report.addProperty("failure", failure.toString());
        try {
            Files.writeString(Path.of("heat-runtime-results.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        } catch (Exception exception) {
            GTTrueSteam.LOGGER.error("Could not save runtime check report", exception);
        }
        world.setChunkForced(0, 0, false);
        world.setChunkForced(0, 1, false);
        world.getServer().halt(false);
    }
}

package site.siredvin.gttruesteam.redstonefixture;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import site.siredvin.gttruesteam.TrueSteamBlocks;
import site.siredvin.gttruesteam.TrueSteamMachines;
import site.siredvin.gttruesteam.api.RedstoneObservable.Type;
import site.siredvin.gttruesteam.machines.industrial_gas_pressurizer.IndustrialGasPressurizer;
import site.siredvin.gttruesteam.machines.industrial_gas_pressurizer.IndustrialGasPressurizerMachine;
import site.siredvin.gttruesteam.machines.industrial_gas_pressurizer.PerfectConditionState;
import site.siredvin.gttruesteam.machines.industrial_heater.InfernalBoiler;
import site.siredvin.gttruesteam.machines.industrial_heater.InfernalBoilerMachine;
import site.siredvin.gttruesteam.machines.redstone.RedstoneHatchMachine;
import site.siredvin.gttruesteam.machines.redstone.RedstoneRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static site.siredvin.gttruesteam.machines.redstone.RedstoneRule.Operator.*;

/** Opt-in integration checks in an isolated development world; excluded from release jars. */
@Mod.EventBusSubscriber(modid = "gttruesteam")
public final class RedstoneRuntimeChecks {
    private static final JsonArray checks = new JsonArray();
    private static final ArrayDeque<Runnable> steps = new ArrayDeque<>();
    private static ServerLevel level;
    private static int ticks;
    private static boolean initialized;
    private static boolean finished;
    private static Fixture boiler;
    private static Fixture pressurizer;
    private static Fixture unsupported;
    private static int unloadStarted;
    private static BlockPos lamp;
    private static Direction lampFace;
    private static int notifications;
    private static int previousNotifications;
    private static final java.util.Set<Long> unloaded = new java.util.HashSet<>();

    @SubscribeEvent
    public static void chunkUnloaded(net.minecraftforge.event.level.ChunkEvent.Unload event) {
        if (event.getLevel() == level) unloaded.add(event.getChunk().getPos().toLong());
    }

    @SubscribeEvent
    public static void neighborNotified(net.minecraftforge.event.level.BlockEvent.NeighborNotifyEvent event) {
        if (event.getLevel() == level && boiler != null && event.getPos().equals(boiler.hatch())) notifications++;
    }

    private record Fixture(BlockPos controller, BlockPos hatch, BlockPos second, BlockState casing) {
        MultiblockControllerMachine machine() {
            return (MultiblockControllerMachine) MetaMachine.getMachine(level, controller);
        }
        RedstoneHatchMachine part() {
            return (RedstoneHatchMachine) MetaMachine.getMachine(level, hatch);
        }
        void form() {
            var machine = machine();
            machine.onStructureInvalid();
            require(machine.checkPatternWithLock(), "pattern matches at " + controller);
            machine.onStructureFormed();
            MultiblockWorldSavedData.getOrCreate(level).addMapping(machine.getMultiblockState());
        }
        void hatch(int tier) {
            machine().onStructureInvalid();
            level.setBlockAndUpdate(hatch, TrueSteamMachines.REDSTONE_HATCHES[tier].defaultBlockState());
            part().setFrontFacing(Direction.NORTH);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (!Boolean.getBoolean("gttruesteam.redstoneTest") || finished || event.phase != TickEvent.Phase.END ||
                "client".equals(System.getProperty("gttruesteam.redstoneTestPhase"))) return;
        MinecraftServer server = event.getServer();
        try {
            if (++ticks < 30) return;
            if (!initialized) {
                initialized = true;
                level = server.overworld();
                force(true);
                if ("reload".equals(System.getProperty("gttruesteam.redstoneTestPhase"))) setupReload();
                else setup();
            }
            if (ticks % 10 != 0) return;
            if (!steps.isEmpty()) steps.removeFirst().run();
            else finish(server, null);
        } catch (Throwable failure) {
            finish(server, failure);
        }
    }

    private static void force(boolean forced) {
        for (int x = 63; x <= 68; x++) for (int z = 63; z <= 65; z++) level.setChunkForced(x, z, forced);
    }

    private static Fixture build(MultiblockMachineDefinition definition, BlockPos origin, Block casingBlock) {
        var pattern = definition.getPatternFactory().get();
        int[] repetitions = Arrays.stream(pattern.aisleRepetitions).mapToInt(range -> range[0]).toArray();
        var preview = pattern.getPreview(repetitions);
        BlockPos controller = null;
        var casings = new ArrayList<BlockPos>();
        BlockPos alternative = null;
        for (int x = 0; x < preview.length; x++) for (int y = 0; y < preview[x].length; y++)
            for (int z = 0; z < preview[x][y].length; z++) {
                var info = preview[x][y][z];
                if (info == null) continue;
                var pos = origin.offset(x, y, z);
                var state = info.getBlockState();
                level.setBlockAndUpdate(pos, state);
                var machine = MetaMachine.getMachine(level, pos);
                if (state.is(definition.getBlock())) controller = pos;
                if (state.is(casingBlock)) casings.add(pos);
                if (machine instanceof FluidHatchPartMachine) alternative = pos;
            }
        require(controller != null && casings.size() >= 2, "preview contains controller and casing positions");
        MetaMachine.getMachine(level, controller).setFrontFacing(Direction.NORTH);
        // Test the shared maximum across a plain casing and a fluid/casing alternative.
        BlockPos second = alternative == null ? casings.get(casings.size() - 1) : alternative;
        if (alternative != null) level.setBlockAndUpdate(second, casingBlock.defaultBlockState());
        return new Fixture(controller, casings.get(0), second, casingBlock.defaultBlockState());
    }

    private static void setup() throws Exception {
        boiler = build(InfernalBoiler.MACHINE, new BlockPos(1024, 100, 1024), TrueSteamBlocks.InfernalAlloyCasing.get());
        pressurizer = build(IndustrialGasPressurizer.MACHINE, new BlockPos(1072, 100, 1024),
                com.gregtechceu.gtceu.common.data.GTBlocks.CASING_STAINLESS_CLEAN.get());
        steps.add(() -> { boiler.form(); pressurizer.form(); check(true, "both original structures form without redstone hatches"); });
        for (int tier = 1; tier <= 6; tier++) {
            final int t = tier;
            steps.add(() -> { boiler.hatch(t); pressurizer.hatch(t); });
            steps.add(() -> {
                boiler.form(); pressurizer.form();
                check(boiler.part().capacity() == t && pressurizer.part().capacity() == t, "tier " + t + " capacity and attachment");
                for (Fixture fixture : List.of(boiler, pressurizer)) {
                    check(fixture.part().replacePartModelWhenFormed() && fixture.casing().equals(
                            fixture.part().getFormedAppearance(level.getBlockState(fixture.hatch()), fixture.hatch(), Direction.NORTH)),
                            "tier " + t + " inherits controller casing at " + fixture.hatch());
                }
                for (int i = 0; i < t; i++) {
                    check(boiler.part().saveRule(i, heat(LESS_EQUAL, "999999", i == 0 ? t : 15)), "boiler tier rule " + t + "/" + i);
                    check(pressurizer.part().saveRule(i, ready(IS_FALSE, i == 0 ? t : 15)), "pressurizer tier rule " + t + "/" + i);
                }
                check(!boiler.part().saveRule(t, heat(EQUAL, "1", 15)), "tier " + t + " rejects excess rules");
            });
            steps.add(() -> {
                int expected = t == 1 ? t : 15;
                check(boiler.part().output() == expected && pressurizer.part().output() == expected, "tier " + t + " ORs all rules on idle formed controllers");
                check(!boiler.part().saveRule(0, heat(EQUAL, "1", 16)), "reject invalid output");

                for (Fixture fixture : List.of(boiler, pressurizer)) {
                    fixture.machine().onStructureInvalid();
                    check(!fixture.part().replacePartModelWhenFormed() && fixture.part().getFormedAppearance(
                            level.getBlockState(fixture.hatch()), fixture.hatch(), Direction.NORTH) == null,
                            "tier " + t + " restores unformed tier hull at " + fixture.hatch());
                    fixture.form();
                }
            });
        }
        steps.add(() -> {
            boiler.part().saveRule(0, heat(LESS_EQUAL, "999999", 0));
            pressurizer.part().saveRule(0, ready(IS_FALSE, 0));
        });
        steps.add(() -> {
            check(boiler.part().output() == 15 && pressurizer.part().output() == 15, "matching zero contributes zero without terminating evaluation");

            for (Direction face : Direction.values()) {
                check(level.getSignal(boiler.hatch(), face.getOpposite()) == (face == Direction.NORTH ? 15 : 0), "front-only signal " + face);
            }
            boiler.part().setFrontFacing(Direction.SOUTH);
        });
        steps.add(() -> {
            check(level.getSignal(boiler.hatch(), Direction.SOUTH) == 0 && level.getSignal(boiler.hatch(), Direction.NORTH) == 15,
                    "rotation clears old face and powers new face");
            boiler.machine().onStructureInvalid();
            check(boiler.part().output() == 0, "invalidation clears immediately");
            boiler.form();
        });
        steps.add(() -> {
            check(boiler.part().output() == 15, "reformation restores evaluation");
            for (Fixture fixture : List.of(boiler, pressurizer)) {
                fixture.machine().onStructureInvalid();
                level.setBlockAndUpdate(fixture.second(), TrueSteamMachines.REDSTONE_HATCHES[1].defaultBlockState());
            }
        });
        steps.add(() -> {
            for (Fixture fixture : List.of(boiler, pressurizer)) {
                check(!fixture.machine().checkPatternWithLock(), "two hatch tiers rejected across casing categories " + fixture.controller());
                level.setBlockAndUpdate(fixture.second(), fixture.casing());
            }
        });
        steps.add(() -> { boiler.form(); pressurizer.form(); testValues(); testRecipes(); });
        steps.add(() -> {
            lampFace = Arrays.stream(Direction.values()).filter(face -> level.isEmptyBlock(boiler.hatch().relative(face)))
                    .findFirst().orElseThrow();
            lamp = boiler.hatch().relative(lampFace);
            boiler.part().setFrontFacing(lampFace);
            level.setBlockAndUpdate(lamp, net.minecraft.world.level.block.Blocks.REDSTONE_LAMP.defaultBlockState());
            level.neighborChanged(lamp, level.getBlockState(boiler.hatch()).getBlock(), boiler.hatch());
        });
        steps.add(() -> {
            check(level.getBlockState(lamp).getValue(net.minecraft.world.level.block.RedstoneLampBlock.LIT), "front output powers real lamp");
            previousNotifications = notifications;
        });
        steps.add(() -> {
            check(notifications == previousNotifications, "unchanged output avoids redundant neighbor notifications");
            boiler.part().setFrontFacing(lampFace.getOpposite());
        });
        steps.add(() -> {
            check(!level.getBlockState(lamp).getValue(net.minecraft.world.level.block.RedstoneLampBlock.LIT), "rotation extinguishes former-front lamp");
            level.removeBlock(lamp, false);
            level.removeBlock(boiler.hatch(), false);
        });
        steps.add(() -> {
            check(!boiler.machine().isFormed(), "breaking hatch invalidates controller");
            boiler.hatch(6);
        });
        steps.add(() -> {
            boiler.machine().onStructureInvalid();
            check(boiler.part().output() == 0 && boiler.part().provider() == null, "unattached new hatch outputs zero");
            boiler.form();
            unsupported = build(com.gregtechceu.gtceu.common.data.machines.GTMultiMachines.ELECTRIC_BLAST_FURNACE,
                    new BlockPos(1060, 80, 1040), com.gregtechceu.gtceu.common.data.GTBlocks.CASING_INVAR_HEATPROOF.get());
        });
        steps.add(() -> {
            unsupported.form();
            check(boiler.part().saveRule(0, heat(LESS_EQUAL, "999999", 15)), "prepare rule for unsupported attachment");
            // Deliberately inject an unsupported association; normal patterns never permit this part.
            boiler.part().removedFromController(boiler.machine());
            boiler.part().addedToController(unsupported.machine());
        });
        steps.add(() -> {
            check(unsupported.machine().isFormed() && boiler.part().provider() != null && boiler.part().output() == 0,
                    "generic controller supplies defaults but missing custom values do not match");
            var provider = boiler.part().provider();
            check(provider.readRedstoneValue("recipe_progress_percent").orElseThrow().value().equals(0.0),
                    "idle recipe percentage is zero");
            check(provider.readRedstoneValue("recipe_progress_ticks").orElseThrow().value().equals(0L) &&
                    provider.readRedstoneValue("recipe_id").isEmpty(), "idle generic controller exposes zero progress and unavailable recipe ID");
            var logic = ((com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine) unsupported.machine()).getRecipeLogic();
            var recipe = com.gregtechceu.gtceu.common.data.GTRecipeTypes.BLAST_RECIPES.recipeBuilder("redstone_fixture")
                    .duration(200).buildRawRecipe();
            logic.setupRecipe(recipe);
            logic.setProgress(12);
            var percentage = provider.readRedstoneValue("recipe_progress_percent").orElseThrow();
            check(percentage.type() == Type.FLOAT && percentage.value().equals(1200.0 / logic.getDuration()),
                    "recipe percentage uses effective duration and floating point");
            logic.setProgress(1);
            check(provider.readRedstoneValue("recipe_progress_percent").orElseThrow().value().equals(100.0 / logic.getDuration()),
                    "recipe percentage preserves fractional progress");
            check(new RedstoneRule("recipe_progress_percent", Type.FLOAT, GREATER, "0.1", 9)
                    .matches(provider.readRedstoneValue("recipe_progress_percent").orElseThrow()),
                    "percentage observation supports fractional rule thresholds");
            logic.setProgress(logic.getDuration() + 1);
            check(provider.readRedstoneValue("recipe_progress_percent").orElseThrow().value().equals(100.0),
                    "recipe percentage is capped at 100");
            logic.setProgress(12);
            check(provider.readRedstoneValue("recipe_progress_ticks").orElseThrow().value().equals(12L), "generic recipe progress uses ticks");
            check(provider.readRedstoneValue("recipe_duration_ticks").orElseThrow().value().equals((long) logic.getDuration()), "generic duration uses effective recipe duration");
            check(provider.readRedstoneValue("recipe_id").orElseThrow().value().equals(recipe.id.toString()), "generic active recipe ID is namespaced");
            logic.resetRecipeLogic();
            check(provider.readRedstoneValue("recipe_progress_percent").orElseThrow().value().equals(0.0),
                    "reset recipe percentage clears cached progress");
            check(provider.readRedstoneValue("recipe_id").isEmpty(), "idle cached recipe is not exposed as active");
            boiler.part().removedFromController(unsupported.machine());
            boiler.form();
        });
        steps.add(() -> {
            while (!boiler.part().rules().isEmpty()) boiler.part().deleteRule(0);
            while (!pressurizer.part().rules().isEmpty()) pressurizer.part().deleteRule(0);
            boiler.part().saveRule(0, heat(LESS_EQUAL, "999999", 11));
            pressurizer.part().saveRule(0, ready(IS_FALSE, 7));
            CompoundTag saved = new CompoundTag();
            boiler.part().saveCustomPersistedData(saved, false);
            boiler.part().loadCustomPersistedData(saved);
            check(boiler.part().output() == 0 && boiler.part().rules().size() == 1, "load keeps rules but resets authoritative signal");
            writePositions();
        });
        steps.add(() -> {
            check(boiler.part().output() == 11 && pressurizer.part().output() == 7, "final persisted configuration is live");
            level.getServer().saveEverything(false, true, true);
            unloadStarted = ticks;
            unloaded.clear();
            force(false);
            steps.add(RedstoneRuntimeChecks::awaitUnload);
        });
    }

    private static void awaitUnload() {
        long boilerChunk = new net.minecraft.world.level.ChunkPos(boiler.controller()).toLong();
        long gasChunk = new net.minecraft.world.level.ChunkPos(pressurizer.controller()).toLong();
        if (!unloaded.contains(boilerChunk) || !unloaded.contains(gasChunk)) {
            require(ticks - unloadStarted < 1200, "both fixture chunks actually unload");
            steps.addFirst(RedstoneRuntimeChecks::awaitUnload);
            return;
        }
        check(true, "actual ChunkEvent.Unload observed for both controllers");
        force(true);
        for (int i = 0; i < 8; i++) steps.add(() -> {});
        steps.add(() -> {
            check(boiler.part().rules().equals(List.of(heat(LESS_EQUAL, "999999", 11))), "chunk reload restores ordered rules");
            check(pressurizer.part().rules().equals(List.of(ready(IS_FALSE, 7))), "chunk reload restores boolean rules");
            boiler.form(); pressurizer.form();
        });
        steps.add(() -> check(boiler.part().output() == 11 && pressurizer.part().output() == 7, "chunk reload resumes signals"));
    }

    private static void testValues() {
        var machine = (InfernalBoilerMachine) boiler.machine();
        machine.getRecipeLogic().setInfernalCharges(2);
        check(machine.readRedstoneValue("cycles_until_throttle").orElseThrow().value().equals(2L), "remaining throttle cycles exposed");
        machine.getRecipeLogic().trackCycle(1);
        check(machine.readRedstoneValue("cycles_until_throttle").orElseThrow().value().equals(1L), "remaining cycles decrease on recipe completion");
        machine.getRecipeLogic().setInfernalCharges(0);
        check(machine.readRedstoneValue("cycles_until_throttle").orElseThrow().value().equals(0L), "throttled boiler reports zero cycles");
        machine.getRecipeLogic().setInfernalCharges(512);
        for (int heat : new int[] { 0, 32, 33, 64, 65, 128, 129, 256, 257, 512 }) {
            machine.getRecipeLogic().setCycleCounter(heat);
            check(machine.readRedstoneValue("heat_counter").orElseThrow().value().equals((long) heat), "counter " + heat);
            check(machine.readRedstoneValue("heat_level").orElseThrow().value().equals(machine.getRecipeLogic().getHeatLevel().name()), "unchanged heat threshold " + heat);
        }
        machine.getRecipeLogic().decreaseCycleCounter();
        check(machine.readRedstoneValue("heat_counter").orElseThrow().value().equals((long) machine.getRecipeLogic().getCycleCounter()), "heat decay observation");
        var gas = (IndustrialGasPressurizerMachine) pressurizer.machine();
        check(gas.readRedstoneValue("perfect_condition").orElseThrow().value().equals(gas.getState() == PerfectConditionState.REACHED), "readiness uses existing state");
        // Fixture preview may contain no input after choosing the cross-category position. Restore a real input tank.
        BlockPos input = pressurizer.second();
        gas.onStructureInvalid();
        level.setBlockAndUpdate(input, com.gregtechceu.gtceu.common.data.GTMachines.FLUID_IMPORT_HATCH[1].defaultBlockState());
        steps.addFirst(() -> {
            pressurizer.form();
            var tanks = gas.getCapabilitiesFlat().get(com.gregtechceu.gtceu.api.capability.recipe.IO.IN)
                    .get(com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability.CAP).stream()
                    .filter(handler -> handler instanceof NotifiableFluidTank).map(handler -> (NotifiableFluidTank) handler)
                    .flatMap(tank -> Arrays.stream(tank.getStorages())).toList();
            for (var storage : tanks) storage.setFluid(GTMaterials.Water.getFluid(storage.getCapacity() / 2));
            gas.getRecipeLogic().setLastCraftingTime(0L);
            check(gas.getState() == PerfectConditionState.REACHED && (Boolean) gas.readRedstoneValue("perfect_condition").orElseThrow().value(), "ready tanks expose true");
            for (var storage : tanks) storage.setFluid(GTMaterials.Water.getFluid(storage.getCapacity()));
            check(gas.getState() == PerfectConditionState.TOO_HIGH_FLUID_LEVEL && !(Boolean) gas.readRedstoneValue("perfect_condition").orElseThrow().value(), "high fluid exposes false");
            for (var storage : tanks) storage.setFluid(GTMaterials.Water.getFluid(1));
            check(gas.getState() == PerfectConditionState.TOO_LOW_FLUID_LEVEL, "low fluid exposes false");
            gas.getRecipeLogic().setLastCraftingTime(System.currentTimeMillis());
            check(gas.getState() == PerfectConditionState.ON_COOLDOWN, "cooldown exposes false");
        });
    }

    private static void testRecipes() {
        for (int tier = 1; tier <= 6; tier++) {
            var id = TrueSteamMachines.REDSTONE_HATCHES[tier].getId();
            var item = TrueSteamMachines.REDSTONE_HATCHES[tier].asStack().getItem();
            var recipes = level.getRecipeManager().getRecipes().stream().filter(recipe ->
                    recipe.getResultItem(level.registryAccess()).is(item)).toList();
            check(!recipes.isEmpty(), "runtime recipe registered for " + id);
            var recipe = (net.minecraft.world.item.crafting.CraftingRecipe) recipes.get(0);
            var crafting = new net.minecraft.world.inventory.TransientCraftingContainer(
                    net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(level).inventoryMenu, 3, 3);
            var ingredients = recipe.getIngredients();
            check(ingredients.size() == 3, "three recipe ingredients " + id);
            for (int row = 0; row < 3; row++) crafting.setItem(row * 3, ingredients.get(row).getItems()[0].copy());
            check(recipe.matches(crafting, level) && recipe.assemble(crafting, level.registryAccess()).is(item),
                    "survival crafting matches and assembles " + id);
        }
    }

    private static RedstoneRule heat(RedstoneRule.Operator op, String operand, int strength) {
        return new RedstoneRule("heat_counter", Type.INTEGER, op, operand, strength);
    }
    private static RedstoneRule ready(RedstoneRule.Operator op, int strength) {
        return new RedstoneRule("perfect_condition", Type.BOOLEAN, op, "", strength);
    }

    private static void writePositions() {
        try {
            var tag = new JsonObject();
            for (var entry : java.util.Map.of("boiler", boiler, "pressurizer", pressurizer).entrySet()) {
                var data = new JsonObject();
                data.addProperty("controller", entry.getValue().controller().asLong());
                data.addProperty("hatch", entry.getValue().hatch().asLong());
                data.addProperty("second", entry.getValue().second().asLong());
                tag.add(entry.getKey(), data);
            }
            Files.writeString(Path.of("redstone-positions.json"), tag.toString());
        } catch (Exception failure) { throw new RuntimeException(failure); }
    }

    private static void setupReload() throws Exception {
        var positions = com.google.gson.JsonParser.parseString(Files.readString(Path.of("redstone-positions.json"))).getAsJsonObject();
        boiler = restore(positions.getAsJsonObject("boiler"), TrueSteamBlocks.InfernalAlloyCasing.get().defaultBlockState());
        pressurizer = restore(positions.getAsJsonObject("pressurizer"), com.gregtechceu.gtceu.common.data.GTBlocks.CASING_STAINLESS_CLEAN.get().defaultBlockState());
        steps.add(() -> {
            check(boiler.part().rules().equals(List.of(heat(LESS_EQUAL, "999999", 11))), "world restart restores boiler rules");
            check(pressurizer.part().rules().equals(List.of(ready(IS_FALSE, 7))), "world restart restores boolean rules");
            boiler.form(); pressurizer.form();
        });
        steps.add(() -> check(boiler.part().output() == 11 && pressurizer.part().output() == 7, "world restart recomputes both outputs"));
    }

    private static Fixture restore(JsonObject data, BlockState casing) {
        return new Fixture(BlockPos.of(data.get("controller").getAsLong()), BlockPos.of(data.get("hatch").getAsLong()),
                BlockPos.of(data.get("second").getAsLong()), casing);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static void check(boolean condition, String message) {
        require(condition, message);
        checks.add(message);
    }
    private static void finish(MinecraftServer server, Throwable failure) {
        finished = true;
        var report = new JsonObject();
        report.addProperty("passed", failure == null);
        report.add("checks", checks);
        if (failure != null) { report.addProperty("failure", failure.toString()); failure.printStackTrace(); }
        try {
            Files.writeString(Path.of("redstone-" + System.getProperty("gttruesteam.redstoneTestPhase") + ".json"),
                    new GsonBuilder().setPrettyPrinting().create().toJson(report));
        } catch (Exception exception) { throw new RuntimeException(exception); }
        server.halt(false);
    }
}

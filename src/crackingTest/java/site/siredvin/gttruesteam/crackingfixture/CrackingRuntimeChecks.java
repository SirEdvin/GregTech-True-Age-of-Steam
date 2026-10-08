package site.siredvin.gttruesteam.crackingfixture;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.MaintenanceHatchPartMachine;
import com.gregtechceu.gtceu.data.recipe.serialized.chemistry.PetrochemRecipes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamSteams;
import site.siredvin.gttruesteam.recipe.CriticalSteamCrackingRecipes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys.LIQUID;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

/** Opt-in real-loader checks, with accelerated recipe ticks in a disposable world; not shipped. */
@Mod.EventBusSubscriber(modid = "gttruesteam")
public final class CrackingRuntimeChecks {
    private static final JsonArray checks = new JsonArray();
    private static final ArrayDeque<Runnable> steps = new ArrayDeque<>();
    private static final Map<ResourceLocation, GTRecipe> recipes = new LinkedHashMap<>();
    private static final Map<ResourceLocation, GTRecipe> upstream = new LinkedHashMap<>();
    private static ServerLevel level;
    private static Fixture cracker;
    private static final BlockPos DISTILLERY = new BlockPos(1040, 100, 1024);
    private static int ticks;
    private static boolean finished;

    private record Fixture(BlockPos controller, List<BlockPos> inputs, List<BlockPos> outputs, BlockPos energy, BlockPos bus) {
        CoilWorkableElectricMultiblockMachine machine() {
            return (CoilWorkableElectricMultiblockMachine) MetaMachine.getMachine(level, controller);
        }
        FluidHatchPartMachine input(int i) { return (FluidHatchPartMachine) MetaMachine.getMachine(level, inputs.get(i)); }
        FluidHatchPartMachine output(int i) { return (FluidHatchPartMachine) MetaMachine.getMachine(level, outputs.get(i)); }
        EnergyHatchPartMachine power() { return (EnergyHatchPartMachine) MetaMachine.getMachine(level, energy); }
        void form() {
            var machine = machine();
            machine.onStructureInvalid();
            require(machine.checkPatternWithLock(), "pattern matches " + controller);
            machine.onStructureFormed();
            MultiblockWorldSavedData.getOrCreate(level).addMapping(machine.getMultiblockState());
            check(machine.isFormed() && machine.hasCapabilityProxies(), "formed fixture has capabilities " + controller);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (!Boolean.getBoolean("gttruesteam.crackingTest") || finished || event.phase != TickEvent.Phase.END) return;
        try {
            ticks++;
            if (ticks == 30) {
                level = event.getServer().overworld();
                for (int x = 64; x <= 66; x++) for (int z = 64; z <= 65; z++) level.setChunkForced(x, z, true);
                validateRecipes();
                cracker = buildCracker(new BlockPos(1024, 100, 1024));
                level.setBlockAndUpdate(DISTILLERY, GTMachines.DISTILLERY[HV].defaultBlockState());
                steps.add(() -> { cracker.form(); queueProcessing(); });
            } else if (ticks > 40 && ticks % 5 == 0) {
                if (steps.isEmpty()) finish(event, null);
                else steps.removeFirst().run();
            }
            if (ticks > 2000) throw new AssertionError("Fixture timed out");
        } catch (Throwable failure) {
            finish(event, failure);
        }
    }

    private static JsonElement json(GTRecipe recipe) {
        return GTRecipeSerializer.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry()), recipe)
                .getOrThrow(false, message -> { throw new AssertionError(message); });
    }

    private static GTRecipe loaded(ResourceLocation id) {
        return (GTRecipe) level.getRecipeManager().byKey(id).orElseThrow(() -> new AssertionError("Missing recipe " + id));
    }

    private static List<FluidIngredient> fluids(Map<com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability<?>, List<Content>> contents) {
        return contents.getOrDefault(FluidRecipeCapability.CAP, List.of()).stream()
                .map(c -> FluidRecipeCapability.CAP.of(c.content)).toList();
    }

    private static void fluid(FluidIngredient actual, FluidStack expected) {
        require(actual.getAmount() == expected.getAmount() && actual.test(expected), "fluid matches " + expected);
    }

    private static int circuit(GTRecipe recipe) {
        var items = recipe.inputs.get(ItemRecipeCapability.CAP);
        require(items != null && items.size() == 1, "one circuit ingredient " + recipe.id);
        return IntCircuitBehaviour.getCircuitConfiguration(((Ingredient) items.get(0).content).getItems()[0]);
    }

    private static void validateRecipes() throws Exception {
        var variants = List.of(TrueSteamSteams.SUPERHOT, TrueSteamSteams.HELLISH);
        int[] colors = { 0x9666CC, 0x663399 };
        double[] coefficients = { 1.5, 1.7 };
        String[] names = { "supercritical", "most_hellish" };
        for (int i = 0; i < variants.size(); i++) {
            var steam = variants.get(i);
            check(steam.getConfiguration().crackingYieldCoefficient() == coefficients[i], "configured coefficient " + names[i]);
            check(steam.getCriticalSteam().getResourceLocation().equals(GTTrueSteam.id(names[i] + "_steam")), "critical steam ID preserved " + names[i]);
            check(steam.getCrackingResidue().getResourceLocation().equals(GTTrueSteam.id(names[i] + "_steam_cracking_residue")) &&
                    steam.getCrackingResidue().getMaterialRGB() == colors[i] &&
                    steam.getCrackingResidue().getFluid(LIQUID) == steam.getCrackingResidue().getFluid(), "residue identity/color/liquid " + names[i]);
            CriticalSteamCrackingRecipes.register(finished -> {
                var data = new JsonObject();
                finished.serializeRecipeData(data);
                var recipe = GTRecipeSerializer.SERIALIZER.fromJson(finished.getId(), data);
                require(recipes.put(recipe.id, recipe) == null, "unique emitted ID " + recipe.id);
            }, steam);
        }
        check(ForgeRegistries.FLUIDS.getKeys().stream().noneMatch(id -> id.getNamespace().equals("gttruesteam") &&
                (id.getPath().startsWith("lightly_") || id.getPath().startsWith("severely_"))), "prototype cracked fluids absent");
        check(recipes.size() == 32, "32 emitted cracking recipes");
        var loadedAddon = level.getRecipeManager().getRecipes().stream().filter(r -> r instanceof GTRecipe g &&
                g.recipeType == CRACKING_RECIPES && g.id.getNamespace().equals("gttruesteam")).toList();
        check(loadedAddon.size() == 32, "32 loaded addon cracker recipes");
        check(level.getRecipeManager().getRecipes().stream().filter(r -> r.getId().getNamespace().equals("gttruesteam") &&
                r.getId().getPath().contains("crack_")).count() == 32, "no excluded cracking recipe family or variant");
        PetrochemRecipes.init(finished -> {
            var data = new JsonObject();
            finished.serializeRecipeData(data);
            if (finished.getType() == GTRecipeSerializer.SERIALIZER) {
                var expected = GTRecipeSerializer.SERIALIZER.fromJson(finished.getId(), data);
                var actual = loaded(expected.id);
                require(json(actual).equals(json(expected)), "upstream definition unchanged " + expected.id);
                upstream.put(expected.id, actual);
            }
        });
        check(!upstream.isEmpty(), "pinned petrochemical cracking/distillation definitions unchanged: " + upstream.size());
        var raw = List.of(LightFuel, HeavyFuel, Naphtha, RefineryGas);
        var light = List.of(LightlySteamCrackedLightFuel, LightlySteamCrackedHeavyFuel, LightlySteamCrackedNaphtha, LightlySteamCrackedGas);
        var severe = List.of(SeverelySteamCrackedLightFuel, SeverelySteamCrackedHeavyFuel, SeverelySteamCrackedNaphtha, SeverelySteamCrackedGas);
        var dump = new JsonObject();
        for (var steam : variants) for (int i = 0; i < raw.size(); i++) for (boolean isSevere : List.of(false, true)) {
            String severity = isSevere ? "severely" : "lightly";
            var baseline = loaded(new ResourceLocation("gtceu", "cracker/" + severity + "_steam_crack_" + raw.get(i).getName()));
            require(baseline.duration == (isSevere ? 160 : 80) && circuit(baseline) == (isSevere ? 3 : 1), "pinned baseline circuit/timing");
            for (boolean recovery : List.of(false, true)) {
                var id = GTTrueSteam.id("cracker/" + severity + "_" + steam.getCriticalSteam().getName() +
                        (recovery ? "_residue_crack_" : "_crack_") + raw.get(i).getName());
                var actual = loaded(id);
                require(json(actual).equals(json(recipes.get(id))), "loaded matches emitted " + id);
                require(actual.recipeType == CRACKING_RECIPES && circuit(actual) == circuit(baseline) &&
                        actual.duration == baseline.duration / (recovery ? 1 : 2) &&
                        actual.getInputEUt().equals(baseline.getInputEUt()), "type/circuit/duration/EUt " + id);
                var in = fluids(actual.inputs);
                var out = fluids(actual.outputs);
                require(in.size() == 2 && out.size() == (recovery ? 1 : 2), "exact fluid IO counts " + id);
                fluid(in.get(0), raw.get(i).getFluid(1000));
                fluid(in.get(1), (recovery ? steam.getCrackingResidue() : steam.getCriticalSteam()).getFluid(1000));
                int amount = recovery ? (steam == TrueSteamSteams.SUPERHOT ? 11500 : 13700) : 500;
                fluid(out.get(0), (isSevere ? severe : light).get(i).getFluid(amount));
                if (!recovery) fluid(out.get(1), steam.getCrackingResidue().getFluid(100));
                check(true, "matrix verified " + id);
                dump.add(id.toString(), json(actual));
            }
        }
        Files.writeString(Path.of("cracking-recipes.json"), new GsonBuilder().setPrettyPrinting().create().toJson(dump));
    }

    private static Fixture buildCracker(BlockPos origin) {
        var definition = GTMultiMachines.CRACKER;
        var pattern = definition.getPatternFactory().get();
        var preview = pattern.getPreview(Arrays.stream(pattern.aisleRepetitions).mapToInt(r -> r[0]).toArray());
        var casings = new ArrayList<BlockPos>();
        BlockPos controller = null;
        for (int x = 0; x < preview.length; x++) for (int y = 0; y < preview[x].length; y++) for (int z = 0; z < preview[x][y].length; z++) {
            var info = preview[x][y][z];
            if (info == null) continue;
            var pos = origin.offset(x, y, z);
            var state = info.getBlockState();
            level.setBlockAndUpdate(pos, state);
            if (state.is(definition.getBlock())) controller = pos;
            else if (state.is(GTBlocks.CASING_STAINLESS_CLEAN.get()) || MetaMachine.getMachine(level, pos) != null) {
                level.setBlockAndUpdate(pos, GTBlocks.CASING_STAINLESS_CLEAN.getDefaultState());
                casings.add(pos);
            }
        }
        require(controller != null && casings.size() >= 20, "cracker preview contains enough casing alternatives");
        MetaMachine.getMachine(level, controller).setFrontFacing(Direction.NORTH);
        var definitions = List.of(GTMachines.FLUID_IMPORT_HATCH[HV], GTMachines.FLUID_IMPORT_HATCH[HV],
                GTMachines.FLUID_EXPORT_HATCH[HV], GTMachines.FLUID_EXPORT_HATCH[HV], GTMachines.ENERGY_INPUT_HATCH[HV],
                GTMachines.ITEM_IMPORT_BUS[HV], GTMachines.MAINTENANCE_HATCH, GTMachines.MUFFLER_HATCH[HV]);
        for (int i = 0; i < definitions.size(); i++) {
            level.setBlockAndUpdate(casings.get(i), definitions.get(i).defaultBlockState());
            MetaMachine.getMachine(level, casings.get(i)).setFrontFacing(Direction.UP);
        }
        ((MaintenanceHatchPartMachine) MetaMachine.getMachine(level, casings.get(6))).fixAllMaintenanceProblems();
        return new Fixture(controller, List.of(casings.get(0), casings.get(1)), List.of(casings.get(2), casings.get(3)), casings.get(4), casings.get(5));
    }

    private static void clear() {
        cracker.machine().getRecipeLogic().resetRecipeLogic();
        for (int i = 0; i < 2; i++) {
            cracker.input(i).tank.getStorages()[0].setFluid(FluidStack.EMPTY);
            cracker.output(i).tank.getStorages()[0].setFluid(FluidStack.EMPTY);
        }
    }

    private static void prepare(GTRecipe recipe, int agentAmount) {
        var input = fluids(recipe.inputs);
        cracker.input(0).tank.getStorages()[0].setFluid(input.get(0).getStacks()[0].copy());
        var agent = input.get(1).getStacks()[0].copy();
        agent.setAmount(agentAmount);
        cracker.input(1).tank.getStorages()[0].setFluid(agent);
        ((ItemBusPartMachine) MetaMachine.getMachine(level, cracker.bus())).getCircuitInventory().setStackInSlot(0,
                IntCircuitBehaviour.stack(circuit(recipe)));
        cracker.power().energyContainer.setEnergyStored(cracker.power().energyContainer.getEnergyCapacity());
    }

    private static int amount(com.gregtechceu.gtceu.api.data.chemical.material.Material material) {
        int amount = 0;
        for (int i = 0; i < 2; i++) {
            var stack = cracker.output(i).tank.getFluidInTank(0);
            if (stack.getFluid() == material.getFluid()) amount += stack.getAmount();
        }
        return amount;
    }

    private static void process(GTRecipe recipe) {
        var logic = cracker.machine().getRecipeLogic();
        logic.resetRecipeLogic();
        prepare(recipe, 1000);
        logic.findAndHandleRecipe();
        require(logic.isWorking() && logic.getLastRecipe().id.equals(recipe.id), "normal recipe search starts " + recipe.id +
                "; status=" + logic.getStatus() + "; formed=" + cracker.machine().isFormed() +
                "; failures=" + logic.getFailureReasons() + "; inputs=" + cracker.input(0).tank.getFluidInTank(0) +
                ", " + cracker.input(1).tank.getFluidInTank(0));
        var expected = cracker.machine().fullModifyRecipe(recipe);
        require(expected != null && json(expected).equals(json(logic.getLastRecipe())), "standard coil/overclock modifiers " + recipe.id);
        int limit = logic.getDuration() + 20;
        for (int i = 0; i < limit && logic.isWorking(); i++) {
            cracker.power().energyContainer.setEnergyStored(cracker.power().energyContainer.getEnergyCapacity());
            logic.serverTick();
        }
        require(!logic.isWorking() && cracker.input(0).tank.getFluidInTank(0).isEmpty() &&
                cracker.input(1).tank.getFluidInTank(0).isEmpty(), "recipe completes and consumes both fluids " + recipe.id);
    }

    private static void queueProcessing() {
        for (var steam : List.of(TrueSteamSteams.SUPERHOT, TrueSteamSteams.HELLISH)) for (boolean severe : List.of(false, true)) {
            final String prefix = (severe ? "severely" : "lightly") + "_" + steam.getCriticalSteam().getName();
            steps.add(() -> {
                clear();
                var first = loaded(GTTrueSteam.id("cracker/" + prefix + "_crack_light_fuel"));
                var recovery = loaded(GTTrueSteam.id("cracker/" + prefix + "_residue_crack_light_fuel"));
                var output = severe ? SeverelySteamCrackedLightFuel : LightlySteamCrackedLightFuel;
                for (int i = 0; i < 10; i++) process(first);
                check(amount(output) == 5000 && amount(steam.getCrackingResidue()) == 1000, "ten initial crafts accumulate outputs " + prefix);
                for (int i = 0; i < 2; i++) {
                    var storage = cracker.output(i).tank.getStorages()[0];
                    if (storage.getFluid().getFluid() == steam.getCrackingResidue().getFluid()) storage.setFluid(FluidStack.EMPTY);
                }
                process(recovery);
                check(amount(output) == (steam == TrueSteamSteams.SUPERHOT ? 16500 : 18700) && amount(steam.getCrackingResidue()) == 0,
                        "eleven-craft cycle " + prefix);
                clear();
                prepare(recovery, 999);
                cracker.machine().getRecipeLogic().findAndHandleRecipe();
                check(!cracker.machine().getRecipeLogic().isWorking() && cracker.input(0).tank.getFluidInTank(0).getAmount() == 1000 &&
                        cracker.input(1).tank.getFluidInTank(0).getAmount() == 999, "insufficient residue preserves inputs " + prefix);
                for (var blocked : List.of(first, recovery)) {
                    clear();
                    prepare(blocked, 1000);
                    for (int i = 0; i < 2; i++) {
                        var storage = cracker.output(i).tank.getStorages()[0];
                        storage.setFluid(Water.getFluid(storage.getCapacity()));
                    }
                    cracker.machine().getRecipeLogic().findAndHandleRecipe();
                    check(!cracker.machine().getRecipeLogic().isWorking() && cracker.input(0).tank.getFluidInTank(0).getAmount() == 1000,
                            "blocked output preserves feedstock " + blocked.id);
                }
            });
        }
        for (var steam : List.of(TrueSteamSteams.SUPERHOT, TrueSteamSteams.HELLISH)) {
            for (var first : recipes.values().stream().filter(r -> !r.id.getPath().contains("residue_crack") &&
                    r.id.getPath().contains(steam.getCriticalSteam().getName())).toList()) {
                steps.add(() -> {
                    clear();
                    process(first);
                    var produced = fluids(first.outputs).get(0).getStacks()[0];
                    var route = upstream.values().stream().filter(r -> r.recipeType == DISTILLERY_RECIPES &&
                            fluids(r.inputs).size() == 1 && fluids(r.inputs).get(0).test(produced)).findFirst().orElseThrow();
                    int needed = fluids(route.inputs).get(0).getAmount();
                    int accumulated = produced.getAmount();
                    while (accumulated < needed) {
                        process(first);
                        accumulated += produced.getAmount();
                    }
                    distillProducedFluid(route, produced);
                    check(true, "actual cracking output distills through existing route " + first.id + " -> " + route.id);
                });
            }
        }
        steps.add(() -> { clear(); check(true, "processing checks finished"); });
    }

    private static void distillProducedFluid(GTRecipe route, FluidStack produced) {
        var machine = (SimpleTieredMachine) MetaMachine.getMachine(level, DISTILLERY);
        var logic = machine.getRecipeLogic();
        logic.resetRecipeLogic();
        for (var tank : machine.importFluids.getStorages()) tank.setFluid(FluidStack.EMPTY);
        for (var tank : machine.exportFluids.getStorages()) tank.setFluid(FluidStack.EMPTY);
        for (int i = 0; i < machine.exportItems.getSlots(); i++) machine.exportItems.setStackInSlot(i, net.minecraft.world.item.ItemStack.EMPTY);
        int needed = fluids(route.inputs).get(0).getAmount();
        FluidStack transferred = FluidStack.EMPTY;
        for (int i = 0; i < 2; i++) {
            var storage = cracker.output(i).tank.getStorages()[0];
            if (storage.getFluid().isFluidEqual(produced)) transferred = storage.drain(needed, FluidAction.EXECUTE);
        }
        require(transferred.getAmount() == needed && transferred.isFluidEqual(produced), "transfer actual cracker output to distillery");
        machine.importFluids.getStorages()[0].setFluid(transferred);
        machine.getCircuitInventory().setStackInSlot(0, IntCircuitBehaviour.stack(circuit(route)));
        machine.energyContainer.setEnergyStored(machine.energyContainer.getEnergyCapacity());
        logic.findAndHandleRecipe();
        require(logic.isWorking() && logic.getLastRecipe().id.equals(route.id), "existing distillation recipe starts " + route.id);
        var expected = fluids(logic.getLastRecipe().outputs).get(0).getStacks()[0].copy();
        int limit = logic.getDuration() + 20;
        for (int i = 0; i < limit && logic.isWorking(); i++) {
            machine.energyContainer.setEnergyStored(machine.energyContainer.getEnergyCapacity());
            logic.serverTick();
        }
        var actual = machine.exportFluids.getFluidInTank(0);
        require(!logic.isWorking() && actual.isFluidEqual(expected) && actual.getAmount() == expected.getAmount(),
                "existing distillation produces expected output " + route.id);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void check(boolean condition, String message) {
        require(condition, message);
        checks.add(message);
    }

    private static void finish(TickEvent.ServerTickEvent event, Throwable failure) {
        finished = true;
        var report = new JsonObject();
        report.addProperty("passed", failure == null);
        report.add("checks", checks);
        report.addProperty("acceleratedRecipeTicks", true);
        if (failure != null) { report.addProperty("failure", failure.toString()); failure.printStackTrace(); }
        try {
            Files.writeString(Path.of("cracking-server.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        } catch (Exception exception) { throw new RuntimeException(exception); }
        event.getServer().halt(false);
    }
}

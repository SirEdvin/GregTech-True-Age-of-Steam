package site.siredvin.gttruesteam.redstonefixture;

import com.gregtechceu.gtceu.api.gui.factory.MachineUIFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.SelectorWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import site.siredvin.gttruesteam.TrueSteamMachines;
import site.siredvin.gttruesteam.machines.redstone.RedstoneHatchMachine;
import site.siredvin.gttruesteam.machines.redstone.RedstoneHatchUI;
import site.siredvin.gttruesteam.machines.redstone.RedstoneRuleWidget;
import site.siredvin.gttruesteam.machines.industrial_heater.InfernalBoilerMachine;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = "gttruesteam", value = Dist.CLIENT)
public final class RedstoneClientChecks {
    private static boolean opening;
    private static boolean finished;
    private static int phase;
    private static int rowIndex;
    private static int ticks;
    private static int totalTicks;
    private static CompletableFuture<Void> operation = CompletableFuture.completedFuture(null);
    private static final JsonArray checks = new JsonArray();
    private static BlockPos boiler;
    private static BlockPos boilerHatch;
    private static BlockPos gas;
    private static BlockPos gasHatch;
    private static final Path OUTPUT = Path.of(System.getenv("TMPDIR"), "redstone-client");

    @SubscribeEvent
    public static void renderTierItems(net.minecraftforge.client.event.ScreenEvent.Render.Post event) {
        if (!"client".equals(System.getProperty("gttruesteam.redstoneTestPhase")) || ui() == null) return;
        for (int tier = 1; tier <= 6; tier++) {
            event.getGuiGraphics().renderItem(TrueSteamMachines.REDSTONE_HATCHES[tier].asStack(), 8, 8 + tier * 28);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (!"client".equals(System.getProperty("gttruesteam.redstoneTestPhase")) || finished || event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        try {
            if (!opening && (mc.screen instanceof TitleScreen || mc.screen instanceof AccessibilityOnboardingScreen)) {
                opening = true;
                mc.options.guiScale().set(1);
                mc.options.pauseOnLostFocus = false;
                mc.createWorldOpenFlows().loadLevel(mc.screen, "redstone-client");
                return;
            }
            if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
            if (++totalTicks > 2400) throw new AssertionError("Client timed out at phase " + phase);
            if (!operation.isDone()) return;
            operation.join();
            if (++ticks < 15) return;
            ticks = 0;
            switch (phase++) {
                case 0 -> {
                    var positions = JsonParser.parseString(Files.readString(Path.of("redstone-positions.json"))).getAsJsonObject();
                    boiler = BlockPos.of(positions.getAsJsonObject("boiler").get("controller").getAsLong());
                    boilerHatch = BlockPos.of(positions.getAsJsonObject("boiler").get("hatch").getAsLong());
                    gas = BlockPos.of(positions.getAsJsonObject("pressurizer").get("controller").getAsLong());
                    gasHatch = BlockPos.of(positions.getAsJsonObject("pressurizer").get("hatch").getAsLong());
                    server(() -> {
                        var level = mc.getSingleplayerServer().overworld();
                        for (int x = 63; x <= 68; x++) for (int z = 63; z <= 65; z++) level.setChunkForced(x, z, true);
                        var player = mc.getSingleplayerServer().getPlayerList().getPlayers().get(0);
                        player.setGameMode(GameType.CREATIVE);
                        player.teleportTo(level, boilerHatch.getX() + 0.5, boilerHatch.getY(), boilerHatch.getZ() - 2, 0, 0);
                    });
                }
                case 1 -> server(() -> { form(boiler); open(boilerHatch); });
                case 2 -> {
                    check(ui() != null, "real synchronized hatch screen opens");
                    var clientPart = (RedstoneHatchMachine) MetaMachine.getMachine(mc.level, boilerHatch);
                    check(clientPart.replacePartModelWhenFormed() && clientPart.getFormedAppearance(
                            mc.level.getBlockState(boilerHatch), boilerHatch, net.minecraft.core.Direction.NORTH) != null,
                            "formed hatch appearance is synchronized to client");
                    for (int tier = 1; tier <= 6; tier++) {
                        var model = mc.getItemRenderer().getModel(TrueSteamMachines.REDSTONE_HATCHES[tier].asStack(), mc.level, mc.player, 0);
                        check(model != mc.getModelManager().getMissingModel(), "tier " + tier + " item model loads");
                    }
                    screenshot("integer.png");
                    rowIndex = 1;
                }
                case 3 -> { edit(24, "0"); edit(196, "3"); }
                case 4 -> save();
                case 5 -> server(() -> check(part(boilerHatch).rules().size() == 2 && part(boilerHatch).output() == 11, "UI adds rule contributing to OR"));
                case 6 -> screenshot("rows.png");
                case 7 -> server(() -> check(part(boilerHatch).rules().get(0).strength() == 11, "editing second row leaves first rule unchanged"));
                case 8 -> { screenshot("ordered.png"); edit(196, "16"); }
                case 9 -> save();
                case 10 -> server(() -> check(part(boilerHatch).rules().get(1).strength() == 3 && part(boilerHatch).output() == 11, "UI rejects strength 16 atomically"));
                case 11 -> edit(196, "0");
                case 12 -> save();
                case 13 -> server(() -> check(part(boilerHatch).output() == 11, "UI matching zero leaves other contributions unchanged"));
                case 14 -> delete();
                case 15 -> server(() -> check(part(boilerHatch).rules().size() == 1 && part(boilerHatch).output() == 11, "UI delete restores next rule"));
                case 16 -> rowIndex = 0;
                case 17 -> choose(24, 1);
                case 18 -> {
                    check(candidateCount(selector(196)) == 2, "string offers only equality and inequality");
                    edit(24, "NONE"); edit(196, "5");
                    server(() -> ((InfernalBoilerMachine) machine(boiler)).getRecipeLogic().setCycleCounter(0));
                }
                case 19 -> save();
                case 20 -> server(() -> check(part(boilerHatch).output() == 5, "string UI matches named heat level"));
                case 21 -> { screenshot("string.png"); server(() -> { form(gas); open(gasHatch); }); }
                case 22 -> rowIndex = 0;
                case 23 -> {
                    check(!field(24).isVisible() && candidateCount(selector(196)) == 2, "boolean UI hides operand and offers true/false");
                    screenshot("boolean.png"); choose(196, 0);
                }
                case 24 -> save();
                case 25 -> server(() -> check(part(gasHatch).output() == 0, "boolean true rule does not match low tanks"));
                case 26 -> choose(196, 1);
                case 27 -> save();
                case 28 -> server(() -> check(part(gasHatch).output() == 7, "boolean false rule matches"));
                case 29 -> server(() -> machine(gas).onStructureInvalid());
                case 30 -> { screenshot("disconnected.png"); server(() -> check(part(gasHatch).output() == 0, "disconnect while editor open clears output")); }
                case 31 -> server(() -> form(gas));
                case 32 -> server(() -> check(part(gasHatch).output() == 7, "open editor survives reconnection"));
                case 33 -> {
                    var send = Widget.class.getDeclaredMethod("writeClientAction", int.class, java.util.function.Consumer.class);
                    send.setAccessible(true);
                    send.invoke(field(196), 1, (java.util.function.Consumer<net.minecraft.network.FriendlyByteBuf>) buffer -> buffer.writeUtf("150"));
                }
                case 34 -> save();
                case 35 -> server(() -> check(part(gasHatch).rules().get(0).strength() == 7, "oversized strength packet is rejected rather than truncated to 15"));
                case 36 -> server(() -> {
                    while (!part(boilerHatch).rules().isEmpty()) part(boilerHatch).deleteRule(0);
                    open(boilerHatch);
                });
                case 37 -> choose(24, 0);
                case 38 -> choose(196, 1);
                case 39 -> { edit(24, "15"); edit(196, "15"); }
                case 40 -> save();
                case 41 -> server(() -> {
                    check(part(boilerHatch).rules().size() == 1 &&
                            part(boilerHatch).rules().get(0).operator() == site.siredvin.gttruesteam.machines.redstone.RedstoneRule.Operator.GREATER &&
                            part(boilerHatch).rules().get(0).operand().equals("15"), "fresh hatch saves heat_counter > 15 without hidden Add prerequisite");
                });
                case 42 -> delete();
                case 43 -> choose(196, 1);
                case 44 -> edit(24, "15");
                case 45 -> save();
                case 46 -> server(() -> check(part(boilerHatch).rules().size() == 1 &&
                        part(boilerHatch).rules().get(0).operand().equals("15"), "editor saves again after deleting the last rule"));
                case 47 -> server(() -> {
                    for (int i = 1; i < 6; i++) check(part(boilerHatch).saveRule(i,
                            new site.siredvin.gttruesteam.machines.redstone.RedstoneRule("heat_counter",
                                    site.siredvin.gttruesteam.api.RedstoneObservable.Type.INTEGER,
                                    site.siredvin.gttruesteam.machines.redstone.RedstoneRule.Operator.EQUAL, "0", i)),
                            "populate visible rule " + i);
                });
                case 48 -> { rowIndex = 5; scroll(-1); }
                case 49 -> { screenshot("sixth-row.png"); choose(24, 1); }
                case 50 -> { edit(24, "NONE"); edit(196, "6"); }
                case 51 -> save();
                case 52 -> server(() -> check(part(boilerHatch).rules().get(5).valueId().equals("heat_level") &&
                        part(boilerHatch).rules().get(5).operand().equals("NONE") && part(boilerHatch).rules().get(5).strength() == 6 &&
                        part(boilerHatch).rules().get(4).valueId().equals("heat_counter"),
                        "sixth row scrolls, opens unclipped dropdown and saves independently"));
                case 53 -> { rowIndex = 1; scroll(1); }
                case 54 -> delete();
                case 55 -> server(() -> check(part(boilerHatch).rules().size() == 5 && part(boilerHatch).rules().get(1).strength() == 2,
                        "deleting middle row retains following rules"));
                case 56 -> edit(196, "9");
                case 57 -> save();
                case 58 -> server(() -> check(part(boilerHatch).rules().get(1).strength() == 9 &&
                        part(boilerHatch).rules().get(1).operand().equals("0"), "shifted row refreshes editor before next save"));
                case 59 -> screenshot("final-rows.png");
                case 60 -> choose(24, 4);
                case 61 -> choose(196, 1);
                case 62 -> { edit(24, "12.5"); edit(196, "8"); }
                case 63 -> save();
                case 64 -> server(() -> check(part(boilerHatch).rules().get(1).valueId().equals("recipe_progress_percent") &&
                        part(boilerHatch).rules().get(1).type() == site.siredvin.gttruesteam.api.RedstoneObservable.Type.FLOAT &&
                        part(boilerHatch).rules().get(1).operand().equals("12.5"), "percentage UI saves fractional thresholds"));
                case 65 -> ((com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup) ui().widgets.get(1)).setScrollYOffset(0);
                case 66 -> {
                    check(com.lowdragmc.lowdraglib.utils.LocalizationUtils.format("gttruesteam.redstone.recipe_progress_percent")
                            .equals("Recipe progress (%)"), "percentage label escapes the literal percent sign");
                    screenshot("percentage.png");
                }
                default -> finish(null);
            }
        } catch (Throwable failure) { finish(failure); }
    }

    private static MultiblockControllerMachine machine(BlockPos pos) {
        return (MultiblockControllerMachine) MetaMachine.getMachine(Minecraft.getInstance().getSingleplayerServer().overworld(), pos);
    }
    private static RedstoneHatchMachine part(BlockPos pos) {
        return (RedstoneHatchMachine) MetaMachine.getMachine(Minecraft.getInstance().getSingleplayerServer().overworld(), pos);
    }
    private static void form(BlockPos pos) {
        var machine = machine(pos);
        machine.onStructureInvalid();
        if (!machine.checkPatternWithLock()) throw new AssertionError("client fixture pattern mismatch");
        machine.onStructureFormed();
    }
    private static void open(BlockPos pos) {
        var server = Minecraft.getInstance().getSingleplayerServer();
        var player = server.getPlayerList().getPlayers().get(0);
        player.teleportTo(server.overworld(), pos.getX() + 0.5, pos.getY(), pos.getZ() - 2, 0, 0);
        MachineUIFactory.INSTANCE.openUI(part(pos), player);
    }
    private static void server(Runnable task) {
        operation = CompletableFuture.runAsync(task, action -> Minecraft.getInstance().getSingleplayerServer().execute(action));
    }
    private static RedstoneHatchUI find(Widget widget) {
        if (widget instanceof RedstoneHatchUI ui) return ui;
        if (widget instanceof WidgetGroup group) for (var child : group.widgets) {
            var found = find(child); if (found != null) return found;
        }
        return null;
    }
    private static RedstoneHatchUI ui() {
        if (Minecraft.getInstance().screen instanceof ModularUIGuiContainer screen) return find(screen.modularUI.mainGroup);
        return null;
    }
    private static TextFieldWidget field(int x) {
        return row().widgets.stream().filter(widget -> widget instanceof TextFieldWidget && widget.getSelfPosition().x == x)
                .map(widget -> (TextFieldWidget) widget).findFirst().orElseThrow();
    }
    private static SelectorWidget selector(int x) {
        return row().widgets.stream().filter(widget -> widget instanceof SelectorWidget && widget.getSelfPosition().x == x)
                .map(widget -> (SelectorWidget) widget).findFirst().orElseThrow();
    }
    private static void edit(int x, String value) {
        var position = field(x).getPosition();
        clickAbsolute(position.x + 8, position.y + 8);
        Minecraft.getInstance().screen.keyPressed(269, 0, 0);
        for (int i = 0; i < 256; i++) Minecraft.getInstance().screen.keyPressed(259, 0, 0);
        for (char character : value.toCharArray()) Minecraft.getInstance().screen.charTyped(character, 0);
    }

    private static void choose(int x, int index) {
        var position = selector(x).getPosition();
        clickAbsolute(position.x + 8, position.y + 8);
        position = selector(x).getPosition();
        clickAbsolute(position.x + 8, position.y + 18 + index * 15 + 7);
    }
    private static void save() {
        var position = row().getPosition();
        clickAbsolute(position.x + 250, position.y + 34);
    }
    private static void delete() {
        var position = row().getPosition();
        clickAbsolute(position.x + 284, position.y + 34);
    }
    private static void clickAbsolute(int x, int y) {
        Minecraft.getInstance().screen.mouseClicked(x, y, 0);
        Minecraft.getInstance().screen.mouseReleased(x, y, 0);
    }
    private static void scroll(int direction) {
        var position = ui().getPosition();
        for (int i = 0; i < 40; i++) Minecraft.getInstance().screen.mouseScrolled(position.x + 300, position.y + 180, direction);
    }
    private static RedstoneRuleWidget row() {
        return findRow(ui());
    }
    private static RedstoneRuleWidget findRow(Widget widget) {
        if (widget instanceof RedstoneRuleWidget row && row.getSelfPosition().y == rowIndex * RedstoneRuleWidget.HEIGHT) return row;
        if (widget instanceof WidgetGroup group) for (var child : group.widgets) {
            var result = findRow(child);
            if (result != null) return result;
        }
        return null;
    }
    private static void screenshot(String name) throws Exception {
        Files.createDirectories(OUTPUT);
        Screenshot.grab(OUTPUT.toFile(), name, Minecraft.getInstance().getMainRenderTarget(), component -> {});
    }
    private static int candidateCount(SelectorWidget selector) throws Exception {
        var field = SelectorWidget.class.getDeclaredField("candidates");
        field.setAccessible(true);
        return ((java.util.List<?>) field.get(selector)).size();
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks.add(message);
    }
    private static void finish(Throwable failure) {
        finished = true;
        var report = new JsonObject(); report.addProperty("passed", failure == null); report.add("checks", checks);
        if (failure != null) { report.addProperty("failure", failure.toString()); failure.printStackTrace(); }
        try {
            Files.createDirectories(OUTPUT);
            Files.writeString(OUTPUT.resolve("report.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        } catch (Exception exception) { throw new RuntimeException(exception); }
        Minecraft.getInstance().stop();
    }
}

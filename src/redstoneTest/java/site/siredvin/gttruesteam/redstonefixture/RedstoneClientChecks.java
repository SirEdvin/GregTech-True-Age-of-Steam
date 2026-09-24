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
import site.siredvin.gttruesteam.machines.industrial_heater.InfernalBoilerMachine;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = "gttruesteam", value = Dist.CLIENT)
public final class RedstoneClientChecks {
    private static boolean opening;
    private static boolean finished;
    private static int phase;
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
                    for (int tier = 1; tier <= 6; tier++) {
                        var model = mc.getItemRenderer().getModel(TrueSteamMachines.REDSTONE_HATCHES[tier].asStack(), mc.level, mc.player, 0);
                        check(model != mc.getModelManager().getMissingModel(), "tier " + tier + " item model loads");
                    }
                    screenshot("integer.png");
                    click(8, 127);
                }
                case 3 -> { edit(4, "0"); edit(190, "3"); }
                case 4 -> click(250, 174);
                case 5 -> server(() -> check(part(boilerHatch).rules().size() == 2 && part(boilerHatch).output() == 11, "UI adds lower priority rule"));
                case 6 -> click(80, 130);
                case 7 -> { screenshot("after-up.png"); server(() -> check(part(boilerHatch).output() == 3, "UI up control changes first-match output: " + part(boilerHatch).rules() + " output=" + part(boilerHatch).output())); }
                case 8 -> { screenshot("ordered.png"); edit(190, "16"); }
                case 9 -> click(250, 174);
                case 10 -> server(() -> check(part(boilerHatch).rules().get(0).strength() == 3 && part(boilerHatch).output() == 3, "UI rejects strength 16 atomically"));
                case 11 -> edit(190, "0");
                case 12 -> click(250, 174);
                case 13 -> server(() -> check(part(boilerHatch).output() == 0, "UI saves matching zero output"));
                case 14 -> click(220, 130);
                case 15 -> server(() -> check(part(boilerHatch).rules().size() == 1 && part(boilerHatch).output() == 11, "UI delete restores next rule"));
                case 16 -> click(12, 36);
                case 17 -> choose(4, 1);
                case 18 -> {
                    check(candidateCount(selector(190)) == 2, "string offers only equality and inequality");
                    edit(4, "NONE"); edit(190, "5");
                    server(() -> ((InfernalBoilerMachine) machine(boiler)).getRecipeLogic().setCycleCounter(0));
                }
                case 19 -> click(250, 174);
                case 20 -> server(() -> check(part(boilerHatch).output() == 5, "string UI matches named heat level"));
                case 21 -> { screenshot("string.png"); server(() -> { form(gas); open(gasHatch); }); }
                case 22 -> click(12, 36);
                case 23 -> {
                    check(!field(4).isVisible() && candidateCount(selector(190)) == 2, "boolean UI hides operand and offers true/false");
                    screenshot("boolean.png"); choose(190, 0);
                }
                case 24 -> click(250, 174);
                case 25 -> server(() -> check(part(gasHatch).output() == 0, "boolean true rule does not match low tanks"));
                case 26 -> choose(190, 1);
                case 27 -> click(250, 174);
                case 28 -> server(() -> check(part(gasHatch).output() == 7, "boolean false rule matches"));
                case 29 -> server(() -> machine(gas).onStructureInvalid());
                case 30 -> { screenshot("disconnected.png"); server(() -> check(part(gasHatch).output() == 0, "disconnect while editor open clears output")); }
                case 31 -> server(() -> form(gas));
                case 32 -> server(() -> check(part(gasHatch).output() == 7, "open editor survives reconnection"));
                case 33 -> {
                    var send = Widget.class.getDeclaredMethod("writeClientAction", int.class, java.util.function.Consumer.class);
                    send.setAccessible(true);
                    send.invoke(field(190), 1, (java.util.function.Consumer<net.minecraft.network.FriendlyByteBuf>) buffer -> buffer.writeUtf("150"));
                }
                case 34 -> click(250, 174);
                case 35 -> server(() -> check(part(gasHatch).rules().get(0).strength() == 7, "oversized strength packet is rejected rather than truncated to 15"));
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
        return ui().widgets.stream().filter(widget -> widget instanceof TextFieldWidget && widget.getSelfPosition().x == x)
                .map(widget -> (TextFieldWidget) widget).findFirst().orElseThrow();
    }
    private static SelectorWidget selector(int x) {
        return ui().widgets.stream().filter(widget -> widget instanceof SelectorWidget && widget.getSelfPosition().x == x)
                .map(widget -> (SelectorWidget) widget).findFirst().orElseThrow();
    }
    private static void edit(int x, String value) {
        click(x + 8, 174);
        Minecraft.getInstance().screen.keyPressed(269, 0, 0);
        for (int i = 0; i < 256; i++) Minecraft.getInstance().screen.keyPressed(259, 0, 0);
        for (char character : value.toCharArray()) Minecraft.getInstance().screen.charTyped(character, 0);
    }

    private static void choose(int x, int index) {
        click(x + 8, 150);
        click(x + 8, 162 + index * 15 + 7);
    }
    private static void click(int x, int y) {
        var position = ui().getPosition();
        Minecraft.getInstance().screen.mouseClicked(position.x + x, position.y + y, 0);
        Minecraft.getInstance().screen.mouseReleased(position.x + x, position.y + y, 0);
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

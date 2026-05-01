package site.siredvin.gttruesteam.client;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import site.siredvin.gttruesteam.GTTrueSteam;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

public class MultiblockPreviewGenerator {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String ENABLED_PROPERTY = "gttruesteam.generateMultiblockPreviews";
    private static final String OUTPUT_PROPERTY = "gttruesteam.multiblockPreviewOutput";
    private static final String FORMAT_PROPERTY = "gttruesteam.multiblockPreviewFormat";
    private static final String DEBUG_PROPERTY = "gttruesteam.multiblockPreviewDebug";
    private static final String FILTER_PROPERTY = "gttruesteam.multiblockPreviewFilter";
    private static final String LEVEL_NAME = "gttruesteam_multiblock_previews";
    private static final BlockPos ORIGIN = new BlockPos(0, 72, 0);
    private static final Bounds EMPTY_BOUNDS = new Bounds(0, 0, 0, 0, 0, 0);
    private static final int STARTUP_TICKS = 100;
    private static final int RENDER_SETTLE_TICKS = 40;
    private static final int FRAME_SETTLE_TICKS = 8;
    private static final int GIF_FRAME_COUNT = 64;
    private static final int GIF_FRAME_DELAY_CENTISECONDS = 3;
    private static final double CAMERA_FOV_DEGREES = 40.0;
    private static final double CAMERA_DISTANCE_MARGIN = 1.05;
    private static final double CAMERA_TARGET_Y_OFFSET = -0.20;

    private final Path outputRoot;
    private final Path screenshotsRoot;
    private final Path framesRoot;
    private final PreviewFormat format;
    private final boolean debugFrames;
    private final String machineFilter;
    private final List<Preview> previews = new ArrayList<>();
    private final List<CapturedPreview> capturedPreviews = new ArrayList<>();

    private boolean openingWorld;
    private boolean started;
    private boolean preparingPreview;
    private boolean screenshotPending;
    private AtomicBoolean screenshotDone = new AtomicBoolean();
    private AtomicBoolean previewPrepared = new AtomicBoolean();
    private AtomicReference<Throwable> previewError = new AtomicReference<>();
    private int startupTicks = STARTUP_TICKS;
    private int renderSettleTicks;
    private int pendingSettleTicks;
    private int previewIndex;
    private int frameIndex;
    private Preview currentPreview;
    private Bounds currentBounds;
    private Bounds lastBounds = EMPTY_BOUNDS;

    private MultiblockPreviewGenerator(Path outputRoot, PreviewFormat format, boolean debugFrames,
                                       String machineFilter) {
        this.outputRoot = outputRoot;
        this.screenshotsRoot = outputRoot.resolve("screenshots");
        this.framesRoot = screenshotsRoot.resolve("frames");
        this.format = format;
        this.debugFrames = debugFrames;
        this.machineFilter = machineFilter;
    }

    public static void registerIfEnabled() {
        if (!Boolean.getBoolean(ENABLED_PROPERTY)) {
            return;
        }
        String output = System.getProperty(OUTPUT_PROPERTY, "build/multiblock-previews");
        PreviewFormat format = PreviewFormat.parse(System.getProperty(FORMAT_PROPERTY, "gif"));
        boolean debugFrames = Boolean.getBoolean(DEBUG_PROPERTY);
        String machineFilter = System.getProperty(FILTER_PROPERTY, "").trim();
        MinecraftForge.EVENT_BUS.register(
                new MultiblockPreviewGenerator(Path.of(output).toAbsolutePath(), format, debugFrames, machineFilter));
        LOGGER.info("Enabled GT True Steam multiblock preview generation in {} mode{}{}",
                format.name().toLowerCase(Locale.ROOT), debugFrames ? " with debug frames" : "",
                machineFilter.isEmpty() ? "" : " filtered by '" + machineFilter + "'");
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        try {
            tick(minecraft);
        } catch (Throwable e) {
            LOGGER.error("Failed to generate multiblock previews", e);
            finish(minecraft, 2);
        }
    }

    private void tick(Minecraft minecraft) throws IOException {
        if (minecraft.level == null || minecraft.player == null) {
            openWorldIfNeeded(minecraft);
            return;
        }

        MinecraftServer server = minecraft.getSingleplayerServer();
        if (server == null || server.overworld().players().isEmpty()) {
            return;
        }

        if (!started) {
            if (startupTicks-- > 0) {
                return;
            }
            start(minecraft, server);
        }

        if (currentPreview == null) {
            if (previewIndex >= previews.size()) {
                writeIndex();
                LOGGER.info("Generated {} multiblock previews in {}", capturedPreviews.size(), outputRoot);
                finish(minecraft, 0);
                return;
            }
            beginPreparePreview(minecraft, server, previews.get(previewIndex));
            return;
        }

        if (preparingPreview) {
            Throwable error = previewError.get();
            if (error != null) {
                throw new IllegalStateException("Failed to prepare preview " + currentPreview.definition().getId(),
                        error);
            }
            if (!previewPrepared.get()) {
                return;
            }
            preparingPreview = false;
            renderSettleTicks = pendingSettleTicks;
            minecraft.levelRenderer.allChanged();
            return;
        }

        if (renderSettleTicks-- > 0) {
            return;
        }

        if (!screenshotPending) {
            takeScreenshot(minecraft, currentPreview);
            return;
        }

        if (screenshotDone.get()) {
            screenshotPending = false;
            finishFrame(minecraft, server);
        }
    }

    private void openWorldIfNeeded(Minecraft minecraft) {
        if (openingWorld || !(minecraft.screen instanceof TitleScreen ||
                minecraft.screen instanceof AccessibilityOnboardingScreen)) {
            return;
        }

        openingWorld = true;
        configureClientOptions(minecraft);
        GameRules rules = new GameRules();
        rules.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
        rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, null);
        rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);

        if (minecraft.getLevelSource().levelExists(LEVEL_NAME)) {
            LOGGER.info("Opening existing multiblock preview world");
            minecraft.createWorldOpenFlows().loadLevel(minecraft.screen, LEVEL_NAME);
        } else {
            LOGGER.info("Creating multiblock preview world");
            minecraft.createWorldOpenFlows().createFreshLevel(
                    LEVEL_NAME,
                    new LevelSettings("GT True Steam Multiblock Previews", GameType.CREATIVE, false,
                            Difficulty.PEACEFUL, true, rules, WorldDataConfiguration.DEFAULT),
                    new WorldOptions(WorldOptions.randomSeed(), false, false),
                    registryAccess -> registryAccess.registryOrThrow(Registries.WORLD_PRESET)
                            .getOrThrow(WorldPresets.FLAT)
                            .createWorldDimensions());
        }
    }

    private void start(Minecraft minecraft, MinecraftServer server) throws IOException {
        started = true;
        configureClientOptions(minecraft);
        server.execute(() -> configureWorld(server.overworld()));
        prepareOutput();

        GTRegistries.MACHINES.values().stream()
                .filter(MultiblockMachineDefinition.class::isInstance)
                .map(MultiblockMachineDefinition.class::cast)
                .filter(definition -> GTTrueSteam.MOD_ID.equals(definition.getId().getNamespace()))
                .filter(definition -> matchesFilter(definition, machineFilter))
                .sorted(Comparator.comparing(definition -> definition.getId().toString()))
                .forEach(this::addPreviews);

        LOGGER.info("Found {} GT True Steam multiblock preview shapes", previews.size());
        if (previews.isEmpty()) {
            throw new IllegalStateException(machineFilter.isEmpty() ? "No GT True Steam multiblocks were found" :
                    "No GT True Steam multiblocks matched filter '" + machineFilter + "'");
        }
    }

    private boolean matchesFilter(MultiblockMachineDefinition definition, String filter) {
        if (filter.isEmpty()) {
            return true;
        }
        String id = definition.getId().toString();
        String path = definition.getId().getPath();
        return id.equals(filter) || path.equals(filter) || id.contains(filter) || path.contains(filter);
    }

    private void configureClientOptions(Minecraft minecraft) {
        minecraft.options.hideGui = true;
        minecraft.options.pauseOnLostFocus = false;
        minecraft.options.cloudStatus().set(CloudStatus.OFF);
        minecraft.options.particles().set(ParticleStatus.MINIMAL);
        minecraft.options.tutorialStep = TutorialSteps.NONE;
        minecraft.options.renderDistance().set(8);
        minecraft.options.gamma().set(1.0);
        minecraft.options.fov().set((int) CAMERA_FOV_DEGREES);
    }

    private void configureWorld(ServerLevel level) {
        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());
        level.setDayTime(6000L);
        level.setWeatherParameters(0, 0, false, false);
    }

    private void prepareOutput() throws IOException {
        Files.createDirectories(screenshotsRoot);
        Files.createDirectories(framesRoot);
        try (DirectoryStream<Path> files = Files.newDirectoryStream(screenshotsRoot, "*.png")) {
            for (Path file : files) {
                Files.deleteIfExists(file);
            }
        }
        try (DirectoryStream<Path> files = Files.newDirectoryStream(screenshotsRoot, "*.gif")) {
            for (Path file : files) {
                Files.deleteIfExists(file);
            }
        }
        try (DirectoryStream<Path> files = Files.newDirectoryStream(framesRoot, "*.png")) {
            for (Path file : files) {
                Files.deleteIfExists(file);
            }
        }
        Files.deleteIfExists(outputRoot.resolve("index.html"));
    }

    private void addPreviews(MultiblockMachineDefinition definition) {
        List<MultiblockShapeInfo> shapes = definition.getMatchingShapes();
        for (int i : selectedShapeIndexes(shapes.size())) {
            previews.add(new Preview(definition, shapes.get(i), i + 1, shapes.size()));
        }
    }

    private List<Integer> selectedShapeIndexes(int shapeCount) {
        if (shapeCount <= 5) {
            List<Integer> indexes = new ArrayList<>();
            for (int i = 0; i < shapeCount; i++) {
                indexes.add(i);
            }
            return indexes;
        }
        Set<Integer> indexes = new LinkedHashSet<>();
        indexes.add(0);
        indexes.add(Math.min(2, shapeCount - 1));
        indexes.add(Math.min(4, shapeCount - 1));
        return List.copyOf(indexes);
    }

    private void beginPreparePreview(Minecraft minecraft, MinecraftServer server, Preview preview) {
        currentPreview = preview;
        currentBounds = null;
        preparingPreview = true;
        screenshotPending = false;
        screenshotDone.set(false);
        previewPrepared.set(false);
        previewError.set(null);
        frameIndex = 0;
        pendingSettleTicks = RENDER_SETTLE_TICKS;

        server.execute(() -> {
            try {
                ServerLevel level = server.overworld();
                ServerPlayer player = level.players().get(0);
                Bounds bounds = bounds(preview.shapeInfo());
                clearPreviewArea(level, bounds.union(lastBounds));
                placeShape(level, preview.shapeInfo());
                lastBounds = bounds;
                currentBounds = bounds;

                positionCamera(player, bounds, frameAngle(0));
            } catch (Throwable e) {
                previewError.set(e);
            } finally {
                previewPrepared.set(true);
            }
        });

        LOGGER.info("Rendering preview {}/{}: {} shape {}/{}", previewIndex + 1, previews.size(),
                preview.definition().getId(), preview.shapeIndex(), preview.shapeCount());
    }

    private Bounds bounds(MultiblockShapeInfo shapeInfo) {
        BlockInfo[][][] blocks = shapeInfo.getBlocks();
        int sizeX = blocks.length;
        int sizeY = 0;
        int sizeZ = 0;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (int x = 0; x < blocks.length; x++) {
            BlockInfo[][] aisle = blocks[x];
            sizeY = Math.max(sizeY, aisle.length);
            for (int y = 0; y < aisle.length; y++) {
                BlockInfo[] column = aisle[y];
                sizeZ = Math.max(sizeZ, column.length);
                for (int z = 0; z < column.length; z++) {
                    if (column[z].getBlockState().isAir()) {
                        continue;
                    }
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    minZ = Math.min(minZ, z);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                    maxZ = Math.max(maxZ, z);
                }
            }
        }
        if (minX == Integer.MAX_VALUE) {
            return new Bounds(0, 0, 0, Math.max(0, sizeX - 1), Math.max(0, sizeY - 1), Math.max(0, sizeZ - 1));
        }
        return new Bounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private void clearPreviewArea(ServerLevel level, Bounds bounds) {
        BlockPos min = ORIGIN.offset(bounds.minX() - 4, bounds.minY() - 2, bounds.minZ() - 4);
        BlockPos max = ORIGIN.offset(bounds.maxX() + 4, bounds.maxY() + 4, bounds.maxZ() + 4);
        BlockState air = Blocks.AIR.defaultBlockState();
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            level.setBlockAndUpdate(pos, air);
        }
    }

    private void placeShape(ServerLevel level, MultiblockShapeInfo shapeInfo) {
        BlockInfo[][][] blocks = shapeInfo.getBlocks();
        for (int x = 0; x < blocks.length; x++) {
            BlockInfo[][] aisle = blocks[x];
            for (int y = 0; y < aisle.length; y++) {
                BlockInfo[] column = aisle[y];
                for (int z = 0; z < column.length; z++) {
                    BlockState state = column[z].getBlockState();
                    if (!state.isAir()) {
                        level.setBlockAndUpdate(ORIGIN.offset(x, y, z), state);
                    }
                }
            }
        }
    }

    private void positionCamera(ServerPlayer player, Bounds bounds, double angle) {
        double centerX = ORIGIN.getX() + bounds.centerX();
        double centerY = ORIGIN.getY() + bounds.centerY();
        double centerZ = ORIGIN.getZ() + bounds.centerZ();
        double targetY = centerY + bounds.sizeY() * CAMERA_TARGET_Y_OFFSET;
        double radius = Math.sqrt(bounds.sizeX() * bounds.sizeX() + bounds.sizeY() * bounds.sizeY() +
                bounds.sizeZ() * bounds.sizeZ()) / 2.0;
        double distance = Math.max(4.0,
                radius / Math.tan(Math.toRadians(CAMERA_FOV_DEGREES / 2.0)) * CAMERA_DISTANCE_MARGIN);
        double cameraX = centerX + Math.cos(angle) * distance;
        double cameraY = centerY + Math.max(1.5, bounds.sizeY() * 0.25);
        double cameraZ = centerZ + Math.sin(angle) * distance;

        double lookX = centerX - cameraX;
        double lookY = targetY - cameraY;
        double lookZ = centerZ - cameraZ;
        double horizontal = Math.sqrt(lookX * lookX + lookZ * lookZ);
        float yaw = (float) (Math.toDegrees(Math.atan2(lookZ, lookX)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(lookY, horizontal));

        player.setGameMode(GameType.SPECTATOR);
        player.connection.teleport(cameraX, cameraY, cameraZ, yaw, pitch);
    }

    private double frameAngle(int frame) {
        return Math.toRadians(-135.0 + (360.0 * frame / GIF_FRAME_COUNT));
    }

    private void takeScreenshot(Minecraft minecraft, Preview preview) {
        String fileName = format == PreviewFormat.PNG ? previewFileName(preview, "png") :
                frameFileName(preview, frameIndex);
        screenshotPending = true;
        screenshotDone.set(false);
        Screenshot.grab(outputRoot.toFile(), fileName, minecraft.getMainRenderTarget(),
                component -> screenshotDone.set(true));
        if (format == PreviewFormat.PNG) {
            capturedPreviews.add(new CapturedPreview(preview, "screenshots/" + fileName));
        }
    }

    private void finishFrame(Minecraft minecraft, MinecraftServer server) throws IOException {
        if (format == PreviewFormat.PNG) {
            cropPng(screenshotsRoot.resolve(previewFileName(currentPreview, "png")));
            finishPreview();
            return;
        }

        if (frameIndex + 1 >= GIF_FRAME_COUNT) {
            String fileName = previewFileName(currentPreview, "gif");
            writeGif(currentPreview, screenshotsRoot.resolve(fileName));
            capturedPreviews.add(new CapturedPreview(currentPreview, "screenshots/" + fileName));
            if (debugFrames) {
                LOGGER.info("Kept debug GIF frames for {}", currentPreview.definition().getId());
            } else {
                deleteFrameImages(currentPreview);
            }
            finishPreview();
            return;
        }

        frameIndex++;
        beginFrame(server);
    }

    private void finishPreview() {
        currentPreview = null;
        currentBounds = null;
        previewIndex++;
    }

    private void beginFrame(MinecraftServer server) {
        preparingPreview = true;
        previewPrepared.set(false);
        previewError.set(null);
        pendingSettleTicks = FRAME_SETTLE_TICKS;
        server.execute(() -> {
            try {
                ServerLevel level = server.overworld();
                ServerPlayer player = level.players().get(0);
                positionCamera(player, currentBounds, frameAngle(frameIndex));
            } catch (Throwable e) {
                previewError.set(e);
            } finally {
                previewPrepared.set(true);
            }
        });
    }

    private String previewFileName(Preview preview, String extension) {
        return previewBaseName(preview) + "." + extension;
    }

    private String previewBaseName(Preview preview) {
        String id = preview.definition().getId().getPath().replace('/', '_');
        if (preview.shapeCount() == 1) {
            return id;
        }
        return id + "_shape_" + preview.shapeIndex();
    }

    private String frameFileName(Preview preview, int frame) {
        return "frames/" + previewBaseName(preview) + "_frame_" +
                String.format(Locale.ROOT, "%02d.png", frame);
    }

    private Path framePath(Preview preview, int frame) {
        return screenshotsRoot.resolve(frameFileName(preview, frame));
    }

    private void writeGif(Preview preview, Path output) throws IOException {
        try (ImageOutputStream stream = ImageIO.createImageOutputStream(output.toFile())) {
            BufferedImage first = readGifFrame(preview, 0);
            GifSequenceWriter writer = new GifSequenceWriter(stream, first.getType(), GIF_FRAME_DELAY_CENTISECONDS,
                    true);
            writer.writeToSequence(first);
            for (int i = 1; i < GIF_FRAME_COUNT; i++) {
                writer.writeToSequence(readGifFrame(preview, i));
            }
            writer.close();
        }
    }

    private BufferedImage readGifFrame(Preview preview, int frame) throws IOException {
        Path path = framePath(preview, frame);
        BufferedImage image = cropToSquare(ImageIO.read(path.toFile()));
        if (debugFrames) {
            ImageIO.write(image, "png", path.toFile());
        }
        return image;
    }

    private void cropPng(Path path) throws IOException {
        ImageIO.write(cropToSquare(ImageIO.read(path.toFile())), "png", path.toFile());
    }

    private BufferedImage cropToSquare(BufferedImage image) {
        int size = Math.min(image.getWidth(), image.getHeight());
        int x = (image.getWidth() - size) / 2;
        int y = (image.getHeight() - size) / 2;
        BufferedImage square = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = square.createGraphics();
        graphics.drawImage(image, -x, -y, null);
        graphics.dispose();
        return square;
    }

    private void deleteFrameImages(Preview preview) throws IOException {
        for (int i = 0; i < GIF_FRAME_COUNT; i++) {
            Files.deleteIfExists(framePath(preview, i));
        }
    }

    private void writeIndex() throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n")
                .append("<meta charset=\"utf-8\" />\n")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n")
                .append("<title>GT True Steam Multiblock Previews</title>\n")
                .append("<style>body{margin:0;font-family:Arial,sans-serif;background:#151515;color:#eee}")
                .append(".content{display:grid;grid-template-columns:repeat(auto-fit,minmax(28rem,1fr));gap:1rem;padding:1rem}")
                .append(".card{background:#202020;border:1px solid #333;padding:.75rem}.card img{width:100%;height:auto;display:block}")
                .append(".name{display:block;margin-top:.5rem;text-align:center;color:#ddd}</style>\n")
                .append("</head>\n<body>\n<div class=\"content\">\n");

        for (CapturedPreview captured : capturedPreviews) {
            String label = captured.label();
            html.append("<div class=\"card\"><img src=\"")
                    .append(escapeHtml(captured.path()))
                    .append("\" alt=\"")
                    .append(escapeHtml(label))
                    .append("\" /><span class=\"name\">")
                    .append(escapeHtml(label))
                    .append("</span></div>\n");
        }
        html.append("</div>\n</body>\n</html>\n");
        Files.writeString(outputRoot.resolve("index.html"), html.toString(), StandardCharsets.UTF_8);
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void finish(Minecraft minecraft, int exitCode) {
        MinecraftForge.EVENT_BUS.unregister(this);
        minecraft.options.hideGui = false;
        minecraft.stop();
        System.exit(exitCode);
    }

    private enum PreviewFormat {

        GIF,
        PNG;

        private static PreviewFormat parse(String value) {
            if ("png".equalsIgnoreCase(value)) {
                return PNG;
            }
            if (!"gif".equalsIgnoreCase(value)) {
                LOGGER.warn("Unknown multiblock preview format '{}', falling back to gif", value);
            }
            return GIF;
        }
    }

    private static class GifSequenceWriter {

        private final ImageWriter writer;
        private final ImageWriteParam params;
        private final IIOMetadata metadata;

        private GifSequenceWriter(ImageOutputStream outputStream, int imageType, int delayCentiseconds, boolean loop)
                                                                                                                      throws IOException {
            writer = ImageIO.getImageWritersBySuffix("gif").next();
            params = writer.getDefaultWriteParam();
            ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(
                    imageType == BufferedImage.TYPE_CUSTOM ? BufferedImage.TYPE_INT_ARGB : imageType);
            metadata = writer.getDefaultImageMetadata(imageTypeSpecifier, params);

            String metaFormatName = metadata.getNativeMetadataFormatName();
            Node root = metadata.getAsTree(metaFormatName);
            IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
            graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
            graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
            graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
            graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(delayCentiseconds));
            graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

            IIOMetadataNode appExtensionsNode = getNode(root, "ApplicationExtensions");
            IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
            child.setAttribute("applicationID", "NETSCAPE");
            child.setAttribute("authenticationCode", "2.0");
            int loopCount = loop ? 0 : 1;
            child.setUserObject(new byte[] { 0x1, (byte) (loopCount & 0xFF), (byte) ((loopCount >> 8) & 0xFF) });
            appExtensionsNode.appendChild(child);

            metadata.setFromTree(metaFormatName, root);
            writer.setOutput(outputStream);
            writer.prepareWriteSequence(null);
        }

        private void writeToSequence(BufferedImage image) throws IOException {
            writer.writeToSequence(new IIOImage(image, null, metadata), params);
        }

        private void close() throws IOException {
            writer.endWriteSequence();
        }

        private static IIOMetadataNode getNode(Node rootNode, String nodeName) {
            for (int i = 0; i < rootNode.getChildNodes().getLength(); i++) {
                Node node = rootNode.getChildNodes().item(i);
                if (node.getNodeName().equalsIgnoreCase(nodeName)) {
                    return (IIOMetadataNode) node;
                }
            }

            IIOMetadataNode node = new IIOMetadataNode(nodeName);
            rootNode.appendChild(node);
            return node;
        }
    }

    private record Preview(MultiblockMachineDefinition definition, MultiblockShapeInfo shapeInfo, int shapeIndex,
                           int shapeCount) {}

    private record CapturedPreview(Preview preview, String path) {

        private String label() {
            if (preview.shapeCount() == 1) {
                return preview.definition().getId().toString();
            }
            return preview.definition().getId() + " shape " + preview.shapeIndex() + " of " + preview.shapeCount();
        }
    }

    private record Bounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

        private int sizeX() {
            return maxX - minX + 1;
        }

        private int sizeY() {
            return maxY - minY + 1;
        }

        private int sizeZ() {
            return maxZ - minZ + 1;
        }

        private double centerX() {
            return (minX + maxX) / 2.0;
        }

        private double centerY() {
            return (minY + maxY) / 2.0;
        }

        private double centerZ() {
            return (minZ + maxZ) / 2.0;
        }

        private Bounds union(Bounds other) {
            return new Bounds(Math.min(minX, other.minX), Math.min(minY, other.minY), Math.min(minZ, other.minZ),
                    Math.max(maxX, other.maxX), Math.max(maxY, other.maxY), Math.max(maxZ, other.maxZ));
        }
    }
}

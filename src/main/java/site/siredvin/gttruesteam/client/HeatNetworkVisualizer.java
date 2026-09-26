package site.siredvin.gttruesteam.client;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import site.siredvin.gttruesteam.TrueSteamItems;
import site.siredvin.gttruesteam.machines.parts.HeatHatchMachine;
import site.siredvin.gttruesteam.machines.shared.heat.HeatNetwork;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Client-only physical connectivity aid; never loads chunks or queries server-owned heat state. */
@Mod.EventBusSubscriber(modid = "gttruesteam", value = Dist.CLIENT)
public final class HeatNetworkVisualizer {
    private static final int RADIUS = 32;
    private static final Set<BlockPos> HATCHES = new HashSet<>();
    private static final Set<BlockPos> VENTS = new HashSet<>();
    private static ClientLevel lastLevel;
    private static int cooldown;
    private static PoseStack worldPose;

    private HeatNetworkVisualizer() {}

    private static boolean held() {
        var player = Minecraft.getInstance().player;
        return player != null && (player.getMainHandItem().is(TrueSteamItems.DebugHeatNetworkVisualizer.get()) ||
                player.getOffhandItem().is(TrueSteamItems.DebugHeatNetworkVisualizer.get()));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        if (level != lastLevel || !held()) {
            HATCHES.clear();
            VENTS.clear();
            cooldown = 0;
            lastLevel = level;
        }
        if (level == null || !held() || cooldown-- > 0) return;
        cooldown = 9;
        HATCHES.clear();
        VENTS.clear();
        BlockPos center = minecraft.player.blockPosition();
        Map<BlockPos, HeatHatchMachine> sources = new HashMap<>();
        for (int x = (center.getX() - RADIUS) >> 4; x <= (center.getX() + RADIUS) >> 4; x++) {
            for (int z = (center.getZ() - RADIUS) >> 4; z <= (center.getZ() + RADIUS) >> 4; z++) {
                var chunk = level.getChunkSource().getChunk(x, z, ChunkStatus.FULL, false);
                if (chunk == null) continue;
                for (BlockPos pos : chunk.getBlockEntitiesPos()) {
                    if (pos.distSqr(center) <= RADIUS * RADIUS &&
                            MetaMachine.getMachine(chunk, pos) instanceof HeatHatchMachine hatch) {
                        sources.put(pos.immutable(), hatch);
                    }
                }
            }
        }
        // Cache node reads across all nearby sources, including overlapping networks.
        Map<BlockPos, HeatNetwork.Node> nodes = new HashMap<>();
        HeatNetwork.Lookup lookup = new HeatNetwork.Lookup() {
            @Override
            public boolean loaded(BlockPos pos) {
                return !level.isOutsideBuildHeight(pos) &&
                        level.getChunkSource().getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false) != null;
            }

            @Override
            public HeatNetwork.Node node(BlockPos pos) {
                return nodes.computeIfAbsent(pos, key -> {
                    var chunk = level.getChunkSource().getChunk(key.getX() >> 4, key.getZ() >> 4, ChunkStatus.FULL, false);
                    if (chunk != null) {
                        if (chunk.getBlockState(key).is(GTBlocks.COMPUTER_HEAT_VENT.get())) {
                            return new HeatNetwork.Node(HeatNetwork.Kind.VENT, null);
                        }
                        if (MetaMachine.getMachine(chunk, key) instanceof HeatHatchMachine hatch) {
                            return new HeatNetwork.Node(HeatNetwork.Kind.HATCH, hatch.getFrontFacing());
                        }
                    }
                    return new HeatNetwork.Node(HeatNetwork.Kind.BLOCKED, null);
                });
            }
        };
        sources.forEach((pos, hatch) -> {
            var trace = HeatNetwork.trace(pos, hatch.getFrontFacing(), lookup);
            if (!trace.hatches().isEmpty()) {
                HATCHES.add(pos);
                HATCHES.addAll(trace.hatches());
                VENTS.addAll(trace.vents());
            }
        });
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        // Forge 1.20.1 passes its projection-effects stack at AFTER_LEVEL, not the
        // camera-rotated world stack. Capture the latter before the late overlay pass.
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            worldPose = null;
            if (held() && !HATCHES.isEmpty()) {
                worldPose = new PoseStack();
                worldPose.last().pose().set(event.getPoseStack().last().pose());
                worldPose.last().normal().set(event.getPoseStack().last().normal());
            }
            return;
        }
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
        var pose = worldPose;
        worldPose = null;
        if (pose == null || !held() || HATCHES.isEmpty()) return;
        var camera = event.getCamera().getPosition();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        try {
            var buffer = Tesselator.getInstance().getBuilder();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            for (BlockPos pos : VENTS) {
                LevelRenderer.addChainedFilledBoxVertices(pose, buffer,
                        pos.getX() - 0.01, pos.getY() - 0.01, pos.getZ() - 0.01,
                        pos.getX() + 1.01, pos.getY() + 1.01, pos.getZ() + 1.01, 1f, 0.55f, 0.1f, 0.22f);
            }
            for (BlockPos pos : HATCHES) {
                LevelRenderer.addChainedFilledBoxVertices(pose, buffer,
                        pos.getX() - 0.01, pos.getY() - 0.01, pos.getZ() - 0.01,
                        pos.getX() + 1.01, pos.getY() + 1.01, pos.getZ() + 1.01, 0.1f, 0.9f, 1f, 0.3f);
            }
            BufferUploader.drawWithShader(buffer.end());
            RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
            RenderSystem.lineWidth(5f);
            buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            for (BlockPos pos : VENTS) {
                LevelRenderer.renderLineBox(pose, buffer, new AABB(pos).inflate(0.015), 1f, 0.55f, 0.1f, 1f);
            }
            for (BlockPos pos : HATCHES) {
                LevelRenderer.renderLineBox(pose, buffer, new AABB(pos).inflate(0.015), 0.1f, 0.9f, 1f, 1f);
            }
            BufferUploader.drawWithShader(buffer.end());
        } finally {
            RenderSystem.lineWidth(1f);
            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            pose.popPose();
        }
    }
}

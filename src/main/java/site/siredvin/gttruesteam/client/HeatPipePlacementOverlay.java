package site.siredvin.gttruesteam.client;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import site.siredvin.gttruesteam.common.InsulatedHeatPipeBlock;
import site.siredvin.gttruesteam.common.InsulatedHeatPipeItem;

/** GTCEu's placement renderer only accepts its block-entity-backed pipe classes. */
@Mod.EventBusSubscriber(modid = "gttruesteam", value = Dist.CLIENT)
public final class HeatPipePlacementOverlay {
    private HeatPipePlacementOverlay() {}

    public static boolean shouldRender(ItemStack mainHand, ItemStack offHand, BlockState target) {
        // Native tool rendering takes priority, as it does for GregTech pipes.
        return ToolHelper.getToolTypes(mainHand).isEmpty() &&
                (mainHand.getItem() instanceof InsulatedHeatPipeItem ||
                        mainHand.isEmpty() && offHand.getItem() instanceof InsulatedHeatPipeItem) &&
                target.getBlock() instanceof InsulatedHeatPipeBlock;
    }

    @SubscribeEvent
    public static void render(RenderHighlightEvent.Block event) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        var level = minecraft.level;
        var hit = event.getTarget();
        if (player == null || level == null || !shouldRender(player.getMainHandItem(), player.getOffhandItem(),
                level.getBlockState(hit.getBlockPos()))) return;
        var pose = event.getPoseStack();
        var buffers = event.getMultiBufferSource();
        var front = hit.getDirection();
        var selected = ICoverable.traceCoverSide(hit);
        var camera = event.getCamera().getPosition();
        pose.pushPose();
        try {
            pose.translate(-camera.x, -camera.y, -camera.z);
            pose.translate(front.getStepX() * 0.01, front.getStepY() * 0.01, front.getStepZ() * 0.01);
            RenderUtil.moveToFace(pose, hit.getBlockPos().getCenter().toVector3f(), front);
            RenderUtil.rotateToFace(pose, front, Direction.SOUTH);
            pose.scale(1f / 16, 1f / 16, 1f / 16);
            pose.translate(-8, -8, 0);
            var lines = buffers.getBuffer(RenderType.lines());
            for (int coordinate : new int[] { 4, 12 }) {
                line(pose, lines, coordinate, 0, coordinate, 16);
                line(pose, lines, 0, coordinate, 16, coordinate);
            }
            Direction[] sides = { front.getOpposite(), RelativeDirection.DOWN.getActualDirection(front), front.getOpposite(),
                    RelativeDirection.LEFT.getActualDirection(front), front, RelativeDirection.RIGHT.getActualDirection(front),
                    front.getOpposite(), RelativeDirection.UP.getActualDirection(front), front.getOpposite() };
            for (int i = 0; i < sides.length; i++) {
                if (!level.isEmptyBlock(hit.getBlockPos().relative(sides[i]))) continue;
                icon(pose, buffers, (i % 3) * 6, (i / 3) * 6, sides[i] == selected ? 0xffffffff : 0x44ffffff);
            }
        } finally {
            pose.popPose();
        }
    }

    private static void line(PoseStack pose, VertexConsumer buffer, float x1, float y1, float x2, float y2) {
        float nx = x2 == x1 ? 0 : 1;
        float ny = y2 == y1 ? 0 : 1;
        buffer.vertex(pose.last().pose(), x1, y1, 0).color(0.4f, 0.4f, 1f, 1f)
                .normal(pose.last().normal(), nx, ny, 0).endVertex();
        buffer.vertex(pose.last().pose(), x2, y2, 0).color(0.4f, 0.4f, 1f, 1f)
                .normal(pose.last().normal(), nx, ny, 0).endVertex();
    }

    private static void icon(PoseStack pose, MultiBufferSource buffers, float x, float y, int color) {
        var texture = GuiTextures.TOOL_PIPE_CONNECT;
        var vertices = buffers.getBuffer(RenderType.text(texture.imageLocation));
        var matrix = pose.last().pose();
        float left = x + 0.2f, right = x + 3.8f, bottom = y + 0.2f, top = y + 3.8f;
        float u = texture.offsetX, v = texture.offsetY, w = texture.imageWidth, h = texture.imageHeight;
        vertices.vertex(matrix, left, top, 0).color(color).uv(u, v + h).uv2(LightTexture.FULL_BRIGHT).endVertex();
        vertices.vertex(matrix, right, top, 0).color(color).uv(u + w, v + h).uv2(LightTexture.FULL_BRIGHT).endVertex();
        vertices.vertex(matrix, right, bottom, 0).color(color).uv(u + w, v).uv2(LightTexture.FULL_BRIGHT).endVertex();
        vertices.vertex(matrix, left, bottom, 0).color(color).uv(u, v).uv2(LightTexture.FULL_BRIGHT).endVertex();
    }
}

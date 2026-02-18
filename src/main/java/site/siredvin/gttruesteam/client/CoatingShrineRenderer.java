package site.siredvin.gttruesteam.client;

import com.google.common.cache.Cache;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import site.siredvin.gttruesteam.machines.coating_shrine.CoatingShrineMachine;

public class CoatingShrineRenderer extends DynamicRender<IRecipeLogicMachine, CoatingShrineRenderer> {

    // spotless:off
    @SuppressWarnings("deprecation")
    public static final Codec<CoatingShrineRenderer> CODEC =  Codec.unit(CoatingShrineRenderer::new);
    public static final DynamicRenderType<IRecipeLogicMachine, CoatingShrineRenderer> TYPE = new DynamicRenderType<>(CoatingShrineRenderer.CODEC);
    // spotless:on

    @Override
    public @NotNull DynamicRenderType<IRecipeLogicMachine, CoatingShrineRenderer> getType() {
        return TYPE;
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(IRecipeLogicMachine machine) {
        BlockPos pos = machine.self().getPos();
        var facing = ((CoatingShrineMachine)machine).getFrontFacing();
        var rightFacing = facing.getClockWise();
        var rightCorner = pos.offset(rightFacing.getStepX() * 2, 0, rightFacing.getStepZ() * 2);
        var leftCorner = pos.offset(-rightFacing.getStepX() * 2, 0, -rightFacing.getStepZ() * 2);
        var leftBackCorner = leftCorner.offset(-facing.getStepX() * 5, 5, -facing.getStepZ() * 5);
        return new AABB(rightCorner, leftBackCorner);
    }

    @Override
    public void render(@NotNull IRecipeLogicMachine machine, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (machine.isActive() && machine instanceof CoatingShrineMachine coatingMachine) {
            var recipeLogic = machine.getRecipeLogic();
            var lastRecipe = recipeLogic.getLastRecipe();
            if (lastRecipe != null) {
                var itemOutputs = lastRecipe.inputs.get(GTRecipeCapabilities.ITEM);
                var firstItem = itemOutputs.get(0).content;
                if (firstItem instanceof SizedIngredient firstItemStack) {
                    var direction = coatingMachine.getFrontFacing().getOpposite();
                    var movingDirection = direction.getClockWise();
                    var level = coatingMachine.getLevel();
                    var progress = recipeLogic.getProgressPercent() - 1;
                    poseStack.pushPose();
                    poseStack.translate(direction.getStepX() * 1.5, 0.8, direction.getStepZ() * 1.5);
                    poseStack.translate(movingDirection.getStepX() * progress, 0, movingDirection.getStepZ() * progress);
                    poseStack.mulPose(Axis.YP.rotationDegrees(90));
                    var stack = firstItemStack.getItems()[0];
                    var model = Minecraft.getInstance().getItemRenderer().getModel(stack, level, null, packedLight);
                    Minecraft.getInstance().getItemRenderer().render(
                            stack,
                            ItemDisplayContext.GROUND,
                            true,
                            poseStack,
                            buffer,
                            LightTexture.FULL_BRIGHT,
                            packedOverlay,
                            model
                    );
                    poseStack.popPose();
                }
            }
        }
    }
}

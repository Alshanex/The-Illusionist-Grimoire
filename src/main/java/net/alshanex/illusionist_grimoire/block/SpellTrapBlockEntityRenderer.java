package net.alshanex.illusionist_grimoire.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.util.Color;

public class SpellTrapBlockEntityRenderer extends GeoBlockRenderer<SpellTrapBlockEntity> {

    public SpellTrapBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(new MagicCircleModel());
        // Add glowing layer
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void preRender(PoseStack poseStack, SpellTrapBlockEntity blockEntity,
                          BakedGeoModel model, MultiBufferSource bufferSource,
                          com.mojang.blaze3d.vertex.VertexConsumer buffer,
                          boolean isReRender, float partialTick, int packedLight,
                          int packedOverlay, int color) {
        super.preRender(poseStack, blockEntity, model, bufferSource, buffer,
                isReRender, partialTick, packedLight, packedOverlay, color);

        Direction facing = blockEntity.getBlockState().getValue(SpellTrapBlock.FACING);

        // Move to center of block
        poseStack.translate(0.5, 0.5, 0.5);

        // Apply rotation based on facing direction
        switch (facing) {
            case UP -> {
                // Default orientation (circle facing up)
                // No rotation needed
            }
            case DOWN -> {
                // Flip upside down
                poseStack.mulPose(Axis.XP.rotationDegrees(180));
            }
            case NORTH -> {
                // Rotate to face north (rotate around X axis)
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
            }
            case SOUTH -> {
                // Rotate to face south
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
            }
            case EAST -> {
                // Rotate to face east
                poseStack.mulPose(Axis.ZP.rotationDegrees(90));
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
            }
            case WEST -> {
                // Rotate to face west
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90));
                poseStack.mulPose(Axis.YP.rotationDegrees(-90));
            }
        }

        // Move back and slightly offset from the block face
        poseStack.translate(-0.5, -0.48, -0.5);
    }

    @Override
    public Color getRenderColor(SpellTrapBlockEntity blockEntity, float partialTick, int packedLight) {
        Vector3f color = blockEntity.getSpellColor();

        return Color.ofRGB(color.x, color.y, color.z);
    }

    @Override
    public RenderType getRenderType(SpellTrapBlockEntity animatable, ResourceLocation texture,
                                    MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public boolean shouldRenderOffScreen(SpellTrapBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}

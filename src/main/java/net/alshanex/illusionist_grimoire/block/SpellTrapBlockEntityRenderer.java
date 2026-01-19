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
    private static final double THICKNESS = 0.0625;

    public SpellTrapBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(new MagicCircleModel());
        // Add glowing layer
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void render(SpellTrapBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        Direction facing = blockEntity.getBlockState().getValue(SpellTrapBlock.FACING);

        // Position and orient the model to match the voxel shape
        switch (facing) {
            case UP -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                poseStack.translate(0, -0.5, -0.55);
            }
            case DOWN -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                poseStack.translate(0, -0.5, 0.55);
            }
            case NORTH -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(-180));
                poseStack.mulPose(Axis.XP.rotationDegrees(-270));
                poseStack.translate(-1.0, -1.05, -1.0);
            }
            case SOUTH -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(-180));
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                poseStack.translate(-1.0, -0.05, 0);
            }
            case EAST -> {
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90));
                poseStack.translate(-1.0, -0.05, 0);
            }
            case WEST -> {
                poseStack.mulPose(Axis.ZP.rotationDegrees(90));
                poseStack.translate(0, -1.05, 0);
            }
        }

        super.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
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

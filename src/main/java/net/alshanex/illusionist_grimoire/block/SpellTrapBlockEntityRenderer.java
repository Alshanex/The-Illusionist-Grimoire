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

        poseStack.pushPose();

        // Position and orient the model to match the voxel shape
        switch (facing) {
            case UP -> {
                // Voxel: y=0 to y=0.0625 (bottom of block)
                // Circle should be horizontal at the bottom
                poseStack.translate(0.5, THICKNESS / 2.0, 0.5);
                // No rotation - default orientation is horizontal
            }
            case DOWN -> {
                // Voxel: y=15.9375 to y=16 (top of block)
                // Circle should be horizontal at the top, facing down
                poseStack.translate(0.5, 1.0 - THICKNESS / 2.0, 0.5);
                poseStack.mulPose(Axis.XP.rotationDegrees(180));
            }
            case NORTH -> {
                // Voxel: z=15.9375 to z=16 (far side)
                // Circle should be vertical on north wall
                poseStack.translate(0.5, 0.5, 1.0 - THICKNESS / 2.0);
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
            }
            case SOUTH -> {
                // Voxel: z=0 to z=0.0625 (near side)
                // Circle should be vertical on south wall
                poseStack.translate(0.5, 0.5, THICKNESS / 2.0);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            }
            case EAST -> {
                // Voxel: x=0 to x=0.0625 (left side)
                // Circle should be vertical on east wall
                poseStack.translate(THICKNESS / 2.0, 0.5, 0.5);
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90));
            }
            case WEST -> {
                // Voxel: x=15.9375 to x=16 (right side)
                // Circle should be vertical on west wall
                poseStack.translate(1.0 - THICKNESS / 2.0, 0.5, 0.5);
                poseStack.mulPose(Axis.ZP.rotationDegrees(90));
            }
        }

        // Call parent render with transformations applied
        super.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);

        poseStack.popPose();
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

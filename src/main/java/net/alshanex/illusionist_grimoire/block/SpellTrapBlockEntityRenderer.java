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

public class SpellTrapBlockEntityRenderer extends GeoBlockRenderer<SpellTrapBlockEntity> {

    public SpellTrapBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(new MagicCircleModel());
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, SpellTrapBlockEntity animatable, BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        Direction facing = animatable.getBlockState().getValue(SpellTrapBlock.FACING);

        // Rotate based on facing direction
        switch (facing) {
            case UP -> {
                // No rotation needed (default is horizontal)
            }
            case DOWN -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(180));
            }
            case NORTH -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
            }
            case SOUTH -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            }
            case EAST -> {
                poseStack.mulPose(Axis.ZP.rotationDegrees(90));
            }
            case WEST -> {
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90));
            }
        }
        super.scaleModelForRender(widthScale, heightScale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }

    @Override
    public RenderType getRenderType(SpellTrapBlockEntity animatable, ResourceLocation texture,
                                    MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public int getPackedOverlay(SpellTrapBlockEntity animatable, float u, float partialTick) {
        // No overlay tinting
        return 0;
    }

    @Override
    public void actuallyRender(PoseStack poseStack, SpellTrapBlockEntity blockEntity,
                               BakedGeoModel model, RenderType renderType,
                               MultiBufferSource bufferSource, VertexConsumer buffer,
                               boolean isReRender, float partialTick, int packedLight,
                               int packedOverlay, int renderColor) {

        // Get spell school color for tinting
        int tintColor = getSpellSchoolColor(blockEntity);

        // Call parent render with custom color (OVERRIDE the renderColor parameter)
        super.actuallyRender(poseStack, blockEntity, model, renderType, bufferSource,
                buffer, isReRender, partialTick, packedLight, packedOverlay, tintColor);
    }

    private int getSpellSchoolColor(SpellTrapBlockEntity blockEntity) {
        ResourceLocation spellId = blockEntity.getSpellId(); // Add this getter if missing
        if (spellId == null) {
            return 0xFFFFFFFF; // White if no spell
        }

        AbstractSpell spell = SpellRegistry.getSpell(spellId);
        if (spell == null) {
            return 0xFFFFFFFF; // White if spell not found
        }

        // Get the targeting color as Vector3f (RGB values 0.0-1.0)
        Vector3f color = spell.getSchoolType().getTargetingColor();

        // Convert Vector3f to ARGB integer
        int r = Math.min(255, Math.max(0, (int)(color.x * 255)));
        int g = Math.min(255, Math.max(0, (int)(color.y * 255)));
        int b = Math.min(255, Math.max(0, (int)(color.z * 255)));

        // Return as ARGB with full opacity
        return 0xFF000000 | (r << 16) | (g << 8) | b;
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

package net.alshanex.illusionist_grimoire.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.alshanex.illusionist_grimoire.data.IGClientSquishData;
import net.alshanex.illusionist_grimoire.data.SquishData;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to apply squish rendering to all living entities
 */
@Mixin(LivingEntityRenderer.class)
public abstract class SquishRenderMixin {

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V",
                    ordinal = 0))
    private void applySquishScale(LivingEntity entity, float entityYaw, float partialTicks,
                                  PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                  CallbackInfo ci) {
        // Only apply squish if entity has the effect
        if (!entity.hasEffect(IGEffectRegistry.SQUISH)) {
            return;
        }

        SquishData squishData = IGClientSquishData.getSquishData(entity);
        if (squishData == null || !squishData.isSquished()) {
            return;
        }

        // Get the scale for each axis
        float[] scales = squishData.getScales();

        // Apply the squish scale
        // scales[0] = X-axis, scales[1] = Y-axis, scales[2] = Z-axis
        poseStack.scale(scales[0], scales[1], scales[2]);
    }
}

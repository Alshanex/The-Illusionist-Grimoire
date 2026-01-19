package net.alshanex.illusionist_grimoire.mixin;

import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class SkipBlockRenderingMixin {
    @Inject(
            method = "renderSectionLayer",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onRenderSectionLayer(
            RenderType renderType,
            double x,
            double y,
            double z,
            Matrix4f frustumMatrix,
            Matrix4f projectionMatrix,
            CallbackInfo ci
    ) {
        // 1. Get the player
        var player = Minecraft.getInstance().player;

        if (player != null && player.hasEffect(IGEffectRegistry.EMPTINESS)) {
            // 3. Cancel the render call.
            ci.cancel();
        }
    }
}

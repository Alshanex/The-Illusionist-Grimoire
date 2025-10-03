package net.alshanex.illusionist_grimoire.mixin;

import net.alshanex.illusionist_grimoire.event.EntityRenderHandler;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class ShadowRendererMixin {
    @Inject(method = "getShadowRadius", at = @At("RETURN"), cancellable = true)
    private void hideShadow(Entity entity, CallbackInfoReturnable<Float> cir) {
        if (EntityRenderHandler.shouldHideEntity(entity)) {
            cir.setReturnValue(0.0F);
        }
    }
}

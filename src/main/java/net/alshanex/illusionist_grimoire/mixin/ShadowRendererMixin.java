package net.alshanex.illusionist_grimoire.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.alshanex.illusionist_grimoire.data.IGClientData;
import net.alshanex.illusionist_grimoire.event.EntityRenderHandler;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class ShadowRendererMixin {
    @Inject(method = "getShadowRadius", at = @At("RETURN"), cancellable = true)
    private void hideShadow(Entity entity, CallbackInfoReturnable<Float> cir) {
        if (EntityRenderHandler.shouldHideEntity(entity)) {
            cir.setReturnValue(0.0F);
        }
        if (entity instanceof Player player && player.hasEffect(IGEffectRegistry.DISGUISED)) {
            var disguiseData = IGClientData.getDisguiseData(player);

            if (!disguiseData.isDisguisedAsPlayer()) {
                cir.setReturnValue(0.0F);
            }
        }
    }
}

package net.alshanex.illusionist_grimoire.mixin;

import net.alshanex.illusionist_grimoire.data.IGClientData;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.GlowSquid;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class PlayerLightLevelMixin {

    @Inject(method = "getBlockLightLevel(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)I",
            at = @At("RETURN"),
            cancellable = true)
    private void modifyLightLevel(Entity entity, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        // Only apply to players
        if (entity instanceof AbstractClientPlayer player) {
            // Check if player is disguised as glow squid
            if (player.hasEffect(IGEffectRegistry.DISGUISED)) {
                var disguiseData = IGClientData.getDisguiseData(player);

                if (!disguiseData.isDisguisedAsPlayer()) {
                    LivingEntity mobEntity = disguiseData.getMobDisguiseEntity();

                    if (mobEntity instanceof GlowSquid glowSquid) {
                        // Calculate light level based on darkTicksRemaining (same as GlowSquidRenderer)
                        int glowLevel = (int)Mth.clampedLerp(0.0F, 15.0F,
                                1.0F - (float)glowSquid.getDarkTicksRemaining() / 10.0F);

                        if (glowLevel == 15) {
                            cir.setReturnValue(15);
                        } else {
                            // Return the maximum of the glow level or original light level
                            cir.setReturnValue(Math.max(glowLevel, cir.getReturnValueI()));
                        }
                    }
                }
            }
        }
    }
}

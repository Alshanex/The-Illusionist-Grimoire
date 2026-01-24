package net.alshanex.illusionist_grimoire.mixin;

import net.alshanex.illusionist_grimoire.data.IGClientSquishData;
import net.alshanex.illusionist_grimoire.data.SquishData;
import net.alshanex.illusionist_grimoire.entity.SpellTrapDummyEntity;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityEyeHeightMixin {

    @Inject(method = "getEyeHeight(Lnet/minecraft/world/entity/Pose;)F", at = @At("HEAD"), cancellable = true)
    private void overrideDummyEyeHeight(Pose pose, CallbackInfoReturnable<Float> cir) {
        Entity self = (Entity)(Object)this;
        if (self instanceof SpellTrapDummyEntity) {
            cir.setReturnValue(0.0F);
        }
    }

    @Inject(method = "getEyeHeight()F", at = @At("HEAD"), cancellable = true)
    private void overrideDummyEyeHeightNoPose(CallbackInfoReturnable<Float> cir) {
        Entity self = (Entity)(Object)this;
        // Handle SpellTrapDummyEntity
        if (self instanceof SpellTrapDummyEntity) {
            cir.setReturnValue(0.0F);
        }
    }

    @Inject(method = "getEyeHeight()F", at = @At("RETURN"), cancellable = true)
    private void modifySquishEyeHeightNoPose(CallbackInfoReturnable<Float> cir) {
        Entity self = (Entity)(Object)this;

        // Handle squished entities
        if (self instanceof LivingEntity livingEntity) {
            SquishData squishData = IGClientSquishData.getSquishData(livingEntity);
            if (squishData != null && squishData.isSquished()) {
                float[] scales = squishData.getScales();
                float originalEyeHeight = cir.getReturnValue();

                // Scale eye height based on Y-axis squish
                float newEyeHeight = originalEyeHeight * scales[1];
                cir.setReturnValue(Math.max(newEyeHeight, 0.2f));
            }
        }
    }
}

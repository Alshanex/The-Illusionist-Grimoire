package net.alshanex.illusionist_grimoire.mixin;

import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.alshanex.illusionist_grimoire.util.IGUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobDepthTrickMixin {

    @Shadow
    public abstract LivingEntity getTarget();

    @Inject(method = "isWithinMeleeAttackRange", at = @At("HEAD"), cancellable = true)
    private void modifyMeleeAttackRange(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        Mob self = (Mob)(Object)this;

        if (self.hasEffect(IGEffectRegistry.DEPTH_TRICK) && !IGUtils.canMobBypassIllusions(self)) {
            // Make the mob think the target is 3 blocks closer
            double actualDistanceSq = self.distanceToSqr(target);
            double perceivedDistance = Math.sqrt(actualDistanceSq) - 5.0;

            if (perceivedDistance <= 0) {
                // Mob thinks it's already in range
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "doHurtTarget", at = @At("HEAD"), cancellable = true)
    private void preventIllusoryDamage(Entity target, CallbackInfoReturnable<Boolean> cir) {
        Mob self = (Mob)(Object)this;

        if (self.hasEffect(IGEffectRegistry.DEPTH_TRICK) && !IGUtils.canMobBypassIllusions(self)) {
            if (target instanceof LivingEntity livingTarget) {
                AABB attackBox = ((MobAccessor)self).invokeGetAttackBoundingBox();
                boolean actuallyInRange = attackBox.intersects(livingTarget.getHitbox());

                if (!actuallyInRange) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}

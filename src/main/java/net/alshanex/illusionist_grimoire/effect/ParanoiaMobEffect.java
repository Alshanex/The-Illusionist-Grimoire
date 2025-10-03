package net.alshanex.illusionist_grimoire.effect;

import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class ParanoiaMobEffect extends MagicMobEffect {

    private static final double WANDER_SPEED = 1.0;
    private static final int RETARGET_INTERVAL = 40;

    public ParanoiaMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Mob mob) {
            // Clear current target
            mob.setTarget(null);

            // Stop any current navigation
            mob.getNavigation().stop();

            // Check if mob should flee from their last attacker
            LivingEntity oldTarget = mob.getLastHurtByMob();
            if (oldTarget != null && mob.getRandom().nextFloat() < 0.2f) {
                // Flee away from the last entity that hurt them
                Vec3 fleeDirection = mob.position().subtract(oldTarget.position()).normalize();
                Vec3 fleePos = mob.position().add(fleeDirection.scale(10));
                mob.getNavigation().moveTo(fleePos.x, fleePos.y, fleePos.z, WANDER_SPEED * 1.5);
            } else if (mob.getRandom().nextFloat() < 0.3f) {
                // Otherwise, wander to a random position (30% chance)
                Vec3 randomPos = getRandomPosition(mob, 8, 7);
                if (randomPos != null) {
                    mob.getNavigation().moveTo(randomPos.x, randomPos.y, randomPos.z, WANDER_SPEED * 1.5);
                }
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    private Vec3 getRandomPosition(Mob mob, int horizontalRange, int verticalRange) {
        Vec3 currentPos = mob.position();
        double x = currentPos.x + (mob.getRandom().nextDouble() - 0.5) * 2 * horizontalRange;
        double y = currentPos.y + (mob.getRandom().nextDouble() - 0.5) * 2 * verticalRange;
        double z = currentPos.z + (mob.getRandom().nextDouble() - 0.5) * 2 * horizontalRange;
        return new Vec3(x, y, z);
    }
}

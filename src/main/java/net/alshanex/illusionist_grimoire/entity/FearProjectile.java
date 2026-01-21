package net.alshanex.illusionist_grimoire.entity;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.alshanex.illusionist_grimoire.registry.IGEntityRegistry;
import net.alshanex.illusionist_grimoire.registry.IGSpellRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class FearProjectile extends AbstractMagicProjectile {
    public FearProjectile(EntityType<? extends FearProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public FearProjectile(EntityType<? extends FearProjectile> entityType, Level levelIn, LivingEntity shooter) {
        this(entityType, levelIn);
        setOwner(shooter);
    }

    public FearProjectile(Level levelIn, LivingEntity shooter) {
        this(IGEntityRegistry.FEAR_BOLT.get(), levelIn, shooter);
    }

    @Override
    public void impactParticles(double x, double y, double z) {

    }

    @Override
    public float getSpeed() {
        return 1.3f;
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.empty();
    }

    @Override
    protected void doImpactSound(Holder<SoundEvent> sound) {
        level().playSound(null, getX(), getY(), getZ(), sound, SoundSource.NEUTRAL, 2, 0.9f + Utils.random.nextFloat() * .4f);
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        discard();

    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);

        if (DamageSources.applyDamage(entityHitResult.getEntity(), damage, IGSpellRegistry.FEAR_BOLT_SPELL.get().getDamageSource(this, getOwner()))) {
            if (entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
                livingEntity.addEffect(new MobEffectInstance(IGEffectRegistry.FEARED, 6 * 20));
                livingEntity.addEffect(new MobEffectInstance(MobEffectRegistry.GUIDING_BOLT, 25 * 20));
            }
        }
        pierceOrDiscard();

    }

    @Override
    public void trailParticles() {
        Vec3 vec3 = getDeltaMovement();
        double d0 = this.getX() - vec3.x;
        double d1 = this.getY() - vec3.y;
        double d2 = this.getZ() - vec3.z;
        var count = Mth.clamp((int) (vec3.lengthSqr() * 2), 1, 4);
        for (int i = 0; i < count; i++) {
            Vec3 random = Utils.getRandomVec3(getBbHeight() * .2f);
            var f = i / ((float) count);
            var x = Mth.lerp(f, d0, this.getX() + vec3.x);
            var y = Mth.lerp(f, d1, this.getY() + vec3.y);
            var z = Mth.lerp(f, d2, this.getZ() + vec3.z);
            this.level().addParticle(ParticleTypes.CRIT, true,x - random.x, y + getBbHeight() * .5f - random.y, z - random.z, 0,0,0/*motion.x * .5f, motion.y * .5f, motion.z * .5f*/);
        }
    }
}

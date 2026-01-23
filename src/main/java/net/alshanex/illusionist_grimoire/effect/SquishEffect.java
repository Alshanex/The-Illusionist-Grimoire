package net.alshanex.illusionist_grimoire.effect;

import io.redspace.ironsspellbooks.effect.ISyncedMobEffect;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.alshanex.illusionist_grimoire.data.IGClientSquishData;
import net.alshanex.illusionist_grimoire.data.SquishData;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class SquishEffect extends MagicMobEffect implements ISyncedMobEffect {

    public SquishEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        super.onEffectStarted(entity, amplifier);
        SquishData.getSquishData(entity);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        SquishData squishData;
        if (entity.level().isClientSide()) {
            // Client side - use the synced data from packets
            squishData = IGClientSquishData.getSquishData(entity);
        } else {
            // Server side - use the attachment data
            squishData = SquishData.getSquishData(entity);
        }

        if (squishData != null && squishData.isSquished()) {
            // Refresh dimensions to apply squish to hitbox on both client and server
            entity.refreshDimensions();
        }
        return super.applyEffectTick(entity, amplifier);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // Apply every tick to keep dimensions updated
        return duration % 5 == 0;
    }

    @Override
    public void onEffectRemoved(LivingEntity pLivingEntity, int pAmplifier) {
        SquishData squishData;
        if (pLivingEntity.level().isClientSide()) {
            // Client side - use the synced data from packets
            squishData = IGClientSquishData.getSquishData(pLivingEntity);

            if (squishData != null && squishData.isSquished()) {
                // Refresh dimensions to apply squish to hitbox on both client and server
                squishData.setSquish(-1, 1.0f);
                pLivingEntity.refreshDimensions();
            }
        } else {
            // Server side - use the attachment data
            squishData = SquishData.getSquishData(pLivingEntity);

            if (squishData != null && squishData.isSquished()) {
                // Refresh dimensions to apply squish to hitbox on both client and server
                squishData.setSquish(-1, 1.0f);
                pLivingEntity.refreshDimensions();
            }
        }
        super.onEffectRemoved(pLivingEntity, pAmplifier);
        pLivingEntity.refreshDimensions();
    }
}

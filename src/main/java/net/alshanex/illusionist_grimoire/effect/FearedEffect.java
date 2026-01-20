package net.alshanex.illusionist_grimoire.effect;

import io.redspace.ironsspellbooks.effect.ISyncedMobEffect;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.alshanex.illusionist_grimoire.registry.IGSoundRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class FearedEffect extends MagicMobEffect implements ISyncedMobEffect {
    public FearedEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void onEffectAdded(LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity instanceof ServerPlayer player) {
            player.playNotifySound(
                    IGSoundRegistry.FLASHBANG.value(),
                    SoundSource.PLAYERS,
                    1.0f,
                    1.0f
            );
        }
        super.onEffectAdded(pLivingEntity, pAmplifier);
    }
}

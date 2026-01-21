package net.alshanex.illusionist_grimoire.effect;

import io.redspace.ironsspellbooks.effect.ISyncedMobEffect;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.alshanex.illusionist_grimoire.util.PlayerDisguiseProvider;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class DisguisedEffect extends MagicMobEffect implements ISyncedMobEffect {
    public DisguisedEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void onEffectRemoved(LivingEntity livingEntity, int amplifier) {
        // Clear disguise when effect ends
        if (livingEntity instanceof PlayerDisguiseProvider provider) {
            provider.illusionistGrimoire$setDisguiseEntity(null);
        }
        super.onEffectRemoved(livingEntity, amplifier);
    }
}

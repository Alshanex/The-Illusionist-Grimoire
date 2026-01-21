package net.alshanex.illusionist_grimoire.effect;

import io.redspace.ironsspellbooks.effect.ISyncedMobEffect;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class DisguisedEffect extends MagicMobEffect implements ISyncedMobEffect {
    public DisguisedEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }
}

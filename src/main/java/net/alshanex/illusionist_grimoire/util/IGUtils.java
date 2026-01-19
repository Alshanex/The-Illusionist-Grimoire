package net.alshanex.illusionist_grimoire.util;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.entity.LivingEntity;

public class IGUtils {
    public static boolean canMobBypassIllusions(LivingEntity entity) {
        boolean resistEvocation = entity.getAttributeValue(AttributeRegistry.EVOCATION_MAGIC_RESIST) > 1.2;
        return resistEvocation || entity.getType().is(ModTags.ILLUSION_IMMUNE_ENTITIES);
    }

    public static boolean canBypassIllusions(LivingEntity entity, double illusionistSpellPower) {
        double resist = (1 - entity.getAttributeValue(AttributeRegistry.EVOCATION_MAGIC_RESIST));
        return resist > (Math.max(0, (illusionistSpellPower - 1)) / 1.5);
    }
}

package net.alshanex.illusionist_grimoire.util;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.entity.LivingEntity;

public class IGUtils {
    public static boolean canMobBypassIllusions(LivingEntity entity) {
        return entity.getAttributeValue(AttributeRegistry.EVOCATION_MAGIC_RESIST) > 1.2 || entity.getType().is(ModTags.ILLUSION_IMMUNE_ENTITIES);
    }

    public static boolean canBypassIllusions(LivingEntity entity, double illusionistSpellPower) {
        return entity.getAttributeValue(AttributeRegistry.EVOCATION_MAGIC_RESIST) > 1 + (Math.max(0, (illusionistSpellPower - 1)) / 1.5);
    }
}

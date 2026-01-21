package net.alshanex.illusionist_grimoire.util;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public class IGUtils {
    public static boolean canMobBypassIllusions(LivingEntity entity) {
        boolean resistEvocation = entity.getAttributeValue(AttributeRegistry.SPELL_RESIST) > 1.2;
        return resistEvocation || entity.getType().is(ModTags.ILLUSION_IMMUNE_ENTITIES);
    }

    public static boolean canBypassIllusions(LivingEntity entity, double illusionistSpellPower) {
        double resist = (1 - entity.getAttributeValue(AttributeRegistry.SPELL_RESIST));
        return resist > (Math.max(0, (illusionistSpellPower - 1)) / 1.5);
    }

    public static void handlePlayerDisguisePacket(Player player, ResourceLocation entityTypeId, CompoundTag entityNbt){
        if (player instanceof PlayerDisguiseProvider provider) {
            // If entity type is "empty", clear disguise
            if (entityTypeId.equals(ResourceLocation.withDefaultNamespace("empty"))) {
                provider.illusionistGrimoire$setDisguiseEntity(null);
                return;
            }

            // Create or update disguise entity
            if (entityNbt != null && !entityNbt.isEmpty()) {
                entityNbt.putString("id", entityTypeId.toString());
                Optional<EntityType<?>> typeOpt = EntityType.by(entityNbt);

                if (typeOpt.isPresent()) {
                    LivingEntity currentDisguise = provider.illusionistGrimoire$getDisguiseEntity();

                    // Create new entity if type changed or doesn't exist
                    if (currentDisguise == null || !typeOpt.get().equals(currentDisguise.getType())) {
                        currentDisguise = (LivingEntity) typeOpt.get().create(player.level());
                        provider.illusionistGrimoire$setDisguiseEntity(currentDisguise);
                    }

                    // Load entity data
                    if (currentDisguise != null) {
                        currentDisguise.load(entityNbt);
                    }
                }
            }
        }
    }
}

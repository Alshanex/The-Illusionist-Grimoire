package net.alshanex.illusionist_grimoire.event;

import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.alshanex.illusionist_grimoire.entity.SpellTrapDummyEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import javax.annotation.Nullable;
import java.util.UUID;

@EventBusSubscriber(modid = "illusionist_grimoire")
public class SpellTrapOwnershipHandler {

    private static final String TRAP_OWNER_KEY = "TrapOwnerUUID";
    private static final String TRAP_OWNER_CHECKED_KEY = "TrapOwnerChecked";

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        Entity entity = event.getEntity();

        // Only process once per entity
        if (entity.getPersistentData().getBoolean(TRAP_OWNER_CHECKED_KEY)) {
            return;
        }
        entity.getPersistentData().putBoolean(TRAP_OWNER_CHECKED_KEY, true);

        // Check if this entity was spawned by a SpellTrapDummyEntity
        UUID trapOwnerUuid = checkAndPropagateTrapOwnership(entity, (ServerLevel) event.getLevel());

        if (trapOwnerUuid != null) {
            // Store trap owner in persistent data for alliance checking
            entity.getPersistentData().putUUID(TRAP_OWNER_KEY, trapOwnerUuid);
        }
    }

    /**
     * Checks if an entity was spawned by a SpellTrapDummyEntity and returns the trap owner's UUID
     */
    @Nullable
    private static UUID checkAndPropagateTrapOwnership(Entity entity, ServerLevel level) {
        // Case 1: Projectile entities (AbstractMagicProjectile, AoeEntity, etc.)
        if (entity instanceof Projectile projectile) {
            Entity owner = projectile.getOwner();
            if (owner instanceof SpellTrapDummyEntity dummy) {
                return dummy.getOwnerUuid();
            }
        }

        // Case 2: IMagicSummon entities (summoned creatures)
        if (entity instanceof IMagicSummon magicSummon) {
            Entity summoner = magicSummon.getSummoner();
            if (summoner instanceof SpellTrapDummyEntity dummy) {
                return dummy.getOwnerUuid();
            }
        }

        // Case 3: OwnableEntity (tameable mobs that might be spawned)
        if (entity instanceof OwnableEntity ownable) {
            UUID ownerUuid = ownable.getOwnerUUID();
            if (ownerUuid != null) {
                Entity owner = level.getEntity(ownerUuid);
                if (owner instanceof SpellTrapDummyEntity dummy) {
                    return dummy.getOwnerUuid();
                }
            }
        }

        // Case 4: Check if entity has owner stored in persistent data
        if (entity.getPersistentData().hasUUID("OwnerUUID")) {
            UUID ownerUuid = entity.getPersistentData().getUUID("OwnerUUID");
            Entity owner = level.getEntity(ownerUuid);
            if (owner instanceof SpellTrapDummyEntity dummy) {
                return dummy.getOwnerUuid();
            }
        }

        return null;
    }

    @Nullable
    public static UUID getTrapOwnerUuid(Entity entity) {
        if (entity.getPersistentData().hasUUID(TRAP_OWNER_KEY)) {
            return entity.getPersistentData().getUUID(TRAP_OWNER_KEY);
        }
        return null;
    }
}

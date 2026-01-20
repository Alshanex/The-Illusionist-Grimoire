package net.alshanex.illusionist_grimoire.mixin;

import net.alshanex.illusionist_grimoire.entity.SpellTrapDummyEntity;
import net.alshanex.illusionist_grimoire.event.SpellTrapOwnershipHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.UUID;

@Mixin(Projectile.class)
public abstract class ProjectileMixin {
    /**
     * Override getOwner() to return the trap owner instead of the dummy entity
     */
    @Inject(method = "getOwner", at = @At("RETURN"), cancellable = true)
    private void getTrapOwnerInstead(CallbackInfoReturnable<Entity> cir) {
        Entity originalOwner = cir.getReturnValue();

        // If the original owner is a SpellTrapDummyEntity, return the trap owner instead
        if (originalOwner instanceof SpellTrapDummyEntity dummy) {
            UUID trapOwnerUuid = dummy.getOwnerUuid();
            if (trapOwnerUuid != null) {
                Projectile self = (Projectile)(Object)this;
                if (self.level() instanceof ServerLevel serverLevel) {
                    Player trapOwner = serverLevel.getServer().getPlayerList().getPlayer(trapOwnerUuid);
                    if (trapOwner != null) {
                        cir.setReturnValue(trapOwner);
                        return;
                    }
                }
            }
        }

        // If original owner is null or removed, check if we have a stored trap owner
        if (originalOwner == null || originalOwner.isRemoved()) {
            Projectile self = (Projectile)(Object)this;
            UUID trapOwnerUuid = SpellTrapOwnershipHandler.getTrapOwnerUuid(self);

            if (trapOwnerUuid != null && self.level() instanceof ServerLevel serverLevel) {
                Player trapOwner = serverLevel.getServer().getPlayerList().getPlayer(trapOwnerUuid);
                if (trapOwner != null) {
                    cir.setReturnValue(trapOwner);
                    return;
                }
            }
        }
    }

    @Inject(method = "canHitEntity", at = @At("HEAD"), cancellable = true)
    private void preventHittingTrapOwner(Entity target, CallbackInfoReturnable<Boolean> cir) {
        Projectile self = (Projectile)(Object)this;

        // Check if this projectile has a trap owner
        UUID trapOwnerUuid = SpellTrapOwnershipHandler.getTrapOwnerUuid(self);
        if (trapOwnerUuid != null) {
            // Don't hit the trap owner
            if (target.getUUID().equals(trapOwnerUuid)) {
                cir.setReturnValue(false);
                return;
            }

            // Don't hit entities allied to the trap owner
            if (self.level() instanceof ServerLevel serverLevel) {
                Player owner = serverLevel.getServer().getPlayerList().getPlayer(trapOwnerUuid);
                if (owner != null) {
                    if (target.isAlliedTo((Entity)owner) || owner.isAlliedTo((Entity)target)) {
                        cir.setReturnValue(false);
                        return;
                    }

                    // Don't hit the owner's pets
                    if (target instanceof net.minecraft.world.entity.OwnableEntity ownable) {
                        if (trapOwnerUuid.equals(ownable.getOwnerUUID())) {
                            cir.setReturnValue(false);
                            return;
                        }
                    }
                }
            }
        }
    }
}

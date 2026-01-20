package net.alshanex.illusionist_grimoire.mixin;

import net.alshanex.illusionist_grimoire.event.SpellTrapOwnershipHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityAllianceMixin {

    @Inject(
            method = "isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void checkTrapOwnerAlliance(Entity other, CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity)(Object)this;

        // Check if this entity has a trap owner
        UUID trapOwnerUuid = SpellTrapOwnershipHandler.getTrapOwnerUuid(self);
        if (trapOwnerUuid != null) {
            // Check if the other entity is the trap owner
            if (other.getUUID().equals(trapOwnerUuid)) {
                cir.setReturnValue(true);
                return;
            }

            // Check if the other entity is allied to the trap owner
            if (self.level() instanceof ServerLevel serverLevel) {
                Player owner = serverLevel.getServer().getPlayerList().getPlayer(trapOwnerUuid);
                if (owner != null) {
                    if (owner.isAlliedTo(other) || other.isAlliedTo(owner)) {
                        cir.setReturnValue(true);
                        return;
                    }
                }
            }
        }

        // Check reverse - if the other entity has a trap owner that is allied to self
        UUID otherTrapOwnerUuid = SpellTrapOwnershipHandler.getTrapOwnerUuid(other);
        if (otherTrapOwnerUuid != null) {
            if (otherTrapOwnerUuid.equals(self.getUUID())) {
                cir.setReturnValue(true);
                return;
            }
        }
    }
}

package net.alshanex.illusionist_grimoire.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.alshanex.illusionist_grimoire.data.IGClientData;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class AquaticMovementMixin {

    @Unique
    private boolean illusionist_grimoire$isAquatic(LivingEntity disguise) {
        if (disguise == null) return false;

        MobCategory category = disguise.getType().getCategory();

        // Check if entity is aquatic type
        return category == MobCategory.WATER_CREATURE ||
                category == MobCategory.WATER_AMBIENT ||
                category == MobCategory.UNDERGROUND_WATER_CREATURE;
    }

    @Inject(method = "travel", at = @At("HEAD"))
    private void onTravel(Vec3 movementInput, CallbackInfo ci) {
        // Cast this to check if it's a player
        if ((Object) this instanceof Player player) {

            // Check if player is disguised
            if (!player.hasEffect(IGEffectRegistry.DISGUISED)) {
                return;
            }

            var disguiseData = IGClientData.getDisguiseData(player);
            if (disguiseData.isDisguisedAsPlayer()) {
                return; // Skip for player disguises
            }

            LivingEntity disguise = disguiseData.getMobDisguiseEntity();

            // Only apply to aquatic mobs
            if (disguise != null && illusionist_grimoire$isAquatic(disguise)) {
                boolean inWater = player.isInWater();
                boolean inBubbleColumn = player.level().getBlockState(player.blockPosition()).is(Blocks.BUBBLE_COLUMN);

                // Apply 6-directional movement when in water
                if (inWater || inBubbleColumn) {
                    // Different speeds for different aquatic mobs
                    double speedMultiplier = disguise.getType() == EntityType.DOLPHIN ? 0.4 : 0.25;

                    Vec3 input = movementInput;

                    // Get Minecraft client for input checking
                    Minecraft mc = Minecraft.getInstance();

                    // Jump key = swim down (negative Y)
                    if (mc.options.keyJump.isDown()) {
                        input = input.add(0, -1.0, 0);
                    }

                    // Sneak key = swim up (positive Y)
                    if (mc.options.keyShift.isDown()) {
                        input = input.add(0, 1.0, 0);
                    }

                    // Convert local space movement to world space based on look direction
                    Vec3 lookVec = player.getViewVector(1.0F);
                    Vec3 upVec = new Vec3(0, 1, 0);
                    Vec3 rightVec = upVec.cross(lookVec).normalize();
                    Vec3 adjustedUpVec = rightVec.cross(lookVec).normalize();

                    // Calculate world-space input vector
                    Vec3 worldInput = rightVec.scale(input.x)
                            .add(adjustedUpVec.scale(input.y))
                            .add(lookVec.scale(input.z));

                    // Apply velocity if there's input
                    if (worldInput.lengthSqr() > 0.0001) {
                        player.setDeltaMovement(worldInput.normalize().scale(speedMultiplier));
                    }
                }
            }
        }
    }
}

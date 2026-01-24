package net.alshanex.illusionist_grimoire.event;

import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.data.SquishData;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = IllusionistGrimoireMod.MODID)
public class SquishEventHandler {
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();

        // Only process if entity has the squish effect
        if (!entity.hasEffect(IGEffectRegistry.SQUISH)) {
            return;
        }

        SquishData squishData = SquishData.getSquishData(entity);
        if (squishData == null) {
            return;
        }

        var damageSource = event.getSource();

        // Falling blocks (anvils, sand, gravel, etc.)
        if (damageSource.is(DamageTypes.FALLING_BLOCK) || damageSource.is(DamageTypes.FALLING_ANVIL)) {
            handleFallingBlockSquish(entity, squishData, event.getOriginalDamage());
        }
        // Flying into walls at high speed (elytra crashes)
        else if (damageSource.is(DamageTypes.FLY_INTO_WALL)) {
            handleWallSplatSquish(entity, squishData, event.getOriginalDamage());
        }
        else if (damageSource.is(DamageTypes.FALL)) {
            handleFallDamageSquish(entity, squishData, event.getOriginalDamage());
        } else {
            handleDirectionalDamageSquish(entity, squishData, damageSource, event.getOriginalDamage());
        }
    }

    private static void handleFallingBlockSquish(LivingEntity entity, SquishData squishData, float damage) {
        // Anvils and heavy blocks deal more damage, so they squish more
        if (damage >= 10.0f) {
            // Heavy block (likely anvil) - total squash!
            squishData.applyTotalSquash();
        } else if (damage >= 4.0f) {
            // Medium weight block - severe squash
            squishData.applySquish(Direction.Axis.Y, 0.25f);
        } else {
            // Light block - moderate squash
            squishData.applySquish(Direction.Axis.Y, 0.4f);
        }
        entity.refreshDimensions();
    }

    private static void handleWallSplatSquish(LivingEntity entity, SquishData squishData, float damage) {
        Vec3 motion = entity.getDeltaMovement();

        // Determine which axis had the most velocity (that's the splat direction)
        double absX = Math.abs(motion.x);
        double absY = Math.abs(motion.y);
        double absZ = Math.abs(motion.z);

        Direction.Axis splatAxis;
        if (absX > absY && absX > absZ) {
            splatAxis = Direction.Axis.X;
        } else if (absZ > absY && absZ > absX) {
            splatAxis = Direction.Axis.Z;
        } else {
            splatAxis = Direction.Axis.Y;
        }

        // More damage = faster impact = more squish
        float squishAmount;
        if (damage >= 8.0f) {
            squishAmount = 0.2f; // Extreme splat!
        } else if (damage >= 4.0f) {
            squishAmount = 0.35f; // Heavy splat
        } else {
            squishAmount = 0.5f; // Moderate splat
        }

        squishData.applySquish(splatAxis, squishAmount);
        entity.refreshDimensions();
    }

    private static void handleFallDamageSquish(LivingEntity entity, SquishData squishData, float damage) {
        // Squish into the ground (Y-axis)
        // Fall damage increases with height, so higher falls = more squish
        if (damage >= 15.0f) {
            // Extreme fall (20+ blocks) - pancake!
            squishData.applyTotalSquash();
        } else if (damage >= 10.0f) {
            // High fall (15-20 blocks) - severe squish
            squishData.applySquish(Direction.Axis.Y, 0.2f);
        } else if (damage >= 5.0f) {
            // Medium fall (10-15 blocks) - moderate squish
            squishData.applySquish(Direction.Axis.Y, 0.35f);
        } else {
            // Small fall (5-10 blocks) - light squish
            squishData.applySquish(Direction.Axis.Y, 0.5f);
        }
        entity.refreshDimensions();
    }

    private static void handleDirectionalDamageSquish(LivingEntity entity, SquishData squishData,
                                                      net.minecraft.world.damagesource.DamageSource damageSource,
                                                      float damage) {
        // Try to get the position where the damage came from
        Vec3 sourcePos = damageSource.getSourcePosition();

        if (sourcePos == null) {
            // If no source position, check if there's a direct entity (like a projectile or attacker)
            Entity directEntity = damageSource.getDirectEntity();
            if (directEntity != null) {
                sourcePos = directEntity.position();
            }
        }

        if (sourcePos != null) {
            // Calculate direction vector from damage source to entity
            Vec3 entityPos = entity.position();
            Vec3 direction = entityPos.subtract(sourcePos);

            // Find which axis had the most impact (largest absolute difference)
            double absX = Math.abs(direction.x);
            double absY = Math.abs(direction.y);
            double absZ = Math.abs(direction.z);

            Direction.Axis squishAxis;
            if (absX > absY && absX > absZ) {
                squishAxis = Direction.Axis.X;
            } else if (absY > absX && absY > absZ) {
                squishAxis = Direction.Axis.Y;
            } else {
                squishAxis = Direction.Axis.Z;
            }

            // Squish amount based on damage (more damage = more squish)
            float squishAmount;
            if (damage >= (entity.getMaxHealth() * 0.66f)) {
                squishAmount = 0.25f; // Heavy hit
            } else if (damage >= (entity.getMaxHealth() * 0.33f)) {
                squishAmount = 0.4f; // Medium hit
            } else {
                squishAmount = 0.6f; // Light hit
            }

            squishData.applySquish(squishAxis, squishAmount);
        }

        entity.refreshDimensions();
    }
}

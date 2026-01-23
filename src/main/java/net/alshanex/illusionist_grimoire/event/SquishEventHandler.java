package net.alshanex.illusionist_grimoire.event;

import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.data.SquishData;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = IllusionistGrimoireMod.MODID)
public class SquishEventHandler {

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();

        if(entity instanceof LivingEntity livingEntity){
            // Only process if entity has the squish effect
            if (!livingEntity.hasEffect(IGEffectRegistry.SQUISH)) {
                return;
            }

            // Check if entity is being pushed by a piston
            checkPistonSquish(livingEntity);
        }
    }

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
        if (damageSource.is(DamageTypes.FALLING_BLOCK)) {
            handleFallingBlockSquish(entity, squishData, event.getOriginalDamage());
        }
        // Flying into walls at high speed (elytra crashes) - very Looney Tunes!
        else if (damageSource.is(DamageTypes.FLY_INTO_WALL)) {
            handleWallSplatSquish(entity, squishData, event.getOriginalDamage());
        }
        // Being crushed/crammed by too many entities
        else if (damageSource.is(DamageTypes.CRAMMING)) {
            handleCrammingSquish(entity, squishData);
        }
        else if (damageSource.is(DamageTypes.FALL)) {
            handleFallDamageSquish(entity, squishData, event.getOriginalDamage());
        }
    }

    private static void checkPistonSquish(LivingEntity entity) {
        BlockPos entityPos = entity.blockPosition();

        // Check surrounding blocks for moving pistons
        for (Direction direction : Direction.values()) {
            BlockPos checkPos = entityPos.relative(direction);

            if (entity.level().getBlockEntity(checkPos) instanceof PistonMovingBlockEntity pistonMoving) {
                // Get the direction the piston is pushing
                Direction pistonDirection = pistonMoving.getDirection();

                // Check if piston is extending (pushing)
                if (pistonMoving.isExtending()) {
                    // Apply squish in the direction of the push
                    SquishData squishData = SquishData.getSquishData(entity);
                    if (squishData != null) {
                        // Reduce thickness by 50% (0.5)
                        squishData.applySquish(pistonDirection.getAxis(), 0.5f);
                    }
                }
            }
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
    }


    private static void handleCrammingSquish(LivingEntity entity, SquishData squishData) {
        Direction.Axis axis = entity.getRandom().nextBoolean() ? Direction.Axis.X : Direction.Axis.Z;
        squishData.applySquish(axis, 0.5f);
    }

    private static boolean isBlockSolid(LivingEntity entity, BlockPos pos) {
        BlockState state = entity.level().getBlockState(pos);
        return !state.isAir() && state.canOcclude();
    }
}

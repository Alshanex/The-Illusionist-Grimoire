package net.alshanex.illusionist_grimoire.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.alshanex.illusionist_grimoire.data.IGClientData;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.alshanex.illusionist_grimoire.util.GuardianAnimationTracker;
import net.alshanex.illusionist_grimoire.util.SquidAnimationTracker;
import net.alshanex.illusionist_grimoire.util.WitherHeadRotation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private PlayerRendererMixin() {
        super(null, null, 0);
    }

    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onRenderPlayer(AbstractClientPlayer player, float yaw, float partialTicks,
                                PoseStack poseStack, MultiBufferSource buffer, int light,
                                CallbackInfo ci) {
        if (!player.hasEffect(IGEffectRegistry.DISGUISED)) {
            return;
        }

        var disguiseData = IGClientData.getDisguiseData(player);

        // Let player disguise logic handle player-to-player disguises
        if (disguiseData.isDisguisedAsPlayer()) {
            return;
        }

        // Handle mob disguises
        LivingEntity mobEntity = disguiseData.getMobDisguiseEntity();
        if (mobEntity != null) {
            Minecraft mc = Minecraft.getInstance();
            if (player == mc.player && mc.options.getCameraType().isFirstPerson()) {
                ci.cancel(); // Don't render anything in first person
                return;
            }

            if (player.isCrouching()) {
                poseStack.translate(0.0, 0.125, 0.0);
            }

            // Sync player state to mob entity
            syncPlayerStateToMob(player, mobEntity, partialTicks);

            // Apply entity-specific updates
            applyEntitySpecificUpdates(player, mobEntity);

            // Render the mob entity instead of the player
            @SuppressWarnings("unchecked")
            EntityRenderer<? super LivingEntity> renderer =
                    (EntityRenderer<? super LivingEntity>) Minecraft.getInstance()
                            .getEntityRenderDispatcher().getRenderer(mobEntity);

            // Apply biped arm poses if applicable
            if (renderer instanceof LivingEntityRenderer<?, ?> livingRenderer) {
                setBipedArmPoses(player, mobEntity, livingRenderer);
            }

            renderer.render(mobEntity, yaw, partialTicks, poseStack, buffer, light);

            ci.cancel(); // Cancel vanilla player rendering
        }
    }

    private void syncPlayerStateToMob(AbstractClientPlayer player, LivingEntity mobEntity, float partialTicks) {
        // Sync limb animation
        WalkAnimationStateAccessor targetWalk = (WalkAnimationStateAccessor) (Object) mobEntity.walkAnimation;
        WalkAnimationStateAccessor sourceWalk = (WalkAnimationStateAccessor) (Object) player.walkAnimation;

        targetWalk.setPosition(sourceWalk.getPosition());
        targetWalk.setSpeedOld(sourceWalk.getSpeedOld());
        mobEntity.walkAnimation.setSpeed(player.walkAnimation.speed());

        // Sync hand swinging
        mobEntity.swinging = player.swinging;
        mobEntity.swingTime = player.swingTime;
        mobEntity.oAttackAnim = player.oAttackAnim;
        mobEntity.attackAnim = player.attackAnim;

        // Sync rotation
        mobEntity.yBodyRot = player.yBodyRot;
        mobEntity.yBodyRotO = player.yBodyRotO;
        mobEntity.yHeadRot = player.yHeadRot;
        mobEntity.yHeadRotO = player.yHeadRotO;
        mobEntity.xRotO = player.xRotO;
        mobEntity.setXRot(player.getXRot());

        // Special handling for Phantom (inverted pitch)
        if (mobEntity instanceof Phantom) {
            mobEntity.setXRot(-player.getXRot());
            mobEntity.xRotO = -player.xRotO;
        }

        // Sync state flags
        mobEntity.setOnGround(player.onGround());
        mobEntity.setDeltaMovement(player.getDeltaMovement());
        mobEntity.setSprinting(player.isSprinting());
        mobEntity.setShiftKeyDown(player.isShiftKeyDown());

        // Swimming
        ((EntityAccessor) mobEntity).setTouchingWater(player.isInWater());

        mobEntity.setPose(player.getPose());
        mobEntity.tickCount = player.tickCount;

        // Sync using item state
        if (player.isUsingItem()) {
            mobEntity.startUsingItem(player.getUsedItemHand());
            mobEntity.useItemRemaining = player.getUseItemRemainingTicks();
        } else {
            mobEntity.stopUsingItem();
        }

        // Sync equipment
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            mobEntity.setItemSlot(slot, player.getItemBySlot(slot).copy());
        }

        // Sync attacking state for mobs
        if (mobEntity instanceof Mob mob) {
            mob.setAggressive(player.isUsingItem());
        }
    }

    private void applyEntitySpecificUpdates(AbstractClientPlayer player, LivingEntity mobEntity) {
        // === BAT ===
        if (mobEntity instanceof Bat bat) {
            bat.flyAnimationState.startIfStopped(bat.tickCount);
        }

        // === PARROT ===
        if (mobEntity instanceof Parrot parrot) {
            ParrotAccessor parrotAccessor = (ParrotAccessor) parrot;

            // Replicate calculateFlapping() from Parrot class
            parrot.oFlap = parrot.flap;
            parrot.oFlapSpeed = parrot.flapSpeed;

            // Calculate flapSpeed: +4*0.3 when flying, -1*0.3 when on ground
            float flapIncrement = (!player.onGround() && !parrot.isPassenger()) ? 4 : -1;
            parrot.flapSpeed += flapIncrement * 0.3F;
            parrot.flapSpeed = Mth.clamp(parrot.flapSpeed, 0.0F, 1.0F);

            // Update flapping state
            float flapping = parrotAccessor.getFlapping();
            if (!player.onGround() && flapping < 1.0F) {
                flapping = 1.0F;
            }
            flapping *= 0.9F;
            parrotAccessor.setFlapping(flapping);

            // Update flap animation
            parrot.flap += flapping * 2.0F;
        }

        // === SQUID ===
        if (mobEntity instanceof Squid squid) {
            SquidAccessor squidAccessor = (SquidAccessor) squid;
            SquidAnimationTracker tickTracker = (SquidAnimationTracker) squid;

            // Only update animation once per game tick, not every render frame
            int currentTick = player.tickCount;
            boolean shouldUpdate = tickTracker.illusionist_grimoire$getLastAnimationTick() != currentTick;

            if (shouldUpdate) {
                tickTracker.illusionist_grimoire$setLastAnimationTick(currentTick);

                // Save old values for interpolation
                squidAccessor.setXBodyRotO(squidAccessor.getXBodyRot());
                squidAccessor.setZBodyRotO(squidAccessor.getZBodyRot());
                squidAccessor.setOldTentacleMovement(squidAccessor.getTentacleMovement());
                squidAccessor.setOldTentacleAngle(squidAccessor.getTentacleAngle());

                // Update tentacle movement cycle
                float tentacleMovement = squidAccessor.getTentacleMovement();
                float tentacleSpeed = squidAccessor.getTentacleSpeed();
                tentacleMovement += tentacleSpeed;

                if (tentacleMovement > (Math.PI * 2)) {
                    tentacleMovement -= (float)(Math.PI * 2);
                }
                squidAccessor.setTentacleMovement(tentacleMovement);

                // Different animations for in-water vs out-of-water
                if (player.isInWater()) {
                    // Swimming animation - tentacles
                    if (tentacleMovement < (float)Math.PI) {
                        float phase = tentacleMovement / (float)Math.PI;
                        float tentacleAngle = Mth.sin(phase * phase * (float)Math.PI) * (float)Math.PI * 0.25F;
                        squidAccessor.setTentacleAngle(tentacleAngle);
                    } else {
                        squidAccessor.setTentacleAngle(0.0F);
                    }

                    // Rotate squid body - with damping for smooth turning
                    Vec3 velocity = player.getDeltaMovement();
                    double horizontalDist = velocity.horizontalDistance();

                    // Only update rotation if moving with significant velocity (reduces jitter)
                    if (horizontalDist > 0.003) { // Minimum velocity threshold
                        // Calculate target yaw with smoothing
                        float targetYaw = -((float)Mth.atan2(velocity.x, velocity.z)) * (180F / (float)Math.PI);
                        float currentYaw = squid.yBodyRot;
                        float deltaYaw = Mth.wrapDegrees(targetYaw - currentYaw);

                        // Apply smoother rotation with smaller multiplier to reduce vibration
                        squid.yBodyRot += deltaYaw * 0.05F; // Reduced from 0.1F for smoother turning
                        squid.setYRot(squid.yBodyRot);

                        // Update xBodyRot based on vertical movement
                        float targetXRot = -((float)Mth.atan2(horizontalDist, velocity.y)) * (180F / (float)Math.PI);
                        float currentXRot = squidAccessor.getXBodyRot();
                        float deltaXRot = Mth.wrapDegrees(targetXRot - currentXRot);

                        float newXBodyRot = currentXRot + deltaXRot * 0.05F; // Reduced from 0.1F
                        squidAccessor.setXBodyRot(newXBodyRot);
                    }

                    // Update zBodyRot for body spinning (independent of movement)
                    float zBodyRot = squidAccessor.getZBodyRot();
                    zBodyRot += (float)Math.PI * 0.015F;
                    squidAccessor.setZBodyRot(zBodyRot);

                } else {
                    // Out of water - flailing animation
                    float tentacleAngle = Mth.abs(Mth.sin(tentacleMovement)) * (float)Math.PI * 0.25F;
                    squidAccessor.setTentacleAngle(tentacleAngle);

                    float xBodyRot = squidAccessor.getXBodyRot();
                    xBodyRot += (-90.0F - xBodyRot) * 0.02F;
                    squidAccessor.setXBodyRot(xBodyRot);
                }
            }
        }

        // === ENDER DRAGON ===
        if (mobEntity instanceof EnderDragon dragon) {
            // Wing animation
            dragon.oFlapTime = dragon.flapTime;
            dragon.flapTime += 0.01F;

            float targetYaw = player.getYRot() + 180.0F;

            // Smooth rotation interpolation
            float currentYaw = dragon.getYRot();
            float deltaYaw = Mth.wrapDegrees(targetYaw - currentYaw);

            // Apply smooth rotation
            dragon.setYRot(currentYaw + deltaYaw * 0.2F);
            dragon.yBodyRot = dragon.getYRot();
            dragon.yBodyRotO = dragon.yBodyRot;

            // Initialize positions array on first use
            if (dragon.posPointer < 0) {
                for (int i = 0; i < dragon.positions.length; i++) {
                    dragon.positions[i][0] = (double) targetYaw;
                    dragon.positions[i][1] = player.getY();
                    dragon.positions[i][2] = 0.0;
                }
                dragon.posPointer = 0;
            }

            // Update tail segment positions smoothly every frame
            if (++dragon.posPointer >= dragon.positions.length) {
                dragon.posPointer = 0;
            }

            // Store current position with smoothed rotation
            dragon.positions[dragon.posPointer][0] = (double) dragon.getYRot();
            dragon.positions[dragon.posPointer][1] = player.getY();
        }

        // === WITHER ===
        if (mobEntity instanceof WitherBoss wither) {
            WitherHeadRotation witherRotation = (WitherHeadRotation) wither;

            // Update previous rotations for interpolation
            witherRotation.illusionist_grimoire$setYRotOHeads(0, witherRotation.illusionist_grimoire$getYRotHeads(0));
            witherRotation.illusionist_grimoire$setYRotOHeads(1, witherRotation.illusionist_grimoire$getYRotHeads(1));
            witherRotation.illusionist_grimoire$setXRotOHeads(0, witherRotation.illusionist_grimoire$getXRotHeads(0));
            witherRotation.illusionist_grimoire$setXRotOHeads(1, witherRotation.illusionist_grimoire$getXRotHeads(1));

            // When no targets, side heads should look at body rotation
            float targetYaw = wither.yBodyRot;

            for (int i = 0; i < 2; i++) {
                float currentYaw = witherRotation.illusionist_grimoire$getYRotHeads(i);
                float deltaYaw = Mth.wrapDegrees(targetYaw - currentYaw);

                float newYaw = currentYaw + deltaYaw * 0.1F;
                witherRotation.illusionist_grimoire$setYRotHeads(i, newYaw);

                // Keep heads level (no vertical rotation when no target)
                witherRotation.illusionist_grimoire$setXRotHeads(i, 0.0F);
            }
        }

        // === ENDERMAN ===
        if (mobEntity instanceof EnderMan enderman) {
            // Show carried block if player holds a BlockItem
            if (player.getMainHandItem().getItem() instanceof BlockItem blockItem) {
                enderman.setCarriedBlock(blockItem.getBlock().defaultBlockState());
            } else {
                enderman.setCarriedBlock(null);
            }
        }

        // === GUARDIAN / ELDER GUARDIAN ===
        if (mobEntity instanceof Guardian guardian) {
            GuardianAccessor guardianAccessor = (GuardianAccessor) guardian;
            GuardianAnimationTracker tickTracker = (GuardianAnimationTracker) guardian;

            // Only update animation once per game tick, not every render frame
            int currentTick = player.tickCount;
            boolean shouldUpdate = tickTracker.illusionist_grimoire$getLastAnimationTick() != currentTick;

            if (shouldUpdate) {
                tickTracker.illusionist_grimoire$setLastAnimationTick(currentTick);

                // Update previous animation values for interpolation
                guardianAccessor.setClientSideTailAnimationO(guardianAccessor.getClientSideTailAnimation());
                guardianAccessor.setClientSideSpikesAnimationO(guardianAccessor.getClientSideSpikesAnimation());

                // Set moving state based on player's velocity
                Vec3 velocity = player.getDeltaMovement();
                double horizontalDist = velocity.horizontalDistance();
                boolean isMoving = horizontalDist > 0.003; // Same threshold as squid
                guardianAccessor.invokeSetMoving(isMoving);

                // Update tail animation
                if (player.isInWater()) {
                    if (isMoving) {
                        float tailSpeed = guardianAccessor.getClientSideTailAnimationSpeed();
                        if (tailSpeed < 0.5F) {
                            guardianAccessor.setClientSideTailAnimationSpeed(4.0F);
                        } else {
                            guardianAccessor.setClientSideTailAnimationSpeed(tailSpeed + (0.5F - tailSpeed) * 0.1F);
                        }
                    } else {
                        float tailSpeed = guardianAccessor.getClientSideTailAnimationSpeed();
                        guardianAccessor.setClientSideTailAnimationSpeed(tailSpeed + (0.125F - tailSpeed) * 0.2F);
                    }
                } else {
                    // Out of water
                    guardianAccessor.setClientSideTailAnimationSpeed(2.0F);
                }

                float newTailAnimation = guardianAccessor.getClientSideTailAnimation() +
                        guardianAccessor.getClientSideTailAnimationSpeed();
                guardianAccessor.setClientSideTailAnimation(newTailAnimation);

                // Update spikes animation (the quills!)
                if (!player.isInWaterOrBubble()) {
                    guardianAccessor.setClientSideSpikesAnimation(player.getRandom().nextFloat());
                } else if (isMoving) {
                    float spikes = guardianAccessor.getClientSideSpikesAnimation();
                    guardianAccessor.setClientSideSpikesAnimation(spikes + (0.0F - spikes) * 0.25F);
                } else {
                    float spikes = guardianAccessor.getClientSideSpikesAnimation();
                    guardianAccessor.setClientSideSpikesAnimation(spikes + (1.0F - spikes) * 0.06F);
                }
            }
        }

        // === RABBIT ===
        if (mobEntity instanceof Rabbit rabbit) {
            RabbitAccessor rabbitAccessor = (RabbitAccessor) rabbit;

            // Detect jump start
            boolean wasOnGround = rabbitAccessor.getWasOnGround();
            boolean isOnGround = player.onGround();

            if (!isOnGround && wasOnGround) {
                // Player just jumped - start jump animation
                rabbitAccessor.setJumpDuration(10);
                rabbitAccessor.setJumpTicks(0);
                rabbit.setJumping(true);
            }

            // Update jump animation
            int jumpTicks = rabbitAccessor.getJumpTicks();
            int jumpDuration = rabbitAccessor.getJumpDuration();

            if (jumpTicks != jumpDuration) {
                rabbitAccessor.setJumpTicks(jumpTicks + 1);
            } else if (jumpDuration != 0) {
                rabbitAccessor.setJumpTicks(0);
                rabbitAccessor.setJumpDuration(0);
                rabbit.setJumping(false);
            }

            // Detect landing
            if (isOnGround && !wasOnGround) {
                rabbit.setJumping(false);
            }

            // Update wasOnGround for next frame
            rabbitAccessor.setWasOnGround(isOnGround);
        }
    }

    private void setBipedArmPoses(AbstractClientPlayer player, LivingEntity mobEntity,
                                  LivingEntityRenderer<?, ?> renderer) {
        if (!(renderer.getModel() instanceof HumanoidModel<?> bipedModel)) {
            return;
        }

        if (mobEntity.isSpectator()) {
            bipedModel.setAllVisible(false);
            bipedModel.head.visible = true;
            bipedModel.hat.visible = true;
        } else {
            bipedModel.setAllVisible(true);
            //bipedModel.crouching = mobEntity.isShiftKeyDown();

            // Set arm poses based on player's hand usage
            HumanoidModel.ArmPose mainHandPose = HumanoidModel.ArmPose.EMPTY;
            HumanoidModel.ArmPose offHandPose = HumanoidModel.ArmPose.EMPTY;

            if (player.isUsingItem()) {
                mainHandPose = HumanoidModel.ArmPose.ITEM;
                if (!player.getOffhandItem().isEmpty()) {
                    offHandPose = HumanoidModel.ArmPose.ITEM;
                }
            }

            bipedModel.rightArmPose = mainHandPose;
            bipedModel.leftArmPose = offHandPose;
        }
    }
}

package net.alshanex.illusionist_grimoire.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.alshanex.illusionist_grimoire.data.IGClientData;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Phantom;
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

            // Render nametag if enabled
            if (player != Minecraft.getInstance().player) {
                this.renderNameTag(player, player.getDisplayName(), poseStack, buffer, light, partialTicks);
            }

            ci.cancel(); // Cancel vanilla player rendering
        }
    }

    private void syncPlayerStateToMob(AbstractClientPlayer player, LivingEntity mobEntity, float partialTicks) {
        // Sync limb animation
        mobEntity.walkAnimation.setSpeed(player.walkAnimation.speed());
        mobEntity.walkAnimation.speedOld = player.walkAnimation.speedOld;
        mobEntity.walkAnimation.position(partialTicks);

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
        mobEntity.setSwimming(player.isSwimming());
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
        // Bat: set roosting based on ground state
        if (mobEntity instanceof Bat bat) {
            bat.setResting(player.onGround());
        }

        // Ender Dragon: update wing animation
        if (mobEntity instanceof EnderDragon dragon) {
            // These fields may need accessors depending on version
            // dragon.flapTime += 0.01F;
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
            bipedModel.crouching = mobEntity.isShiftKeyDown();

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

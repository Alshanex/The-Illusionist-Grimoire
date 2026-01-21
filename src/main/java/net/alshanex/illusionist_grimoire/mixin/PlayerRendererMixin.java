package net.alshanex.illusionist_grimoire.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.alshanex.illusionist_grimoire.data.DisguiseData;
import net.alshanex.illusionist_grimoire.data.IGClientData;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.alshanex.illusionist_grimoire.util.PlayerDisguiseProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel<AbstractClientPlayer> model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Inject(
            method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void illusionistGrimoire$renderDisguise(AbstractClientPlayer player, float yaw, float partialTick,
                                                    PoseStack poseStack, MultiBufferSource bufferSource, int light, CallbackInfo ci) {

        // Check if player has disguise effect
        if (!player.hasEffect(IGEffectRegistry.DISGUISED)) {
            return;
        }

        DisguiseData disguiseData = IGClientData.getDisguiseData(player);
        if (disguiseData.isDisguisedAsPlayer()) {
            // Player disguises are handled by AbstractClientPlayerMixin (skin system)
            return;
        }

        // Get disguise entity from player
        LivingEntity disguiseEntity = ((PlayerDisguiseProvider) player).illusionistGrimoire$getDisguiseEntity();

        if (disguiseEntity == null) {
            return;
        }

        // === SYNC ALL ANIMATION DATA ===

        // Walk animation
        WalkAnimationStateAccessor targetWalk = (WalkAnimationStateAccessor) disguiseEntity.walkAnimation;
        WalkAnimationStateAccessor sourceWalk = (WalkAnimationStateAccessor) player.walkAnimation;
        targetWalk.setSpeedOld(sourceWalk.getSpeedOld());
        targetWalk.setSpeed(sourceWalk.getSpeed());
        targetWalk.setPosition(sourceWalk.getPosition());

        // Attack/swing
        disguiseEntity.swinging = player.swinging;
        disguiseEntity.swingTime = player.swingTime;
        disguiseEntity.oAttackAnim = player.oAttackAnim;
        disguiseEntity.attackAnim = player.attackAnim;

        // Rotations
        disguiseEntity.yBodyRot = player.yBodyRot;
        disguiseEntity.yBodyRotO = player.yBodyRotO;
        disguiseEntity.yHeadRot = player.yHeadRot;
        disguiseEntity.yHeadRotO = player.yHeadRotO;
        disguiseEntity.setXRot(player.getXRot());
        disguiseEntity.xRotO = player.xRotO;

        // States
        disguiseEntity.tickCount = player.tickCount;
        disguiseEntity.setOnGround(player.onGround());
        disguiseEntity.setShiftKeyDown(player.isShiftKeyDown());
        disguiseEntity.setSprinting(player.isSprinting());
        disguiseEntity.setSwimming(player.isSwimming());
        disguiseEntity.setPose(player.getPose());

        // Equipment
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            disguiseEntity.setItemSlot(slot, player.getItemBySlot(slot).copy());
        }

        // Mob specific
        if (disguiseEntity instanceof Mob mob) {
            mob.setAggressive(player.isUsingItem());
        }

        // === RENDER ===
        @SuppressWarnings("unchecked")
        EntityRenderer<? super LivingEntity> renderer =
                (EntityRenderer<? super LivingEntity>) Minecraft.getInstance()
                        .getEntityRenderDispatcher().getRenderer(disguiseEntity);

        renderer.render(disguiseEntity, yaw, partialTick, poseStack, bufferSource, light);

        // Cancel vanilla rendering
        ci.cancel();
    }

    @Inject(
            method = "renderHand",
            at = @At("HEAD"),
            cancellable = true
    )
    private void illusionistGrimoire$hideDisguisedHand(PoseStack poseStack, MultiBufferSource bufferSource, int light,
                                                       AbstractClientPlayer player, ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
        if (player.hasEffect(IGEffectRegistry.DISGUISED)) {
            ci.cancel();
        }
    }
}

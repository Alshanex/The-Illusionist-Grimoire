package net.alshanex.illusionist_grimoire.event;

import com.mojang.authlib.GameProfile;
import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.data.IGClientData;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

@EventBusSubscriber(modid = IllusionistGrimoireMod.MODID, value = Dist.CLIENT)
public class ClientEvents {
	@SubscribeEvent
	private static void renderShapeshift(RenderLivingEvent.Pre<LivingEntity, EntityModel<LivingEntity>> event) {
		LivingEntity living = event.getEntity();
		if (living instanceof Player player && player.hasEffect(IGEffectRegistry.DISGUISED)) {
			var disguiseData = IGClientData.getDisguiseData(living);
			ResourceLocation entityLoc = disguiseData.getShapeshiftedEntityId();

			// Check if disguised as a player with specific skin data
			if (disguiseData.isDisguisedAsPlayer() && disguiseData.getDisguisedPlayerProfile() != null) {
				// Create a fake player entity with the target's skin
				GameProfile targetProfile = disguiseData.getDisguisedPlayerProfile();

				var fakePlayer = new RemotePlayer(
						(ClientLevel) living.level(),
						targetProfile
				);

				syncRemotePlayer(fakePlayer, player);

				// Render the fake player with their skin
				Minecraft.getInstance().getEntityRenderDispatcher()
						.getRenderer(fakePlayer)
						.render(fakePlayer, living.yBodyRot, event.getPartialTick(),
								event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());

				event.setCanceled(true);
			} else {
				// Original code for non-player entities
				LivingEntity e = (LivingEntity) BuiltInRegistries.ENTITY_TYPE.get(entityLoc).create(living.level());
				if(e != null){
					e.yBodyRot = living.yBodyRot;
					e.yBodyRotO = living.yBodyRotO;
					e.yHeadRot = living.yHeadRot;
					e.yHeadRotO = living.yHeadRotO;
					e.xRotO = living.xRotO;
					e.setXRot(living.getXRot());
					e.walkAnimation.setSpeed(living.walkAnimation.speed());
					e.walkAnimation.speedOld = living.walkAnimation.speedOld;
					e.walkAnimation.position = living.walkAnimation.position;
					e.attackAnim = living.attackAnim;
					e.oAttackAnim = living.oAttackAnim;
					e.swinging = living.swinging;
					e.setSprinting(living.isSprinting());
					Arrays.stream(EquipmentSlot.values()).forEach((slot) ->
							e.setItemSlot(slot, living.getItemBySlot(slot).copy())
					);

					Minecraft.getInstance().getEntityRenderDispatcher()
							.getRenderer(e)
							.render(e, living.yBodyRot, event.getPartialTick(),
									event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
					event.setCanceled(true);
				}
			}
		}
	}

	public static void syncRemotePlayer(RemotePlayer remote, Player source) {
		// === POSITION AND ROTATION ===
		remote.setPos(source.getX(), source.getY(), source.getZ());
		remote.setXRot(source.getXRot());
		remote.setYRot(source.getYRot());
		remote.setYHeadRot(source.getYHeadRot());
		remote.setYBodyRot(source.yBodyRot);

		// Previous tick values (for interpolation/rendering)
		remote.xo = source.xo;
		remote.yo = source.yo;
		remote.zo = source.zo;
		remote.xRotO = source.xRotO;
		remote.yRotO = source.yRotO;
		remote.yHeadRotO = source.yHeadRotO;
		remote.yBodyRotO = source.yBodyRotO;

		// Old position (used for some calculations)
		remote.xOld = source.xOld;
		remote.yOld = source.yOld;
		remote.zOld = source.zOld;

		// === MOVEMENT ===
		remote.setDeltaMovement(source.getDeltaMovement());
		remote.setOnGround(source.onGround());
		remote.horizontalCollision = source.horizontalCollision;
		remote.verticalCollision = source.verticalCollision;
		remote.verticalCollisionBelow = source.verticalCollisionBelow;
		remote.minorHorizontalCollision = source.minorHorizontalCollision;

		// Fall distance (affects fall animation and sounds)
		remote.fallDistance = source.fallDistance;

		// Walk distance (affects footstep sounds/animation timing)
		remote.walkDist = source.walkDist;
		remote.walkDistO = source.walkDistO;
		remote.moveDist = source.moveDist;
		remote.walkAnimation.setSpeed(source.walkAnimation.speed());
		remote.walkAnimation.speedOld = source.walkAnimation.speedOld;
		remote.walkAnimation.position = source.walkAnimation.position;

		// === POSE AND FLAGS ===
		remote.setPose(source.getPose());
		remote.setSprinting(source.isSprinting());
		remote.setSwimming(source.isSwimming());
		remote.setShiftKeyDown(source.isShiftKeyDown());

		byte customization = source.getEntityData().get(Player.DATA_PLAYER_MODE_CUSTOMISATION);
		remote.getEntityData().set(Player.DATA_PLAYER_MODE_CUSTOMISATION, customization);

		remote.setMainArm(source.getMainArm());

		// Fall flying (elytra) - use setSharedFlag for the flag
		remote.setSharedFlag(7, source.isFallFlying());

		remote.swimAmount = source.swimAmount;
		remote.swimAmountO = source.swimAmountO;

		// Glowing effect
		remote.setGlowingTag(source.hasGlowingTag());

		// Invisibility
		remote.setInvisible(source.isInvisible());

		// === SWING / ATTACK ANIMATION ===
		remote.swinging = source.swinging;
		remote.swingTime = source.swingTime;
		remote.swingingArm = source.swingingArm;
		remote.attackAnim = source.attackAnim;
		remote.oAttackAnim = source.oAttackAnim;

		// === BOB ANIMATION ===
		remote.bob = source.bob;
		remote.oBob = source.oBob;

		// === HURT ANIMATION ===
		remote.hurtTime = source.hurtTime;
		remote.hurtDuration = source.hurtDuration;
		remote.hurtDir = source.getHurtDir();

		// === DEATH ANIMATION ===
		remote.deathTime = source.deathTime;

		// === HEALTH (affects some visual states) ===
		remote.setHealth(source.getHealth());

		// === USING ITEM STATE ===
		if (source.isUsingItem()) {
			// Sync the using item state
			InteractionHand hand = source.getUsedItemHand();
			if (!remote.isUsingItem() || remote.getUsedItemHand() != hand) {
				remote.startUsingItem(hand);
			}
		} else if (remote.isUsingItem()) {
			remote.stopUsingItem();
		}

		// === EQUIPMENT ===
		Arrays.stream(EquipmentSlot.values()).forEach((slot) ->
				remote.setItemSlot(slot, source.getItemBySlot(slot).copy())
		);

		// === FIRE STATE ===
		remote.setRemainingFireTicks(source.getRemainingFireTicks());

		// === FREEZING STATE ===
		remote.setTicksFrozen(source.getTicksFrozen());
		remote.isInPowderSnow = source.isInPowderSnow;
		remote.wasInPowderSnow = source.wasInPowderSnow;

		// === SLEEP STATE ===
		if (source.isSleeping()) {
			source.getSleepingPos().ifPresent(pos -> {
				if (!remote.isSleeping() || !remote.getSleepingPos().equals(Optional.of(pos))) {
					remote.setSleepingPos(pos);
				}
			});
		} else if (remote.isSleeping()) {
			remote.clearSleepingPos();
		}

		// === SPIN ATTACK (Trident Riptide) ===
		remote.autoSpinAttackTicks = source.autoSpinAttackTicks;

		// === TICK COUNT (can affect some animations) ===
		remote.tickCount = source.tickCount;

		// === AbstractClientPlayer specific ===
		if (source instanceof AbstractClientPlayer sourceClient) {
			remote.deltaMovementOnPreviousTick = sourceClient.deltaMovementOnPreviousTick;
			remote.elytraRotX = sourceClient.elytraRotX;
			remote.elytraRotY = sourceClient.elytraRotY;
			remote.elytraRotZ = sourceClient.elytraRotZ;
		}

		// === CLOAK ANIMATION (cape physics) ===
		remote.xCloak = source.xCloak;
		remote.yCloak = source.yCloak;
		remote.zCloak = source.zCloak;
		remote.xCloakO = source.xCloakO;
		remote.yCloakO = source.yCloakO;
		remote.zCloakO = source.zCloakO;
	}

	@SubscribeEvent
	public static void onRenderNameplate(RenderNameTagEvent event) {
		if (event.getEntity() instanceof Player player && player.hasEffect(IGEffectRegistry.DISGUISED)) {
			var disguiseData = IGClientData.getDisguiseData(player);
			if (disguiseData.isDisguisedAsPlayer() && disguiseData.getDisguisedPlayerName() != null) {
				event.setContent(Component.literal(disguiseData.getDisguisedPlayerName()));
			}
		}
	}

	@SubscribeEvent
	public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
		Player player = event.getEntity();
		Minecraft mc = Minecraft.getInstance();

		if (player == mc.player
				&& mc.options.getCameraType() == CameraType.FIRST_PERSON
				&& player.hasEffect(IGEffectRegistry.DISGUISED)) {
			event.setCanceled(true);
		}
	}

	private static final ResourceLocation OVERLAY_TEXTURE = ResourceLocation.fromNamespaceAndPath(IllusionistGrimoireMod.MODID, "textures/gui/job_application_screen.png");

	@SubscribeEvent
	public static void onRenderGuiLayer(RenderGuiLayerEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;

		if (player == null || !player.hasEffect(IGEffectRegistry.FEARED)) {
			return;
		}

		var effectInstance = player.getEffect(IGEffectRegistry.FEARED);
		int duration = effectInstance.getDuration();
		int totalDuration = 120;
		if (duration > totalDuration - 30) {
			return;
		}

		GuiGraphics guiGraphics = event.getGuiGraphics();
		int screenWidth = mc.getWindow().getGuiScaledWidth();
		int screenHeight = mc.getWindow().getGuiScaledHeight();

		// Your texture dimensions
		int textureWidth = 229;
		int textureHeight = 300;

		float scale = (float) screenHeight / textureHeight;
		int renderWidth = (int) (textureWidth * scale);
		int renderHeight = screenHeight;

		// Center position
		int x = (screenWidth - renderWidth) / 2;
		int y = 0;

		// Render the texture
		guiGraphics.blit(
				OVERLAY_TEXTURE,
				x, y,
				renderWidth, renderHeight,
				0, 0,
				textureWidth, textureHeight,
				textureWidth, textureHeight
		);
	}
}

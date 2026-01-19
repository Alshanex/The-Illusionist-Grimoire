package net.alshanex.illusionist_grimoire.event;

import com.mojang.authlib.GameProfile;
import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.data.IGClientData;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

import java.util.Arrays;

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

				var fakePlayer = new net.minecraft.client.player.RemotePlayer(
						(ClientLevel) living.level(),
						targetProfile
				);

				// Copy all the animation and rotation data
				fakePlayer.yBodyRot = living.yBodyRot;
				fakePlayer.yBodyRotO = living.yBodyRotO;
				fakePlayer.yHeadRot = living.yHeadRot;
				fakePlayer.yHeadRotO = living.yHeadRotO;
				fakePlayer.xRotO = living.xRotO;
				fakePlayer.setXRot(living.getXRot());
				fakePlayer.walkAnimation.setSpeed(living.walkAnimation.speed());
				fakePlayer.walkAnimation.speedOld = living.walkAnimation.speedOld;
				fakePlayer.walkAnimation.position = living.walkAnimation.position;
				fakePlayer.attackAnim = living.attackAnim;
				fakePlayer.oAttackAnim = living.oAttackAnim;
				fakePlayer.swinging = living.swinging;
				fakePlayer.setSprinting(living.isSprinting());

				// Copy equipment
				Arrays.stream(EquipmentSlot.values()).forEach((slot) ->
						fakePlayer.setItemSlot(slot, living.getItemBySlot(slot).copy())
				);

				// Set position for rendering
				fakePlayer.setPos(living.getX(), living.getY(), living.getZ());

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
}

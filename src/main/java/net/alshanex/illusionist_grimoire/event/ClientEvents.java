package net.alshanex.illusionist_grimoire.event;

import com.mojang.authlib.GameProfile;
import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.data.IGClientData;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.GlowSquid;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Arrays;

@EventBusSubscriber(modid = IllusionistGrimoireMod.MODID, value = Dist.CLIENT)
public class ClientEvents {

	@SubscribeEvent
	public static void onRenderNameplate(RenderNameTagEvent event) {
		if (event.getEntity() instanceof Player player && player.hasEffect(IGEffectRegistry.DISGUISED)) {
			var disguiseData = IGClientData.getDisguiseData(player);
			if (disguiseData.isDisguisedAsPlayer() && disguiseData.getDisguisedPlayerName() != null) {
				event.setContent(Component.literal(disguiseData.getDisguisedPlayerName()));
			}
			if (!disguiseData.isDisguisedAsPlayer()) {
				event.setContent(Component.empty());
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

	@SubscribeEvent
	public static void onClientPlayerTick(PlayerTickEvent.Post event) {
		if (event.getEntity() instanceof LocalPlayer player) {
			// Check if player is disguised
			if (player.hasEffect(IGEffectRegistry.DISGUISED)) {
				var disguiseData = IGClientData.getDisguiseData(player);

				// Check if disguised as glow squid
				if (!disguiseData.isDisguisedAsPlayer()) {
					LivingEntity mobEntity = disguiseData.getMobDisguiseEntity();

					if (mobEntity instanceof GlowSquid) {
						// Spawn glow particles around the player (same as GlowSquid.aiStep())
						player.level().addParticle(
								ParticleTypes.GLOW,
								player.getRandomX(0.6),
								player.getRandomY(),
								player.getRandomZ(0.6),
								0.0, 0.0, 0.0
						);
					}
				}
			}
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

package net.alshanex.illusionist_grimoire.event;

import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.data.IGClientData;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

import java.util.Arrays;

@EventBusSubscriber(modid = IllusionistGrimoireMod.MODID, value = Dist.CLIENT)
public class ClientEvents {
	@SubscribeEvent
	private static void renderShapeshift(RenderLivingEvent.Pre<LivingEntity, EntityModel<LivingEntity>> event) {
		LivingEntity living = event.getEntity();
		if (living instanceof Player player && player.hasEffect(IGEffectRegistry.DISGUISED)) {
			ResourceLocation entityLoc = IGClientData.getDisguiseData(living).getShapeshiftedEntityId();
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
				Arrays.stream(EquipmentSlot.values()).forEach((slot) -> e.setItemSlot(slot, living.getItemBySlot(slot).copy()));

				Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(e).render(e, living.yBodyRot, event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
				event.setCanceled(true);
			}
		}
	}
}

package net.alshanex.illusionist_grimoire.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

@EventBusSubscriber(modid = IllusionistGrimoireMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class DepthTrickClientEventHandler {
    @SubscribeEvent
    public static void onRenderEntity(RenderLivingEvent.Pre<?, ?> event) {
        Player player = Minecraft.getInstance().player;

        if (player != null && player.hasEffect(IGEffectRegistry.DEPTH_TRICK)) {
            LivingEntity entity = event.getEntity();

            // Check if entity is visible to player
            if (canPlayerSeeEntity(player, entity, event.getPartialTick())) {
                PoseStack poseStack = event.getPoseStack();

                Vec3 playerPos = player.getEyePosition(event.getPartialTick());
                Vec3 entityPos = entity.getPosition(event.getPartialTick());
                Vec3 direction = entityPos.subtract(playerPos).normalize();

                // Move 3 blocks closer
                Vec3 offset = direction.scale(-7.0);

                poseStack.pushPose();
                poseStack.translate(offset.x, offset.y, offset.z);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderEntityPost(RenderLivingEvent.Post<?, ?> event) {
        Player player = Minecraft.getInstance().player;

        if (player != null && player.hasEffect(IGEffectRegistry.DEPTH_TRICK)) {
            if (canPlayerSeeEntity(player, event.getEntity(), event.getPartialTick())) {
                event.getPoseStack().popPose();
            }
        }
    }

    private static boolean canPlayerSeeEntity(Player player, Entity entity, float partialTick) {
        Vec3 playerLook = player.getLookAngle();
        Vec3 playerPos = player.getEyePosition(partialTick);
        Vec3 toEntity = entity.getPosition(partialTick).subtract(playerPos).normalize();

        // Check if within FOV
        double dot = playerLook.dot(toEntity);
        return dot > -0.5; // Wider than just front-facing
    }
}

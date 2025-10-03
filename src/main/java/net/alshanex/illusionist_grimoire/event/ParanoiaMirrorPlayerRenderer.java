package net.alshanex.illusionist_grimoire.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ParanoiaMirrorPlayerRenderer {

    private static final float MIN_MIRROR_DISTANCE = 1.0f;
    private static final float MAX_MIRROR_DISTANCE = 6.0f;
    private static float currentMirrorDistance = 4.0f;
    private static int distanceChangeCooldown = 0;
    private static final int DISTANCE_CHANGE_INTERVAL = 100;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null || !player.hasEffect(IGEffectRegistry.PARANOIA)) {
            return;
        }

        if (!(player instanceof AbstractClientPlayer clientPlayer)) {
            return;
        }

        renderMirror(event, mc, clientPlayer);
    }

    private static void renderMirror(RenderLevelStageEvent event, Minecraft mc, AbstractClientPlayer player) {
        EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();

        var entityRenderer = renderManager.getRenderer(player);
        if (!(entityRenderer instanceof PlayerRenderer playerRenderer)) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        // Update mirror distance periodically
        if (distanceChangeCooldown <= 0) {
            currentMirrorDistance = MIN_MIRROR_DISTANCE +
                    mc.level.random.nextFloat() * (MAX_MIRROR_DISTANCE - MIN_MIRROR_DISTANCE);
            distanceChangeCooldown = DISTANCE_CHANGE_INTERVAL;
        } else {
            distanceChangeCooldown--;
        }

        // Get player position and look direction
        Vec3 playerPos = player.position();
        Vec3 lookVec = player.getLookAngle();
        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();

        // Position mirror at varying distance ahead of player
        Vec3 mirrorPos = playerPos.add(
                lookVec.x * currentMirrorDistance,
                0,
                lookVec.z * currentMirrorDistance
        );

        // Calculate camera-relative position
        double renderX = mirrorPos.x - camera.x;
        double renderY = mirrorPos.y - camera.y;
        double renderZ = mirrorPos.z - camera.z;

        // Calculate yaw for mirror to face the player
        double deltaX = playerPos.x - mirrorPos.x;
        double deltaZ = playerPos.z - mirrorPos.z;
        float mirrorYaw = (float)(Math.atan2(deltaZ, deltaX) * (180.0 / Math.PI)) - 90.0f;

        // Store original rotations
        float originalYaw = player.getYRot();
        float originalYHeadRot = player.yHeadRot;
        float originalYBodyRot = player.yBodyRot;
        float originalXRot = player.getXRot();
        float originalYHeadRotO = player.yHeadRotO;
        float originalYRotO = player.yRotO;
        float originalYBodyRotO = player.yBodyRotO;
        float originalXRotO = player.xRotO;

        // Calculate the pitch angle for the mirror to look at the player
        double deltaY = playerPos.y + player.getEyeHeight() - (mirrorPos.y + player.getEyeHeight());
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float mirrorPitch = (float)(Math.atan2(deltaY, horizontalDistance) * (180.0 / Math.PI));

        // Set mirror rotations to face player
        player.setYRot(mirrorYaw);
        player.yHeadRot = mirrorYaw;
        player.yBodyRot = mirrorYaw;
        player.setXRot(mirrorPitch);
        player.yHeadRotO = mirrorYaw;
        player.yRotO = mirrorYaw;
        player.yBodyRotO = mirrorYaw;
        player.xRotO = mirrorPitch;

        poseStack.pushPose();
        poseStack.translate(renderX, renderY, renderZ);

        // Get lighting
        int packedLight = mc.getEntityRenderDispatcher().getPackedLightCoords(player, partialTick);

        // Render the mirror
        playerRenderer.render(player, mirrorYaw, partialTick, poseStack, bufferSource, packedLight);

        poseStack.popPose();

        // Restore original rotations immediately
        player.setYRot(originalYaw);
        player.yHeadRot = originalYHeadRot;
        player.yBodyRot = originalYBodyRot;
        player.setXRot(originalXRot);
        player.yHeadRotO = originalYHeadRotO;
        player.yRotO = originalYRotO;
        player.yBodyRotO = originalYBodyRotO;
        player.xRotO = originalXRotO;

        // Flush buffer
        bufferSource.endBatch();
    }
}

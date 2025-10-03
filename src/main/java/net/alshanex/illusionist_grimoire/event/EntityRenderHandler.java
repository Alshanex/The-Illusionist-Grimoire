package net.alshanex.illusionist_grimoire.event;

import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.block.IllusionBlockEntity;
import net.alshanex.illusionist_grimoire.registry.IGBlockRegistry;
import net.alshanex.illusionist_grimoire.util.IGUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = IllusionistGrimoireMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class EntityRenderHandler {

    private static final double DETECTION_RADIUS = 5.0;
    private static final Set<Entity> hiddenEntities = new HashSet<>();

    @SubscribeEvent
    public static void onRenderEntity(RenderLivingEvent.Pre<?, ?> event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (IGUtils.canMobBypassIllusions(mc.player)) return;

        Entity entity = event.getEntity();
        Vec3 playerPos = mc.player.getEyePosition(event.getPartialTick());
        Vec3 entityPos = entity.position().add(0, entity.getBbHeight() / 2, 0);

        Set<BlockPos> phaseBlocks = getPhaseBlocksInRay(mc, playerPos, entityPos);

        if (!phaseBlocks.isEmpty()) {
            for (BlockPos blockPos : phaseBlocks) {
                Vec3 blockCenter = Vec3.atCenterOf(blockPos);
                if (entityPos.distanceTo(blockCenter) <= DETECTION_RADIUS) {
                    hiddenEntities.add(entity);
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderEntityPost(RenderLivingEvent.Post<?, ?> event) {
        hiddenEntities.remove(event.getEntity());
    }

    public static boolean shouldHideEntity(Entity entity) {
        return hiddenEntities.contains(entity);
    }

    private static Set<BlockPos> getPhaseBlocksInRay(Minecraft mc, Vec3 start, Vec3 end) {
        Set<BlockPos> phaseBlocks = new HashSet<>();

        Vec3 direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);

        for (double d = 0; d < distance; d += 0.1) {
            Vec3 checkPos = start.add(direction.scale(d));
            BlockPos pos = BlockPos.containing(checkPos);

            if(mc.level.getBlockEntity(pos) instanceof IllusionBlockEntity illusionBlockEntity && !mc.player.is(illusionBlockEntity.getSummoner())) {
                if(!IGUtils.canBypassIllusions(mc.player, illusionBlockEntity.getOwnerSpellPower())) {
                    phaseBlocks.add(pos);
                }
            }
        }

        return phaseBlocks;
    }
}

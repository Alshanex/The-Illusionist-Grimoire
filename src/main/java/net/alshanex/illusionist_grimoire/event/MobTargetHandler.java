package net.alshanex.illusionist_grimoire.event;

import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.registry.IGBlockRegistry;
import net.alshanex.illusionist_grimoire.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = IllusionistGrimoireMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class MobTargetHandler {

    @SubscribeEvent
    public static void onMobChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || event.getEntity().getType().is(ModTags.ILLUSION_IMMUNE_ENTITIES)) return;

        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (newTarget == null) return;

        if (hasPhaseBlockBetween(mob, newTarget)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (event.getEntity().tickCount % 10 != 0) return;

        if (!(event.getEntity() instanceof Mob mob) || event.getEntity().getType().is(ModTags.ILLUSION_IMMUNE_ENTITIES)) return;

        LivingEntity target = mob.getTarget();
        if (target == null) return;

        // If phase block is between mob and current target, clear the target
        if (hasPhaseBlockBetween(mob, target)) {
            mob.setTarget(null);
        }
    }

    private static boolean hasPhaseBlockBetween(LivingEntity mob, LivingEntity target) {
        Vec3 mobPos = mob.getEyePosition();
        Vec3 targetPos = target.getEyePosition();

        Set<BlockPos> phaseBlocks = getPhaseBlocksInRay(mob.level(), mobPos, targetPos);

        return !phaseBlocks.isEmpty();
    }

    private static Set<BlockPos> getPhaseBlocksInRay(net.minecraft.world.level.Level level, Vec3 start, Vec3 end) {
        Set<BlockPos> phaseBlocks = new HashSet<>();

        Vec3 direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);

        for (double d = 0; d < distance; d += 0.5) {
            Vec3 checkPos = start.add(direction.scale(d));
            BlockPos pos = BlockPos.containing(checkPos);

            if (level.getBlockState(pos).getBlock() == IGBlockRegistry.ILLUSION_BLOCK.get()) {
                phaseBlocks.add(pos);
                return phaseBlocks;
            }
        }

        return phaseBlocks;
    }
}

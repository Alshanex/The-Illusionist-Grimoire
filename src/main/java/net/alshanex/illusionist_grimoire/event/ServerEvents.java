package net.alshanex.illusionist_grimoire.event;

import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.data.DisguiseData;
import net.alshanex.illusionist_grimoire.data.SquishData;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = IllusionistGrimoireMod.MODID)
public class ServerEvents {
    @SubscribeEvent
    public static void onStartTracking(final PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && event.getTarget() instanceof ServerPlayer targetPlayer) {
            DisguiseData.getDisguiseData(targetPlayer).syncToPlayer(serverPlayer);
            DisguiseData.getDisguiseData(serverPlayer).syncToPlayer(targetPlayer);
            SquishData.getSquishData(targetPlayer).syncToPlayer(serverPlayer);
            SquishData.getSquishData(serverPlayer).syncToPlayer(targetPlayer);
        }
    }

    @SubscribeEvent
    public static void onRespawn(final PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            DisguiseData.getDisguiseData(serverPlayer).syncToPlayer(serverPlayer);
            SquishData.getSquishData(serverPlayer).syncToPlayer(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onChangeDim(final PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            DisguiseData.getDisguiseData(serverPlayer).syncToPlayer(serverPlayer);
            SquishData.getSquishData(serverPlayer).syncToPlayer(serverPlayer);
        }

    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            DisguiseData.getDisguiseData(serverPlayer).syncToPlayer(serverPlayer);
            SquishData.getSquishData(serverPlayer).syncToPlayer(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void refreshDimensionsAfterSquish(MobEffectEvent.Remove event){
        if (event.getEffect().is(IGEffectRegistry.SQUISH)){
            event.getEntity().refreshDimensions();
        }
    }
}

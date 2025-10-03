package net.alshanex.illusionist_grimoire.event;

import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ParanoiaShaderHandler {

    @SubscribeEvent
    public static void onFogRender(ViewportEvent.RenderFog event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.hasEffect(IGEffectRegistry.PARANOIA)) {
            // Reduce fog distance based on amplifier (like blindness does)
            float fogDistance = 5.0f;

            event.setNearPlaneDistance(0.0f);
            event.setFarPlaneDistance(fogDistance);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.hasEffect(IGEffectRegistry.PARANOIA)) {
            // Darken the fog color for a more ominous effect
            float factor = 0.5f; // Darken by 50%
            event.setRed(event.getRed() * factor);
            event.setGreen(event.getGreen() * factor);
            event.setBlue(event.getBlue() * factor);
        }
    }
}

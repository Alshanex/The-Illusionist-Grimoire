package net.alshanex.illusionist_grimoire.setup;

import net.alshanex.illusionist_grimoire.registry.IGEntityRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        //event.registerEntityRenderer(IGEntityRegistry.PARANOIA_HALLUCINATION.get(), ParanoiaHallucinationRenderer::new);
    }
}

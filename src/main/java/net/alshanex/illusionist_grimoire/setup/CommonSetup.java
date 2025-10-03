package net.alshanex.illusionist_grimoire.setup;

import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.registry.IGEntityRegistry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = IllusionistGrimoireMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class CommonSetup {
    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        //event.put(IGEntityRegistry.PARANOIA_HALLUCINATION.get(), ParanoiaHallucinationEntity.createAttributes().build());
    }
}

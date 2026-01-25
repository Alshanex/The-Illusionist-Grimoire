package net.alshanex.illusionist_grimoire.setup;

import io.redspace.ironsspellbooks.registries.OverlayRegistry;
import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.block.SpellTrapBlockEntityRenderer;
import net.alshanex.illusionist_grimoire.registry.IGBlockEntityRegistry;
import net.alshanex.illusionist_grimoire.registry.IGEntityRegistry;
import net.alshanex.illusionist_grimoire.screen.ParanoiaOverlay;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(IGEntityRegistry.SPELL_TRAP_DUMMY.get(), NoopRenderer::new);
        event.registerBlockEntityRenderer(
                IGBlockEntityRegistry.SPELL_TRAP.get(),
                SpellTrapBlockEntityRenderer::new
        );

        event.registerEntityRenderer(IGEntityRegistry.FEAR_BOLT.get(), NoopRenderer::new);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerBelow(
                VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(IllusionistGrimoireMod.MODID, "paranoia_overlay"),
                new ParanoiaOverlay()
        );
    }
}

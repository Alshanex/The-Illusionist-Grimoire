package net.alshanex.illusionist_grimoire.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class ParanoiaOverlay implements LayeredDraw.Layer {
    private static final ResourceLocation VIGNETTE_LOCATION =
            ResourceLocation.withDefaultNamespace("textures/misc/vignette.png");

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player != null && player.hasEffect(IGEffectRegistry.PARANOIA)) {
            // Get the float value from the DeltaTracker for smooth animation
            float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);

            int screenWidth = guiGraphics.guiWidth();
            int screenHeight = guiGraphics.guiHeight();

            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            // --- Customization Section ---
            int amplifier = player.getEffect(IGEffectRegistry.PARANOIA).getAmplifier();
            float baseAlpha = 0.5F + (amplifier * 0.1F);
            baseAlpha = Mth.clamp(baseAlpha, 0.0F, 1.0F);

            // Use the extracted partialTick for the pulse animation
            float time = (player.tickCount + partialTick) / 20.0F;
            float pulse = 0.05F * Mth.sin(time * 2.0F);
            float alpha = Mth.clamp(baseAlpha + pulse, 0.0F, 1.0F);

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, VIGNETTE_LOCATION);

            guiGraphics.blit(VIGNETTE_LOCATION, 0, 0, -90, 0.0F, 0.0F, screenWidth, screenHeight, screenWidth, screenHeight);

            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(0.35F, 0.50F, 0.53F, 1.0F);
        }
    }
}

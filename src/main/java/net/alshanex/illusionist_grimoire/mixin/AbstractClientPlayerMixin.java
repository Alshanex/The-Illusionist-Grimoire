package net.alshanex.illusionist_grimoire.mixin;

import com.mojang.authlib.GameProfile;
import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.data.DisguiseData;
import net.alshanex.illusionist_grimoire.data.IGClientData;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {

    @Inject(
            method = "getSkin()Lnet/minecraft/client/resources/PlayerSkin;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void illusionist_grimoire$overrideSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;

        // Check if player has disguise effect
        if (!player.hasEffect(IGEffectRegistry.DISGUISED)) {
            return;
        }

        // Get disguise data
        DisguiseData disguiseData = IGClientData.getDisguiseData(player);
        if (!disguiseData.isDisguisedAsPlayer()) {
            return;
        }

        // Get the target player's profile
        GameProfile disguisedProfile = disguiseData.getDisguisedPlayerProfile();
        if (disguisedProfile == null) {
            return;
        }

        try {
            // Fetch the disguised player's complete skin data from Minecraft's skin manager
            // This includes skin texture, cape, model type, and elytra
            PlayerSkin disguisedSkin = Minecraft.getInstance()
                    .getSkinManager()
                    .getInsecureSkin(disguisedProfile);

            // Return the disguised player's complete skin instead of the original
            cir.setReturnValue(disguisedSkin);
        } catch (Exception e) {
            // If we fail to get the skin, fall back to default behavior
            IllusionistGrimoireMod.LOGGER.debug("[Illusionist Grimoire] Failed to load disguised skin for profile: "
                    + disguisedProfile.getName());
        }
    }
}

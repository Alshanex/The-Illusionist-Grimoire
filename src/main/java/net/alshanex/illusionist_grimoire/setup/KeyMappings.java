package net.alshanex.illusionist_grimoire.setup;

import com.mojang.blaze3d.platform.InputConstants;
import io.redspace.ironsspellbooks.player.ExtendedKeyMapping;
import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

@EventBusSubscriber(value = Dist.CLIENT, modid = IllusionistGrimoireMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class KeyMappings {
    public static final String KEY_CATEGORY= "key.categories.illusionist_grimoire";
    public static final ExtendedKeyMapping SCREEN_KEYMAP = new ExtendedKeyMapping(getResourceName("open_disguise_gui"), KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_H, KEY_CATEGORY);

    private static String getResourceName(String name) {
        return String.format("key.illusionist_grimoire.%s", name);
    }

    @SubscribeEvent
    public static void onRegisterKeybinds(RegisterKeyMappingsEvent event) {
        event.register(SCREEN_KEYMAP);
    }
}

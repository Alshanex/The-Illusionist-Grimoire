package net.alshanex.illusionist_grimoire.registry;

import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.data.DisguiseData;
import net.alshanex.illusionist_grimoire.data.DisguiseDataProvider;
import net.alshanex.illusionist_grimoire.data.SquishData;
import net.alshanex.illusionist_grimoire.data.SquishDataProvider;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class IGDataAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, IllusionistGrimoireMod.MODID);

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<DisguiseData>> DISGUISE_DATA = ATTACHMENT_TYPES.register("disguise_data",
            () -> AttachmentType.builder((holder) -> holder instanceof ServerPlayer serverPlayer ? new DisguiseData(serverPlayer) : new DisguiseData()).serialize(new DisguiseDataProvider()).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SquishData>> SQUISH_DATA = ATTACHMENT_TYPES.register("squish_data",
            () -> AttachmentType.builder(() -> new SquishData()).serialize(new SquishDataProvider()).build());

}

package net.alshanex.illusionist_grimoire.setup;

import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.data.IGClientData;
import net.alshanex.illusionist_grimoire.network.IGSyncEntityDataPacket;
import net.alshanex.illusionist_grimoire.network.IGSyncPlayerDataPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = IllusionistGrimoireMod.MODID)
public class IGPayloadHandler {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar payloadRegistrar = event.registrar(IllusionistGrimoireMod.MODID).versioned("1.0.0").optional();

        payloadRegistrar.playToClient(IGSyncPlayerDataPacket.TYPE, IGSyncPlayerDataPacket.STREAM_CODEC, IGSyncPlayerDataPacket::handle);

        payloadRegistrar.playToClient(
                IGSyncEntityDataPacket.TYPE,
                IGSyncEntityDataPacket.STREAM_CODEC,
                (packet, context) -> {
                    context.enqueueWork(() -> IGClientData.handleEntitySyncedData(packet.entityId(), packet.data()));
                }
        );
    }
}

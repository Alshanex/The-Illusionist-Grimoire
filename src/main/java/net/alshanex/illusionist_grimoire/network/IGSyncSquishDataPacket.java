package net.alshanex.illusionist_grimoire.network;

import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.data.IGClientSquishData;
import net.alshanex.illusionist_grimoire.data.SquishData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class IGSyncSquishDataPacket implements CustomPacketPayload {
    SquishData data;
    public static final CustomPacketPayload.Type<IGSyncSquishDataPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IllusionistGrimoireMod.MODID, "sync_squish_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, IGSyncSquishDataPacket> STREAM_CODEC = CustomPacketPayload.codec(IGSyncSquishDataPacket::write, IGSyncSquishDataPacket::new);

    public IGSyncSquishDataPacket(FriendlyByteBuf buf) {
        data = SquishData.read(buf);
    }

    public IGSyncSquishDataPacket(SquishData data) {
        this.data = data;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buf) {
        SquishData.write(buf, data);
    }

    public static void handle(IGSyncSquishDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            IGClientSquishData.handleSquishDataSync(packet.data);
        });
    }
}

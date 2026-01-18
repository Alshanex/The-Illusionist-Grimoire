package net.alshanex.illusionist_grimoire.network;

import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.data.DisguiseData;
import net.alshanex.illusionist_grimoire.data.IGClientData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class IGSyncPlayerDataPacket implements CustomPacketPayload {
    DisguiseData data;
    public static final CustomPacketPayload.Type<IGSyncPlayerDataPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IllusionistGrimoireMod.MODID, "sync_magic_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, IGSyncPlayerDataPacket> STREAM_CODEC = CustomPacketPayload.codec(IGSyncPlayerDataPacket::write, IGSyncPlayerDataPacket::new);

    public IGSyncPlayerDataPacket(FriendlyByteBuf buf) {
        data = DisguiseData.read(buf);
    }
    public IGSyncPlayerDataPacket(DisguiseData data) {
        this.data = data;
    }
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buf) {
        DisguiseData.write(buf, data);
    }

    public static void handle(IGSyncPlayerDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            IGClientData.handlePlayerSyncedData(packet.data);
        });
    }
}

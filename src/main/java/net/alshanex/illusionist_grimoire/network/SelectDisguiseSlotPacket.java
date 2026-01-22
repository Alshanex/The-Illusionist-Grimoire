package net.alshanex.illusionist_grimoire.network;

import net.alshanex.illusionist_grimoire.util.IGUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SelectDisguiseSlotPacket(int slot) implements CustomPacketPayload {

    public static final Type<SelectDisguiseSlotPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("illusionist_grimoire", "select_disguise_slot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SelectDisguiseSlotPacket> STREAM_CODEC =
            CustomPacketPayload.codec(SelectDisguiseSlotPacket::write, SelectDisguiseSlotPacket::new);

    public SelectDisguiseSlotPacket(FriendlyByteBuf buf) {
        this(buf.readInt());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(slot);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SelectDisguiseSlotPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            IGUtils.handleSlotSelection(context.player(), packet.slot);
        });
    }
}

package net.alshanex.illusionist_grimoire.network;

import net.alshanex.illusionist_grimoire.item.PictureBookItem;
import net.alshanex.illusionist_grimoire.util.IGUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClearDisguiseSlotPacket(int slot) implements CustomPacketPayload {

    public static final Type<ClearDisguiseSlotPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("illusionist_grimoire", "clear_disguise_slot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClearDisguiseSlotPacket> STREAM_CODEC =
            CustomPacketPayload.codec(ClearDisguiseSlotPacket::write, ClearDisguiseSlotPacket::new);

    public ClearDisguiseSlotPacket(FriendlyByteBuf buf) {
        this(buf.readInt());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(slot);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClearDisguiseSlotPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            IGUtils.handleSlotClear(context.player(), packet.slot);
        });
    }
}

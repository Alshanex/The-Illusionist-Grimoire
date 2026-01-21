package net.alshanex.illusionist_grimoire.network;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.data.DisguiseData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import javax.swing.text.html.parser.Entity;

public record IGSyncEntityDataPacket(int entityId, DisguiseData data) implements CustomPacketPayload {
    public static final Type<IGSyncEntityDataPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IllusionistGrimoireMod.MODID, "sync_entity_data"));

    public static final StreamCodec<FriendlyByteBuf, IGSyncEntityDataPacket> STREAM_CODEC = StreamCodec.of(
            IGSyncEntityDataPacket::write,
            IGSyncEntityDataPacket::read
    );

    public IGSyncEntityDataPacket(DisguiseData data, LivingEntity entity) {
        this(entity.getId(), data);
    }

    public static void write(FriendlyByteBuf buffer, IGSyncEntityDataPacket packet) {
        buffer.writeInt(packet.entityId);
        DisguiseData.write(buffer, packet.data);
    }

    public static IGSyncEntityDataPacket read(FriendlyByteBuf buffer) {
        int entityId = buffer.readInt();
        DisguiseData data = DisguiseData.read(buffer);
        return new IGSyncEntityDataPacket(entityId, data);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

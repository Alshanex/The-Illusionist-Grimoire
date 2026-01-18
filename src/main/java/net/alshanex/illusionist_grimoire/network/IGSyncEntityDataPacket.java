package net.alshanex.illusionist_grimoire.network;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.data.DisguiseData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class IGSyncEntityDataPacket implements CustomPacketPayload {
    DisguiseData data;
    int entityId;
    public static final CustomPacketPayload.Type<IGSyncEntityDataPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IllusionistGrimoireMod.MODID, "sync_entity_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, IGSyncEntityDataPacket> STREAM_CODEC = CustomPacketPayload.codec(IGSyncEntityDataPacket::write, IGSyncEntityDataPacket::new);

    public IGSyncEntityDataPacket(DisguiseData data, IMagicEntity entity) {
        this.data = data;
        this.entityId = ((Entity) entity).getId();
    }

    public IGSyncEntityDataPacket(FriendlyByteBuf buf) {
        entityId = buf.readInt();
        data = DisguiseData.read(buf);
    }
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        DisguiseData.write(buf, data);
    }
    public static void handle(IGSyncEntityDataPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {

        });
    }
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

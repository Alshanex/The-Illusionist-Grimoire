package net.alshanex.illusionist_grimoire.network;

import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.util.IGUtils;
import net.alshanex.illusionist_grimoire.util.PlayerDisguiseProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;
import java.util.UUID;

public record IGSyncPlayerDataPacket(
        UUID playerUUID,
        ResourceLocation entityTypeId,
        CompoundTag entityNbt
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<IGSyncPlayerDataPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IllusionistGrimoireMod.MODID, "sync_player_data"));

    public static final StreamCodec<FriendlyByteBuf, IGSyncPlayerDataPacket> STREAM_CODEC = StreamCodec.of(
            IGSyncPlayerDataPacket::write,
            IGSyncPlayerDataPacket::read
    );

    // Constructor for creating packet from player
    public static IGSyncPlayerDataPacket fromPlayer(Player player) {
        UUID playerUUID = player.getUUID();
        ResourceLocation entityTypeId = ResourceLocation.withDefaultNamespace("empty");
        CompoundTag entityNbt = new CompoundTag();

        if (player instanceof PlayerDisguiseProvider provider) {
            LivingEntity disguiseEntity = provider.illusionistGrimoire$getDisguiseEntity();
            if (disguiseEntity != null) {
                entityTypeId = EntityType.getKey(disguiseEntity.getType());
                disguiseEntity.saveWithoutId(entityNbt);
            }
        }

        return new IGSyncPlayerDataPacket(playerUUID, entityTypeId, entityNbt);
    }

    public static void write(FriendlyByteBuf buffer, IGSyncPlayerDataPacket packet) {
        buffer.writeUUID(packet.playerUUID);
        buffer.writeResourceLocation(packet.entityTypeId);
        buffer.writeNbt(packet.entityNbt);
    }

    public static IGSyncPlayerDataPacket read(FriendlyByteBuf buffer) {
        UUID playerUUID = buffer.readUUID();
        ResourceLocation entityTypeId = buffer.readResourceLocation();
        CompoundTag entityNbt = buffer.readNbt();

        return new IGSyncPlayerDataPacket(playerUUID, entityTypeId, entityNbt);
    }

    public static void handle(IGSyncPlayerDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player().level().getPlayerByUUID(packet.playerUUID);

            IGUtils.handlePlayerDisguisePacket(player, packet.entityTypeId, packet.entityNbt);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

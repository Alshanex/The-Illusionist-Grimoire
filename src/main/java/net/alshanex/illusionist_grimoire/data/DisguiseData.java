package net.alshanex.illusionist_grimoire.data;

import com.mojang.authlib.GameProfile;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import net.alshanex.illusionist_grimoire.network.IGSyncEntityDataPacket;
import net.alshanex.illusionist_grimoire.network.IGSyncPlayerDataPacket;
import net.alshanex.illusionist_grimoire.registry.IGDataAttachments;
import net.alshanex.illusionist_grimoire.util.DimensionsRefresher;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.UUID;

public class DisguiseData {
    private final int serverPlayerId;
    private @Nullable LivingEntity livingEntity;

    public ResourceLocation shapeshiftedEntityId;

    // Player disguise data
    public @Nullable UUID disguisedPlayerUUID;
    public @Nullable String disguisedPlayerName;
    public @Nullable GameProfile disguisedPlayerProfile;

    // Mob disguise data
    public @Nullable LivingEntity mobDisguiseEntity;

    public DisguiseData(int serverPlayerId) {
        this.serverPlayerId = serverPlayerId;
        this.shapeshiftedEntityId = ResourceLocation.withDefaultNamespace("pig");
    }

    public DisguiseData() {
        this.serverPlayerId = -999;
    }

    public DisguiseData(LivingEntity livingEntity) {
        this(livingEntity == null ? -1 : livingEntity.getId());
        this.livingEntity = livingEntity;
    }

    // ========== NETWORK SERIALIZATION ==========

    public static void write(FriendlyByteBuf buffer, DisguiseData data) {
        buffer.writeInt(data.serverPlayerId);
        buffer.writeResourceLocation(data.shapeshiftedEntityId);

        // Player disguise data
        buffer.writeBoolean(data.disguisedPlayerUUID != null);
        if (data.disguisedPlayerUUID != null) {
            buffer.writeUUID(data.disguisedPlayerUUID);
            buffer.writeUtf(data.disguisedPlayerName);
        }

        // Mob disguise entity NBT
        if (data.mobDisguiseEntity != null) {
            CompoundTag mobNBT = new CompoundTag();
            data.mobDisguiseEntity.saveWithoutId(mobNBT);
            buffer.writeNbt(mobNBT);
        } else {
            buffer.writeNbt(new CompoundTag());
        }
    }

    public static DisguiseData read(FriendlyByteBuf buffer) {
        var data = new DisguiseData(buffer.readInt());
        data.shapeshiftedEntityId = buffer.readResourceLocation();

        // Player disguise data
        boolean hasPlayerData = buffer.readBoolean();
        if (hasPlayerData) {
            data.disguisedPlayerUUID = buffer.readUUID();
            data.disguisedPlayerName = buffer.readUtf();
            data.disguisedPlayerProfile = new GameProfile(data.disguisedPlayerUUID, data.disguisedPlayerName);
        }

        // Mob disguise entity NBT
        CompoundTag mobNBT = buffer.readNbt();
        if (mobNBT != null && !mobNBT.isEmpty()) {
            data.loadMobEntityFromNBT(mobNBT);
        }

        return data;
    }

    // ========== NBT PERSISTENCE ==========

    public void saveNBTData(CompoundTag compound, HolderLookup.Provider provider) {
        compound.putString("shapeshiftId", this.shapeshiftedEntityId.toString());

        // Player disguise data
        if (this.disguisedPlayerUUID != null) {
            compound.putUUID("disguisedPlayerUUID", this.disguisedPlayerUUID);
            compound.putString("disguisedPlayerName", this.disguisedPlayerName);
        }

        // Mob disguise entity NBT
        if (this.mobDisguiseEntity != null) {
            CompoundTag mobNBT = new CompoundTag();
            this.mobDisguiseEntity.saveWithoutId(mobNBT);
            compound.put("MobDisguiseData", mobNBT);
        }
    }

    public void loadNBTData(CompoundTag compound, HolderLookup.Provider provider) {
        this.shapeshiftedEntityId = ResourceLocation.parse(compound.getString("shapeshiftId"));

        // Player disguise data
        if (compound.contains("disguisedPlayerUUID")) {
            this.disguisedPlayerUUID = compound.getUUID("disguisedPlayerUUID");
            this.disguisedPlayerName = compound.getString("disguisedPlayerName");
            this.disguisedPlayerProfile = new GameProfile(this.disguisedPlayerUUID, this.disguisedPlayerName);
        }

        // Mob disguise entity NBT
        if (compound.contains("MobDisguiseData")) {
            CompoundTag mobNBT = compound.getCompound("MobDisguiseData");
            loadMobEntityFromNBT(mobNBT);
        }
    }

    // Helper method to create mob entity from NBT
    private void loadMobEntityFromNBT(CompoundTag nbt) {
        if (nbt == null || nbt.isEmpty()) {
            this.mobDisguiseEntity = null;
            return;
        }

        if (livingEntity == null || livingEntity.level() == null) {
            return;
        }

        // Try to get entity type from the shapeshiftedEntityId
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(this.shapeshiftedEntityId);
        if (entityType != null) {
            // Create or reuse entity instance
            if (this.mobDisguiseEntity == null || !this.mobDisguiseEntity.getType().equals(entityType)) {
                this.mobDisguiseEntity = (LivingEntity) entityType.create(livingEntity.level());
            }

            // Load NBT data into entity
            if (this.mobDisguiseEntity != null) {
                this.mobDisguiseEntity.load(nbt);
            }
        }
    }

    // ========== PLAYER DISGUISE METHODS (UNCHANGED) ==========

    public void setDisguisedPlayer(@Nullable Player player) {
        if (player != null) {
            this.disguisedPlayerUUID = player.getUUID();
            this.disguisedPlayerName = player.getName().getString();
            this.disguisedPlayerProfile = player.getGameProfile();
        } else {
            this.disguisedPlayerUUID = null;
            this.disguisedPlayerName = null;
            this.disguisedPlayerProfile = null;
        }
        doSync();
    }

    @Nullable
    public UUID getDisguisedPlayerUUID() {
        return disguisedPlayerUUID;
    }

    @Nullable
    public String getDisguisedPlayerName() {
        return disguisedPlayerName;
    }

    @Nullable
    public GameProfile getDisguisedPlayerProfile() {
        return disguisedPlayerProfile;
    }

    public boolean isDisguisedAsPlayer() {
        return disguisedPlayerUUID != null;
    }

    // ========== MOB DISGUISE METHODS ==========

    public void setMobDisguiseEntity(@Nullable LivingEntity entity) {
        this.mobDisguiseEntity = entity;
        if (entity != null) {
            this.shapeshiftedEntityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        }

        // Refresh dimensions when disguise changes
        if (livingEntity instanceof Player player) {
            ((DimensionsRefresher) player).illusionist_refreshDimensions();
        }

        doSync();
    }

    @Nullable
    public LivingEntity getMobDisguiseEntity() {
        return mobDisguiseEntity;
    }

    public void setShapeshiftId(ResourceLocation loc) {
        this.shapeshiftedEntityId = loc;

        // Clear mob entity when ID changes (will be recreated)
        this.mobDisguiseEntity = null;

        doSync();
    }

    public ResourceLocation getShapeshiftedEntityId() {
        return this.shapeshiftedEntityId;
    }

    // ========== UTILITY METHODS ==========

    public int getServerPlayerId() {
        return serverPlayerId;
    }

    public static DisguiseData getDisguiseData(LivingEntity entity) {
        return entity.getData(IGDataAttachments.DISGUISE_DATA);
    }

    public void doSync() {
        if (livingEntity instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer, new IGSyncPlayerDataPacket(this));
        } else if (livingEntity instanceof LivingEntity abstractSpellCastingMob) {
            PacketDistributor.sendToPlayersTrackingEntity(livingEntity, new IGSyncEntityDataPacket(this, abstractSpellCastingMob));
        }
    }

    public void syncToPlayer(ServerPlayer serverPlayer) {
        PacketDistributor.sendToPlayer(serverPlayer, new IGSyncPlayerDataPacket(this));
    }

    @Override
    protected DisguiseData clone() {
        return new DisguiseData(this.livingEntity);
    }

    public DisguiseData getPersistentData(ServerPlayer serverPlayer) {
        DisguiseData persistentData = new DisguiseData(livingEntity);
        persistentData.livingEntity = serverPlayer;
        return persistentData;
    }
}
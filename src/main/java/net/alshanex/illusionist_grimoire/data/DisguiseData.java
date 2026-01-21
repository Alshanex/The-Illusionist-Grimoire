package net.alshanex.illusionist_grimoire.data;

import com.mojang.authlib.GameProfile;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import net.alshanex.illusionist_grimoire.network.IGSyncEntityDataPacket;
import net.alshanex.illusionist_grimoire.network.IGSyncPlayerDataPacket;
import net.alshanex.illusionist_grimoire.registry.IGDataAttachments;
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
import java.util.Optional;
import java.util.UUID;

public class DisguiseData {
    private final int serverPlayerId;
    private @Nullable LivingEntity livingEntity;

    // Persistent disguise entity
    private @Nullable LivingEntity disguiseEntity;

    private ResourceLocation shapeshiftedEntityId;
    private boolean isMob = false;

    private @Nullable UUID disguisedPlayerUUID;
    private @Nullable String disguisedPlayerName;
    private @Nullable GameProfile disguisedPlayerProfile;

    public DisguiseData(int serverPlayerId) {
        this.serverPlayerId = serverPlayerId;
        this.shapeshiftedEntityId = ResourceLocation.withDefaultNamespace("pig");
    }

    public DisguiseData() {
        this.serverPlayerId = -999;
    }

    public DisguiseData(boolean isMob) {
        this.serverPlayerId = -999;
        this.isMob = isMob;
    }

    public static void write(FriendlyByteBuf buffer, DisguiseData data) {
        buffer.writeInt(data.serverPlayerId);
        buffer.writeResourceLocation(data.shapeshiftedEntityId);

        // Write entity NBT
        if (data.disguiseEntity != null) {
            buffer.writeBoolean(true);
            CompoundTag entityTag = new CompoundTag();
            data.disguiseEntity.saveWithoutId(entityTag);
            buffer.writeNbt(entityTag);
        } else {
            buffer.writeBoolean(false);
        }

        // Write player disguise info
        buffer.writeBoolean(data.disguisedPlayerUUID != null);
        if (data.disguisedPlayerUUID != null) {
            buffer.writeUUID(data.disguisedPlayerUUID);
            buffer.writeUtf(data.disguisedPlayerName);
        }
    }

    public static DisguiseData read(FriendlyByteBuf buffer) {
        var data = new DisguiseData(buffer.readInt());
        data.shapeshiftedEntityId = buffer.readResourceLocation();

        // Read entity NBT
        boolean hasEntity = buffer.readBoolean();
        if (hasEntity) {
            CompoundTag entityTag = buffer.readNbt();
            if (entityTag != null) {
                // Store the tag for later creation
                data.createDisguiseEntityFromTag(entityTag);
            }
        }

        // Read player disguise info
        boolean hasPlayerData = buffer.readBoolean();
        if (hasPlayerData) {
            data.disguisedPlayerUUID = buffer.readUUID();
            data.disguisedPlayerName = buffer.readUtf();
            data.disguisedPlayerProfile = new GameProfile(data.disguisedPlayerUUID, data.disguisedPlayerName);
        }
        return data;
    }

    private void createDisguiseEntityFromTag(CompoundTag entityTag) {
        // This will be called on client side after packet is received
        // We need to defer entity creation until we have a world reference
        // Store the tag for lazy creation
        this.entityNBT = entityTag;
    }

    private CompoundTag entityNBT = null;

    public DisguiseData(LivingEntity livingEntity) {
        this(livingEntity == null ? -1 : livingEntity.getId());
        this.livingEntity = livingEntity;
    }

    public void saveNBTData(CompoundTag compound, HolderLookup.Provider provider) {
        compound.putString("shapeshiftId", this.shapeshiftedEntityId.toString());

        // Save disguise entity
        if (this.disguiseEntity != null) {
            CompoundTag entityTag = new CompoundTag();
            this.disguiseEntity.saveWithoutId(entityTag);
            compound.put("DisguiseEntity", entityTag);
        }

        if (this.disguisedPlayerUUID != null) {
            compound.putUUID("disguisedPlayerUUID", this.disguisedPlayerUUID);
            compound.putString("disguisedPlayerName", this.disguisedPlayerName);
        }
    }

    public void loadNBTData(CompoundTag compound, HolderLookup.Provider provider) {
        this.shapeshiftedEntityId = ResourceLocation.parse(compound.getString("shapeshiftId"));

        // Load disguise entity
        if (compound.contains("DisguiseEntity") && livingEntity != null) {
            CompoundTag entityTag = compound.getCompound("DisguiseEntity");
            Optional<EntityType<?>> typeOpt = EntityType.by(entityTag);
            if (typeOpt.isPresent()) {
                this.disguiseEntity = (LivingEntity) typeOpt.get().create(livingEntity.level());
                if (this.disguiseEntity != null) {
                    this.disguiseEntity.load(entityTag);
                }
            }
        }

        if (compound.contains("disguisedPlayerUUID")) {
            this.disguisedPlayerUUID = compound.getUUID("disguisedPlayerUUID");
            this.disguisedPlayerName = compound.getString("disguisedPlayerName");
            this.disguisedPlayerProfile = new GameProfile(this.disguisedPlayerUUID, this.disguisedPlayerName);
        }
    }

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

    public int getServerPlayerId() {
        return serverPlayerId;
    }

    public void setShapeshiftId(ResourceLocation loc) {
        this.shapeshiftedEntityId = loc;
        doSync();
    }

    // Get or create the disguise entity
    @Nullable
    public LivingEntity getDisguiseEntity() {
        // Lazy creation from NBT if needed
        if (disguiseEntity == null && entityNBT != null && livingEntity != null) {
            Optional<EntityType<?>> typeOpt = EntityType.by(entityNBT);
            if (typeOpt.isPresent()) {
                this.disguiseEntity = (LivingEntity) typeOpt.get().create(livingEntity.level());
                if (this.disguiseEntity != null) {
                    this.disguiseEntity.load(entityNBT);
                }
            }
            entityNBT = null; // Clear after creation
        }
        return disguiseEntity;
    }

    // Set the disguise entity (called server-side)
    public void setDisguiseEntity(@Nullable LivingEntity entity) {
        this.disguiseEntity = entity;
        if (entity != null) {
            this.shapeshiftedEntityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        }
        doSync();
    }

    public ResourceLocation getShapeshiftedEntityId() {
        return this.shapeshiftedEntityId;
    }

    public static DisguiseData getDisguiseData(LivingEntity entity) {
        return entity.getData(IGDataAttachments.DISGUISE_DATA);
    }

    public void doSync() {
        if (livingEntity instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer, IGSyncPlayerDataPacket.fromPlayer(serverPlayer));
        } else if (livingEntity instanceof IMagicEntity abstractSpellCastingMob) {
            PacketDistributor.sendToPlayersTrackingEntity(livingEntity, new IGSyncEntityDataPacket(this, abstractSpellCastingMob));
        }
    }

    public void syncToPlayer(ServerPlayer serverPlayer) {
        PacketDistributor.sendToPlayer(serverPlayer, IGSyncPlayerDataPacket.fromPlayer(serverPlayer));
    }

    @Override
    protected DisguiseData clone() {
        return new DisguiseData(this.livingEntity);
    }

    public DisguiseData getPersistentData(ServerPlayer serverPlayer) {
        DisguiseData persistentData = new DisguiseData(livingEntity);
        persistentData.livingEntity = serverPlayer;
        persistentData.disguiseEntity = this.disguiseEntity;
        persistentData.shapeshiftedEntityId = this.shapeshiftedEntityId;
        persistentData.disguisedPlayerUUID = this.disguisedPlayerUUID;
        persistentData.disguisedPlayerName = this.disguisedPlayerName;
        persistentData.disguisedPlayerProfile = this.disguisedPlayerProfile;
        return persistentData;
    }
}

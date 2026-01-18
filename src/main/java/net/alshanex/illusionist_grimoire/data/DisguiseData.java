package net.alshanex.illusionist_grimoire.data;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import net.alshanex.illusionist_grimoire.network.IGSyncEntityDataPacket;
import net.alshanex.illusionist_grimoire.network.IGSyncPlayerDataPacket;
import net.alshanex.illusionist_grimoire.registry.IGDataAttachments;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;

public class DisguiseData {
    private final int serverPlayerId;
    private @Nullable LivingEntity livingEntity;

    private ResourceLocation shapeshiftedEntityId;
    private boolean isMob = false;

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
    }

    public static DisguiseData read(FriendlyByteBuf buffer) {
        var data = new DisguiseData(buffer.readInt());
        data.shapeshiftedEntityId = buffer.readResourceLocation();
        return data;
    }

    public DisguiseData(LivingEntity livingEntity) {
        this(livingEntity == null ? -1 : livingEntity.getId());
        this.livingEntity = livingEntity;
    }
    public void saveNBTData(CompoundTag compound, HolderLookup.Provider provider) {
        compound.putString("shapeshiftId", this.shapeshiftedEntityId.toString());
    }

    public void loadNBTData(CompoundTag compound, HolderLookup.Provider provider) {
        this.shapeshiftedEntityId = ResourceLocation.parse(compound.getString("shapeshiftId"));
    }


    public int getServerPlayerId() {
        return serverPlayerId;
    }

    public void setShapeshiftId(ResourceLocation loc) {
        this.shapeshiftedEntityId = loc;
        doSync();
    }
    public ResourceLocation getShapeshiftedEntityId() {
        doSync();
        return this.shapeshiftedEntityId;
    }
    public static DisguiseData getDisguiseData(LivingEntity entity) {
        return entity.getData(IGDataAttachments.DISGUISE_DATA);
    }
    public void doSync() {
        if (livingEntity instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer, new IGSyncPlayerDataPacket(this));
        } else if (livingEntity instanceof IMagicEntity abstractSpellCastingMob) {
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
        //This updates the reference while keeping the id the same (because we are in the middle of cloning logic, where id has not been set yet)
        DisguiseData persistentData = new DisguiseData(livingEntity);
        persistentData.livingEntity = serverPlayer;
        return persistentData;
    }
}

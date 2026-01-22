package net.alshanex.illusionist_grimoire.data;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;

public class IGClientData {
    private static final DisguiseData playerMagicData = new DisguiseData();

    private static final HashMap<Integer, DisguiseData> playerSyncedDataLookup = new HashMap<>();
    private static final DisguiseData emptySyncedData = new DisguiseData(-999);

    public static DisguiseData getDisguiseData(LivingEntity livingEntity) {
        if (livingEntity instanceof Player) {
            return playerSyncedDataLookup.getOrDefault(livingEntity.getId(), emptySyncedData);
        }
        return new DisguiseData(null);
    }

    public static void handlePlayerSyncedData(DisguiseData receivedData) {
        // Find the actual player entity in the client world
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        Player player = (Player) mc.level.getEntity(receivedData.getServerPlayerId());
        if (player == null) {
            // Player not loaded yet, just store the data
            playerSyncedDataLookup.put(receivedData.getServerPlayerId(), receivedData);
            return;
        }

        // Get or create the DisguiseData for this player
        DisguiseData existingData = playerSyncedDataLookup.get(receivedData.getServerPlayerId());
        if (existingData == null) {
            existingData = new DisguiseData(player);
            playerSyncedDataLookup.put(receivedData.getServerPlayerId(), existingData);
        }

        // Update the existing data with received data
        existingData.shapeshiftedEntityId = receivedData.getShapeshiftedEntityId();
        existingData.disguisedPlayerUUID = receivedData.getDisguisedPlayerUUID();
        existingData.disguisedPlayerName = receivedData.getDisguisedPlayerName();
        existingData.disguisedPlayerProfile = receivedData.getDisguisedPlayerProfile();

        // Handle mob disguise entity
        CompoundTag mobNBT = receivedData.getPendingMobNBT();
        if (mobNBT != null && !mobNBT.isEmpty()) {
            // Get the entity type
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(receivedData.getShapeshiftedEntityId());

            if (entityType != null) {
                // Create or reuse the mob entity
                if (existingData.mobDisguiseEntity == null ||
                        !existingData.mobDisguiseEntity.getType().equals(entityType)) {
                    existingData.mobDisguiseEntity = (LivingEntity) entityType.create(player.level());
                }

                // Load NBT data into our entity
                if (existingData.mobDisguiseEntity != null) {
                    existingData.mobDisguiseEntity.load(mobNBT);
                }
            }
        } else {
            // Clear mob disguise if no NBT data
            existingData.mobDisguiseEntity = null;
        }
    }

    public static void handleEntitySyncedData(int entityId, DisguiseData data) {
        playerSyncedDataLookup.put(entityId, data);
    }
}

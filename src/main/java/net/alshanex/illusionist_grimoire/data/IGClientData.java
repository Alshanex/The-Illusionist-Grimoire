package net.alshanex.illusionist_grimoire.data;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import net.minecraft.client.Minecraft;
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

    public static void handlePlayerSyncedData(DisguiseData playerSyncedData) {
        playerSyncedDataLookup.put(playerSyncedData.getServerPlayerId(), playerSyncedData);
    }
}

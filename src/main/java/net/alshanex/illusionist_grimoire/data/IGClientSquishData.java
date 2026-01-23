package net.alshanex.illusionist_grimoire.data;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;

public class IGClientSquishData {
    private static final HashMap<Integer, SquishData> entitySquishDataLookup = new HashMap<>();
    private static final SquishData emptySquishData = new SquishData();

    public static SquishData getSquishData(LivingEntity livingEntity) {
        return entitySquishDataLookup.getOrDefault(livingEntity.getId(), emptySquishData);
    }

    public static void handleSquishDataSync(SquishData receivedData) {
        // Find the actual entity in the client world
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        var entity = mc.level.getEntity(receivedData.getEntityId());
        if (entity == null) {
            // Entity not loaded yet, just store the data
            entitySquishDataLookup.put(receivedData.getEntityId(), receivedData);
            if (entity instanceof net.minecraft.world.entity.LivingEntity livingEntity) {
                livingEntity.refreshDimensions();
            }
            return;
        }

        // Store or update the squish data for this entity
        entitySquishDataLookup.put(receivedData.getEntityId(), receivedData);
    }

    public static void clear() {
        entitySquishDataLookup.clear();
    }
}

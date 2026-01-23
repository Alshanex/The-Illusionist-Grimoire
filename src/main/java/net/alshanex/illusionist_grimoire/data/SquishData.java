package net.alshanex.illusionist_grimoire.data;

import net.alshanex.illusionist_grimoire.network.IGSyncSquishDataPacket;
import net.alshanex.illusionist_grimoire.registry.IGDataAttachments;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public class SquishData {
    // Minimum thickness an entity can be squished to (2 pixels = 0.125 blocks)
    public static final float MIN_THICKNESS = 0.125f;

    private final int entityId;
    private LivingEntity livingEntity;

    // The axis along which the entity is squished (0=X, 1=Y, 2=Z)
    private int squishAxis = -1; // -1 means not squished

    // How much the entity is squished (0.125 to 1.0, where 1.0 is normal size)
    private float squishAmount = 1.0f;

    public SquishData(int entityId) {
        this.entityId = entityId;
    }

    public SquishData() {
        this.entityId = -1;
    }

    public SquishData(LivingEntity livingEntity) {
        this(livingEntity == null ? -1 : livingEntity.getId());
        this.livingEntity = livingEntity;
    }

    public void setSquish(int axis, float amount) {
        this.squishAxis = axis;
        this.squishAmount = Math.max(MIN_THICKNESS, Math.min(1.0f, amount));
        doSync();
    }


    public void applySquish(Direction.Axis axis, float amount) {
        int axisIndex = axis.ordinal(); // X=0, Y=1, Z=2

        // If already squished on same axis, make it worse
        if (this.squishAxis == axisIndex) {
            this.squishAmount = Math.max(MIN_THICKNESS, this.squishAmount * amount);
        } else {
            // New squish direction
            this.squishAxis = axisIndex;
            this.squishAmount = Math.max(MIN_THICKNESS, amount);
        }
        doSync();
    }

    public void applyTotalSquash() {
        this.squishAxis = 1; // Y-axis (vertical)
        this.squishAmount = MIN_THICKNESS;
        doSync();
    }

    public boolean isSquished() {
        return squishAxis >= 0 && squishAmount < 1.0f;
    }

    public int getSquishAxis() {
        return squishAxis;
    }

    public float getSquishAmount() {
        return squishAmount;
    }

    public float[] getScales() {
        float[] scales = new float[] {1.0f, 1.0f, 1.0f};

        if (isSquished()) {
            scales[squishAxis] = squishAmount;

            // Compensate on other axes to preserve volume (cartoon physics)
            float compensation = (float) Math.sqrt(1.0f / squishAmount);
            for (int i = 0; i < 3; i++) {
                if (i != squishAxis) {
                    scales[i] = compensation;
                }
            }
        }

        return scales;
    }

    // ========== NETWORK SERIALIZATION ==========

    public static void write(FriendlyByteBuf buffer, SquishData data) {
        buffer.writeInt(data.entityId);
        buffer.writeInt(data.squishAxis);
        buffer.writeFloat(data.squishAmount);
    }

    public static SquishData read(FriendlyByteBuf buffer) {
        var data = new SquishData(buffer.readInt());
        data.squishAxis = buffer.readInt();
        data.squishAmount = buffer.readFloat();
        return data;
    }

    // ========== SYNC METHODS ==========

    public void doSync() {
        if (livingEntity instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer, new IGSyncSquishDataPacket(this));
        } else if (livingEntity != null) {
            PacketDistributor.sendToPlayersTrackingEntity(livingEntity, new IGSyncSquishDataPacket(this));
        }
    }

    public void syncToPlayer(ServerPlayer serverPlayer) {
        PacketDistributor.sendToPlayer(serverPlayer, new IGSyncSquishDataPacket(this));
    }

    public int getEntityId() {
        return entityId;
    }

    // ========== NBT PERSISTENCE ==========

    public void saveNBTData(CompoundTag compound, HolderLookup.Provider provider) {
        compound.putInt("squishAxis", this.squishAxis);
        compound.putFloat("squishAmount", this.squishAmount);
    }

    public void loadNBTData(CompoundTag compound, HolderLookup.Provider provider) {
        this.squishAxis = compound.getInt("squishAxis");
        this.squishAmount = compound.getFloat("squishAmount");
    }

    // ========== UTILITY ==========

    public static SquishData getSquishData(LivingEntity entity) {
        return entity.getData(IGDataAttachments.SQUISH_DATA);
    }
}

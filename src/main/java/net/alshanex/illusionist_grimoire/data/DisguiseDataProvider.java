package net.alshanex.illusionist_grimoire.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import javax.annotation.Nullable;

public class DisguiseDataProvider implements IAttachmentSerializer<CompoundTag, DisguiseData> {
    @Override
    public DisguiseData read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
        // Create DisguiseData with the entity reference if available
        DisguiseData disguiseData;

        if (holder instanceof ServerPlayer serverPlayer) {
            // Server-side: we have the player entity
            disguiseData = new DisguiseData(serverPlayer);
        } else if (holder instanceof LivingEntity livingEntity) {
            // Generic living entity (for mobs with disguise capability)
            disguiseData = new DisguiseData(livingEntity);
        } else {
            // Client-side or no entity reference yet
            disguiseData = new DisguiseData();
        }

        // Load saved data from NBT
        disguiseData.loadNBTData(tag, provider);

        return disguiseData;
    }

    @Override
    public @Nullable CompoundTag write(DisguiseData disguiseData, HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        disguiseData.saveNBTData(tag, provider);
        return tag;
    }
}

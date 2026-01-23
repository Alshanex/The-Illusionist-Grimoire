package net.alshanex.illusionist_grimoire.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import org.jetbrains.annotations.Nullable;

public class SquishDataProvider implements IAttachmentSerializer<CompoundTag, SquishData> {
    @Override
    public SquishData read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
        SquishData squishData;

        if (holder instanceof LivingEntity livingEntity) {
            // Create SquishData with the entity reference
            squishData = new SquishData(livingEntity);
        } else {
            // No entity reference yet
            squishData = new SquishData();
        }

        squishData.loadNBTData(tag, provider);
        return squishData;
    }

    @Override
    public @Nullable CompoundTag write(SquishData squishData, HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        squishData.saveNBTData(tag, provider);
        return tag;
    }
}

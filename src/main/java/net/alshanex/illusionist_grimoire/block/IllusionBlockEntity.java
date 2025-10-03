package net.alshanex.illusionist_grimoire.block;

import net.alshanex.illusionist_grimoire.registry.IGBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class IllusionBlockEntity extends BlockEntity {
    private int ticksExisted = 0;
    private static final int MAX_TICKS = 600; // 30 seconds

    public IllusionBlockEntity(BlockPos pos, BlockState state) {
        super(IGBlockEntityRegistry.ILLUSION_BLOCK_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, IllusionBlockEntity blockEntity) {
        if (level.isClientSide) return;

        blockEntity.ticksExisted++;

        if (blockEntity.ticksExisted >= MAX_TICKS) {
            // Replace with air after 60 seconds
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("TicksExisted", this.ticksExisted);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.ticksExisted = tag.getInt("TicksExisted");
    }
}

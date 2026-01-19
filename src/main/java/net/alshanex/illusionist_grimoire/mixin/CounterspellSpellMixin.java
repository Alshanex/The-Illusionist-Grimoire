package net.alshanex.illusionist_grimoire.mixin;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.spells.ender.CounterspellSpell;
import net.alshanex.illusionist_grimoire.block.SpellTrapBlock;
import net.alshanex.illusionist_grimoire.block.SpellTrapBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CounterspellSpell.class)
public class CounterspellSpellMixin {

    @Inject(method = "onCast", at = @At("HEAD"), remap = false)
    private void removeSpellTraps(Level world, int spellLevel, LivingEntity entity,
                                  CastSource castSource, MagicData playerMagicData,
                                  CallbackInfo ci) {
        if (world.isClientSide) return;

        // Perform the same raycast as Counterspell (80 blocks forward)
        Vec3 start = entity.getEyePosition();
        Vec3 forward = entity.getForward().normalize();
        Vec3 end = start.add(forward.scale(80));

        // Step through the raycast path checking for spell trap blocks
        double distance = start.distanceTo(end);
        int steps = (int) Math.ceil(distance * 2); // Check every 0.5 blocks

        for (int i = 0; i < steps; i++) {
            double t = i / (double) steps;
            Vec3 pos = start.add(end.subtract(start).scale(t));
            BlockPos blockPos = BlockPos.containing(pos);
            BlockState blockState = world.getBlockState(blockPos);
            BlockEntity blockEntity = world.getBlockEntity(blockPos);

            // Check if this block is a spell trap
            if (blockState.getBlock() instanceof SpellTrapBlock && blockEntity instanceof SpellTrapBlockEntity) {
                BlockState prevState = blockEntity.getBlockState();
                world.removeBlock(blockPos, false);
                BlockState postState = blockEntity.getBlockState();
                world.sendBlockUpdated(blockPos, prevState, postState, 3);

                // Play break sound
                world.playSound(null, blockPos, SoundEvents.AMETHYST_CLUSTER_BREAK,
                        SoundSource.BLOCKS, 1.0f, 0.8f);
                break;
            }

            // Stop if we hit a solid block
            if (!blockState.isAir() && blockState.isSolidRender(world, blockPos)) {
                break;
            }
        }
    }
}

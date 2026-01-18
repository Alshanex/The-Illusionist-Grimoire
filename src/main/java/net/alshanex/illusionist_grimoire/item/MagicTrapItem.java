package net.alshanex.illusionist_grimoire.item;

import io.redspace.ironsspellbooks.api.spells.IPresetSpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import net.alshanex.illusionist_grimoire.block.SpellTrapBlock;
import net.alshanex.illusionist_grimoire.block.SpellTrapBlockEntity;
import net.alshanex.illusionist_grimoire.registry.IGBlockRegistry;
import net.alshanex.illusionist_grimoire.util.PlayerSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class MagicTrapItem extends Item implements IPresetSpellContainer {
    public MagicTrapItem(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeSpellContainer(ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }

        if (!ISpellContainer.isSpellContainer(itemStack)) {
            ISpellContainer.set(itemStack, ISpellContainer.create(1, true, true));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();

        // Check if the item has a spell
        if (!ISpellContainer.isSpellContainer(itemStack)) {
            if (player != null && !level.isClientSide) {
                player.displayClientMessage(
                        Component.literal("This trap has no spell bound to it!")
                                .withStyle(style -> style.withColor(0xFF5555)),
                        true
                );
            }
            return InteractionResult.FAIL;
        }

        ISpellContainer spellContainer = ISpellContainer.get(itemStack);
        if (spellContainer == null || spellContainer.isEmpty()) {
            if (player != null && !level.isClientSide) {
                player.displayClientMessage(
                        Component.literal("This trap has no spell bound to it!")
                                .withStyle(style -> style.withColor(0xFF5555)),
                        true
                );
            }
            return InteractionResult.FAIL;
        }

        // Get the first spell from the container
        SpellData spellData = spellContainer.getSpellAtIndex(0);
        if (spellData == null || spellData.getSpell() == null) {
            if (player != null && !level.isClientSide) {
                player.displayClientMessage(
                        Component.literal("This trap has no spell bound to it!")
                                .withStyle(style -> style.withColor(0xFF5555)),
                        true
                );
            }
            return InteractionResult.FAIL;
        }

        // Calculate position for the trap block (on the clicked face)
        BlockPos trapPos = clickedPos.relative(clickedFace);

        // Check if we can place the block there
        if (!level.getBlockState(trapPos).canBeReplaced()) {
            return InteractionResult.FAIL;
        }

        // Check if the supporting block is sturdy
        BlockPos supportPos = trapPos.relative(clickedFace.getOpposite());
        if (!level.getBlockState(supportPos).isFaceSturdy(level, supportPos, clickedFace)) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            // Place the spell trap block
            BlockState trapState = IGBlockRegistry.SPELL_TRAP.get().defaultBlockState()
                    .setValue(SpellTrapBlock.FACING, clickedFace);

            level.setBlock(trapPos, trapState, 3);

            // Configure the block entity
            if (level.getBlockEntity(trapPos) instanceof SpellTrapBlockEntity trapEntity) {
                // Set the spell
                trapEntity.setSpell(spellData.getSpell().getSpellResource(), spellData.getLevel());

                // Capture and set player snapshot
                if (player != null) {
                    PlayerSnapshot snapshot = PlayerSnapshot.fromPlayer(player);
                    trapEntity.setPlayerSnapshot(snapshot);
                }

                trapEntity.setChanged();
            }

            // Play placement sound
            level.playSound(null, trapPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);

            // Consume the item in survival mode
            if (player != null && !player.isCreative()) {
                itemStack.shrink(1);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.literal("Right-click on a block face to place the trap")
                .withStyle(style -> style.withColor(0x888888).withItalic(true)));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Make the item glow if it has a spell
        if (ISpellContainer.isSpellContainer(stack)) {
            ISpellContainer container = ISpellContainer.get(stack);
            return container != null && !container.isEmpty();
        }
        return false;
    }
}

package net.alshanex.illusionist_grimoire.mixin;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.gui.arcane_anvil.ArcaneAnvilMenu;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.alshanex.illusionist_grimoire.registry.IGItemRegistry;
import net.alshanex.illusionist_grimoire.util.ModTags;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.SimpleContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArcaneAnvilMenu.class)
public abstract class ArcaneAnvilMenuMixin {

    @Inject(
            method = "createResult",
            at = @At("TAIL"),
            remap = false
    )
    private void onCreateResult(CallbackInfo ci) {
        ItemCombinerMenuAccessor accessor = (ItemCombinerMenuAccessor) this;

        ItemStack baseItemStack = accessor.getInputSlots().getItem(0);
        ItemStack modifierItemStack = accessor.getInputSlots().getItem(1);

        if (baseItemStack.is(IGItemRegistry.MAGIC_TRAP_ITEM.get()) &&
                modifierItemStack.getItem() instanceof Scroll) {
            ISpellContainer container = ISpellContainer.get(modifierItemStack);
            if(!container.isEmpty()){
                AbstractSpell boundSpell = container.getSpellAtIndex(0).getSpell();
                if(boundSpell.getCastType() == CastType.CONTINUOUS || ModTags.isSpellInTag(boundSpell, ModTags.TRAP_SPELL_BLACKLIST)){
                    accessor.getResultSlots().setItem(0, ItemStack.EMPTY);
                }
            }
        }
    }
}

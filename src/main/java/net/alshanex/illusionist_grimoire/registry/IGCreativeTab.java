package net.alshanex.illusionist_grimoire.registry;

import io.redspace.ironsspellbooks.registries.CreativeTabRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class IGCreativeTab {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IllusionistGrimoireMod.MODID);


    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = TABS.register("familiars_main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + IllusionistGrimoireMod.MODID + ".main_tab"))
            .icon(() -> new ItemStack(ItemRegistry.ARCHEVOKER_HELMET))
            .displayItems((enabledFeatures, entries) -> {
                entries.accept(IGItemRegistry.MAGIC_TRAP_ITEM.get());

                entries.accept(IGItemRegistry.PICTURE_BOOK.get());
            })
            .withTabsBefore(CreativeTabRegistry.EQUIPMENT_TAB.getId())
            .build());
}

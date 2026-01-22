package net.alshanex.illusionist_grimoire.registry;

import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.data.PictureBookData;
import net.alshanex.illusionist_grimoire.item.MagicTrapItem;
import net.alshanex.illusionist_grimoire.item.PictureBookItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class IGItemRegistry {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, IllusionistGrimoireMod.MODID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static final DeferredHolder<Item, Item> MAGIC_TRAP_ITEM = ITEMS.register("magic_trap_item",
            () -> new MagicTrapItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> PICTURE_BOOK = ITEMS.register("picture_book",
            () -> new PictureBookItem(new Item.Properties()
                    .stacksTo(1)
                    .component(IGDataComponents.PICTURE_BOOK_DATA, PictureBookData.DEFAULT)
            ));
}

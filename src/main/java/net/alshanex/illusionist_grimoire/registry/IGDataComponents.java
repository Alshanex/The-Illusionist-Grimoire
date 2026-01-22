package net.alshanex.illusionist_grimoire.registry;

import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.data.PictureBookData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class IGDataComponents {
    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, IllusionistGrimoireMod.MODID);

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PictureBookData>> PICTURE_BOOK_DATA =
            DATA_COMPONENTS.register("picture_book_data", () ->
                    DataComponentType.<PictureBookData>builder()
                            .persistent(PictureBookData.CODEC)
                            .networkSynchronized(PictureBookData.STREAM_CODEC)
                            .build()
            );
}

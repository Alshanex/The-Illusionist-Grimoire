package net.alshanex.illusionist_grimoire.block;

import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MagicCircleModel extends GeoModel<SpellTrapBlockEntity> {

    @Override
    public ResourceLocation getModelResource(SpellTrapBlockEntity blockEntity) {
        return ResourceLocation.fromNamespaceAndPath(
                IllusionistGrimoireMod.MODID,
                "geo/magic_circle.geo.json"
        );
    }

    @Override
    public ResourceLocation getTextureResource(SpellTrapBlockEntity blockEntity) {
        return ResourceLocation.fromNamespaceAndPath(
                IllusionistGrimoireMod.MODID,
                "textures/block/magic_circle.png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(SpellTrapBlockEntity blockEntity) {
        return ResourceLocation.fromNamespaceAndPath(
                IllusionistGrimoireMod.MODID,
                "animations/magic_circle.animation.json"
        );
    }
}

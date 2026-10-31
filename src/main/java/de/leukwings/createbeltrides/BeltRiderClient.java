
package de.leukwings.createbeltrides;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(
        modid = CreateBeltRides.MODID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class BeltRiderClient {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                CreateBeltRides.BELT_RIDER.get(),
                InvisibleBeltRiderRenderer::new
        );
    }

    private static class InvisibleBeltRiderRenderer
            extends EntityRenderer<BeltRiderEntity> {

        public InvisibleBeltRiderRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public ResourceLocation getTextureLocation(BeltRiderEntity entity) {
            return ResourceLocation.withDefaultNamespace("textures/misc/white.png");
        }
    }
}

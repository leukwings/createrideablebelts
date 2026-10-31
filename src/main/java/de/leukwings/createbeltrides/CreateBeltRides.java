
package de.leukwings.createbeltrides;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(CreateBeltRides.MODID)
public class CreateBeltRides {

    public static final String MODID = "createbeltrides";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<BeltRiderEntity>> BELT_RIDER =
            ENTITY_TYPES.register("belt_rider", () ->
                    EntityType.Builder.<BeltRiderEntity>of(
                                    BeltRiderEntity::new,
                                    MobCategory.MISC
                            )
                            .sized(0.1F, 0.1F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("createbeltrides:belt_rider")
            );

    public CreateBeltRides(IEventBus modEventBus, ModContainer modContainer) {

        // Register entities
        ENTITY_TYPES.register(modEventBus);

    }
}
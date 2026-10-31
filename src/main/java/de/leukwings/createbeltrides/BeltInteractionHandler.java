
package de.leukwings.createbeltrides;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = CreateBeltRides.MODID)
public class BeltInteractionHandler {

    @SubscribeEvent
    public static void onRightClickBelt(
            PlayerInteractEvent.RightClickBlock event
    ) {
        Player player = event.getEntity();

        if (event.getHand() != InteractionHand.MAIN_HAND
                || player.isShiftKeyDown()
                || player.isSpectator()
                || player.isPassenger()
                || !event.getItemStack().isEmpty()
                || event.getFace() != Direction.UP) {
            return;
        }

        Level level = event.getLevel();
        BlockPos clickedPos = event.getPos();

        if (!AllBlocks.BELT.has(level.getBlockState(clickedPos))) {
            return;
        }

        if (!(level.getBlockEntity(clickedPos)
                instanceof BeltBlockEntity segment)) {
            return;
        }

        BeltBlockEntity controller = segment.getControllerBE();

        if (controller == null || !controller.isController()) {
            return;
        }

        double offset = segment.index + 0.5;

        if (offset < 0 || offset > controller.beltLength) {
            return;
        }

        event.setCanceled(true);

        if (level.isClientSide) {
            return;
        }

        BeltRiderEntity rider =
                CreateBeltRides.BELT_RIDER.get().create(level);

        if (rider == null || !rider.setBelt(controller, offset)) {
            return;
        }

        if (!level.addFreshEntity(rider)) {
            return;
        }

        player.startRiding(rider, true);
    }
}
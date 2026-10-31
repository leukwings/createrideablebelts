
package de.leukwings.createbeltrides;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BeltRiderEntity extends Entity {

    private BlockPos controllerPos = BlockPos.ZERO;
    private double beltOffset;

    public BeltRiderEntity(
            EntityType<? extends BeltRiderEntity> type,
            Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public boolean setBelt(BeltBlockEntity controller, double offset) {
        if (controller == null
                || !controller.isController()
                || controller.beltLength <= 0
                || offset < 0
                || offset > controller.beltLength) {
            return false;
        }

        controllerPos = controller.getBlockPos().immutable();
        beltOffset = offset;

        updateBeltPosition(controller);
        return true;
    }

    private void updateBeltPosition(BeltBlockEntity controller) {
        Vec3 position = BeltHelper.getVectorForOffset(
                controller,
                (float) beltOffset
        );

        setPos(position.x, position.y, position.z);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            return;
        }

        if (getPassengers().isEmpty()) {
            discard();
            return;
        }

        if (!level().hasChunkAt(controllerPos)) {
            ejectPassengers();
            discard();
            return;
        }

        if (!(level().getBlockEntity(controllerPos)
                instanceof BeltBlockEntity controller)
                || !controller.isController()
                || controller.beltLength <= 0) {
            ejectPassengers();
            discard();
            return;
        }

        double movement =
                controller.getDirectionAwareBeltMovementSpeed();

        if (Math.abs(movement) < 0.0001) {
            updateBeltPosition(controller);
            return;
        }

        double nextOffset = beltOffset + movement;

        if (nextOffset >= 0 && nextOffset <= controller.beltLength) {
            beltOffset = nextOffset;
            updateBeltPosition(controller);
            return;
        }

        if (tryContinueOnNextBelt(controller, movement)) {
            return;
        }

        dismountAtEnd(controller, movement);
    }

    private boolean tryContinueOnNextBelt(
            BeltBlockEntity controller,
            double movement) {

        int endIndex = movement > 0
                ? controller.beltLength - 1
                : 0;

        BlockPos endPos =
                BeltHelper.getPositionForOffset(controller, endIndex);

        Vec3i direction = controller.getBeltChainDirection();

        if (direction.equals(Vec3i.ZERO)) {
            return false;
        }

        BlockPos nextPos = endPos.offset(
                direction.getX(),
                direction.getY(),
                direction.getZ()
        );

        if (!level().hasChunkAt(nextPos)
                || !AllBlocks.BELT.has(level().getBlockState(nextPos))) {
            return false;
        }

        if (!(level().getBlockEntity(nextPos)
                instanceof BeltBlockEntity nextSegment)) {
            return false;
        }

        BeltBlockEntity nextController = nextSegment.getControllerBE();

        if (nextController == null
                || !nextController.isController()
                || nextController.beltLength <= 0
                || nextController.getSpeed() == 0) {
            return false;
        }

        double nextOffset = nextSegment.index + 0.5;

        return setBelt(nextController, nextOffset);
    }

    private void dismountAtEnd(
            BeltBlockEntity controller,
            double movement) {

        int endIndex = movement > 0
                ? controller.beltLength - 1
                : 0;

        BlockPos endPos =
                BeltHelper.getPositionForOffset(controller, endIndex);

        Vec3i direction = controller.getBeltChainDirection();

        Vec3 exitPosition = new Vec3(
                endPos.getX() + 0.5 + direction.getX(),
                endPos.getY() + 0.8 + direction.getY(),
                endPos.getZ() + 0.5 + direction.getZ()
        );

        for (Entity passenger : getPassengers()) {
            passenger.stopRiding();
            passenger.setPos(
                    exitPosition.x,
                    exitPosition.y,
                    exitPosition.z
            );
            passenger.setDeltaMovement(Vec3.ZERO);
        }

        discard();
    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity passenger) {
        return new Vec3(
                getX(),
                getY() + 0.3,
                getZ()
        );
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        controllerPos = new BlockPos(
                tag.getInt("ControllerX"),
                tag.getInt("ControllerY"),
                tag.getInt("ControllerZ")
        );

        beltOffset = tag.getDouble("BeltOffset");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("ControllerX", controllerPos.getX());
        tag.putInt("ControllerY", controllerPos.getY());
        tag.putInt("ControllerZ", controllerPos.getZ());
        tag.putDouble("BeltOffset", beltOffset);
    }
}

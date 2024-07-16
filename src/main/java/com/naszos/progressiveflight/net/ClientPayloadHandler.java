package com.naszos.progressiveflight.net;

import com.naszos.progressiveflight.ProgressiveFlightMod;
import com.naszos.progressiveflight.blocks.FlightBeaconBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3f;

public class ClientPayloadHandler {

    public static void handleData(final BeaconWorkingPayload data, final IPayloadContext context) {
        ProgressiveFlightMod.LOGGER.debug("Beacon at {} is working? {}", data.pos(), data.isWorking());

        Level level = context.player().level();
        Vector3f pos3f = data.pos();
        BlockPos pos = new BlockPos((int) pos3f.x, (int) pos3f.y, (int) pos3f.z);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FlightBeaconBE) {
            ((FlightBeaconBE) blockEntity).setParticlesVisible(data.isWorking());
        }
    }
}